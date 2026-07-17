"""
Entraîne un modèle de classification pour prédire la réussite (ADMIS vs REORIENTE)
à partir du profil élève + filière choisie.

Compare deux configurations de variables :
  - "sans_comportemental" : le modèle de base (RIASEC + notes + score_60_40)
  - "avec_comportemental" : on ajoute le signal issu de l'usage (consultations,
    favoris, similarité RAG)

But : mesurer objectivement si l'intérêt comportemental apporte un gain réel
ou seulement du bruit — c'est un point clé du module Prédiction pour éviter
l'effet "bulle de filtre" (un élève qui consulte Médecine finit recommandé
vers Médecine même si ses notes sont insuffisantes).

Le CSV d'entrée doit être généré par generate_synthetic_data.py (même schéma).
"""

import json
from pathlib import Path

import joblib
import numpy as np
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import GradientBoostingClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report, roc_auc_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder

# ---------- Schéma attendu ----------------------------------------------
RIASEC_COLS = ["riasec_R", "riasec_I", "riasec_A", "riasec_S", "riasec_E", "riasec_C"]
NOTE_COLS = ["notes_n2", "notes_n1", "notes_actuelle", "tendance_notes"]
COMPORTEMENTAL_COLS = [
    "nb_consultations",
    "en_favori",
    "score_similarite_recherche",
]
CAT_COLS = ["serie", "filiere_choisie", "niveau_actuel"]

# Configurations à comparer
CONFIGS = {
    "sans_comportemental": {
        "num": RIASEC_COLS + NOTE_COLS + ["moyenne_generale",
                                          "score_60_40",
                                          "match_riasec",
                                          "ecart_notes_seuil"],
        "cat": CAT_COLS,
    },
    "avec_comportemental": {
        "num": RIASEC_COLS + NOTE_COLS + ["moyenne_generale",
                                          "score_60_40",
                                          "match_riasec",
                                          "ecart_notes_seuil"] + COMPORTEMENTAL_COLS,
        "cat": CAT_COLS,
    },
}

MODELS = {
    "logistic_regression": LogisticRegression(max_iter=2000, class_weight="balanced"),
    "gradient_boosting": GradientBoostingClassifier(random_state=42),
}


def load_data(path="orientation_outcome_synthetic.csv"):
    df = pd.read_csv(path)
    # Cible binaire : ADMIS = 1, REORIENTE = 0
    df["target"] = (df["statut"] == "ADMIS").astype(int)
    # Cast des booléens (lus comme bool depuis generate, sklearn veut numérique)
    if "en_favori" in df.columns:
        df["en_favori"] = df["en_favori"].astype(int)
    if "annee_partielle" in df.columns:
        df["annee_partielle"] = df["annee_partielle"].astype(int)
    return df


def build_pipeline(model, num_cols, cat_cols):
    preprocess = ColumnTransformer([
        ("cat", OneHotEncoder(handle_unknown="ignore"), cat_cols),
    ], remainder="passthrough")
    return Pipeline([("prep", preprocess), ("model", model)])


def evaluate(pipe, X_train, X_test, y_train, y_test, label):
    pipe.fit(X_train, y_train)
    preds = pipe.predict(X_test)
    probas = pipe.predict_proba(X_test)[:, 1]
    report = classification_report(
        y_test, preds, target_names=["REORIENTE", "ADMIS"], output_dict=True
    )
    auc = roc_auc_score(y_test, probas)
    print(f"\n=== {label} ===")
    print(classification_report(y_test, preds, target_names=["REORIENTE", "ADMIS"]))
    print(f"ROC-AUC: {auc:.3f}")
    return {"label": label, "roc_auc": auc, "report": report,
            "f1_admis": report["ADMIS"]["f1-score"],
            "recall_reoriente": report["REORIENTE"]["recall"]}


