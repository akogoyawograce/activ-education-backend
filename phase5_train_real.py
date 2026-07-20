"""
Phase 5 — Entraînement supervisé sur données réelles.

Télécharge le dataset depuis le backend Spring Boot
(`GET /api/v1/admin/prediction/dataset`) puis lance l'entraînement.

Usage :
  1. Lancer le backend en local : ./mvnw spring-boot:run
  2. Récupérer un JWT admin (via /api/v1/auth/login)
  3. Exporter : python phase5_export_dataset.py --token <JWT> [--out real_dataset.csv]
  4. Adapter  : python phase5_train_real.py --csv real_dataset.csv
  5. Analyser : voir console + resultats_phase5.json

Pré-requis : ≥ 5 000 orientation_outcome avec statut ADMIS ou RECALE.
Avec moins, les modèles sur-apprennent — on garde la règle pondérée 50/35/15.

Schema backend (prediction dataset export) :
  row_id, niveau, serie, riasec_top3, riasec_score,
  note_actuelle, note_n1, note_n2, tendance_notes,
  score_aspiration, score_realite, score_engagement, score_recommandation, label

Schema attendu par train_model.py :
  riasec_R, riasec_I, riasec_A, riasec_S, riasec_E, riasec_C,
  notes_n2, notes_n1, notes_actuelle, tendance_notes,
  moyenne_generale, score_60_40, match_riasec, ecart_notes_seuil,
  nb_consultations, en_favori, score_similarite_recherche,
  serie, filiere_choisie, niveau_actuel, statut

Voir phase5_adapt_schema() pour la table de mapping.
"""

import argparse
import json
import sys
from pathlib import Path

import pandas as pd

# ---------- Mapping backend → train_model.py -----------------------------

RIASEC_DIMENSIONS = ["R", "I", "A", "S", "E", "C"]


def phase5_adapt_schema(df: pd.DataFrame) -> pd.DataFrame:
    """Adapte le schéma backend vers le schéma train_model.py.

    Stratégie :
      - Top 3 RIASEC (codes séparés par "|") → 6 colonnes one-hot-like (1 si
        la dimension fait partie du top 3, 0 sinon). Approximation grossière
        mais préserve l'intention (profil dominant). Avec le vrai dataset on
        pourra extraire les 6 dimensions depuis le JSONB snapshot.
      - notes_n2/n1/actuelle : rename direct.
      - moyenne_generale ≈ note_actuelle.
      - score_60_40 : pas dans l'export → recalculé depuis
        (score_aspiration, score_realite) avec poids 0.4 / 0.6
        (cf. Recommandation3SignauxService : 0.50*realite + 0.35*aspiration
        + 0.15*engagement ; approximation 60/40 → 0.6*realite + 0.4*asp).
      - match_riasec ≈ score_aspiration.
      - ecart_notes_seuil : pas directement dispo. On approxime par
        (note_actuelle - 10) / 10, en supposant le seuil d'admission à 10/20.
        Si on a le seuil d'admission par filière, on l'utilisera ici.
      - nb_consultations, en_favori, score_similarite_recherche :
        pas dans l'export Phase 3 (volontairement plafonnés par la 50/35/15).
        On met 0 — c'est la config "sans_comportemental" qui est comparable
        au prototype synthétique.
      - serie : direct.
      - filiere_choisie : pas dans l'export → 'INCONNU'.
      - niveau_actuel ← niveau.
      - statut : 'ADMIS' si label=1, sinon 'REORIENTE'.
    """
    out = pd.DataFrame()

    # 6 colonnes RIASEC : 1 si la dimension est dans le top 3
    for dim in RIASEC_DIMENSIONS:
        out[f"riasec_{dim}"] = df["riasec_top3"].fillna("").apply(
            lambda s: int(dim in str(s).split("|"))
        )

    # Notes
    out["notes_n2"] = df["note_n2"]
    out["notes_n1"] = df["note_n1"]
    out["notes_actuelle"] = df["note_actuelle"]
    out["tendance_notes"] = df["tendance_notes"]
    out["moyenne_generale"] = df["note_actuelle"]

    # Scores dérivés
    out["score_60_40"] = (
        0.6 * df["score_realite"].fillna(0)
        + 0.4 * df["score_aspiration"].fillna(0)
    )
    out["match_riasec"] = df["score_aspiration"]
    out["ecart_notes_seuil"] = (df["note_actuelle"].fillna(10) - 10) / 10

    # Comportemental (0 — voir note ci-dessus)
    out["nb_consultations"] = 0
    out["en_favori"] = 0
    out["score_similarite_recherche"] = 0.0

    # Catégorielles
    out["serie"] = df["serie"].fillna("INCONNU")
    out["filiere_choisie"] = "INCONNU"  # pas dans l'export
    out["niveau_actuel"] = df["niveau"].fillna("INCONNU")

    # Cible
    out["statut"] = df["label"].map({1: "ADMIS", 0: "REORIENTE"})

    return out


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--csv", required=True, help="CSV téléchargé depuis /admin/prediction/dataset")
    p.add_argument("--out", default="dataset_phase5_adapte.csv",
                   help="CSV adapté pour train_model.py")
    p.add_argument("--analyze-only", action="store_true",
                   help="Affiche les stats du dataset sans entraîner")
    args = p.parse_args()

    src = Path(args.csv)
    if not src.exists():
        print(f"Fichier introuvable : {src}", file=sys.stderr)
        sys.exit(1)

    print(f"Chargement {src}…")
    df = pd.read_csv(src)
    print(f"  Lignes : {len(df)}")
    print(f"  ADMIS : {(df['label'] == 1).sum()} ({(df['label'] == 1).mean():.1%})")
    print(f"  RECALE : {(df['label'] == 0).sum()} ({(df['label'] == 0).mean():.1%})")

    if len(df) < 5000 and not args.analyze_only:
        print(
            f"\n⚠️  ATTENTION : seulement {len(df)} lignes (< 5 000).\n"
            "   La Phase 5 requiert ≥ 5 000 orientation_outcome réels.\n"
            "   Les résultats seront peu fiables — utiliser --analyze-only\n"
            "   pour juste voir les stats, ou continuer quand même pour\n"
            "   valider le pipeline technique."
        )

    adapted = phase5_adapt_schema(df)
    out = Path(args.out)
    adapted.to_csv(out, index=False)
    print(f"\nDataset adapté écrit dans {out} ({len(adapted)} lignes)")

    if args.analyze_only:
        print("\nColonnes du dataset adapté :")
        for c in adapted.columns:
            print(f"  - {c}")
        print("\nStats cibles :")
        print(adapted["statut"].value_counts())
        return

    # Lancer l'entraînement via train_model.py
    print("\nLancement de train_model.py…")
    import subprocess
    result = subprocess.run(
        ["python", "train_model.py"],
        cwd=Path(__file__).parent,
        env={**__import__("os").environ, "PHASE5_DATASET": str(out)},
        check=False,
    )
    if result.returncode != 0:
        print(f"train_model.py a échoué (code {result.returncode})", file=sys.stderr)
        sys.exit(result.returncode)

    # Renommer la sortie pour différencier du prototype synthétique
    if Path("results_prototype.json").exists():
        target = Path("results_phase5.json")
        Path("results_prototype.json").rename(target)
        print(f"Résultats sauvegardés dans {target}")


if __name__ == "__main__":
    main()
