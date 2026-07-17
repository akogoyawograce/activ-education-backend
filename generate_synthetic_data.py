"""
Génère un jeu de données synthétique simulant orientation_outcome
pour prototyper le module Prédiction avant d'avoir des données réelles.

Hypothèses simulées (le "vrai" mécanisme que le modèle devra retrouver) :
  - Un élève réussit mieux dans une filière si son profil RIASEC correspond
    au profil attendu (matching cosinus).
  - Une moyenne académique au-dessus du seuil d'admission augmente fortement
    la probabilité de réussite.
  - La trajectoire sur 2 ans (notes_n2 -> notes_n1 -> notes_actuelle) est
    un signal : une progression compense en partie un écart de niveau initial.
  - L'intérêt comportemental (consultations, favoris, requêtes RAG) biaise
    subtilement le choix de filière mais ne devrait pas dicter le résultat.
  - Un peu de bruit aléatoire (la vraie vie n'est jamais parfaitement prédictible).

Schéma produit (par élève) :
  eleve_id, niveau_actuel,
  notes_n2, notes_n1, notes_actuelle, annee_partielle, tendance_notes,
  serie, riasec_R..C, moyenne_generale,
  filiere_choisie, score_60_40, match_riasec, ecart_notes_seuil,
  nb_consultations, en_favori, score_similarite_recherche,
  statut, satisfaction
"""

import numpy as np
import pandas as pd

RNG = np.random.default_rng(42)

RIASEC_DIMS = ["R", "I", "A", "S", "E", "C"]

# Niveaux scolaires (cf. brief Phase 0)
NIVEAUX = [
    "COLLEGE",     # 6e, 5e, 4e, 3e
    "LYCEE_2ND",   # 2nde
    "LYCEE_1ERE",  # 1ère
    "LYCEE_TLE",   # Terminale
    "BAC_1",       # Bac+1 (L1 / 1ère année BTS/DUT)
    "BAC_2",       # Bac+2 (L2 / 2e année BTS/DUT)
    "BAC_3",       # Bac+3 (Licence)
]

# Filières simulées : profil RIASEC attendu + seuil d'admission.
# Les séries associées servent à simuler le filtre de cohérence bac -> filière.
FILIERES = {
    "Informatique":     {"profil": [0.7, 0.8, 0.2, 0.1, 0.3, 0.4], "seuil": 12,
                         "series_ok": ["C", "D", "E"], "niveaux_ok": ["BAC_1", "BAC_2", "BAC_3"]},
    "Medecine":         {"profil": [0.3, 0.9, 0.1, 0.6, 0.2, 0.5], "seuil": 15,
                         "series_ok": ["C", "D"], "niveaux_ok": ["BAC_1", "BAC_2", "BAC_3"]},
    "Droit":            {"profil": [0.1, 0.5, 0.2, 0.5, 0.6, 0.7], "seuil": 12,
                         "series_ok": ["A", "B", "G"], "niveaux_ok": ["BAC_1", "BAC_2", "BAC_3"]},
    "Genie_Civil":      {"profil": [0.8, 0.6, 0.1, 0.1, 0.4, 0.6], "seuil": 13,
                         "series_ok": ["C", "D", "E"], "niveaux_ok": ["BAC_1", "BAC_2", "BAC_3"]},
    "Communication":    {"profil": [0.1, 0.3, 0.8, 0.6, 0.5, 0.2], "seuil": 10,
                         "series_ok": ["A", "B", "G"], "niveaux_ok": ["LYCEE_TLE", "BAC_1", "BAC_2", "BAC_3"]},
    "Gestion_Commerce": {"profil": [0.1, 0.2, 0.2, 0.4, 0.9, 0.6], "seuil": 10,
                         "series_ok": ["A", "B", "C", "D", "G"],
                         "niveaux_ok": ["LYCEE_TLE", "BAC_1", "BAC_2", "BAC_3"]},
    "Agronomie":        {"profil": [0.7, 0.5, 0.1, 0.3, 0.3, 0.3], "seuil": 11,
                         "series_ok": ["D", "E"], "niveaux_ok": ["BAC_1", "BAC_2", "BAC_3"]},
    "Lettres":          {"profil": [0.1, 0.6, 0.9, 0.5, 0.2, 0.3], "seuil": 10,
                         "series_ok": ["A", "B"],
                         "niveaux_ok": ["LYCEE_1ERE", "LYCEE_TLE", "BAC_1", "BAC_2", "BAC_3"]},
}


