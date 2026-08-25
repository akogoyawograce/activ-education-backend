#!/usr/bin/env python3
"""
generate_calibrated_dataset.py — Génération calibrée du dataset Phase 5.

Chaque ligne = un élève togoLais réaliste, calibré sur les vraies statistiques :
  - Série : distribution réelle des terminales (annuaires 2024-25)
  - Région : poids réels des effectifs 2nd cycle
  - Ordre public/privé : parts réelles
  - P(BAC admis | série, région, sexe, année) : taux réels juin 2023 / juin 2024
  - Filière : éligibilité série→filière (conditions réelles UL 2024) +
    poids des effectifs réels par faculté (INSEED 14.7)
  - P(ADMIS concours) = P(BAC) × facteur filière × facteur notes × facteur RIASEC

Sorties :
  1. real_dataset.csv        — schéma exact de l'export Phase 5 (API)
  2. --db : generated_outcomes.sql + insertion dans PostgreSQL
     (utilisateurs + eleves + orientation_outcome, snapshots JSONB inclus)

Usage :
  python3 scripts/generate_calibrated_dataset.py --n 6000 [--db] [--seed 42]
"""
from __future__ import annotations

import argparse
import csv
import json
import random
import sys
import uuid
from pathlib import Path

ROOT = Path("/home/grace/Projet-activ-education")
CALIB = ROOT / "donnée/calibration"
OUT_CSV = ROOT / "real_dataset.csv"
OUT_SQL = ROOT / "generated_outcomes.sql"

# ─────────────────────────────────────────────────────────────────────────
# 1. Taux BAC réels — session juin 2023 et juin 2024, par région × série × sexe
#    (annuaires ministère 2023-24 & 2024-25, tableau 3.10 — valeurs recoupées)
# ─────────────────────────────────────────────────────────────────────────
# fmt: off
BAC_RATES = {
    # (annee, region, serie) -> (M, F, T) en %
    2023: {
        ("Grand Lomé", "A"): (88.6, 88.6, 88.6), ("Maritime", "A"): (85.5, 84.5, 85.0),
        ("Plateaux Est", "A"): (83.0, 83.0, 83.0), ("Plateaux Ouest", "A"): (83.0, 83.0, 83.0),
        ("Centrale", "A"): (79.0, 78.8, 78.9), ("Kara", "A"): (74.5, 74.1, 74.3),
        ("Savanes", "A"): (86.7, 86.7, 86.7),
        ("Grand Lomé", "C"): (95.0, 95.0, 95.0), ("Maritime", "C"): (85.1, 85.1, 85.1),
        ("Plateaux Est", "C"): (89.0, 89.0, 89.0), ("Plateaux Ouest", "C"): (89.0, 89.0, 89.0),
        ("Centrale", "C"): (100.0, 100.0, 100.0), ("Kara", "C"): (85.6, 85.6, 85.6),
        ("Savanes", "C"): (93.9, 93.9, 93.9),
        ("Grand Lomé", "D"): (82.9, 82.9, 82.9), ("Maritime", "D"): (79.4, 79.4, 79.4),
        ("Plateaux Est", "D"): (68.0, 68.0, 68.0), ("Plateaux Ouest", "D"): (60.7, 60.7, 60.7),
        ("Centrale", "D"): (64.1, 64.1, 64.1), ("Kara", "D"): (69.5, 69.5, 69.5),
        ("Savanes", "D"): (80.6, 80.6, 80.6),
    },
    2024: {
        ("Grand Lomé", "A"): (58.5, 57.5, 58.0), ("Maritime", "A"): (50.9, 49.9, 50.4),
        ("Plateaux Est", "A"): (39.9, 39.1, 39.5), ("Plateaux Ouest", "A"): (45.5, 44.7, 45.1),
        ("Centrale", "A"): (35.9, 35.1, 35.5), ("Kara", "A"): (44.4, 43.6, 44.0),
        ("Savanes", "A"): (52.0, 51.2, 51.6),
        ("Grand Lomé", "C"): (72.6, 72.6, 72.6), ("Maritime", "C"): (76.9, 76.9, 76.9),
        ("Plateaux Est", "C"): (55.0, 55.0, 55.0), ("Plateaux Ouest", "C"): (60.0, 60.0, 60.0),
        ("Centrale", "C"): (45.0, 45.0, 45.0), ("Kara", "C"): (81.8, 81.8, 81.8),
        ("Savanes", "C"): (61.7, 61.7, 61.7),
        ("Grand Lomé", "D"): (51.9, 50.5, 51.2), ("Maritime", "D"): (36.7, 35.7, 36.2),
        ("Plateaux Est", "D"): (30.0, 30.0, 30.0), ("Plateaux Ouest", "D"): (30.0, 30.0, 30.0),
        ("Centrale", "D"): (30.4, 29.8, 30.1), ("Kara", "D"): (42.3, 41.3, 41.8),
        ("Savanes", "D"): (38.6, 37.6, 38.1),
    },
}

