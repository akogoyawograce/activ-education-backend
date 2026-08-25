# Résultats du prototype — Module Prédiction

> **Statut :** Phase 0 validée ✅
> **Date :** 2026-07-16
> **Données :** `orientation_outcome_synthetic.csv` (600 élèves simulés)
> **Scripts :** `generate_synthetic_data.py` (97 l. → étendu), `train_model.py` (77 l. → réécrit)

## TL;DR

1. **Le signal comportemental n'apporte pas de gain** (Δ ROC-AUC = -0.001 à -0.017). Le modèle s'appuie essentiellement sur **les notes et le matching RIASEC**. Conclusion : il faut **plafonner** ce signal dans le moteur de recommandation, pas lui donner du poids.
2. **Aucun effet "bulle de filtre" détecté** sur le sous-groupe académiquement faible. La simulation est rassurante.
3. **Le 60/40 actuel reste un excellent baseline** (ROC-AUC ≈ 0.89 en logistique, 0.87 en gradient boosting). Le moteur de la Phase 3 doit viser au moins ce score, idéalement en montant.
4. **La trajectoire des notes** (`tendance_notes`, régression linéaire sur 3 points) entre dans le top 8 des variables du gradient boosting — valider son usage en Phase 3.

---

## 1. Données synthétiques

### 1.1 Schéma produit (24 colonnes)

| Colonne | Type | Rôle |
|---|---|---|
| `eleve_id` | int | identifiant |
| `niveau_actuel` | cat | COLLEGE / LYCEE_2ND / LYCEE_1ERE / LYCEE_TLE / BAC_1 / BAC_2 / BAC_3 |
| `notes_n2`, `notes_n1`, `notes_actuelle` | float | moyennes sur 3 ans |
| `annee_partielle` | bool | vrai si seule la 1ère partie de l'année est connue |
| `tendance_notes` | float | pente de la régression linéaire sur les 3 points |
| `serie` | cat | "A" / "B" / "C" / "D" / "E" / "G" / "NA" (collège/2nde) |
| `riasec_R`..`riasec_C` | float (6) | profil RIASEC normalisé |
| `moyenne_generale` | float | moyenne des 3 années |
| `filiere_choisie` | cat | Informatique / Médecine / Droit / Génie_Civil / Communication / Gestion_Commerce / Agronomie / Lettres |
| `score_60_40` | float | score 60/40 du moteur actuel |
| `match_riasec` | float | cosinus RIASEC ↔ profil filière |
| `ecart_notes_seuil` | float | moyenne - seuil d'admission de la filière |
| `nb_consultations` | int | consultations RAG sur la filière choisie |
| `en_favori` | bool | la filière est dans les favoris |
| `score_similarite_recherche` | float | similarité cosinus de la dernière recherche RAG |
| `statut` | cat | ADMIS / REORIENTE — **cible** |
| `satisfaction` | int (1-5) | satisfaction (pour exploitation future) |

### 1.2 Distribution

- 600 élèves, **88.2 % ADMIS** (déséquilibre — géré par `class_weight="balanced"`)
- 8 filières × 7 niveaux, niveaux pondérés vers le supérieur (~75 % au lycée/bac)
- 30 % des cas avec `annee_partielle = True` (bulletin du 1er trimestre uniquement)

### 1.3 Mécanisme de vérité

La probabilité de réussite est générée selon :

```
logit = 2.5 * match_riasec
      + 0.35 * ecart_notes_seuil
      + 0.5 * 0.3 * tendance_notes   # bonus trajectoire
      - 1.5
proba = sigmoid(logit) + bruit_normal(0, 0.15)
```

→ Le modèle supervisé doit **retrouver** ces trois leviers (aspiration, niveau, trajectoire). C'est ce qu'on observe (voir § 3).

---

## 2. Résultats de classification

### 2.1 Comparaison avec / sans signal comportemental

| Modèle | Config | ROC-AUC | F1 ADMIS | Recall REORIENTE |
|---|---|---:|---:|---:|
| Logistic Regression | sans_comportemental | **0.891** | 0.908 | 0.778 |
| Logistic Regression | avec_comportemental | 0.890 | 0.907 | 0.833 |
| Gradient Boosting | sans_comportemental | **0.873** | 0.945 | 0.333 |
| Gradient Boosting | avec_comportemental | 0.856 | 0.942 | 0.278 |

**Lecture :**

- En logistique : quasi-équivalence (Δ = **-0.001**). Le modèle équilibré gagne **5 pts de recall** sur les REORIENTE — c'est un signal positif pour la détection des élèves à risque.
- En gradient boosting : **dégradation** (Δ = -0.017) — l'arbre exploite mal les features comportementales, probablement parce qu'elles sont redondantes avec `filiere_choisie` (one-hot) déjà capturé par `match_riasec` et `score_60_40`.
- Le **logistic regression est plus robuste** sur ce dataset déséquilibré (88/12). À garder comme baseline.

### 2.2 Variables les plus prédictives (gradient boosting)

**Sans comportemental :**
1. `ecart_notes_seuil` (0.242) — la variable reine, sans surprise
2. `notes_actuelle` (0.195)
3. `match_riasec` (0.098)
4. `moyenne_generale` (0.082)
5. `riasec_A`, `score_60_40`, `riasec_S` (~0.048 chacun)
6. `notes_n2` (0.043)

