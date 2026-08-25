# Collecte de données réelles — Entraînement du modèle de prédiction

> Objectif : réunir **≥ 5 000 `orientation_outcome` réels** (statut ADMIS/RECALE par filière)
> pour lancer la Phase 5 du pipeline ML (`phase5_export_dataset.py` + `phase5_train_real.py`),
> enrichi de données nationales et internationales (agrégats, diagnostics, classifications).

---

## 1. Le problème : pourquoi la base est vide

La DB ne se remplit que par 2 sources, aucune ne tourne en continu :

| Source | Contenu |
|---|---|
| Scripts `seed/*.sh` | Bibliothèque, quiz, utilisateurs de test (10 élèves, 3 conseillers, 4 admins) |
| Usage réel de l'app | Les `orientation_outcome` (créés quand un élève suit son parcours, puis statut ADMIS/RECALE noté par les conseillers) |

État actuel : **1 seul outcome, encore `EN_COURS`**. Personne n'a utilisé l'app → pas de données.

**Points d'entrée API concernés :**
- `POST /api/v1/eleves/{eleveTrackingId}/orientation-outcome` → crée/met à jour un outcome
- `PATCH /api/v1/eleves/{eleveTrackingId}/orientation-outcome/{outcomeTrackingId}` → passe le statut (ADMIS, RECALE, ABANDON, REORIENTE, EN_COURS)
- `GET /api/v1/admin/prediction/dataset` → export CSV des outcomes (source de la Phase 5)

---

## 2. Sources NATIONALES — check-list par site

### 2.1 opendata.gouv.tg (Agence Togo Digital)

Sur la page « Données relatives à l'éducation », cherche ces jeux de données :

| Jeu de données | Utilité |
|---|---|
| **Indicateurs d'Éducation au Togo** | Taux de réussite, effectifs par niveau → *calibrer les a priori* |
| **Établissements d'enseignement supérieur au Togo** | Liste officielle des établissements par filière |
| **Établissements Scolaires - Lycées au Togo** | Profils des lycées (séries proposées) |
| **Établissements de Formations Techniques au Togo** | Filières techniques et prof. |

⚠️ Formats CSV/XLSX. Ce sont des **annuaires d'établissements** (déjà en partie dans le projet via `Base_Etablissements_Superieurs_Togo.xlsx`) et des **agrégats**, pas des élèves individuels.

### 2.2 inseed.tg (Institut National de la Statistique)

- **« Statistiques sociales » → Éducation → Enseignement supérieur** :
  - Effectifs d'étudiants par filière
  - Nouveaux inscrits par filière
  - **Taux de réussite par filière**
