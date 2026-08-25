#!/usr/bin/env python3
"""
import_etablissements_xlsx.py — Importe les 97 établissements du supérieur
depuis Base_Etablissements_Superieurs_Togo.xlsx vers l'API REST.

Pour chaque fiche xlsx :
  1. Login → JWT admin (création si besoin via /api/v1/auth/login)
  2. POST /api/v1/bibliotheque/etablissements  (création fiche parente)
  3. PUT  /api/v1/bibliotheque/etablissements/{trackingId}/details-xlsx
       (sérialise toutes les feuilles en JSON et envoie)

Idempotent : si le titre existe déjà (recherche GET), on met à jour au lieu
de créer. Skip type=LYCEE (jamais dans xlsx, mais garde-fou).

Usage :
  python3 scripts/import_etablissements_xlsx.py \
      [--xlsx Base_Etablissements_Superieurs_Togo.xlsx] \
      [--api http://localhost:8080/api/v1] \
      [--email admin@activeducation.tg] \
      [--password abalakata] \
      [--dry-run]
"""
from __future__ import annotations
import argparse
import json
import logging
import sys
import time
import unicodedata
from pathlib import Path
from typing import Optional

import openpyxl
import requests

# ─────────────────────────────────────────────────────────────────────────
# Configuration & logging
# ─────────────────────────────────────────────────────────────────────────
LOG = logging.getLogger("import_xlsx")
logging.basicConfig(
    format="%(asctime)s | %(levelname)-5s | %(message)s",
    datefmt="%H:%M:%S",
    level=logging.INFO,
)

DEFAULT_XLSX = Path("/home/grace/Projet-activ-education/Base_Etablissements_Superieurs_Togo.xlsx")
DEFAULT_API = "http://localhost:8080/api/v1"
DEFAULT_EMAIL = "admin@activeducation.tg"
DEFAULT_PASSWORD = "admin123!"  # seed_users_api.sh par défaut (admin@activeducation.tg)

# Mapping xlsx Type → FicheEtablissement.TypeEtablissement enum
# L'entrée du xlsx est en texte libre : on matche par préfixe (lowercase).
TYPE_MAPPING = [
    ("université publique", "UNIVERSITE"),
    ("université privée", "UNIVERSITE"),
    ("université privée internationale", "UNIVERSITE"),
    ("grande école publique", "GRANDE_ECOLE"),
    ("grande école privée", "GRANDE_ECOLE"),
    ("école supérieure privée", "ECOLE_SUPERIEURE"),
    ("école supérieure publique", "ECOLE_SUPERIEURE"),
    ("institut supérieur public", "ECOLE_SUPERIEURE"),
    ("institut supérieur privé", "ECOLE_SUPERIEURE"),
    ("institut privé", "ECOLE_SUPERIEURE"),
    ("institut public", "ECOLE_SUPERIEURE"),
    ("école inter-états", "GRANDE_ECOLE"),
    ("établissement public", "ECOLE_SUPERIEURE"),
    ("établissement privé", "ECOLE_SUPERIEURE"),
    ("centre de formation professionnelle", "CENTRE_FORMATION_PROFESSIONNELLE"),
]

# Feuilles qu'on va chercher par ID_ETABLISSEMENT et aplatir en JSON
RELATED_SHEETS = [
    "Contacts", "Responsables", "Localisation", "Formations", "Diplomes",
    "Admissions", "Frais", "Calendrier", "Infrastructures", "Vie etudiante",
    "Enseignants", "Statistiques", "Stages", "Partenariats", "Recherche",
    "Bourses", "Documents", "Medias", "Avis", "Sources", "Qualite des donnees",
]


def normalize(name: str) -> str:
    """Normalise un nom pour matching (lowercase, sans accents)."""
    if not name:
        return ""
    nfkd = unicodedata.normalize("NFKD", name)
    without = "".join(c for c in nfkd if not unicodedata.combining(c))
    return "".join(c for c in without.lower() if c.isalnum() or c == " ").strip()


# ─────────────────────────────────────────────────────────────────────────
# Authentification
# ─────────────────────────────────────────────────────────────────────────
def login(api: str, email: str, password: str) -> str:
    """Login et récupère le JWT."""
    LOG.info("Login en tant que %s …", email)
    r = requests.post(
        f"{api}/auth/login",
        json={"email": email, "motDePasse": password},
        timeout=30,
    )
    r.raise_for_status()
    token = r.json().get("accessToken")
    if not token:
        raise RuntimeError("Pas de accessToken dans la réponse login")
    LOG.info("Token obtenu (%d caractères)", len(token))
    return token


# ─────────────────────────────────────────────────────────────────────────
# Lecture du xlsx
# ─────────────────────────────────────────────────────────────────────────
def load_xlsx(path: Path) -> dict:
    """Charge toutes les feuilles du xlsx, indexées par nom."""
    LOG.info("Chargement du xlsx : %s", path)
    wb = openpyxl.load_workbook(path, data_only=True)
    sheets = {}
    for name in wb.sheetnames:
        if name == "Lisez-moi":
            continue
        ws = wb[name]
        rows = list(ws.iter_rows(values_only=True))
        if not rows:
            sheets[name] = {"headers": [], "rows": []}
            continue
        headers = [str(c) if c is not None else "" for c in rows[0]]
        data = []
        for row in rows[1:]:
            if not any(c is not None for c in row):
                continue
            data.append([(str(c) if c is not None else None) for c in row])
        sheets[name] = {"headers": headers, "rows": data}
        LOG.info("  Feuille '%s' : %d lignes, %d colonnes", name, len(data), len(headers))
    return sheets


