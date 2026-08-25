#!/usr/bin/env python3
"""
import_etablissements_csv_togo.py — Importe les établissements du Togo
(labels CSV du MEPSTA / formation technique / enseignement supérieur)
depuis donnée/ vers l'API REST /api/v1/bibliotheque/etablissements.

Sources :
  file-etablissements-scolaires-lycee-18-12-2024-20-45-11.csv   (708 lycées)
  file-formations-techniques-etablissements-18-12-2024-20-42-14.csv (256 centres)
  file-enseignement-superieur-etablissements-mepsta-03-01-2025-09-45-04.csv (7 ENI)

Idempotent : normalise les noms (NFD, minuscules) et saute tout ce qui
existe déjà en base. Rapport créés / doublons / erreurs en fin de course.

Usage :
  python3 scripts/import_etablissements_csv_togo.py \
      [--api http://localhost:8080/api/v1] \
      [--email admin@activeducation.tg] \
      [--password admin123!] \
      [--dry-run] [--limit 50]
"""
from __future__ import annotations

import argparse
import json
import logging
import re
import time
import unicodedata
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
from typing import Optional

import requests

LOG = logging.getLogger("import_csv")
logging.basicConfig(
    format="%(asctime)s | %(levelname)-5s | %(message)s",
    datefmt="%H:%M:%S",
    level=logging.INFO,
)

DEFAULT_API = "http://localhost:8080/api/v1"
DEFAULT_EMAIL = "admin@activeducation.tg"
DEFAULT_PASSWORD = "admin123!"

DONNEE_DIR = Path("/home/grace/Projet-activ-education/donnée")
SOURCES = [
    ("lycees", DONNEE_DIR / "file-etablissements-scolaires-lycee-18-12-2024-20-45-11.csv"),
    ("techniques", DONNEE_DIR / "file-formations-techniques-etablissements-18-12-2024-20-42-14.csv"),
    ("sup", DONNEE_DIR / "file-enseignement-superieur-etablissements-mepsta-03-01-2025-09-45-04.csv"),
]

TYPE_LYCEE = "LYCEE"
TYPE_CFP = "CENTRE_FORMATION_PROFESSIONNELLE"
TYPE_SUP = "ECOLE_SUPERIEURE"

PUBLIC_KEYWORDS = ("domaine public", "domaine privé de l'état", "nsp", "autre")


def normaliser(nom: str) -> str:
    """minuscules, sans accents, sans ponctuation ni espaces multiples."""
    t = unicodedata.normalize("NFD", nom or "")
    t = "".join(c for c in t if unicodedata.category(c) != "Mn")
    t = re.sub(r"[^a-z0-9]", " ", t.lower())
    return re.sub(r"\s+", " ", t).strip()


def parser_geometry(geom: str) -> tuple[Optional[float], Optional[float]]:
    """POINT (lng lat) → (lat, lng)"""
    m = re.search(r"POINT\s*\(\s*([-\d.]+)\s+([-\d.]+)\s*\)", geom or "")
    if not m:
        return None, None
    lng, lat = float(m.group(1)), float(m.group(2))
    return lat, lng


def nettoie(v: str) -> str:
    v = (v or "").strip()
    return "" if v.lower() in ("neant", "n/a", "nsp", "ns", "") else v


def build_rows() -> list[dict]:
    rows: list[dict] = []
    for source, path in SOURCES:
        import csv

        with open(path, newline="", encoding="utf-8") as fh:
            for r in csv.DictReader(fh):
                nom = nettoie(r.get("etablissement_nom") or r.get("etab_nom") or "")
                if not nom:
                    continue
                region = r.get("region_nom_bdd") or ""
                prefecture = r.get("prefecture_nom_bdd") or ""
                commune = r.get("commune_nom_bdd") or ""
                localite = nettoie(r.get("nom_localite") or "")
                adresse = nettoie(r.get("etab_adr") or r.get("etab_adresse") or "")
                lat, lng = parser_geometry(r.get("geometry"))
                terrain = (r.get("terrain") or "").lower()

                if source == "lycees":
                    type_etab, niveau = TYPE_LYCEE, "Lycée"
                    est_public = not any(k in terrain for k in ("prive",))
                elif source == "sup":
                    type_etab, niveau = TYPE_SUP, "Supérieur"
                    est_public = True
                else:
                    est_public = True
                    if "lycée" in nom.lower() or "lycee" in nom.lower():
                        type_etab, niveau = TYPE_LYCEE, "Lycée Technique"
                    else:
                        type_etab, niveau = TYPE_CFP, "CAP/BTS/Formation professionnelle"

                ville = commune or localite or prefecture
                resume = f"Établissement d'enseignement {type_etab.replace('_', ' ').title()} situé à {ville}"
                if region:
                    resume += f", région {region}"
                contenu = (
                    f"<p>{resume}.</p>"
                    f"<p>Préfecture : {prefecture or 'non renseignée'} · "
                    f"Commune : {commune or 'non renseignée'} · "
                    f"Localité : {localite or 'non renseignée'}.</p>"
                )
                rows.append(
                    {
                        "source": source,
                        "titre": nom,
                        "resume": resume,
                        "contenu": contenu,
                        "estPublie": True,
                        "adresse": adresse or ville or "Togo",
                        "ville": ville or "Togo",
                        "typeEtablissement": type_etab,
                        "niveau": niveau,
                        "estPublic": est_public,
                        "latitude": lat,
                        "longitude": lng,
                    }
                )
    return rows