- **« Annuaires »** : télécharger l'**Annuaire Statistique National** le plus récent, chapitre « enseignement supérieur »
- **RGPH (Recensement Général de la Population et de l'Habitat)** : niveau d'instruction par âge, région, sexe → *a priori* réalistes par zone

Objectif : **taux d'admission par filière et par série** (ex. Médecine ~15/20, Droit ~12/20) → validation du modèle (un élève prédit ADMIS dans une filière à 2 % de réussite doit être suspect).

### 2.3 planifeducation.gouv.tg (Ministère de l'Éducation)

- **« Annuaire national 2024-2025 »** et **« Tableau de bord annuel »** :
  - Effectifs, taux brut de scolarisation, taux de réussite — version ministère

### 2.4 Universités de Lomé, de Kara + écoles privées — LE FILON PRINCIPAL

Sur `univ-lome.tg`, `univ-kara.tg` et les sites des écoles privées, chercher :

- **« Listes des admis »**
- **« Résultats des concours »**
- **« Listes des candidats retenus »**

Publiées chaque année **par filière**, souvent en PDF. Parcours type :
`Admissions` → `Inscriptions` → `Résultats` par filière et par concours (médecine, droit, informatique, génie civil, …).

**C'est LA variable cible `ADMIS` par filière, élève par élève.** À collecter + extraire (nom, filière) et à croiser avec les profils de l'app.

### 2.5 Office du Baccalauréat du Togo

- **« Résultats du baccalauréat »** par série → admis de première (séries C, D, A, …)
- Les listes nominatives sont souvent derrière un portail OTP, mais les **taux par série** sont publics

### 2.6 Enseignement supérieur et recherche (MESR) + Emploi

- **mesr.gouv.tg (Ministère de l'Enseignement Supérieur et de la Recherche)** :
  - Statistiques des universités, capacité d'accueil par filière, répartition des inscrits
- **ONEF (Observatoire National de l'Emploi et de la Formation)** :
  - Insertion professionnelle des diplômés par filière, taux d'emploi → enrichit la recommandation (un diplôme qui mène au chômage mérite une alerte)
- **ANPE Togo (Agence Nationale Pour l'Emploi)** :
  - Offres d'emploi par domaine, métiers en tension → aligne les filières sur le marché réel
- **INSEED — Enquête sur l'emploi** : chômage des jeunes par niveau de diplôme

---

## 3. Sources INTERNATIONALES

### 3.1 uis.unesco.org (Institut de Statistiques de l'UNESCO)

- Barre de recherche : **« Togo » → Thème « Éducation » → Niveau « Tertiaire »**
  - Taux de réussite, effectifs, répartition par domaine d'études
- **API UIS** disponible (accès programmatique) → facilite la collecte automatisée

### 3.2 World Bank — EdStats & WDI

- **datatopics.worldbank.org/education** : indicateurs éducation Togo (inscription tertiaire, taux d'achèvement, ratio prof/élèves)
- **WDI (World Development Indicators)** : jeunesse, emploi, revenus → variables contextuelles
- Filtre pays = **Togo**, téléchargement CSV/API

### 3.3 OECD — Education at a Glance

- **« Education at a Glance »** (annuel) : taux d'abandon par filière, taux d'accès au supérieur, disparités par domaine — moyennes internationales de référence (à pondérer : pas d'Afrique de l'Ouest dans l'échantillon, mais utiles pour l'ordre de grandeur)

### 3.4 OIT (ILO) — Statistiques du travail

- **ilostat.ilo.org** : chômage des jeunes par niveau d'éducation (Togo + région), emploi par secteur
- Classification **ISCO-08** des professions → référentiel commun pour les métiers

### 3.5 Humanitarian Data Exchange (HDX)

- **data.humdata.org** → recherche « Togo education » : jeux de données scolaires croisés (UNICEF, Banque mondiale, OCHA) en CSV/GeoJSON

### 3.6 Enquêtes internationales en Afrique de l'Ouest

- **PASEC (CONFEMEN, pasec.confemen.org)** : évaluations réelles des élèves (français, maths) dans les pays membres, **Togo inclus** (2014, 2019). Données micro disponibles → vrais niveaux scolaires par région → calibrer la variable « notes »
- **MICS (UNICEF)** : Togo a réalisé des MICS (2017…) → indicateurs éducation ménages, scolarisation
- **DHS (The DHS Program)** : Togo EDS-MICS 2013-14 → niveau d'éducation par individu, âge, sexe (microdonnées anonymisées réelles)

---

## 4. Données DIAGNOSTIQUES (tests d'orientation) — sources réelles

Le module diagnostic (RIASEC, aptitudes, personnalité) peut être calibré/validé avec des référentiels réels :

### 4.1 O*NET (US Department of Labor) — RIASEC par métier

- **onetcenter.org** → base téléchargeable (CSV) : **~900 professions avec codes d'intérêts Holland (RIASEC)** et compétences
- Utilité : associer à chaque filière un profil RIASEC réel et documenté ; valider les correspondances du `ProfilFiliereRiasecCatalog`
- Payant ? Non, gratuit et ouvert. *Référence internationale la plus riche pour le volet intérêts.*

### 4.2 ESCO (Commission Européenne)

- **esco.ec.europa.eu** → données ouvertes : occupations + compétences + connaissances multilingues (dont français)
- Utilité : mapping métiers ↔ compétences pour enrichir les fiches métiers

### 4.3 ONISEP (France) — formations et métiers

- **onisep.fr** : fiches formations/métiers, débouchés, taux d'insertion par diplôme — référentiel francophone de qualité

### 4.4 WEF — Future of Jobs Report

- Rapports **World Economic Forum** (annuels) : compétences émergentes, secteurs porteurs → actualiser les recommandations

### 4.5 Tests psychométriques normés (RIASEC, MBTI, VAK…)

- Référentiels **RIASEC : Holland codes** (théorie de J. Holland) — normes publiées
- **MBTI** : base de répartition des types par profession (statistiques officielles de la fondation MBTI)
- **O*NET Interest Profiler** : questionnaire gratuit validé → comparer avec le quiz RIASEC de l'app pour vérifier sa fiabilité

---

## 5. Synthèse : la fusion des sources

| Source | Ce qu'elle apporte | Pourquoi c'est nécessaire |
|---|---|---|
| opendata.gouv.tg, INSEED, MESR, UNESCO, Banque mondiale | **Agrégats** (taux d'admission, effectifs, emploi) | *A priori* + validation (anti-dérive du modèle) |
| Universités, BAC | **Statut ADMIS/RECALE réel, filière réelle** | La variable cible (le label) |
| PASEC, MICS, DHS | **Vrais niveaux scolaires** (français, maths, scolarisation) | Calibrer les features « notes » avec la réalité du terrain |
| O\*NET, ESCO, ONISEP, WEF | **Référentiels métiers/compétences/RIASEC** | Valider les diagnostics et enrichir les recommandations |
| ONEF, ANPE, OIT | **Marché du travail** | Pondérer la recommandation par l'employabilité réelle |
| L'app Activ Education | **Profil élève** : série, notes, RIASEC, engagement | Les features (les entrées du modèle) |

> **Vérité à assumer :** aucun dataset prêt à l'emploi de 5 000 élèves togolais
> (profil + issue) n'existe en ligne. Les lignes d'entraînement se **construisent** :
> listes d'admission récupérées + profils saisis dans l'app.
> Les sources agrégées servent à **calibrer, valider et contextualiser**, pas à remplacer les lignes réelles.

---

## 6. Processus de collecte recommandé

```
1. Collecter les listes d'admission (universités, BAC)  → statuts réels 2024-2025
2. Saisir les profils correspondants dans l'app (ou import API)
   → POST /eleves/{id}/orientation-outcome
3. Conseillers → PATCH statut ADMIS/RECALE sur chaque outcome
4. Pilote réel (1-2 lycées/universités) pour produire les données
   au fil de l'eau pendant quelques mois
5. Télécharger les agrégats (INSEED, UNESCO, Banque mondiale, O*NET, PASEC)
   → construire un référentiel de calibration + anti-dérive
6. Dès 5 000 outcomes ADMIS/RECALE :
   JWT=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@activeducation.tg","motDePasse":"<mdp>"}' \
     | python3 -c "import sys,json; print(json.load(sys.stdin).get('token',''))")
   python3 phase5_export_dataset.py --token "$JWT" --out real_dataset.csv
   python3 phase5_train_real.py --csv real_dataset.csv   → results_phase5.json
```

Rappels :
- **Seuil Phase 5 : ≥ 5 000 outcomes ADMIS ou RECALE** (`phase5_train_real.py:130`), sinon sur-apprentissage → on garde la règle pondérée 50/35/15 (`Recommandation3SignauxServiceImpl`)
- Le backend **n'utilise pas** les `models/gb_*.joblib` (prototypes synthétiques) — les `.joblib` réels ne serviraient qu'à un futur service ML séparé du Spring Boot
- Un **data center n'est pas nécessaire** : GradientBoosting sur 5 000 lignes s'entraîne en quelques secondes sur un PC