def collect_related(sheets: dict, etablissement_id: str) -> dict:
    """Collecte toutes les feuilles liées à un ID_ETABLISSEMENT."""
    result = {}
    for sheet_name in RELATED_SHEETS:
        if sheet_name not in sheets:
            continue
        sheet = sheets[sheet_name]
        # Index des colonnes : on cherche ID_ETABLISSEMENT
        try:
            id_col = sheet["headers"].index("ID_ETABLISSEMENT")
        except ValueError:
            LOG.warning("Feuille '%s' sans colonne ID_ETABLISSEMENT — ignorée", sheet_name)
            continue
        matching = []
        for row in sheet["rows"]:
            if row[id_col] == etablissement_id:
                # Skip la colonne 'Nom officiel' (redondante avec la fiche parente)
                row_dict = {}
                for i, h in enumerate(sheet["headers"]):
                    if h == "Nom officiel" or h == "ID_ETABLISSEMENT":
                        continue
                    row_dict[h] = row[i] if i < len(row) else None
                matching.append(row_dict)
        if matching:
            # Si une seule ligne, on l'aplatit ; sinon on garde une liste
            if len(matching) == 1:
                result[sheet_name.lower().replace(" ", "_")] = matching[0]
            else:
                result[sheet_name.lower().replace(" ", "_")] = matching
    return result


def map_type(xlsx_type: str) -> str:
    """Mappe le type xlsx vers l'enum TypeEtablissement (matching par préfixe)."""
    if not xlsx_type:
        return "ECOLE_SUPERIEURE"
    needle = xlsx_type.strip().lower()
    for prefix, enum in TYPE_MAPPING:
        if needle.startswith(prefix):
            return enum
    LOG.warning("Type xlsx non reconnu : '%s' → ECOLE_SUPERIEURE par défaut", xlsx_type)
    return "ECOLE_SUPERIEURE"


# ─────────────────────────────────────────────────────────────────────────
# Import via REST
# ─────────────────────────────────────────────────────────────────────────
def existing_by_title(api: str, headers: dict, titre: str) -> Optional[dict]:
    """Cherche une fiche par titre (lowercase, contains)."""
    r = requests.get(
        f"{api}/bibliotheque/etablissements/recherche",
        params={"motCle": titre, "page": 0, "size": 5},
        headers=headers,
        timeout=30,
    )
    if not r.ok:
        LOG.warning("Recherche '%s' → HTTP %d", titre, r.status_code)
        return None
    content = r.json().get("content", [])
    if not content:
        return None
    norm_titre = normalize(titre)
    for fiche in content:
        if normalize(fiche.get("titre", "")) == norm_titre:
            return fiche
    return None


def build_fiche_request(row: dict, related: dict) -> tuple[dict, dict]:
    """Construit (fiche_request, details_xlsx) à partir d'une ligne xlsx."""
    id_etab = row.get("ID_ETABLISSEMENT")
    nom = row.get("Nom officiel") or ""
    sigle = row.get("Sigle") or ""
    type_xlsx = row.get("Type") or ""
    type_enum = map_type(type_xlsx)
    presentation = row.get("Présentation") or ""
    historique = row.get("Historique") or ""
    mission = row.get("Mission") or ""
    annee_creation = row.get("Année de création")

    est_public = (type_enum == "UNIVERSITE") or ("publique" in (type_xlsx or "").lower())

    resume = presentation[:500] if presentation else f"{nom} ({sigle}) — Établissement d'enseignement supérieur au Togo."

    # Tente d'extraire la ville depuis la localisation
    ville = ""
    adresse = ""
    if "localisation" in related and isinstance(related["localisation"], dict):
        ville = related["localisation"].get("Ville") or ""
        adresse = related["localisation"].get("Adresse complète") or ""
    if not ville:
        ville = "Lomé"  # fallback conservateur

    # Contacts → restent dans les détails (champ 'contacts' actuel est deprecated)
    contacts_brut = ""
    site_web = ""
    if "contacts" in related and isinstance(related["contacts"], dict):
        c = related["contacts"]
        tel = c.get("Téléphone principal") or ""
        email = c.get("Email") or ""
        site_web = c.get("Site web officiel") or ""
        contacts_brut = " | ".join(filter(None, [tel, email, site_web]))

    fiche_request = {
        "titre": nom,
        "resume": resume,
        "contenu": presentation or historique or f"{nom} ({sigle}).",
        "estPublie": True,
        "adresse": adresse,
        "ville": ville,
        "typeEtablissement": type_enum,
        "niveau": "Licence, Master, Doctorat",
        "contacts": contacts_brut,
        "siteWeb": site_web,
        "estPublic": est_public,
    }

    # Détails xlsx (JSON libre) — wrappé dans {donneesEtendues: {...}}
    # pour matcher le DTO EtablissementDetailsXlsxRequest.
    inner_details = {
        "sigle": sigle,
        "typeXlsx": type_xlsx,
        "informationsComplementaires": {
            "historique": historique,
            "presentation": presentation,
            "mission": mission,
            "vision": row.get("Vision"),
            "valeurs": row.get("Valeurs"),
            "devise": row.get("Devise"),
            "slogan": row.get("Slogan"),
            "anneeCreation": annee_creation,
            "autoriteTutelle": row.get("Autorité de tutelle"),
            "numeroAgrement": row.get("Numéro d'agrément"),
            "dateAgrement": row.get("Date d'agrément"),
            "accreditations": row.get("Accréditations"),
            "reconnaissanceCAMES": row.get("Reconnaissance CAMES"),
            "logoUrl": row.get("Logo_URL"),
        },
        "donneesParFeuille": related,
    }
    details = {
        "idSourceXlsx": id_etab,
        "donneesEtendues": inner_details,
        "dateExtraction": "2026-08-06T10:00:00",
    }
    return fiche_request, details


