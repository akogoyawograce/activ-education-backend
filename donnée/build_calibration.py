#!/usr/bin/env python3
"""Construit le référentiel de calibration (a priori réels) pour le modèle de prédiction.

Entrées (dossier donnée/ collecté le 18/08/2026) :
  - taux-de-reussite-au-bac*-*-serie-{a,c,d}.csv   → BAC I par série/région/sexe 2014-2022 (opendata.tg)
  - ministere_annuaire_national_2024_2025.txt      → BAC II session juin 2024 par région/série/sexe (tableau 3.10)
  - observationdata-tabmdfe.csv                    → effectifs universitaires par faculté 2013-2018 (INSEED)
  - collecte_togo/sources/unesco_opri_data_tgo.csv → indicateurs UNESCO UIS pour le Togo
  - collecte_togo/sources/worldbank_tertiary_*.json→ Banque mondiale (scolarisation/achèvement tertiaire)

Sorties : dossier calibration/ (a_priori_bac_series.csv, a_priori_bac2_2024.csv,
effectifs_universites_par_faculte.csv, a_priori_international.csv, README_calibration.md)
"""
from __future__ import annotations

import csv
import json
from collections import defaultdict
from pathlib import Path

BASE = Path(__file__).parent
OUT = BASE / "calibration"
OUT.mkdir(exist_ok=True)

SERIES = {"a": "A", "c": "C", "d": "D"}
REGIONS_ORDRE = ["Togo", "Grand Lomé", "Maritime", "Plateaux Est", "Plateaux Ouest",
                 "Centrale", "Kara", "Savanes"]


# ---------------------------------------------------------------------------
# 1) BAC I — synthèse des séries 2014-2022 (opendata.tg, INSEED)
# ---------------------------------------------------------------------------
def build_a_priori_bac_series() -> list[dict]:
    rows: list[dict] = []
    for lettre, serie in SERIES.items():
        f = BASE / f"taux-de-reussite-au-bac-i-par-region-et-par-sexe-serie-{lettre}.csv"
        if not f.exists():
            f = BASE / f"taux-de-reussite-au-baci-par-region-et-par-sexe-serie-{lettre}.csv"
        agg: dict[tuple[str, str], list[float]] = defaultdict(list)
        with f.open(newline="", encoding="utf-8") as fh:
            for r in csv.DictReader(fh):
                agg[(r["région"].strip(), r["sexe"].strip())].append(float(r["Value"]))
        for (region, sexe), vals in sorted(agg.items()):
            vals.sort()
            rows.append({
                "examen": "BAC I",
                "serie": serie,
                "region": region,
                "sexe": sexe,
                "annees_couvertes": f"2014-2022 ({len(vals)} obs)",
                "taux_min_pct": round(vals[0], 1),
                "taux_max_pct": round(vals[-1], 1),
                "taux_moyen_pct": round(sum(vals) / len(vals), 1),
                "taux_2022_pct": round(vals[-1], 1),
            })
    return rows


# ---------------------------------------------------------------------------
# 2) BAC II — session juin 2024 (annuaire ministère 2024-2025, tableau 3.10)
#    Valeurs extraites et vérifiées dans le .txt (lignes ~1185-1191).
#    Source : https://planifeducation.gouv.tg/wp-content/uploads/2025/09/Annuaire_National_2024_2025_29_09_25_2.pdf
# ---------------------------------------------------------------------------
BAC2_2024 = {
    # region: {serie: {M: , F: , T: }}  (pourcentages, session juin 2024, enseignement général)
    "Grand Lomé":     {"A": (56.5, 59.3, 58.0), "C": (74.1, 70.5, 72.6), "D": (51.2, 51.3, 51.2)},
    "Maritime":       {"A": (51.8, 48.6, 50.4), "C": (77.0, 76.5, 76.9), "D": (38.0, 33.1, 36.2)},
    "Plateaux Est":   {"A": (41.7, 36.5, 39.5), "C": (69.7, 66.7, 68.9), "D": (33.3, 23.6, 30.2)},
    "Plateaux Ouest": {"A": (47.5, 42.4, 45.1), "C": (76.9, 11.1, 60.0), "D": (40.5, 37.4, 39.4)},
    "Centrale":       {"A": (37.9, 32.6, 35.5), "C": (44.2, 47.8, 45.0), "D": (33.5, 23.4, 30.1)},
    "Kara":           {"A": (45.8, 41.9, 44.0), "C": (80.3, 86.4, 81.8), "D": (42.8, 39.6, 41.8)},
    "Savanes":        {"A": (54.9, 47.4, 51.6), "C": (73.5, 30.8, 61.7), "D": (40.3, 33.1, 38.1)},
    "Ensemble":       {"A": (49.1, 47.5, 48.3), "C": (71.6, 67.6, 70.3), "D": (42.9, 40.9, 42.2)},
}