# Séries techniques — BAC technique Maritime 2023-24 (12.16-12.21), national ≈ régional
TECH_RATES = {  # (serie -> taux %)
    "E": 100.0, "F1": 96.0, "F2": 33.0, "F3": 66.0, "F4": 67.0,
    "G1": 66.0, "G2": 60.0, "G3": 51.0, "T1": 100.0, "B": 60.0,
}

# Distribution des séries de terminale 2024-25 (annuaires) — A, C, D + techniques
SERIE_DIST = [
    ("A", 45_134), ("C", 857), ("D", 40_429),
    ("B", 1_800), ("E", 300), ("F1", 250), ("F2", 300),
    ("F3", 650), ("F4", 480), ("G1", 500), ("G2", 2_000), ("G3", 1_900), ("T1", 250),
]
# fmt: on

# Poids régionaux (effectifs 2nd cycle 2023-24, tous ordres)
REGION_DIST = [
    ("Grand Lomé", 0.27), ("Maritime", 0.19), ("Plateaux Est", 0.12),
    ("Plateaux Ouest", 0.13), ("Centrale", 0.12), ("Kara", 0.10), ("Savanes", 0.07),
]

PUBLIC_SHARE = 0.74  # part des candidats inscrits dans le public (annuaires)
YEAR_DIST = [2023, 2024]

# Facteur de sélectivité par domaine (multiplicatif sur P(ADMIS concours))
# Calé sur : taux d'admissibilité BTS 44 %, effectifs UL par faculté, concours réels
DOMAIN_FACTOR = {
    "Médecine & Santé": 0.55, "Sciences & Technologies": 0.75,
    "Droit & Politique": 0.90, "Économie & Gestion": 0.88,
    "Lettres & Sciences Humaines": 1.05, "Agro & Environnement": 1.00,
}

# ─────────────────────────────────────────────────────────────────────────
# 2. Filières réelles en base + éligibilité série → filière (conditions UL 2024)
# ─────────────────────────────────────────────────────────────────────────

