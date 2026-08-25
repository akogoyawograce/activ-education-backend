#!/usr/bin/env python3
"""Mapping O*NET (RIASEC par métier) → filières togolaises.

Entrées :
  - collecte_togo/sources/onet_occupations.csv              : 923 métiers O*NET
  - collecte_togo/sources/onet_career_interest_types.csv    : scores RIASEC (1-7) par métier
  - collecte_togo/sources/onet_specific_interest_areas.csv  : centres d'intérêt détaillés
  - fiches_filiere (base Activ Education) → liste des filières réelles

Sorties (dossier calibration/) :
  - mapping_onet_filieres.csv        : filière → métiers O*NET appariés (codes + titres)
  - profil_riasec_data_driven.csv    : vecteur RIASEC 0-1 par filière (moyenne pondérée)
  - comparaison_catalogue_riasec.md  : comparaison avec ProfilFiliereRiasecCatalog.java
"""
from __future__ import annotations

import csv
from collections import defaultdict
from pathlib import Path

BASE = Path(__file__).parent
SRC = BASE / "collecte_togo" / "sources"
OUT = BASE / "calibration"
OUT.mkdir(exist_ok=True)

# Filière réelle (base Activ Education) → mots-clés de titres O*NET (anglais)
FILIERES = {
    "Agronomie":                ["agronom", "agricultur", "soil", "crop", "plant scientist", "horticultur"],
    "Anglais":                  ["interpreter", "translator", "english", "language"],
    "Arts Plastiques":          ["artist", "sculptor", "painter", "art", "designer", "photograph"],
    "Banque et Assurance":      ["bank", "insurance", "loan", "credit", "financial risk", "underwriter"],
    "Biologie":                 ["biolog", "microbiolog", "geneticist", "botanist", "zoolog", "biochemist"],
    "Communication et Journalisme": ["journalist", "reporter", "broadcast", "editor", "public relations", "communications"],
    "Droit":                    ["lawyer", "judge", "attorney", "paralegal", "legal", "arbitrator"],
    "Économie":                 ["economist", "market research", "financial analyst", "statistician"],
    "Enseignement Primaire":    ["elementary school teacher", "kindergarten", "preschool", "primary school"],
    "Finance et Comptabilité":  ["accountant", "auditor", "bookkeeping", "tax", "financial analyst", "treasurer"],
    "Génie Civil":              ["civil engineer", "construction", "surveyor", "structural"],
    "Génie Électrique":         ["electrical engineer", "electrician", "electronics engineer", "power"],
    "Génie Informatique":       ["software developer", "software engineer", "programmer", "computer", "developer"],
    "Génie Mécanique":          ["mechanical engineer", "machinist", "industrial engineer", "mechanical draft"],
    "Géographie":               ["geographer", "cartographer", "geospatial", "geographic", "surveyor"],
    "Gestion des Entreprises":  ["chief executive", "operations manager", "general manager", "business", "administrative services manager"],
    "Histoire":                 ["historian", "archaeologist", "anthropologist", "archivist"],
    "Informatique":             ["computer", "software", "data scientist", "information security", "network", "database", "programmer"],
    "Lettres Modernes":         ["writer", "author", "poet", "professor", "philosopher", "librarian"],
    "Mathématiques Appliquées": ["mathematician", "statistician", "actuary", "data scientist", "operations research"],
    "Médecine":                 ["physician", "surgeon", "doctor", "medical", "anesthesiologist", "obstetrician", "pediatrician"],
    "Odontologie":              ["dentist", "dental"],
    "Pharmacie":                ["pharmacist", "pharmaceutical"],
    "Physique-Chimie":          ["physicist", "chemist", "material scientist", "laboratory technician", "chemical technician"],
    "Psychologie":              ["psychologist", "counselor", "therapist", "mental health"],
    "Sciences de l'Éducation":  ["education", "teacher", "instructional", "curriculum", "professor"],
    "Sciences Environnementales": ["environmental scientist", "ecologist", "conservation", "environmental engineer", "hydrologist"],
    "Sciences Forestières":     ["forester", "forestry", "conservation scientist", "natural resources"],
    "Sciences Infirmières":     ["nurse", "healthcare", "medical assistant", "health services"],
    "Sciences Politiques":      ["political scientist", "public policy", "government", "legislator", "intelligence analyst"],
    "Sociologie":               ["sociologist", "social worker", "community", "anthropologist"],
    "Tourisme et Hôtellerie":   ["hotel", "travel", "tourism", "restaurant", "lodging", "flight attendant", "concierge"],
}


