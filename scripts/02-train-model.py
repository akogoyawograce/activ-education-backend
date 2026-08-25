import json
import os
from pathlib import Path

import joblib
import numpy as np
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import GradientBoostingClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (
    classification_report,
    roc_auc_score,
    average_precision_score,
    brier_score_loss,
    roc_curve,
)
from sklearn.model_selection import (
    StratifiedKFold,
    cross_val_score,
    GridSearchCV,
    train_test_split,
)
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder
from sklearn.calibration import CalibratedClassifierCV

RIASEC_COLS = ["riasec_R", "riasec_I", "riasec_A", "riasec_S", "riasec_E", "riasec_C"]
NOTE_COLS = ["notes_n2", "notes_n1", "notes_actuelle", "tendance_notes"]
COMPORTEMENTAL_COLS = [
    "nb_consultations",
    "en_favori",
    "score_similarite_recherche",
]
CONVERSATIONNEL_COLS = [
    "domaine_declare_match_filiere",
    "constance_ambition",
    "nb_echanges_domaine",
]
CAT_COLS = ["serie", "filiere_choisie", "niveau_actuel"]
CONTEXT_CAT_COLS = ["region", "ordre", "sexe"]
CONTEXT_NUM_COLS = ["annee_session"]

CONFIGS = {
    "sans_comportemental": {
        "num": RIASEC_COLS + NOTE_COLS + [
            "moyenne_generale", "score_60_40", "match_riasec", "ecart_notes_seuil"
        ] + CONTEXT_NUM_COLS,
        "cat": CAT_COLS + CONTEXT_CAT_COLS,
    },
    "avec_comportemental": {
        "num": RIASEC_COLS + NOTE_COLS + [
            "moyenne_generale", "score_60_40", "match_riasec", "ecart_notes_seuil"
        ] + CONTEXT_NUM_COLS + COMPORTEMENTAL_COLS,
        "cat": CAT_COLS + CONTEXT_CAT_COLS,
    },
    "avec_conversationnel": {
        "num": RIASEC_COLS + NOTE_COLS + [
            "moyenne_generale", "score_60_40", "match_riasec", "ecart_notes_seuil"
        ] + CONTEXT_NUM_COLS + CONVERSATIONNEL_COLS,
        "cat": CAT_COLS + CONTEXT_CAT_COLS,
    },
}

GB_DEFAULT = GradientBoostingClassifier(random_state=42)

GB_GRID = {
    "n_estimators": [50, 100, 200],
    "max_depth": [2, 3, 4],
    "learning_rate": [0.05, 0.1, 0.2],
    "min_samples_leaf": [5, 10, 20],
}

MODELS = {
    "logistic_regression": LogisticRegression(max_iter=2000, class_weight="balanced"),
    "gradient_boosting": GB_DEFAULT,
}


def load_data(path: str | None = None):
    if path is None:
        path = os.environ.get("PHASE5_DATASET", "orientation_outcome_synthetic.csv")
    df = pd.read_csv(path)
    df["target"] = (df["statut"] == "ADMIS").astype(int)
    for col in ["en_favori", "annee_partielle"]:
        if col in df.columns:
            df[col] = df[col].astype(int)
    if "domaine_declare_match_filiere" in df.columns:
        df["domaine_declare_match_filiere"] = df["domaine_declare_match_filiere"].astype(int)
    # Colonnes contexte (Phase 5.1) : absentes des anciens CSV → valeurs neutres
    for col, default in [("region", "INCONNU"), ("ordre", "INCONNU"), ("sexe", "INCONNU"), ("annee_session", 0)]:
        if col not in df.columns:
            df[col] = default
    return df


def build_pipeline(model, num_cols, cat_cols):
    preprocess = ColumnTransformer([
        ("cat", OneHotEncoder(handle_unknown="ignore"), cat_cols),
    ], remainder="passthrough")
    return Pipeline([("prep", preprocess), ("model", model)])