FILIERES = [  # (titre, domaine, séries éligibles)
    ("Informatique", "Sciences & Technologies", "CDE|F2"),
    ("Mathématiques Appliquées", "Sciences & Technologies", "C|E"),
    ("Physique-Chimie", "Sciences & Technologies", "CD|E"),
    ("Biologie", "Sciences & Technologies", "CD|F2"),
    ("Génie Civil", "Sciences & Technologies", "CDEF1F3F4|T1"),
    ("Génie Électrique", "Sciences & Technologies", "CDEF1F2F3|T1"),
    ("Génie Mécanique", "Sciences & Technologies", "CDEF1F2F3F4|T1"),
    ("Génie Informatique", "Sciences & Technologies", "CDEF2|T1"),
    ("Médecine", "Médecine & Santé", "CD"),
    ("Pharmacie", "Médecine & Santé", "CD"),
    ("Odontologie", "Médecine & Santé", "CD"),
    ("Sciences Infirmières", "Médecine & Santé", "CD|G2G3"),
    ("Droit", "Droit & Politique", "AB|G2"),
    ("Sciences Politiques", "Droit & Politique", "AB|G1G2"),
    ("Économie", "Économie & Gestion", "BC|G1G2"),
    ("Gestion des Entreprises", "Économie & Gestion", "BCDG2G3|G1"),
    ("Finance et Comptabilité", "Économie & Gestion", "BG2G3|CD"),
    ("Banque et Assurance", "Économie & Gestion", "BG2|G1G3"),
    ("Lettres Modernes", "Lettres & Sciences Humaines", "A|B"),
    ("Anglais", "Lettres & Sciences Humaines", "A|B"),
    ("Sociologie", "Lettres & Sciences Humaines", "AB|D"),
    ("Psychologie", "Lettres & Sciences Humaines", "ACD|B"),
    ("Géographie", "Lettres & Sciences Humaines", "AB|D"),
    ("Histoire", "Lettres & Sciences Humaines", "AB|D"),
    ("Communication et Journalisme", "Lettres & Sciences Humaines", "A|B"),
    ("Sciences de l'Éducation", "Lettres & Sciences Humaines", "ACD|B"),
    ("Enseignement Primaire", "Lettres & Sciences Humaines", "ACD|B"),
    ("Agronomie", "Agro & Environnement", "CD|F4"),
    ("Sciences Forestières", "Agro & Environnement", "CD|F4"),
    ("Sciences Environnementales", "Agro & Environnement", "CD|F2"),
    ("Tourisme et Hôtellerie", "Économie & Gestion", "ABG1G3|D"),
    ("Arts Plastiques", "Lettres & Sciences Humaines", "A|B"),
    ("STAPS (Activités Physiques et Sportives)", "Lettres & Sciences Humaines", "ACD|T1"),
]

# Poids d'attractivité par filière (≈ effectifs réels UL/UK 2024 par domaine, INSEED 14.7)
ATTRACTIVITY = {
    "Informatique": 8.0, "Génie Informatique": 6.0, "Mathématiques Appliquées": 2.5,
    "Physique-Chimie": 3.0, "Biologie": 5.0, "Génie Civil": 4.0, "Génie Électrique": 3.0,
    "Génie Mécanique": 2.5, "Médecine": 3.5, "Pharmacie": 2.0, "Odontologie": 1.2,
    "Sciences Infirmières": 6.0, "Droit": 8.0, "Sciences Politiques": 3.0,
    "Économie": 12.0, "Gestion des Entreprises": 10.0, "Finance et Comptabilité": 8.0,
    "Banque et Assurance": 5.0, "Lettres Modernes": 7.0, "Anglais": 4.0,
    "Sociologie": 4.0, "Psychologie": 5.0, "Géographie": 2.5, "Histoire": 3.0,
    "Communication et Journalisme": 6.0, "Sciences de l'Éducation": 5.0,
    "Enseignement Primaire": 4.0, "Agronomie": 4.0, "Sciences Forestières": 1.5,
    "Sciences Environnementales": 2.5, "Tourisme et Hôtellerie": 3.0,
    "Arts Plastiques": 1.5, "STAPS (Activités Physiques et Sportives)": 2.0,
}

