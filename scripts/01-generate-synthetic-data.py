import numpy as np
import pandas as pd

RNG = np.random.default_rng(42)

RIASEC_DIMS = ["R", "I", "A", "S", "E", "C"]

NIVEAUX = [
    "COLLEGE",
    "LYCEE_2ND",
    "LYCEE_1ERE",
    "LYCEE_TLE",
    "BAC_1",
    "BAC_2",
    "BAC_3",
]

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
    x = np.array([0.0, 1.0, 2.0])
    y = np.array([notes_n2, notes_n1, notes_actuelle], dtype=float)
    pente = np.polyfit(x, y, 1)[0]
    return float(pente)


def generate(n_students=600):
    rows = []
    filiere_names = list(FILIERES.keys())

    for i in range(n_students):
        niveau = RNG.choice(NIVEAUX, p=[0.05, 0.08, 0.12, 0.20, 0.25, 0.18, 0.12])

        if niveau in ("COLLEGE", "LYCEE_2ND"):
            serie = "NA"
        else:
            serie = RNG.choice(["A", "B", "C", "D", "E", "G"],
                               p=[0.15, 0.05, 0.30, 0.25, 0.10, 0.15])

        riasec = RNG.uniform(0, 1, size=6)
        riasec = riasec / riasec.max()

        base = RNG.normal(12.5, 2.0)
        notes_n2 = float(np.clip(RNG.normal(base, 1.2), 6, 19))
        notes_n1 = float(np.clip(RNG.normal(base + RNG.normal(0, 0.5), 1.2), 6, 19))
        notes_actuelle = float(np.clip(RNG.normal(base + RNG.normal(0, 0.5), 1.2), 6, 19))

        annee_partielle = RNG.random() < 0.30
        if annee_partielle:
            notes_actuelle = float(np.clip(notes_actuelle + RNG.normal(0, 1.5), 6, 19))

        tendance = tendance_lineaire(notes_n2, notes_n1, notes_actuelle)
        moyenne_generale = (notes_n2 + notes_n1 + notes_actuelle) / 3.0

        filieres_eligibles = [f for f, info in FILIERES.items()
                              if niveau in info["niveaux_ok"]]

        scores_60_40 = {}
        for nom in filieres_eligibles:
            info = FILIERES[nom]
            aspiration = cosine_sim(riasec, info["profil"])
            realite = np.clip((moyenne_generale - info["seuil"] + 5) / 10, 0, 1)
            scores_60_40[nom] = 0.6 * aspiration + 0.4 * realite

        if not scores_60_40:
            filiere_choisie = RNG.choice(filiere_names)
        else:
            top_candidates = sorted(scores_60_40, key=scores_60_40.get, reverse=True)[:3]

            nb_consultations = {}
            en_favori = {}
            score_similarite = {}
            for nom in filiere_names:
                if nom in top_candidates:
                    nb_consultations[nom] = int(RNG.integers(2, 12))
                else:
                    nb_consultations[nom] = int(RNG.integers(0, 3))
                en_favori[nom] = bool(RNG.random() < 0.25 and nom in top_candidates)
                if nom in top_candidates:
                    score_similarite[nom] = float(np.clip(RNG.normal(0.75, 0.10), 0, 1))
                else:
                    score_similarite[nom] = float(np.clip(RNG.normal(0.30, 0.15), 0, 1))

            comportemental_weight = 0.15
            decision_scores = {}
            for nom in scores_60_40:
                bonus = (
                    comportemental_weight * (
                        np.log1p(nb_consultations[nom]) / np.log1p(12)
                        + 0.3 * float(en_favori[nom])
                        + 0.5 * score_similarite[nom]
                    ) / 1.8
                )
                decision_scores[nom] = scores_60_40[nom] + bonus
            filiere_choisie = max(decision_scores, key=decision_scores.get)

        info = FILIERES[filiere_choisie]

        match_riasec = cosine_sim(riasec, info["profil"])
        ecart_notes = moyenne_generale - info["seuil"]
        bonus_trajectoire = 0.3 * tendance

        logit = 2.5 * match_riasec + 0.35 * ecart_notes + 0.5 * bonus_trajectoire - 1.5
        proba_reussite = 1 / (1 + np.exp(-logit))
        bruit = RNG.normal(0, 0.15)
        reussite = 1 if (proba_reussite + bruit) > 0.5 else 0

        satisfaction = int(np.clip(RNG.normal(3 + 1.5 * reussite, 0.8), 1, 5))

        # ----- Signal conversationnel simulé --------------------------------
        # Domaine déclaré match : l'élève parle-t-il à ORIA d'un domaine qui
        # correspond à sa filière choisie ?
        domain_labels = {
            "Informatique": "informatique", "Medecine": "santé", "Droit": "droit",
            "Genie_Civil": "génie civil", "Communication": "communication",
            "Gestion_Commerce": "gestion", "Agronomie": "agriculture", "Lettres": "lettres",
        }
        # On simule que l'élève parle du bon domaine ~50% du temps
        domaine_declare = RNG.choice(
            [domain_labels.get(filiere_choisie, "autre"),
             RNG.choice(list(domain_labels.values()))],
            p=[0.5, 0.5]
        )
        domaine_declare_match = 1 if domaine_declare == domain_labels.get(filiere_choisie, "") else 0
        # Constance : l'élève mentionne-t-il toujours le même domaine ?
        constance_ambition = float(np.clip(RNG.normal(0.7 if domaine_declare_match else 0.3, 0.1), 0, 1))
        # Nombre d'échanges ORIA sur ce domaine
        nb_echanges_domaine = int(RNG.integers(0, 30))
        if not domaine_declare_match:
            nb_echanges_domaine = int(RNG.integers(0, 5))

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
            "domaine_declare_match_filiere": domaine_declare_match,
            "constance_ambition": round(constance_ambition, 3),
            "nb_echanges_domaine": nb_echanges_domaine,
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
    print("Nouvelles colonnes conversationnelles :")
    for c in ["domaine_declare_match_filiere", "constance_ambition", "nb_echanges_domaine"]:
        print(f"  {c}: {df[c].describe().to_string()}")
    print()
    print("Aperçu :")
    print(df.head().to_string())