def evaluate_cv(X, y, model, num_cols, cat_cols, label):
    skf = StratifiedKFold(n_splits=5, shuffle=True, random_state=42)
    pipe = build_pipeline(model, num_cols, cat_cols)
    roc_scores = cross_val_score(pipe, X, y, cv=skf, scoring="roc_auc")
    pr_scores = cross_val_score(pipe, X, y, cv=skf, scoring="average_precision")
    pipe.fit(X, y)
    probas = pipe.predict_proba(X)[:, 1]
    brier = brier_score_loss(y, probas)
    preds = pipe.predict(X)
    report = classification_report(y, preds, target_names=["REORIENTE", "ADMIS"], output_dict=True)
    print(f"\n=== {label} ===")
    print(f"  ROC-AUC : {roc_scores.mean():.3f} (+/- {roc_scores.std():.3f})")
    print(f"  PR-AUC  : {pr_scores.mean():.3f} (+/- {pr_scores.std():.3f})")
    print(f"  Brier   : {brier:.4f} (0=parfait, 0.25=naïf)")
    print(f"  F1 ADMIS: {report['ADMIS']['f1-score']:.3f}")
    return {
        "label": label,
        "roc_auc_mean": float(roc_scores.mean()),
        "roc_auc_std": float(roc_scores.std()),
        "pr_auc_mean": float(pr_scores.mean()),
        "pr_auc_std": float(pr_scores.std()),
        "brier": float(brier),
        "f1_admis": report["ADMIS"]["f1-score"],
        "recall_reoriente": report["REORIENTE"]["recall"],
        "report": report,
    }


def grid_search_gb(X, y, num_cols, cat_cols, label):
    pipe = build_pipeline(GB_DEFAULT, num_cols, cat_cols)
    param_grid = {f"model__{k}": v for k, v in GB_GRID.items()}
    gs = GridSearchCV(
        pipe, param_grid, cv=StratifiedKFold(5, shuffle=True, random_state=42),
        scoring="roc_auc", n_jobs=-1, verbose=0,
    )
    gs.fit(X, y)
    print(f"\n=== GridSearch {label} ===")
    print(f"  Best params: {gs.best_params_}")
    print(f"  Best CV ROC-AUC: {gs.best_score_:.3f}")
    return gs.best_estimator_, gs.best_params_


def feature_importance(pipe, num_cols, cat_cols, top_n=10):
    names = pipe.named_steps["prep"].get_feature_names_out()
    imps = pipe.named_steps["model"].feature_importances_
    top = sorted(zip(names, imps), key=lambda x: -x[1])[:top_n]
    print(f"\n  Top {top_n} variables :")
    for n, i in top:
        print(f"    {n}: {i:.3f}")
    return [(str(n), float(i)) for n, i in top]


def bubble_test(df, config_name, signal_cols, signal_label):
    print(f"\n=== Test bulle de filtre : {signal_label} ===")
    cfg = CONFIGS[config_name]
    X = df[cfg["num"] + cfg["cat"]]
    y = df["target"]
    pipe = build_pipeline(GB_DEFAULT, cfg["num"], cfg["cat"])
    pipe.fit(X, y)
    probas = pipe.predict_proba(X)[:, 1]
    faible = df["ecart_notes_seuil"] < 0
    n = int(faible.sum())
    if n == 0:
        print("  Aucun élève avec ecart_notes_seuil < 0")
        return
    taux_pred = float((probas[faible] > 0.5).mean())
    taux_reel = float(y[faible].mean())
    print(f"  Élèves à profil académique faible (n={n})")
    print(f"    ADMIS prédit: {taux_pred:.1%}")
    print(f"    ADMIS réel:   {taux_reel:.1%}")
    print(f"    Dérive:       {taux_pred - taux_reel:+.1%}")
    if signal_cols and all(c in df.columns for c in signal_cols):
        if "nb_echanges_domaine" in signal_cols:
            fort = df["nb_echanges_domaine"] > df["nb_echanges_domaine"].median()
            faible_et_fort = faible & fort
            n2 = int(faible_et_fort.sum())
            if n2 > 0:
                taux_pred2 = float((probas[faible_et_fort] > 0.5).mean())
                taux_reel2 = float(y[faible_et_fort].mean())
                print(f"    Sous-groupe avec signal fort (n={n2}):")
                print(f"      ADMIS prédit: {taux_pred2:.1%}")
                print(f"      ADMIS réel:   {taux_reel2:.1%}")
                print(f"      Dérive:       {taux_pred2 - taux_reel2:+.1%}")
    return {
        "config": config_name,
        "n_faible": int(n),
        "taux_pred_faible": float(taux_pred),
        "taux_reel_faible": float(taux_reel),
        "derive": float(taux_pred - taux_reel),
    }