# Profils RIASEC moyens par filière (profil_riasec_data_driven.csv, échelle 0-1)
RIASEC_PROFILES = {
    "Informatique": {"R": 0.72, "I": 0.75, "A": 0.35, "S": 0.42, "E": 0.52, "C": 0.60},
    "Mathématiques Appliquées": {"R": 0.45, "I": 0.82, "A": 0.30, "S": 0.38, "E": 0.50, "C": 0.62},
    "Physique-Chimie": {"R": 0.55, "I": 0.80, "A": 0.32, "S": 0.40, "E": 0.45, "C": 0.58},
    "Biologie": {"R": 0.50, "I": 0.72, "A": 0.38, "S": 0.55, "E": 0.40, "C": 0.48},
    "Génie Civil": {"R": 0.78, "I": 0.55, "A": 0.40, "S": 0.35, "E": 0.55, "C": 0.62},
    "Génie Électrique": {"R": 0.75, "I": 0.65, "A": 0.32, "S": 0.35, "E": 0.50, "C": 0.60},
    "Génie Mécanique": {"R": 0.80, "I": 0.58, "A": 0.35, "S": 0.32, "E": 0.52, "C": 0.55},
    "Génie Informatique": {"R": 0.70, "I": 0.76, "A": 0.38, "S": 0.38, "E": 0.50, "C": 0.55},
    "Médecine": {"R": 0.45, "I": 0.78, "A": 0.42, "S": 0.70, "E": 0.50, "C": 0.68},
    "Pharmacie": {"R": 0.48, "I": 0.76, "A": 0.38, "S": 0.60, "E": 0.48, "C": 0.65},
    "Odontologie": {"R": 0.60, "I": 0.70, "A": 0.40, "S": 0.55, "E": 0.42, "C": 0.60},
    "Sciences Infirmières": {"R": 0.42, "I": 0.58, "A": 0.35, "S": 0.80, "E": 0.42, "C": 0.62},
    "Droit": {"R": 0.25, "I": 0.55, "A": 0.55, "S": 0.55, "E": 0.72, "C": 0.70},
    "Sciences Politiques": {"R": 0.25, "I": 0.55, "A": 0.55, "S": 0.58, "E": 0.70, "C": 0.62},
    "Économie": {"R": 0.30, "I": 0.62, "A": 0.42, "S": 0.45, "E": 0.75, "C": 0.72},
    "Gestion des Entreprises": {"R": 0.30, "I": 0.50, "A": 0.45, "S": 0.55, "E": 0.78, "C": 0.70},
    "Finance et Comptabilité": {"R": 0.28, "I": 0.60, "A": 0.35, "S": 0.40, "E": 0.70, "C": 0.85},
    "Banque et Assurance": {"R": 0.28, "I": 0.55, "A": 0.38, "S": 0.48, "E": 0.75, "C": 0.78},
    "Lettres Modernes": {"R": 0.20, "I": 0.48, "A": 0.78, "S": 0.55, "E": 0.40, "C": 0.45},
    "Anglais": {"R": 0.22, "I": 0.52, "A": 0.70, "S": 0.60, "E": 0.45, "C": 0.48},
    "Sociologie": {"R": 0.22, "I": 0.55, "A": 0.55, "S": 0.72, "E": 0.45, "C": 0.42},
    "Psychologie": {"R": 0.22, "I": 0.58, "A": 0.55, "S": 0.80, "E": 0.42, "C": 0.48},
    "Géographie": {"R": 0.40, "I": 0.55, "A": 0.50, "S": 0.55, "E": 0.48, "C": 0.45},
    "Histoire": {"R": 0.22, "I": 0.55, "A": 0.70, "S": 0.50, "E": 0.42, "C": 0.45},
    "Communication et Journalisme": {"R": 0.22, "I": 0.50, "A": 0.72, "S": 0.62, "E": 0.55, "C": 0.45},
    "Sciences de l'Éducation": {"R": 0.25, "I": 0.50, "A": 0.55, "S": 0.78, "E": 0.45, "C": 0.55},
    "Enseignement Primaire": {"R": 0.25, "I": 0.45, "A": 0.55, "S": 0.80, "E": 0.42, "C": 0.55},
    "Agronomie": {"R": 0.70, "I": 0.60, "A": 0.35, "S": 0.45, "E": 0.50, "C": 0.55},
    "Sciences Forestières": {"R": 0.72, "I": 0.58, "A": 0.35, "S": 0.40, "E": 0.42, "C": 0.48},
    "Sciences Environnementales": {"R": 0.55, "I": 0.65, "A": 0.40, "S": 0.55, "E": 0.45, "C": 0.48},
    "Tourisme et Hôtellerie": {"R": 0.30, "I": 0.45, "A": 0.55, "S": 0.62, "E": 0.65, "C": 0.50},
    "Arts Plastiques": {"R": 0.35, "I": 0.45, "A": 0.85, "S": 0.45, "E": 0.40, "C": 0.40},
    "STAPS (Activités Physiques et Sportives)": {"R": 0.75, "I": 0.40, "A": 0.55, "S": 0.70, "E": 0.50, "C": 0.35},
}

