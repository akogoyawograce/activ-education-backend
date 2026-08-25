#!/usr/bin/env python3
"""
Lie les établissements du XLSX à leurs fiches filières existantes.

Source : feuille `Formations` du XLSX (colonne ID_ETABLISSEMENT + Filière)
Cible : table `etablissement_filiere` (FK vers fiches_etablissement + fiches_filiere)

Stratégie de matching (par titre de filière) :
- Normalisation : lowercase + retrait des accents + espaces multiples + ponctuation
- Match exact après normalisation
- Pour les non-matchés : rapport (pas de création auto, à valider à la main)
"""
from __future__ import annotations

import argparse
import subprocess
import unicodedata
from collections import defaultdict
from pathlib import Path

import openpyxl

ROOT = Path(__file__).resolve().parent.parent
XLSX_PATH = ROOT / "Base_Etablissements_Superieurs_Togo.xlsx"
DB_NAME = "activ_education"
DB_USER = "postgres"
CONTAINER_NAME = "activeducation-db"


def run_psql(sql: str) -> str:
    res = subprocess.run(
        ["docker", "exec", CONTAINER_NAME, "psql", "-U", DB_USER, "-d", DB_NAME,
         "-t", "-A", "-c", sql],
        capture_output=True, text=True, check=True,
    )
    return res.stdout.strip()


def normalize(s: str) -> str:
    if not s:
        return ""
    s = unicodedata.normalize("NFD", s)
    s = "".join(c for c in s if unicodedata.category(c) != "Mn")  # retire accents
    s = s.lower().strip()
    s = " ".join(s.split())  # espaces multiples → simple
    return s


# ── Chargement ──────────────────────────────────────────────────────────────
def charger_xlsx_relations() -> dict[str, set[str]]:
    """{ID_ETABLISSEMENT: set(filière_normalisée)}."""
    wb = openpyxl.load_workbook(XLSX_PATH, data_only=True)
    ws = wb["Formations"]
    headers = [str(c.value).strip() if c.value else "" for c in next(
        ws.iter_rows(min_row=1, max_row=1))]
    idx = {h: i for i, h in enumerate(headers)}
    out: dict[str, set[str]] = defaultdict(set)
    for row in ws.iter_rows(min_row=2, values_only=True):
        if not row or not row[idx["ID_ETABLISSEMENT"]]:
            continue
        eid = str(row[idx["ID_ETABLISSEMENT"]]).strip()
        filiere = str(row[idx["Filière"]]).strip() if row[idx["Filière"]] else ""
        if filiere:
            out[eid].add(filiere)
    return out


def charger_filieres_db() -> tuple[dict[str, int], list[tuple[str, int]]]:
    """Renvoie ({nom_normalisé: id}, [(nom_original, id)]) — exclut les fiches test."""
    raw = run_psql(
        "SELECT fi.id, f.titre FROM fiches_filiere fi "
        "JOIN fiches f ON f.id=fi.id "
        "WHERE f.est_publie=true "
        "AND f.titre NOT ILIKE '%test%' AND f.titre NOT ILIKE '%save button%';"
    )
    norm_map: dict[str, int] = {}
    original: list[tuple[str, int]] = []
    for line in raw.splitlines():
        if "|" not in line:
            continue
        fid, titre = line.split("|", 1)
        fid_int = int(fid.strip())
        titre_clean = titre.strip()
        norm_map[normalize(titre_clean)] = fid_int
        original.append((titre_clean, fid_int))
    return norm_map, original


def mapping_db_xlsx() -> dict[str, int]:
    raw = run_psql(
        "SELECT id_source_xlsx, fiche_id FROM etablissement_details_xlsx "
        "WHERE id_source_xlsx IS NOT NULL;"
    )
    out: dict[str, int] = {}
    for line in raw.splitlines():
        if "|" not in line:
            continue
        xid, fid = line.split("|", 1)
        out[xid.strip()] = int(fid.strip())
    return out


def lien_existe(etab_id: int, filiere_id: int) -> bool:
    raw = run_psql(
        f"SELECT 1 FROM etablissement_filiere "
        f"WHERE etablissement_id={etab_id} AND filiere_id={filiere_id};"
    )
    return bool(raw.strip())


def inserer_lien(etab_id: int, filiere_id: int) -> None:
    # ON CONFLICT DO NOTHING pour idempotence
    run_psql(
        f"INSERT INTO etablissement_filiere (etablissement_id, filiere_id) "
        f"VALUES ({etab_id}, {filiere_id}) ON CONFLICT DO NOTHING;"
    )