def fetch_existing(api: str, token: str) -> set[str]:
    """Tous les titres existants (paginé) → noms normalisés."""
    existing: set[str] = set()
    page, size = 0, 200
    while True:
        r = requests.get(
            f"{api}/bibliotheque/etablissements",
            params={"page": page, "size": size},
            headers={"Authorization": f"Bearer {token}"},
            timeout=30,
        )
        r.raise_for_status()
        data = r.json()
        for e in data.get("content", []):
            existing.add(normaliser(e.get("titre", "")))
        if page + 1 >= data.get("totalPages", 1):
            break
        page += 1
    return existing


def post_one(session: requests.Session, api: str, token: str, row: dict) -> tuple[str, str, str]:
    """→ (titre, statut, message)"""
    try:
        r = session.post(
            f"{api}/bibliotheque/etablissements",
            headers={"Authorization": f"Bearer {token}"},
            json=row,
            timeout=60,
        )
        if r.status_code == 201:
            return row["titre"], "CREATE", ""
        return row["titre"], "ERROR", f"HTTP {r.status_code}: {r.text[:200]}"
    except Exception as e:  # noqa: BLE001
        return row["titre"], "ERROR", str(e)[:200]


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--api", default=DEFAULT_API)
    ap.add_argument("--email", default=DEFAULT_EMAIL)
    ap.add_argument("--password", default=DEFAULT_PASSWORD)
    ap.add_argument("--dry-run", action="store_true", help="prépare seulement, ne POSTe rien")
    ap.add_argument("--limit", type=int, default=0, help="0 = tout, sinon max de créations")
    args = ap.parse_args()

    LOG.info("Chargement des CSV…")
    rows = build_rows()
    LOG.info("%d établissements bruts (lycées + techniques + supérieur)", len(rows))

    # dédup interne (doublons entre CSV / lignes répétées)
    seen: dict[str, dict] = {}
    for row in rows:
        key = normaliser(row["titre"])
        if key not in seen:
            seen[key] = row
    rows = list(seen.values())
    LOG.info("%d après dédup interne", len(rows))

    LOG.info("Login %s…", args.email)
    r = requests.post(
        f"{args.api}/auth/login",
        json={"email": args.email, "motDePasse": args.password},
        timeout=30,
    )
    r.raise_for_status()
    token = r.json().get("accessToken")
    if not token:
        raise SystemExit("Pas de accessToken dans la réponse de login")

    LOG.info("Lecture des établissements existants…")
    existing = fetch_existing(args.api, token)
    LOG.info("%d titres déjà en base", len(existing))

    todo = [row for row in rows if normaliser(row["titre"]) not in existing]
    dup = len(rows) - len(todo)
    LOG.info("%d à créer, %d doublons ignorés", len(todo), dup)
    if args.dry_run:
        for row in todo[:50]:
            LOG.info("  [DRY] %s | %s | %s", row["titre"], row["typeEtablissement"], row["ville"])
        LOG.info("DRY-RUN terminé : %d créations auraient eu lieu", len(todo))
        return

    if args.limit:
        todo = todo[: args.limit]

    created = errors = 0
    session = requests.Session()
    with ThreadPoolExecutor(max_workers=8) as pool:
        futures = {pool.submit(post_one, session, args.api, token, row): row for row in todo}
        for i, fut in enumerate(as_completed(futures), 1):
            titre, status, msg = fut.result()
            if status == "CREATE":
                created += 1
            else:
                errors += 1
                LOG.warning("  [%s] %s — %s", status, titre, msg)
            if i % 50 == 0:
                LOG.info("  %d/%d traités (créés=%d, erreurs=%d)", i, len(todo), created, errors)
            time.sleep(0.02)

    LOG.info("═" * 60)
    LOG.info("RÉSULTAT : %d créés, %d doublons, %d erreurs (sur %d tentés)", created, dup, errors, len(todo))


if __name__ == "__main__":
    main()