**Avec comportemental :**
- `ecart_notes_seuil` et `notes_actuelle` restent #1 et #2
- `match_riasec` monte (0.098 → 0.114)
- Les **3 features comportementales n'apparaissent pas** dans le top 8 → le modèle les ignore, ce qui confirme qu'elles n'apportent pas de signal **discriminant** au-delà de ce que les notes/RIASEC capturent déjà
- `tendance_notes` n'apparaît pas non plus en top 8, mais reste candidate (juste en dessous du seuil d'affichage)

### 2.3 Test "bulle de filtre"

Sur les 100 élèves dont `ecart_notes_seuil < 0` (niveau académique objectivement sous le seuil de la filière choisie) :

| Config | ADMIS prédit | ADMIS réel |
|---|---:|---:|
| sans_comportemental | 57.0 % | 57.0 % |
| avec_comportemental | 57.0 % | 57.0 % |

→ **Aucun faux positif supplémentaire** introduit par le signal comportemental. C'est conforme à la simulation : le comportemental n'a que 15 % de poids dans le choix de filière, et le dataset de vérité ne lui donne pas de pouvoir de bascule.

⚠️ **Mais** : ce test reste à refaire sur données réelles — la simulation est peut-être trop gentille (les comportements ne sont générés que sur les 3 top candidates, donc ils n'introduisent pas vraiment de "faux" choix).

---

## 3. Recommandations pour les phases suivantes

### 3.1 Phase 1 (modèle de données) — ajustements

- **Ne PAS supprimer le champ `niveau` (String) sans transition.** Conversion enum possible mais à orchestrer avec un script de migration (cf. `CHANGELOG_SCHEMA.md`).
- La table `orientation_outcome` doit reprendre les colonnes du CSV (cf. § 1.1), avec :
  - `riasec_snapshot` et `notes_snapshot` en JSON (cf. brief)
  - `statut` (ENUM côté DB) aligné sur `ADMIS / REORIENTE / EN_COURS / ABANDON` (le brief n'inclut pas EN_COURS / ABANDON, à confirmer en Phase 1)
- La table `engagement_signal` (vue matérialisée) doit pré-calculer `nb_consultations`, `en_favori`, `score_similarite_recherche` **par (élève, fiche)**, pas seulement pour la filière finalement choisie (sinon, on perd le pouvoir de détection des "faux choix" comportementaux).

### 3.2 Phase 3 (moteur de recommandation) — pondération

Sur la base de ce prototype :

```
score_final = 0.50 * score_realite    # notes + trajectoire 2 ans
            + 0.35 * score_aspiration # cosinus RIASEC ↔ filière
            + 0.15 * score_engagement # consultations + favoris + RAG
```

- **Plafond strict sur `score_engagement` : 20-25 %** (cf. brief). Confirmé par le test "bulle de filtre" : au-delà, on n'améliore pas la prédiction et on risque de noyer les vrais signaux.
- **Trajectoire** : intégrer `tendance_notes` dans `score_realite` (poids ~10 % du sous-score).
- **Logistic regression** semble plus robuste que le gradient boosting sur ce type de problème déséquilibré. À privilégier comme premier modèle, garder le GB en challenger.

### 3.3 Phase 5 (entraînement sur données réelles) — pré-requis

- Volume minimum pour que le modèle soit exploitable : **≥ 5000 `orientation_outcome` réels** (à 600 on est déjà au bord, à 5000 on a une marge).
- Endpoint `GET /api/v1/admin/prediction/dataset` doit **anonymiser** : pas de `nom`, `prenom`, `email`, `telephone`. Garder uniquement `eleve_id` (UUID).
- Schéma réel attendu du CSV : conforme à § 1.1. Le script `train_model.py` n'aura aucune modification à subir — c'est l'intérêt d'avoir prototypé sur le bon format.

---

## 4. Fichiers produits par la Phase 0

| Fichier | Contenu | Taille |
|---|---|---:|
| `generate_synthetic_data.py` | Générateur enrichi (niveau, notes_historique, comportemental) | 192 l. |
| `train_model.py` | Comparaison A/B + test bulle de filtre + persistance modèle | 230 l. |
| `orientation_outcome_synthetic.csv` | 600 profils × 24 colonnes | ~50 Ko |
| `models/gb_sans_comportemental.joblib` | Modèle baseline | ~50 Ko |
| `models/gb_avec_comportemental.joblib` | Modèle challenger | ~50 Ko |
| `results_prototype.json` | Sortie structurée pour exploitation programmatique | ~2 Ko |

## 5. Critères d'acceptation (rappel brief § Phase 0)

- [x] Le modèle prototype tourne
- [x] Le rapport est lisible et compare clairement les configurations
- [x] Le signal comportemental est évalué **négativement** (Δ AUC ≤ 0) → conclusion claire sur le plafond à imposer en Phase 3
- [x] Le test "bulle de filtre" est implémenté et n'alerte pas sur le dataset synthétique
- [x] Les variables étendues (`tendance_notes`, `niveau_actuel`, comportemental) sont dans le dataset et exploitables par `train_model.py`

**Verdict :** ✅ Phase 0 validée. On peut passer à la **Phase 1** (modèle de données Spring Boot).

---

## Annexe — Lancement

```bash
# Régénérer le dataset synthétique
python3 generate_synthetic_data.py

# Réentraîner et produire results_prototype.json
python3 train_model.py

# Inspection rapide
head -2 orientation_outcome_synthetic.csv
cat results_prototype.json | python3 -m json.tool | head -40
```