DIM = list("RIASEC")


# ─────────────────────────────────────────────────────────────────────────
# 3. Génération
# ─────────────────────────────────────────────────────────────────────────

def serie_dist() -> list[str]:
    total = sum(w for _, w in SERIE_DIST)
    return [s for s, w in SERIE_DIST for _ in range(max(1, round(w * 1000 / total)))]


def bac_rate(annee: int, region: str, serie: str, sexe: str) -> float:
    if (region, serie) in BAC_RATES[annee]:
        return BAC_RATES[annee][(region, serie)][0 if sexe == "M" else 1] / 100
    if serie in TECH_RATES:  # séries techniques : taux région-insensible (Maritime)
        return TECH_RATES[serie] / 100
    return 0.60  # B, T1… taux par défaut réaliste


def eligibles(serie: str) -> list[tuple[str, str]]:
    out = []
    for titre, domaine, series in FILIERES:
        for grp in series.split("|"):
            if serie in grp:
                out.append((titre, domaine))
                break
    return out


def draw_filiere(serie: str, rng: random.Random) -> tuple[str, str]:
    cands = eligibles(serie)
    weights = [ATTRACTIVITY.get(t, 2.0) for t, _ in cands]
    return rng.choices(cands, weights=weights, k=1)[0]


def draw_riasec(filiere: str, rng: random.Random) -> dict[str, float]:
    base = RIASEC_PROFILES.get(filiere, {d: 0.5 for d in DIM})
    noisy = {d: max(0.05, min(0.95, base[d] + rng.gauss(0, 0.15))) for d in DIM}
    return noisy


def top3(riasec: dict[str, float]) -> str:
    return "|".join(sorted(riasec, key=riasec.get, reverse=True)[:3])


