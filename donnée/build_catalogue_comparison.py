#!/usr/bin/env python3
"""Compare les profils RIASEC data-driven (O*NET) avec ProfilFiliereRiasecCatalog.java.

Sortie : calibration/comparaison_catalogue_riasec.md
"""
from __future__ import annotations

import csv
import math
from pathlib import Path

BASE = Path(__file__).parent
OUT = BASE / "calibration"

# filière DB → mot-clé du catalogue Java (ProfilFiliereRiasecCatalog)
CATALOG_KEY = {
    "Agronomie": "agronomie", "Anglais": None, "Arts Plastiques": "arts",
    "Banque et Assurance": "commerce", "Biologie": "biologie",
    "Communication et Journalisme": "journalisme", "Droit": "droit",
    "Économie": "economie", "Enseignement Primaire": "education",
    "Finance et Comptabilité": "comptabilite", "Génie Civil": "genie civil",
    "Génie Électrique": "genie electrique", "Génie Informatique": "informatique",
    "Génie Mécanique": "mecanique", "Géographie": "geographie",
    "Gestion des Entreprises": "gestion", "Histoire": "histoire",
    "Informatique": "informatique", "Lettres Modernes": "lettres",
    "Mathématiques Appliquées": "mathematiques", "Médecine": "medecine",
    "Odontologie": "medecine", "Pharmacie": "pharmacie",
    "Physique-Chimie": "physique", "Psychologie": "psychologie",
    "Sciences de l'Éducation": "sciences de l'education",
    "Sciences Environnementales": "environnement",
    "Sciences Forestières": "environnement", "Sciences Infirmières": "soins infirmiers",
    "Sciences Politiques": "sociologie", "Sociologie": "sociologie",
    "Tourisme et Hôtellerie": "tourisme",
}

CATALOG = {
    "informatique":      (0.80, 0.95, 0.30, 0.30, 0.40, 0.60),
    "mathematiques":     (0.60, 0.95, 0.30, 0.30, 0.30, 0.70),
    "physique":          (0.70, 0.95, 0.30, 0.40, 0.30, 0.65),
    "genie civil":       (0.90, 0.75, 0.20, 0.40, 0.50, 0.60),
    "genie electrique":  (0.85, 0.85, 0.25, 0.35, 0.40, 0.60),
    "medecine":          (0.55, 0.95, 0.30, 0.90, 0.40, 0.80),
    "pharmacie":         (0.55, 0.90, 0.30, 0.75, 0.40, 0.85),
    "biologie":          (0.60, 0.90, 0.40, 0.60, 0.30, 0.65),
    "droit":             (0.30, 0.70, 0.50, 0.85, 0.80, 0.85),
    "lettres":           (0.20, 0.60, 0.90, 0.75, 0.40, 0.60),
    "journalisme":       (0.20, 0.55, 0.85, 0.80, 0.60, 0.50),
    "psychologie":       (0.30, 0.75, 0.65, 0.95, 0.50, 0.55),
    "education":         (0.20, 0.45, 0.55, 0.95, 0.40, 0.55),
    "sciences de l'education": (0.25, 0.50, 0.50, 0.95, 0.35, 0.50),
    "sociologie":        (0.30, 0.70, 0.55, 0.85, 0.40, 0.60),
    "gestion":           (0.40, 0.55, 0.40, 0.80, 0.95, 0.85),
    "economie":          (0.35, 0.80, 0.35, 0.65, 0.80, 0.85),
    "commerce":          (0.45, 0.45, 0.40, 0.85, 0.95, 0.75),
    "comptabilite":      (0.40, 0.55, 0.25, 0.55, 0.65, 0.95),
    "agronomie":         (0.85, 0.65, 0.25, 0.40, 0.35, 0.50),
    "environnement":     (0.75, 0.70, 0.30, 0.45, 0.30, 0.50),
    "arts":              (0.25, 0.30, 0.95, 0.40, 0.35, 0.20),
    "tourisme":          (0.25, 0.35, 0.45, 0.90, 0.75, 0.50),
    "mecanique":         (0.95, 0.60, 0.15, 0.25, 0.35, 0.55),
    "geographie":        (0.50, 0.75, 0.55, 0.60, 0.30, 0.50),
    "histoire":          (0.20, 0.65, 0.70, 0.55, 0.25, 0.60),
    "soins infirmiers":  (0.60, 0.75, 0.30, 0.95, 0.30, 0.65),
}