def build_a_priori_bac2_2024() -> list[dict]:
    rows: list[dict] = []
    for region, series in BAC2_2024.items():
        for serie, (m, f, t) in series.items():
            rows.append({
                "examen": "BAC II (session juin 2024)",
                "serie": serie,
                "region": region,
                "sexe": "M", "taux_pct": m,
                "source": "Annuaire national 2024-2025 (tableau 3.10)",
            })
            rows.append({"examen": "BAC II (session juin 2024)", "serie": serie,
                         "region": region, "sexe": "F", "taux_pct": f,
                         "source": "Annuaire national 2024-2025 (tableau 3.10)"})
            rows.append({"examen": "BAC II (session juin 2024)", "serie": serie,
                         "region": region, "sexe": "T", "taux_pct": t,
                         "source": "Annuaire national 2024-2025 (tableau 3.10)"})
    return rows


# ---------------------------------------------------------------------------
# 3) Effectifs universitaires par faculté 2013-2018 (INSEED)
# ---------------------------------------------------------------------------
def build_effectifs_facultes() -> list[dict]:
    d: dict[str, dict[str, dict[str, float]]] = defaultdict(
        lambda: defaultdict(dict))  # fac -> annee -> {sexe: value}
    with (BASE / "observationdata-tabmdfe.csv").open(newline="", encoding="utf-8") as fh:
        for r in csv.DictReader(fh):
            try:
                v = float(r["Value"])
            except ValueError:
                continue
            d[r["libellés"].strip()][r["Date"]][r["sexe"].strip()] = v
    rows: list[dict] = []
    for fac in sorted(d):
        for annee in sorted(d[fac]):
            s = d[fac][annee]
            rows.append({
                "faculte": fac,
                "annee": int(annee),
                "hommes": s.get("M"),
                "femmes": s.get("F"),
                "total": s.get("TOTAL") or s.get("Total") or (s.get("M", 0) + s.get("F", 0)),
            })
    return rows


# ---------------------------------------------------------------------------
# 4) Contexte international Togo (UNESCO UIS + Banque mondiale)
# ---------------------------------------------------------------------------
UNESCO_SERIES = {
    "25053": "enrolment in tertiary education, all programmes, both sexes (number)",
    "25055": "enrolment in tertiary education, all programmes, female (number)",
    "GER.5T8": "gross enrolment ratio, tertiary, both sexes (%)",
    "GER.5T8.F": "gross enrolment ratio, tertiary, female (%)",
}


def build_a_priori_international() -> list[dict]:
    rows: list[dict] = []
    # UNESCO
    with (BASE / "collecte_togo/sources/unesco_opri_data_tgo.csv").open(
            newline="", encoding="utf-8") as fh:
        for r in csv.DictReader(fh):
            if r["indicator_id"] in UNESCO_SERIES and r["value"]:
                try:
                    v = float(r["value"])
                except ValueError:
                    continue
                rows.append({
                    "source": "UNESCO UIS (OPRI)",
                    "indicateur": UNESCO_SERIES[r["indicator_id"]],
                    "annee": int(r["year"]), "valeur": v,
                })
    # Banque mondiale
    for name, label in [("tertiary_enrollment",
                         "gross enrollment ratio, tertiary (% of gross)"),
                        ("tertiary_completion",
                         "completion rate, tertiary (% of relevant age group)")]:
        d = json.loads((BASE / f"collecte_togo/sources/worldbank_{name}.json").read_text())
        data = d[1] if isinstance(d, list) and len(d) > 1 else d
        for r in data:
            if r.get("countryiso3code") == "TGO" and r.get("value") is not None:
                rows.append({
                    "source": "Banque mondiale WDI",
                    "indicateur": label,
                    "annee": int(r["date"]), "valeur": float(r["value"]),
                })
    return rows


# ---------------------------------------------------------------------------
# Écriture
# ---------------------------------------------------------------------------
def write_csv(name: str, rows: list[dict]) -> None:
    if not rows:
        print(f"  ! {name} : aucun enregistrement")
        return
    with (OUT / name).open("w", newline="", encoding="utf-8") as fh:
        w = csv.DictWriter(fh, fieldnames=list(rows[0].keys()))
        w.writeheader()
        w.writerows(rows)
    print(f"  {name}: {len(rows)} lignes")


def main() -> None:
    print("Construction du référentiel de calibration…")
    write_csv("a_priori_bac_series.csv", build_a_priori_bac_series())
    write_csv("a_priori_bac2_2024.csv", build_a_priori_bac2_2024())
    write_csv("effectifs_universites_par_faculte.csv", build_effectifs_facultes())
    write_csv("a_priori_international.csv", build_a_priori_international())


if __name__ == "__main__":
    main()