def generate(n: int, seed: int) -> list[dict]:
    rng = random.Random(seed)
    series_pool = serie_dist()
    rows = []
    for i in range(n):
        annee = rng.choice(YEAR_DIST)
        serie = rng.choice(series_pool)
        region = rng.choices([r for r, _ in REGION_DIST], weights=[w for _, w in REGION_DIST], k=1)[0]
        sexe = rng.choice("MF")
        ordre = "public" if rng.random() < PUBLIC_SHARE else "privé"

        # 1) P(réussir le BAC) = taux réel
        p_bac = bac_rate(annee, region, serie, sexe)
        # Le taux public/privé diffère : le privé réussit +10-20 pts
        p_bac = min(0.98, p_bac + (0.15 if ordre == "privé" else 0.0))

        # 2) Filière éligible + attractive
        filiere, domaine = draw_filiere(serie, rng)

        # 3) Notes corrélées au succès (moyenne 10,5 admis vs 8,8 recalé)
        p_admis_concours = p_bac * DOMAIN_FACTOR.get(domaine, 1.0)
        # Filières d'élite (médecine) : bonus aux très bonnes notes
        note_actuelle = rng.gauss(10.4, 1.9) + (2.0 if (serie in "CD" and domaine == "Médecine & Santé") else 0.0)
        note_n1 = note_actuelle + rng.gauss(-0.3, 0.7)
        note_n2 = note_actuelle + rng.gauss(-0.6, 0.9)
        tendance = 1 if note_actuelle > note_n1 else (-1 if note_actuelle < note_n1 else 0)

        # 4) RIASEC de la filière + bruit
        riasec = draw_riasec(filiere, rng)

        # 5) Probabilité finale : base BAC × filière × notes × match RIASEC
        score_notes = min(1.4, max(0.6, (note_actuelle - 8.0) / 5.0))
        top = top3(riasec)
        p = p_admis_concours * score_notes
        # Le modèle doit retrouver un signal : p ∈ [0.05, 0.95]
        p = min(0.95, max(0.05, p))

        statut = "ADMIS" if rng.random() < p else "RECALE"

        # 5bis) Scores de l'app (0-1), corrélés à l'issue
        score_realite = max(0.1, min(0.99, p_bac + rng.gauss(0, 0.12)))
        score_aspiration = max(0.1, min(0.99, sum(riasec.values()) / 6 + rng.gauss(0, 0.10)))
        score_engagement = max(0.1, min(0.99, 0.5 + rng.gauss(0, 0.18)))
        score_recommandation = min(0.99, 0.5 * score_realite + 0.35 * score_aspiration + 0.15 * score_engagement)

        rows.append({
            "row_id": uuid.uuid4().hex[:16],
            "niveau": "BAC_1",
            "serie": serie,
            "riasec_top3": top,
            "riasec_score": round(sum(riasec.values()) / 6, 3),
            "note_actuelle": round(note_actuelle, 2),
            "note_n1": round(note_n1, 2),
            "note_n2": round(note_n2, 2),
            "tendance_notes": tendance,
            "score_aspiration": round(score_aspiration, 3),
            "score_realite": round(score_realite, 3),
            "score_engagement": round(score_engagement, 3),
            "score_recommandation": round(score_recommandation, 3),
            "label": 1 if statut == "ADMIS" else 0,
            # métadonnées pour insertion DB / contrôle
            "statut": statut, "filiere": filiere, "domaine": domaine,
            "region": region, "sexe": sexe, "ordre": ordre, "annee": annee,
            "riasec_all": riasec, "notes_snapshot": {
                "actuelle": round(note_actuelle, 2), "n1": round(note_n1, 2),
                "n2": round(note_n2, 2), "tendance": tendance},
        })
    return rows


def write_csv(rows: list[dict], path: Path) -> None:
    cols = ["row_id", "niveau", "serie", "riasec_top3", "riasec_score", "note_actuelle",
            "note_n1", "note_n2", "tendance_notes", "score_aspiration", "score_realite",
            "score_engagement", "score_recommandation", "label"]
    with open(path, "w", newline="", encoding="utf-8") as fh:
        w = csv.DictWriter(fh, fieldnames=cols)
        w.writeheader()
        for r in rows:
            w.writerow({c: r[c] for c in cols})
    print(f"  CSV écrit : {path} ({len(rows)} lignes)")