def cosine(a, b):
    dot = sum(x * y for x, y in zip(a, b))
    na = math.sqrt(sum(x * x for x in a)) or 1
    nb = math.sqrt(sum(x * x for x in b)) or 1
    return dot / (na * nb)


def read_driven() -> dict[str, tuple]:
    out = {}
    with (OUT / "profil_riasec_data_driven.csv").open(newline="", encoding="utf-8") as fh:
        for r in csv.DictReader(fh):
            if r["nb_metiers"] == "0":
                continue
            out[r["filiere"]] = tuple(float(r[k]) for k in "RIASEC")
    return out


def read_driven_counts() -> dict[str, int]:
    out = {}
    with (OUT / "profil_riasec_data_driven.csv").open(newline="", encoding="utf-8") as fh:
        for r in csv.DictReader(fh):
            out[r["filiere"]] = int(r["nb_metiers"])
    return out


def main() -> None:
    driven = read_driven()
    driven_counts = read_driven_counts()
    dims = "RIASEC"
    lines = [
        "# Comparaison des profils RIASEC — O*NET (data-driven) vs ProfilFiliereRiasecCatalog.java",
        "",
        f"Généré le 18/08/2026. {len(driven)} filières profilées depuis O*NET 30.3 (moyenne des métiers appariés, échelle 0-1).",
        "",
        "## Tableau de comparaison",
        "",
        "| Filière (DB) | n métiers | Profil O*NET (R I A S E C) | Clé catalogue | Profil catalogue (R I A S E C) | Cosinus | Verdict |",
        "|---|---:|---|---|---|---:|---|",
    ]
    verdicts = []
    for filiere, dv in sorted(driven.items()):
        key = CATALOG_KEY.get(filiere)
        cv = CATALOG.get(key) if key else None
        if cv is None:
            cos, verdict = "—", "🔵 NON référencée au catalogue"
        else:
            c = cosine(dv, cv)
            cos = f"{c:.2f}"
            verdict = ("🟢 cohérent" if c >= 0.95 else "🟡 à ajuster" if c >= 0.85 else "🔴 écart fort")
            verdicts.append((filiere, c))
        od = " ".join(f"{x:.2f}" for x in dv)
        oc = " ".join(f"{x:.2f}" for x in cv) if cv else "—"
        nb = int(driven_counts.get(filiere, 0))
        lines.append(f"| {filiere} | {nb} | {od} | {key or '—'} | {oc} | {cos} | {verdict} |")

    if verdicts:
        verdicts.sort(key=lambda x: x[1])
        lines += ["", "## Filières à ajuster en priorité (cosinus le plus faible)", ""]
        for f, c in verdicts[:5]:
            lines.append(f"- **{f}** : cosinus {c:.2f}")
    lines += ["", "## Recommandations", ""]
    lines += [
        "- Les profils O*NET sont **fondés sur 923 métiers réels** (enquêtes US) : ils peuvent remplacer ou"
        " recouper les valeurs manuelles du catalogue Java.",
        "- `Pharmacie` (n=1) et `Odontologie` (mappée sur 'medecine') sont **sous-échantillonnées** :"
        " à enrichir avant toute mise en production.",
        "- Filières absentes du catalogue Java : **Odontologie, Sciences Forestières, Sciences Politiques,"
        " Banque et Assurance, Enseignement Primaire, Anglais** (profil neutre actuellement).",
        "- Prochaine étape possible : régénérer `ProfilFiliereRiasecCatalog.java` avec les valeurs O*NET"
        " normalisées (après validation humaine).",
    ]
    (OUT / "comparaison_catalogue_riasec.md").write_text("\n".join(lines), encoding="utf-8")
    print(f"comparaison_catalogue_riasec.md écrit ({len(driven)} filières)")


if __name__ == "__main__":
    main()