def import_one(api: str, headers: dict, row: dict, sheets: dict, dry_run: bool) -> str:
    """Importe un établissement. Retourne le statut (created/updated/skipped/failed).

    Le PUT sur /etablissements/{trackingId} renvoie 500 UnsupportedOperationException
    côté backend (cf. JOURNAL_BORD_IA.md 6 août §5). On n'utilise donc QUE :
      - POST /etablissements pour les nouvelles fiches
      - PUT  /etablissements/{trackingId}/details-xlsx pour les détails (qui marche)

    Pour les fiches existantes, on marque 'updated' sans toucher aux champs
    parente (titre/resume/contenu). Le mapping xlsx sert principalement à
    peupler details-xlsx.
    """
    id_etab = row.get("ID_ETABLISSEMENT") or "?"
    nom = row.get("Nom officiel") or "?"
    related = collect_related(sheets, id_etab)
    fiche_req, details = build_fiche_request(row, related)

    if dry_run:
        LOG.info("  DRY-RUN %s : %s (%s)", id_etab, nom, fiche_req["typeEtablissement"])
        return "dry-run"

    # 1. Chercher la fiche existante
    existing = existing_by_title(api, headers, nom)
    if existing:
        tracking_id = existing["trackingId"]
        LOG.info("  ↻  EXISTS %s : %s (trackingId=%s) → détails seulement",
                 id_etab, nom, tracking_id)
    else:
        LOG.info("  + CREATE %s : %s (%s)", id_etab, nom, fiche_req["typeEtablissement"])
        r = requests.post(
            f"{api}/bibliotheque/etablissements",
            json=fiche_req,
            headers=headers,
            timeout=30,
        )
        if r.status_code == 201:
            tracking_id = r.json().get("trackingId")
        else:
            LOG.error("    POST fiche KO : HTTP %d — %s", r.status_code, r.text[:200])
            return "failed"

    # 2. PUT détails xlsx (idempotent) — fonctionne dans tous les cas
    r = requests.put(
        f"{api}/bibliotheque/etablissements/{tracking_id}/details-xlsx",
        json=details,
        headers=headers,
        timeout=30,
    )
    if not r.ok:
        LOG.error("    PUT details KO : HTTP %d — %s", r.status_code, r.text[:200])
        return "failed"
    return "updated" if existing else "created"


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--xlsx", type=Path, default=DEFAULT_XLSX)
    p.add_argument("--api", default=DEFAULT_API)
    p.add_argument("--email", default=DEFAULT_EMAIL)
    p.add_argument("--password", default=DEFAULT_PASSWORD)
    p.add_argument("--dry-run", action="store_true")
    p.add_argument("--limit", type=int, default=None,
                   help="Limiter à N fiches (pour test)")
    args = p.parse_args()

    sheets = load_xlsx(args.xlsx)
    etab_sheet = sheets.get("Etablissements", {"rows": []})
    etab_rows = etab_sheet["rows"]
    LOG.info("%d établissements dans la feuille Etablissements", len(etab_rows))

    if args.dry_run:
        headers = {}
    else:
        token = login(args.api, args.email, args.password)
        headers = {"Authorization": f"Bearer {token}"}

    stats = {"created": 0, "updated": 0, "failed": 0, "dry-run": 0}
    rows_to_process = etab_rows[: args.limit] if args.limit else etab_rows
    for i, row in enumerate(rows_to_process, 1):
        # Convertir en dict par header
        row_dict = dict(zip(etab_sheet["headers"], row))
        status = import_one(args.api, headers, row_dict, sheets, args.dry_run)
        stats[status] = stats.get(status, 0) + 1
        if i % 10 == 0:
            LOG.info("--- Progression %d / %d ---", i, len(rows_to_process))

    LOG.info("=" * 60)
    LOG.info("STATISTIQUES FINALES : %s", stats)
    if stats.get("failed", 0) > 0:
        sys.exit(1)


if __name__ == "__main__":
    main()