def main():
    df = load_data()
    print(f"Dataset : {len(df)} élèves, {df['target'].mean():.1%} ADMIS")
    print(f"Distribution 'niveau_actuel' :\n{df['niveau_actuel'].value_counts()}\n")

    # Split stratifié pour respecter l'équilibre de la cible
    train_df, test_df = train_test_split(
        df, test_size=0.25, random_state=42, stratify=df["target"]
    )
    y_train = train_df["target"]
    y_test = test_df["target"]

    results = []

    # 1) Comparaison avec/sans comportemental sur les deux modèles
    for cfg_name, cfg in CONFIGS.items():
        X_train = train_df[cfg["num"] + cfg["cat"]]
        X_test = test_df[cfg["num"] + cfg["cat"]]

        for model_name, model in MODELS.items():
            pipe = build_pipeline(model, cfg["num"], cfg["cat"])
            label = f"{model_name} | {cfg_name}"
            r = evaluate(pipe, X_train, X_test, y_train, y_test, label)
            r["config"] = cfg_name
            r["model"] = model_name
            results.append(r)

    # 2) Top variables (sur le gradient boosting de chaque config)
    print("\n=== Variables les plus prédictives (gradient boosting) ===")
    importance_tables = {}
    for cfg_name, cfg in CONFIGS.items():
        X = df[cfg["num"] + cfg["cat"]]
        y = df["target"]
        pipe = build_pipeline(GradientBoostingClassifier(random_state=42),
                              cfg["num"], cfg["cat"])
        pipe.fit(X, y)
        names = pipe.named_steps["prep"].get_feature_names_out()
        imps = pipe.named_steps["model"].feature_importances_
        top = sorted(zip(names, imps), key=lambda x: -x[1])[:8]
        importance_tables[cfg_name] = [(str(n), float(i)) for n, i in top]
        print(f"\n  -- {cfg_name} --")
        for n, i in top:
            print(f"    {n}: {i:.3f}")

    # 3) Synthèse : gain du signal comportemental
    print("\n=== Gain du signal comportemental (delta ROC-AUC) ===")
    summary = {}
    for model_name in MODELS:
        auc_sans = next(r["roc_auc"] for r in results
                        if r["model"] == model_name and r["config"] == "sans_comportemental")
        auc_avec = next(r["roc_auc"] for r in results
                        if r["model"] == model_name and r["config"] == "avec_comportemental")
        delta = auc_avec - auc_sans
        summary[model_name] = {"sans": auc_sans, "avec": auc_avec, "delta": delta}
        print(f"  {model_name:>22s} : {auc_sans:.3f} -> {auc_avec:.3f}  (Δ = {delta:+.3f})")

    # 4) Test "bulle de filtre" : à profil académique faible, est-ce que
    # le comportemental tire la prédiction vers une filière consultée ?
    print("\n=== Test 'bulle de filtre' (notes faibles + signal comportemental fort) ===")
    bubble_test(df)

    # 5) Sauvegarde des modèles
    Path("models").mkdir(exist_ok=True)
    for cfg_name, cfg in CONFIGS.items():
        X = df[cfg["num"] + cfg["cat"]]
        y = df["target"]
        pipe = build_pipeline(GradientBoostingClassifier(random_state=42),
                              cfg["num"], cfg["cat"])
        pipe.fit(X, y)
        joblib.dump(pipe, f"models/gb_{cfg_name}.joblib")

    # 6) Écriture d'un rapport JSON pour RESULTATS_PROTOTYPE.md
    out = {
        "n_students": int(len(df)),
        "admis_rate": float(df["target"].mean()),
        "results": [{
            "model": r["model"],
            "config": r["config"],
            "roc_auc": float(r["roc_auc"]),
            "f1_admis": float(r["f1_admis"]),
            "recall_reoriente": float(r["recall_reoriente"]),
        } for r in results],
        "summary_delta_auc": summary,
        "importance_top8": importance_tables,
    }
    Path("results_prototype.json").write_text(json.dumps(out, indent=2, ensure_ascii=False))
    print("\nRésultats sauvegardés dans results_prototype.json")


def bubble_test(df):
    """Vérifie si l'ajout du signal comportemental fait passer des élèves
    académique-faibles du statut REORIENTE prédit vers ADMIS — l'effet
    'bulle de filtre' qu'on veut éviter."""
    # Modèles entraînés sur l'ensemble, on regarde les probabilités
    for cfg_name in ["sans_comportemental", "avec_comportemental"]:
        cfg = CONFIGS[cfg_name]
        X = df[cfg["num"] + cfg["cat"]]
        y = df["target"]
        pipe = build_pipeline(GradientBoostingClassifier(random_state=42),
                              cfg["num"], cfg["cat"])
        pipe.fit(X, y)
        proba_admis = pipe.predict_proba(X)[:, 1]

        # Sous-population : élèves dont la moyenne est SOUS le seuil de la
        # filière choisie (profil académique objectivement insuffisant)
        faible = df["ecart_notes_seuil"] < 0
        n = int(faible.sum())
        if n == 0:
            continue
        # Taux de "ADMIS prédit" dans cette sous-population
        taux_admis_faible = float((proba_admis[faible] > 0.5).mean())
        # Pour comparaison : taux de "vrai ADMIS" dans la même sous-pop
        taux_reel_faible = float(y[faible].mean())
        print(f"  {cfg_name:>25s} | n={n:>3d} | ADMIS prédit={taux_admis_faible:6.1%}  "
              f"(réel={taux_reel_faible:6.1%})")


if __name__ == "__main__":
    main()