def main():
    df = load_data()
    print(f"Dataset : {len(df)} élèves, {df['target'].mean():.1%} ADMIS")
    print(f"Distribution 'niveau_actuel' :\n{df['niveau_actuel'].value_counts()}\n")

    all_results = []
    importance_tables = {}

    # 1) Évaluation sur modèle par défaut avec validation croisée
    print("=" * 60)
    print("ÉVALUATION AVEC VALIDATION CROISÉE STRATIFIÉE (5 folds)")
    print("=" * 60)
    for cfg_name, cfg in CONFIGS.items():
        X = df[cfg["num"] + cfg["cat"]]
        y = df["target"]
        for model_name, model in MODELS.items():
            label = f"{model_name} | {cfg_name}"
            r = evaluate_cv(X, y, model, cfg["num"], cfg["cat"], label)
            r["config"] = cfg_name
            r["model"] = model_name
            all_results.append(r)

    # 2) GridSearch sur GradientBoosting (chaque config)
    print("\n" + "=" * 60)
    print("GRIDSEARCH GRADIENT BOOSTING")
    print("=" * 60)
    best_models = {}
    for cfg_name, cfg in CONFIGS.items():
        X = df[cfg["num"] + cfg["cat"]]
        y = df["target"]
        best_pipe, best_params = grid_search_gb(X, y, cfg["num"], cfg["cat"], cfg_name)
        best_models[cfg_name] = best_pipe
        print(f"\nFeature importance ({cfg_name}) — best model from GridSearch:")
        top = feature_importance(best_pipe, cfg["num"], cfg["cat"])
        importance_tables[cfg_name] = top

    # 3) Synthèse ROC-AUC
    print("\n" + "=" * 60)
    print("SYNTHÈSE ROC-AUC (moyenne ± écart-type sur 5 folds)")
    print("=" * 60)
    summary = {}
    for model_name in MODELS:
        row = {}
        for cfg_name in CONFIGS:
            r = next(x for x in all_results
                     if x["model"] == model_name and x["config"] == cfg_name)
            row[cfg_name] = r
            print(f"  {model_name:>22s} | {cfg_name:>22s} : "
                  f"{r['roc_auc_mean']:.3f} ± {r['roc_auc_std']:.3f}")
        # Deltas
        if "sans_comportemental" in row and "avec_comportemental" in row:
            d = row["avec_comportemental"]["roc_auc_mean"] - row["sans_comportemental"]["roc_auc_mean"]
            print(f"  {'':>22s} | {'delta comportemental':>22s} : {d:+.3f}")
        if "sans_comportemental" in row and "avec_conversationnel" in row:
            d = row["avec_conversationnel"]["roc_auc_mean"] - row["sans_comportemental"]["roc_auc_mean"]
            print(f"  {'':>22s} | {'delta conversationnel':>22s} : {d:+.3f}")
        summary[model_name] = {
            k: {"roc_auc_mean": v["roc_auc_mean"], "roc_auc_std": v["roc_auc_std"],
                "pr_auc_mean": v["pr_auc_mean"], "brier": v["brier"]}
            for k, v in row.items()
        }

    # 4) Test bulle de filtre (comportemental + conversationnel)
    print("\n" + "=" * 60)
    print("TESTS ANTI-BULLE DE FILTRE")
    print("=" * 60)
    bubble_results = {}
    bubble_results["comportemental"] = bubble_test(
        df, "avec_comportemental", COMPORTEMENTAL_COLS, "Signal comportemental"
    )
    if all(c in df.columns for c in CONVERSATIONNEL_COLS):
        bubble_results["conversationnel"] = bubble_test(
            df, "avec_conversationnel", CONVERSATIONNEL_COLS, "Signal conversationnel"
        )

    # 5) Sauvegarde
    Path("models").mkdir(exist_ok=True)
    for cfg_name, pipe in best_models.items():
        joblib.dump(pipe, f"models/gb_{cfg_name}.joblib")

    out = {
        "n_students": int(len(df)),
        "admis_rate": float(df["target"].mean()),
        "cv_results": [{
            "model": r["model"],
            "config": r["config"],
            "roc_auc_mean": r["roc_auc_mean"],
            "roc_auc_std": r["roc_auc_std"],
            "pr_auc_mean": r["pr_auc_mean"],
            "pr_auc_std": r["pr_auc_std"],
            "brier": r["brier"],
            "f1_admis": r["f1_admis"],
            "recall_reoriente": r["recall_reoriente"],
        } for r in all_results],
        "summary_delta_auc": summary,
        "importance_top10": importance_tables,
        "bubble_test": bubble_results,
    }
    Path("results_prototype.json").write_text(json.dumps(out, indent=2, ensure_ascii=False))
    print("\nRésultats sauvegardés dans results_prototype.json")


if __name__ == "__main__":
    main()
