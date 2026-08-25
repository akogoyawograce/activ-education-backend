# Référentiel de calibration — a priori réels pour le modèle de prédiction

Généré le 18/08/2026 par `build_calibration.py` à partir des données réelles collectées
(INSEED, Ministère de l'Éducation, opendata.gouv.tg, UNESCO UIS, Banque mondiale).

## Fichiers produits

| Fichier | Contenu | Couverture |
|---|---|---|
| `a_priori_bac_series.csv` | Taux de réussite **BAC I** par série (A/C/D) × région × sexe : min, max, moyenne, dernière valeur | 2014-2022, 63 lignes |
| `a_priori_bac2_2024.csv` | Taux de réussite **BAC II session juin 2024** par série × région × sexe (M/F/T) | 72 lignes, le plus récent |
| `effectifs_universites_par_faculte.csv` | Effectifs étudiants par faculté/école (UL + UK) par année et sexe | 24 entités, 2013-2018, 144 lignes |
| `a_priori_international.csv` | Scolarisation tertiaire (effectifs, GER %) UNESCO UIS + Banque mondiale pour le Togo | 1971-2022, 86 lignes |

## Points saillants (pour la calibration du modèle)

- **BAC II 2024 (national)** : série C = **70,3 %**, série A = **48,3 %**, série D = **42,2 %**.
  Un élève prédit ADMIS en série D à 90 % doit être **suspect** — le modèle doit rester sous le plafond réaliste.
- **BAC I** : variabilité énorme d'une année à l'autre (ex. série C Savanes : 78-100 % ; série D : 29-95 %).
  → Ne jamais utiliser un taux d'une seule année comme vérité absolue : utiliser la moyenne ou l'intervalle.
- **Taille des facultés** : FAST, FLESH, FSS dominent (centaines à milliers d'étudiants) ; CERSA, CIC, ISMA sont minuscules.
  → Pondérer les recommandations par la **capacité d'accueil réelle** (une filière qui reçoit 20 élèves/an ne peut pas absorber des milliers de candidats).

## Usage dans le pipeline ML

### 1. A priori par série (avant entraînement)

Dans `train_model.py`, la colonne `serie` est déjà une feature. Utiliser le référentiel pour :

- **Définir le taux de base attendu** par série pour la variable cible `ADMIS` :
  `p(ADMIS | série) ≈ taux BAC II 2024` (pour BAC_1+), ou moyenne BAC I (pour lycéens).
- **Corriger les classes déséquilibrées** : le taux d'ADMIS réel du dataset d'entraînement
  doit être cohérent avec ces taux ; s'il en dévie fortement (> 15 points), vérifier la collecte.

### 2. Anti-dérive (après entraînement, en production)

Comparer régulièrement la **distribution des prédictions** du modèle avec les taux réels :

```
taux prédit ADMIS par série (modèle)  vs  taux réel BAC (référentiel)
  série A : 55 %   vs 48,3 %   → ok (±10 pts)
  série D : 85 %   vs 42,2 %   → DÉRIVE, investiguer
```

Même logique par région et par filière (effectifs).

### 3. Régle 50/35/15 actuelle (sans Phase 5)

Le référentiel permet de **ré-étalonner le seuil** `score_realite` du `Recommandation3SignauxServiceImpl`
par filière : une filière à 2 % d'admission (ex. Médecine) doit exiger un score proche du maximum.

### 4. Données d'entraînement manquantes

Ces tables servent de **priors et de validation** — elles ne remplacent pas les 5 000 outcomes
`ADMIS`/`RECALE` réels. Pour les obtenir :
- listes de résultats concours (Univ. de Lomé, Kara, écoles privées) — seules sources de labels individuels ;
- profils (série, notes, RIASEC, engagement) depuis l'app Activ Education.

## Traçabilité

- `a_priori_bac_series.csv` : opendata.gouv.tg (INSEED) — fichiers `taux-de-reussite-au-bac*.csv`
- `a_priori_bac2_2024.csv` : Annuaire national 2024-2025, tableau 3.10,
  planifeducation.gouv.tg (PDF officiel du 29/09/2025) — valeurs relues ligne ~1185 du `.txt` extrait
- `effectifs_universites_par_faculte.csv` : INSEED, « Effectifs des étudiants des universités publiques par Faculté_Ecole_Institut et par Sexe » (observationdata-tabmdfe.csv)
- `a_priori_international.csv` : UNESCO UIS (OPRI, `collecte_togo/sources/unesco_opri_data_tgo.csv`)
  + Banque mondiale WDI (`worldbank_tertiary_enrollment/completion.json`)