def match_filiere(filiere_xlsx: str,
                   norm_map: dict[str, int],
                   originaux: list[tuple[str, int]]) -> tuple[int | None, str]:
    """Match strict uniquement. Pas de fuzzy : trop de faux positifs dangereux."""
    key = normalize(filiere_xlsx)
    if key in norm_map:
        return norm_map[key], "exact"
    return None, "aucun"


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    print("Lecture du XLSX (Formations)...")
    xlsx_relations = charger_xlsx_relations()
    total_relations_xlsx = sum(len(s) for s in xlsx_relations.values())
    print(f"  → {len(xlsx_relations)} établissements, {total_relations_xlsx} relations")

    print("Lecture des fiches filières publiées...")
    filieres_db_norm, filieres_db_orig = charger_filieres_db()
    print(f"  → {len(filieres_db_norm)} fiches en base (test exclues)")

    print("Lecture du mapping DB ↔ XLSX...")
    mapping = mapping_db_xlsx()
    print(f"  → {len(mapping)} fiches liées au XLSX")

    matched_exact = 0
    matched_fuzzy = 0
    matched_total = 0
    unmatched: dict[str, int] = defaultdict(int)
    fuzzy_ambigus: dict[str, int] = defaultdict(int)
    inserted = 0
    skipped_existe = 0
    fuzzy_samples: list[tuple[str, str, str]] = []  # (xlsx, db, type)

    for xlsx_id, fiche_etab_id in mapping.items():
        for filiere_xlsx in xlsx_relations.get(xlsx_id, set()):
            key = normalize(filiere_xlsx)
            # Distinguer exact vs fuzzy pour les stats
            filiere_id_exact = filieres_db_norm.get(key)
            if filiere_id_exact is not None:
                matched_exact += 1
                filiere_id = filiere_id_exact
                match_type = "exact"
            else:
                filiere_id, match_type = match_filiere(
                    filiere_xlsx, filieres_db_norm, filieres_db_orig)
                if filiere_id is not None:
                    matched_fuzzy += 1
                    titre_db = next(t for t, i in filieres_db_orig if i == filiere_id)
                    if len(fuzzy_samples) < 10:
                        fuzzy_samples.append((filiere_xlsx, titre_db, match_type))
                elif match_type == "fuzzy_ambigu":
                    fuzzy_ambigus[filiere_xlsx] += 1
                    continue
                else:
                    unmatched[filiere_xlsx] += 1
                    continue

            matched_total += 1
            if lien_existe(fiche_etab_id, filiere_id):
                skipped_existe += 1
                continue
            if not args.dry_run:
                inserer_lien(fiche_etab_id, filiere_id)
                inserted += 1

    # Rapport
    print("\n" + "=" * 70)
    print(f"RÉSULTAT  (mode {'DRY-RUN' if args.dry_run else 'UPDATE'})")
    print("=" * 70)
    print(f"Relations matchées (filière en base) : {matched_total}")
    print(f"  → match exact                       : {matched_exact}")
    print(f"  → match fuzzy (sous-chaîne)         : {matched_fuzzy}")
    print(f"  → liens déjà existants              : {skipped_existe}")
    print(f"  → liens insérés                     : {inserted}")
    print(f"Relations non matchées                : {sum(unmatched.values())}")
    print(f"  → filières uniques non matchées     : {len(unmatched)}")
    print(f"Relations ambiguës (rejetées)         : {sum(fuzzy_ambigus.values())}")
    print(f"  → filières uniques ambiguës         : {len(fuzzy_ambigus)}")

    if fuzzy_samples:
        print("\n--- Échantillon match fuzzy (XLSX → DB, type) ---")
        for xlsx, db, mt in fuzzy_samples:
            print(f"  [{mt}]  '{xlsx}'  →  '{db}'")

    # Export CSV des non-matchés pour traitement manuel
    if unmatched:
        csv_path = ROOT / "scripts" / "filieres_xlsx_non_matchees.csv"
        with open(csv_path, "w", encoding="utf-8") as f:
            f.write("filiere_xlsx,occurrences\n")
            for titre, count in sorted(unmatched.items(), key=lambda x: -x[1]):
                f.write(f'"{titre}",{count}\n')
        print(f"\n→ {len(unmatched)} filières XLSX non matchées exportées dans :")
        print(f"  {csv_path}")

    if unmatched:
        print("\n--- TOP 20 filières XLSX non matchées (par fréquence) ---")
        for titre, count in sorted(unmatched.items(), key=lambda x: -x[1])[:20]:
            print(f"  {count:3}x  {titre}")


if __name__ == "__main__":
    main()