def load_occupations() -> dict[str, str]:
    """O*NET-SOC Code → Title."""
    occ: dict[str, str] = {}
    with (SRC / "onet_occupations.csv").open(newline="", encoding="utf-8") as fh:
        for r in csv.DictReader(fh):
            occ[r["O*NET-SOC Code"]] = r["Title"]
    return occ


def load_riasec() -> dict[str, list[float]]:
    """O*NET-SOC Code → vecteur [R, I, A, S, E, C] (échelle 1-7)."""
    dims = {"Realistic": 0, "Investigative": 1, "Artistic": 2,
            "Social": 3, "Enterprising": 4, "Conventional": 5}
    out: dict[str, list[float]] = defaultdict(lambda: [0.0] * 6)
    with (SRC / "onet_career_interest_types.csv").open(newline="", encoding="utf-8") as fh:
        for r in csv.DictReader(fh):
            if r["Scale Name"] != "Occupational Interests":
                continue
            code = r["O*NET-SOC Code"]
            try:
                out[code][dims[r["Element Name"]]] = float(r["Data Value"])
            except (KeyError, ValueError):
                continue
    return {k: v for k, v in out.items() if sum(v) > 0}


def match_occupations(filiere: str, occ: dict[str, str]) -> list[str]:
    kws = FILIERES[filiere]
    hits = []
    for code, title in occ.items():
        t = title.lower()
        if any(k in t for k in kws):
            hits.append(code)
    return sorted(set(hits))


def normalize(v: list[float]) -> list[float]:
    """(x - 1) / 6 : échelle O*NET 1-7 → 0-1."""
    return [round((x - 1) / 6, 3) for x in v]


def main() -> None:
    occ = load_occupations()
    riasec = load_riasec()
    print(f"O*NET : {len(occ)} métiers, {len(riasec)} avec scores RIASEC")

    mapping_rows, profil_rows = [], []
    for filiere in FILIERES:
        codes = match_occupations(filiere, occ)
        scored = [c for c in codes if c in riasec]
        mapping_rows.append({
            "filiere": filiere,
            "nb_metiers_matches": len(codes),
            "nb_avec_riasec": len(scored),
            "metiers": "; ".join(f"{c} {occ[c]}" for c in codes[:12]) + ("…" if len(codes) > 12 else ""),
        })
        if scored:
            n = len(scored)
            moy = [sum(riasec[c][i] for c in scored) / n for i in range(6)]
            profil_rows.append({
                "filiere": filiere,
                "nb_metiers": n,
                "R": normalize(moy)[0], "I": normalize(moy)[1], "A": normalize(moy)[2],
                "S": normalize(moy)[3], "E": normalize(moy)[4], "C": normalize(moy)[5],
            })
        else:
            profil_rows.append({"filiere": filiere, "nb_metiers": 0,
                                "R": None, "I": None, "A": None, "S": None, "E": None, "C": None})

    with (OUT / "mapping_onet_filieres.csv").open("w", newline="", encoding="utf-8") as fh:
        w = csv.DictWriter(fh, fieldnames=list(mapping_rows[0].keys()))
        w.writeheader(); w.writerows(mapping_rows)
    with (OUT / "profil_riasec_data_driven.csv").open("w", newline="", encoding="utf-8") as fh:
        w = csv.DictWriter(fh, fieldnames=list(profil_rows[0].keys()))
        w.writeheader(); w.writerows(profil_rows)
    print(f"  mapping_onet_filieres.csv : {len(mapping_rows)} filières")
    print(f"  profil_riasec_data_driven.csv : {len(profil_rows)} filières")

    sans = [p["filiere"] for p in profil_rows if p["nb_metiers"] == 0]
    if sans:
        print("  ⚠️  Aucun métier apparié :", ", ".join(sans))


if __name__ == "__main__":
    main()