def cosine_sim(a, b):
    a, b = np.array(a), np.array(b)
    return float(np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b) + 1e-9))


def tendance_lineaire(notes_n2, notes_n1, notes_actuelle):
    """Pente d'une régression linéaire simple sur 3 points (années 0, 1, 2).
    Une pente > 0 = progression, < 0 = régression."""
    x = np.array([0.0, 1.0, 2.0])
    y = np.array([notes_n2, notes_n1, notes_actuelle], dtype=float)
    # Moindres carrés : pente = covariance(x,y) / variance(x)
    pente = np.polyfit(x, y, 1)[0]
    return float(pente)


def generate(n_students=600):
    rows = []
    filiere_names = list(FILIERES.keys())

    for i in range(n_students):
        # ----- Profil de l'élève ------------------------------------------
        # 1. Niveau actuel (uniformément réparti, pondéré vers le sup)
        niveau = RNG.choice(NIVEAUX, p=[0.05, 0.08, 0.12, 0.20, 0.25, 0.18, 0.12])

        # 2. Série (lycée/bac) : cohérente avec le niveau
        if niveau in ("COLLEGE", "LYCEE_2ND"):
            serie = "NA"  # Pas de série au collège/2nde
        else:
            serie = RNG.choice(["A", "B", "C", "D", "E", "G"],
                               p=[0.15, 0.05, 0.30, 0.25, 0.10, 0.15])

        # 3. Profil RIASEC (0 à 1 par dimension, normalisé au max)
        riasec = RNG.uniform(0, 1, size=6)
        riasec = riasec / riasec.max()

        # 4. Notes : 3 années successives (n-2, n-1, année en cours)
        # Centrées autour de 12.5 avec une marche aléatoire et du bruit
        base = RNG.normal(12.5, 2.0)
        notes_n2 = float(np.clip(RNG.normal(base, 1.2), 6, 19))
        notes_n1 = float(np.clip(RNG.normal(base + RNG.normal(0, 0.5), 1.2), 6, 19))
        notes_actuelle = float(np.clip(RNG.normal(base + RNG.normal(0, 0.5), 1.2), 6, 19))

        # Année en cours potentiellement partielle (seulement 1er trimestre)
        # ~30% des cas : moins de données, donc la note est plus "bruyante"
        annee_partielle = RNG.random() < 0.30
        if annee_partielle:
            notes_actuelle = float(np.clip(notes_actuelle + RNG.normal(0, 1.5), 6, 19))

        tendance = tendance_lineaire(notes_n2, notes_n1, notes_actuelle)
        moyenne_generale = (notes_n2 + notes_n1 + notes_actuelle) / 3.0

        # ----- Filière choisie (filtrée par niveau) -----------------------
        # On imite le comportement utilisateur : il regarde les filières
        # compatibles avec son niveau, score 60/40, mais on injecte un biais
        # comportemental (cf. plus bas).
        filieres_eligibles = [f for f, info in FILIERES.items()
                              if niveau in info["niveaux_ok"]]

        scores_60_40 = {}
        for nom in filieres_eligibles:
            info = FILIERES[nom]
            aspiration = cosine_sim(riasec, info["profil"])  # 0 à 1
            realite = np.clip((moyenne_generale - info["seuil"] + 5) / 10, 0, 1)
            scores_60_40[nom] = 0.6 * aspiration + 0.4 * realite

        if not scores_60_40:
            # Cas dégénéré (ne devrait pas arriver avec les pondérations)
            filiere_choisie = RNG.choice(filiere_names)
        else:
            # Biais comportemental : l'élève regarde 1-3 filières, met en
            # favori une ou deux d'entre elles, et finit par choisir
            # "celle qu'il a le plus consultée" plutôt que strictement
            # le top score 60/40.
            top_candidates = sorted(scores_60_40, key=scores_60_40.get, reverse=True)[:3]

            # Tire un nombre de consultations par filière
            nb_consultations = {}
            en_favori = {}
            score_similarite = {}
            for nom in filiere_names:
                if nom in top_candidates:
                    nb_consultations[nom] = int(RNG.integers(2, 12))
                else:
                    nb_consultations[nom] = int(RNG.integers(0, 3))
                en_favori[nom] = bool(RNG.random() < 0.25 and nom in top_candidates)
                # Score de similarité de la requête RAG (0 à 1) : élevé pour
                # les top candidates, plus diffus ailleurs
                if nom in top_candidates:
                    score_similarite[nom] = float(np.clip(RNG.normal(0.75, 0.10), 0, 1))
                else:
                    score_similarite[nom] = float(np.clip(RNG.normal(0.30, 0.15), 0, 1))

            # Décision finale : score 60/40 + bonus comportemental modéré
            # (l'idée est que le signal comportemental *bruit* le choix
            # sans pour autant le dicter — pour mesurer ce risque ensuite)
            comportemental_weight = 0.15  # à comparer dans l'analyse
            decision_scores = {}
            for nom in scores_60_40:
                bonus = (
                    comportemental_weight * (
                        np.log1p(nb_consultations[nom]) / np.log1p(12)
                        + 0.3 * float(en_favori[nom])
                        + 0.5 * score_similarite[nom]
                    ) / 1.8  # normalisation grossière
                )
                decision_scores[nom] = scores_60_40[nom] + bonus
            filiere_choisie = max(decision_scores, key=decision_scores.get)

        info = FILIERES[filiere_choisie]

        # ----- Résultat "réel" (ce que le modèle devra retrouver) ---------
        # Mécanisme de vérité : match RIASEC + écart notes + bonus trajectoire
        match_riasec = cosine_sim(riasec, info["profil"])
        ecart_notes = moyenne_generale - info["seuil"]
        bonus_trajectoire = 0.3 * tendance  # une progression compense

        logit = 2.5 * match_riasec + 0.35 * ecart_notes + 0.5 * bonus_trajectoire - 1.5
        proba_reussite = 1 / (1 + np.exp(-logit))
        bruit = RNG.normal(0, 0.15)
        reussite = 1 if (proba_reussite + bruit) > 0.5 else 0

        satisfaction = int(np.clip(RNG.normal(3 + 1.5 * reussite, 0.8), 1, 5))

        # ----- Ligne CSV --------------------------------------------------
        row = {
            "eleve_id": i,
            "niveau_actuel": niveau,
            "notes_n2": round(notes_n2, 2),
            "notes_n1": round(notes_n1, 2),
            "notes_actuelle": round(notes_actuelle, 2),
            "annee_partielle": bool(annee_partielle),
            "tendance_notes": round(tendance, 3),
            "serie": serie,
            **{f"riasec_{d}": round(riasec[j], 3) for j, d in enumerate(RIASEC_DIMS)},
            "moyenne_generale": round(moyenne_generale, 2),
            "filiere_choisie": filiere_choisie,
            "score_60_40": round(scores_60_40.get(filiere_choisie, 0.0), 3),
            "match_riasec": round(match_riasec, 3),
            "ecart_notes_seuil": round(ecart_notes, 2),
            "nb_consultations": int(nb_consultations[filiere_choisie]),
            "en_favori": bool(en_favori[filiere_choisie]),
            "score_similarite_recherche": round(score_similarite[filiere_choisie], 3),
            "statut": "ADMIS" if reussite else "REORIENTE",
            "satisfaction": satisfaction,
        }
        rows.append(row)

    return pd.DataFrame(rows)


if __name__ == "__main__":
    df = generate(600)
    df.to_csv("orientation_outcome_synthetic.csv", index=False)
    print(f"{len(df)} profils générés -> orientation_outcome_synthetic.csv")
    print()
    print("Répartition 'statut' :")
    print(df["statut"].value_counts(normalize=True).round(3))
    print()
    print("Répartition 'niveau_actuel' :")
    print(df["niveau_actuel"].value_counts(normalize=True).round(3))
    print()
    print("Aperçu :")
    print(df.head().to_string())
