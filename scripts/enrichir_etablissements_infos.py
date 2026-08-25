#!/usr/bin/env python3
"""
Enrichit `fiches_etablissement` avec :
- `contacts`   <- Téléphone principal + secondaire + email + WhatsApp
                  (feuille Contacts du XLSX)
- `site_web`   <- Site web (feuille Contacts)
- `latitude`/`longitude` <- feuille Localisation
- `ville`      <- Ville (feuille Localisation, écrasement si NULL uniquement)

Le lien DB ↔ XLSX passe par `etablissement_details_xlsx.id_source_xlsx`.
N'écrase JAMAIS une valeur déjà renseignée (sauf pour `ville` que la source
XLSX couvre à 100%).
"""
from __future__ import annotations

import argparse
import re
import subprocess
import sys
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


def cell_str(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def is_non_disponible(s: str) -> bool:
    return not s or s.lower() in {"non disponible", "n/a", "nd", "-", "—", ""}


def sql_escape(s: str) -> str:
    return s.replace("'", "''")


# ── Lecture XLSX ──────────────────────────────────────────────────────────────
def charger_contacts_xlsx() -> dict[str, dict]:
    """{ID_ETABLISSEMENT: {contacts, site_web}}."""
    wb = openpyxl.load_workbook(XLSX_PATH, data_only=True)
    ws = wb["Contacts"]
    headers = [cell_str(c.value) for c in next(ws.iter_rows(min_row=1, max_row=1))]
    idx = {h: i for i, h in enumerate(headers)}
    out: dict[str, dict] = {}
    for row in ws.iter_rows(min_row=2, values_only=True):
        if not row or not row[idx["ID_ETABLISSEMENT"]]:
            continue
        eid = cell_str(row[idx["ID_ETABLISSEMENT"]])
        tel_p = cell_str(row[idx["Téléphone principal"]])
        tel_s = cell_str(row[idx["Téléphone secondaire"]])
        whatsapp = cell_str(row[idx["WhatsApp"]])
        email = cell_str(row[idx["Email"]])
        site = cell_str(row[idx["Site web"]])

        parts: list[str] = []
        if not is_non_disponible(tel_p):
            parts.append(f"📞 {tel_p}")
        if not is_non_disponible(tel_s):
            parts.append(f"📞 {tel_s}")
        if not is_non_disponible(whatsapp):
            parts.append(f"💬 WhatsApp : {whatsapp}")
        if not is_non_disponible(email):
            parts.append(f"✉️ {email}")
        contacts_str = "\n".join(parts) if parts else ""

        site_str = site if not is_non_disponible(site) else ""
        out[eid] = {"contacts": contacts_str, "site_web": site_str}
    return out


def charger_localisation_xlsx() -> dict[str, dict]:
    """{ID_ETABLISSEMENT: {latitude, longitude, ville}}."""
    wb = openpyxl.load_workbook(XLSX_PATH, data_only=True)
    ws = wb["Localisation"]
    headers = [cell_str(c.value) for c in next(ws.iter_rows(min_row=1, max_row=1))]
    idx = {h: i for i, h in enumerate(headers)}
    out: dict[str, dict] = {}
    for row in ws.iter_rows(min_row=2, values_only=True):
        if not row or not row[idx["ID_ETABLISSEMENT"]]:
            continue
        eid = cell_str(row[idx["ID_ETABLISSEMENT"]])
        lat_raw = cell_str(row[idx["Latitude"]])
        lng_raw = cell_str(row[idx["Longitude"]])
        ville = cell_str(row[idx["Ville"]])

        # Latitude/Longitude : parser en float (séparateur virgule possible)
        def parse_coord(s: str) -> float | None:
            if is_non_disponible(s):
                return None
            s = s.replace(",", ".")
            try:
                return float(s)
            except ValueError:
                return None

        out[eid] = {
            "latitude": parse_coord(lat_raw),
            "longitude": parse_coord(lng_raw),
            "ville": ville if not is_non_disponible(ville) else "",
        }
    return out


# ── DB ───────────────────────────────────────────────────────────────────────
def mapping_db_xlsx() -> dict[str, int]:
    out_raw = run_psql(
        "SELECT id_source_xlsx, fiche_id FROM etablissement_details_xlsx "
        "WHERE id_source_xlsx IS NOT NULL;"
    )
    out: dict[str, int] = {}
    for line in out_raw.splitlines():
        if "|" not in line:
            continue
        xlsx_id, fiche_id = line.split("|", 1)
        out[xlsx_id.strip()] = int(fiche_id.strip())
    return out


def get_etab(fiche_id: int) -> dict:
    out = run_psql(
        f"SELECT contacts, site_web, latitude, longitude, ville "
        f"FROM fiches_etablissement WHERE id = {fiche_id};"
    )
    fields = (out.split("|") + ["", "", "", "", ""])[:5]
    return {
        "contacts": fields[0] or None,
        "site_web": fields[1] or None,
        "latitude": fields[2] or None,
        "longitude": fields[3] or None,
        "ville": fields[4] or None,
    }


def update_field(fiche_id: int, field: str, value: str | float | None) -> None:
    if isinstance(value, str):
        v = f"'{sql_escape(value)}'"
    elif value is None:
        return
    else:
        v = str(value)
    run_psql(f"UPDATE fiches_etablissement SET {field} = {v} WHERE id = {fiche_id};")


# ── Main ─────────────────────────────────────────────────────────────────────
def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--limit", type=int, default=0)
    args = parser.parse_args()

    if not XLSX_PATH.exists():
        print(f"XLSX introuvable : {XLSX_PATH}", file=sys.stderr)
        sys.exit(1)

    print("Lecture des feuilles Contacts + Localisation...")
    contacts_xlsx = charger_contacts_xlsx()
    loc_xlsx = charger_localisation_xlsx()
    print(f"  Contacts : {len(contacts_xlsx)} établissements")
    print(f"  Localisation : {len(loc_xlsx)} établissements")

    print("Lecture du mapping DB ↔ XLSX...")
    mapping = mapping_db_xlsx()
    print(f"  → {len(mapping)} fiches liées au XLSX")

    counters = {
        "contacts_mis_a_jour": 0,
        "contacts_deja_renseigne": 0,
        "site_web_mis_a_jour": 0,
        "site_web_deja_renseigne": 0,
        "lat_mis_a_jour": 0,
        "lng_mis_a_jour": 0,
        "ville_mis_a_jour": 0,
        "ville_deja_renseigne": 0,
    }
    apercu: list[tuple[int, str, dict]] = []

    for xlsx_id, fiche_id in mapping.items():
        contact_info = contacts_xlsx.get(xlsx_id, {})
        loc_info = loc_xlsx.get(xlsx_id, {})

        titre = run_psql(f"SELECT titre FROM fiches WHERE id = {fiche_id};") \
            or f"(fiche {fiche_id})"
        actuel = get_etab(fiche_id)

        changes: dict = {}

        # contacts : ne pas écraser si déjà renseigné
        new_contacts = contact_info.get("contacts", "")
        if new_contacts and not actuel["contacts"]:
            changes["contacts"] = new_contacts
            counters["contacts_mis_a_jour"] += 1
        elif actuel["contacts"]:
            counters["contacts_deja_renseigne"] += 1

        # site_web : idem
        new_site = contact_info.get("site_web", "")
        if new_site and not actuel["site_web"]:
            changes["site_web"] = new_site
            counters["site_web_mis_a_jour"] += 1
        elif actuel["site_web"]:
            counters["site_web_deja_renseigne"] += 1

        # lat/lng : ne pas écraser
        new_lat = loc_info.get("latitude")
        new_lng = loc_info.get("longitude")
        if new_lat is not None and not actuel["latitude"]:
            changes["latitude"] = new_lat
            counters["lat_mis_a_jour"] += 1
        if new_lng is not None and not actuel["longitude"]:
            changes["longitude"] = new_lng
            counters["lng_mis_a_jour"] += 1

        # ville : ne pas écraser
        new_ville = loc_info.get("ville", "")
        if new_ville and not actuel["ville"]:
            changes["ville"] = new_ville
            counters["ville_mis_a_jour"] += 1
        elif actuel["ville"]:
            counters["ville_deja_renseigne"] += 1

        if changes:
            apercu.append((fiche_id, titre, changes))
            if not args.dry_run:
                for field, value in changes.items():
                    update_field(fiche_id, field, value)

        if args.limit and len(apercu) >= args.limit:
            break

    # Rapport
    print("\n" + "=" * 70)
    print(f"RÉSULTAT  (mode {'DRY-RUN' if args.dry_run else 'UPDATE'})")
    print("=" * 70)
    for k, v in counters.items():
        print(f"  {k:38} : {v}")

    if apercu:
        print("\n--- APERÇU (3 premiers) ---")
        for fid, titre, changes in apercu[:3]:
            print(f"\n[Fiche #{fid}] {titre}")
            for k, v in changes.items():
                v_str = str(v)[:80] + ("…" if len(str(v)) > 80 else "")
                print(f"  {k:12} -> {v_str}")


if __name__ == "__main__":
    main()