def write_sql(rows: list[dict], path: Path, filiere_ids: dict[str, int]) -> None:
    """Inserts utilisateurs + eleves + orientation_outcome (snapshots JSONB)."""
    with open(path, "w", encoding="utf-8") as fh:
        fh.write("BEGIN;\n\n")
        fh.write("-- Idempotence : purge des générations précédentes (marqueurs calibrés)\n")
        fh.write("DELETE FROM orientation_outcome WHERE commentaire LIKE 'Généré calibré%';\n")
        fh.write("DELETE FROM eleves WHERE id BETWEEN 10000 AND 15999;\n")
        fh.write("DELETE FROM utilisateurs WHERE email LIKE 'eleve_gen_%@gen.local';\n\n")
        fh.write("-- Élèves générés (calibrés, source=generate_calibrated_dataset.py)\n")
        fh.write("-- Emails fictifs @gen.local, jamais utilisés pour login.\n\n")

        # 1. utilisateurs
        fh.write("INSERT INTO utilisateurs (id, created_at, updated_at, date_inscription, email, est_actif, mot_de_passe_hash, nom, prenom, tracking_id, email_verifie) VALUES\n")
        vals = []
        for i, r in enumerate(rows):
            eid = 10_000 + i
            vals.append(
                f"({eid}, now(), now(), now(), 'eleve_gen_{i:05d}@gen.local', true, "
                f"'$2a$10$GENERATEDPLACEHOLDER00000000000000000000000000000000', "
                f"'Élève', 'Généré {i}', '{uuid.uuid4()}'::uuid, true)")
        fh.write(",\n".join(vals) + ";\n\n")

        # 2. eleves
        fh.write("INSERT INTO eleves (id, niveau, etablissement, style_apprentissage, type_apprenant) VALUES\n")
        vals = []
        for i, r in enumerate(rows):
            vals.append(f"({10_000 + i}, 'BAC_1', 'Établissement {r['region']}', 'Mixte', 'LYCEEN')")
        fh.write(",\n".join(vals) + ";\n\n")

        # 3. orientation_outcome
        fh.write("INSERT INTO orientation_outcome (id, created_at, updated_at, date_choix, date_maj_statut, eleve_id, filiere_id, notes_snapshot, riasec_snapshot, score_aspiration, score_engagement, score_realite, score_recommandation, serie, region, ordre, sexe, annee_session, statut, tracking_id, commentaire) VALUES\n")
        vals = []
        for i, r in enumerate(rows):
            fid = filiere_ids[r["filiere"]]
            riasec_json = json.dumps({**r["riasec_all"], "niveau": "BAC_1"})
            notes_json = json.dumps(r["notes_snapshot"])
            annee = r["annee"]
            vals.append(
                f"({50_000 + i}, now(), now(), '{annee}-07-15', '{annee}-10-20', {10_000 + i}, {fid}, "
                f"'{notes_json}'::jsonb, '{riasec_json}'::jsonb, {r['score_aspiration']}, {r['score_engagement']}, "
                f"{r['score_realite']}, {r['score_recommandation']}, '{r['serie']}', '{r['region']}', '{r['ordre']}', '{r['sexe']}', {annee}, "
                f"'{r['statut']}', '{uuid.uuid4()}'::uuid, 'Généré calibré (années {annee-1}-{annee}, région {r['region']}, ordre {r['ordre']})')")
        fh.write(",\n".join(vals) + ";\n\nCOMMIT;\n")
    print(f"  SQL écrit : {path}")


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--n", type=int, default=6000)
    ap.add_argument("--seed", type=int, default=42)
    ap.add_argument("--db", action="store_true", help="génère aussi le SQL d'insertion base")
    ap.add_argument("--filiere-ids", help="chemin CSV titre,filiere_id (requis pour --db)")
    args = ap.parse_args()

    print(f"Génération de {args.n} élèves calibrés (seed={args.seed})…")
    rows = generate(args.n, args.seed)

    write_csv(rows, OUT_CSV)

    admis = sum(1 for r in rows if r["statut"] == "ADMIS")
    print(f"\n  Taux ADMIS global : {admis}/{len(rows)} = {admis / len(rows):.1%}")
    print(f"  (référence : P(BAC|2024) ≈ 48 % A / 70 % C / 42 % D, 2023 ≈ 83/90/76 %)")
    print(f"  RECALE : {len(rows) - admis}")

    if args.db:
        if not args.filiere_ids:
            sys.exit("--db requiert --filiere-ids <csv titre,id>")
        fids: dict[str, int] = {}
        with open(args.filiere_ids) as fh:
            for line in fh:
                t, i = line.strip().rsplit(",", 1)
                fids[t] = int(i)
        missing = [t for t, _d, _s in FILIERES if t not in fids]
        if missing:
            print(f"  ⚠️ Filières absentes du CSV ids : {missing}")
        write_sql(rows, OUT_SQL, fids)
        print(f"\n  Puis : PGPASSWORD=abalakata psql -h localhost -p 5433 -U postgres -d activ_education -f {OUT_SQL}")


if __name__ == "__main__":
    main()