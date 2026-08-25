# Plan structuré de constitution du dataset `orientation_outcome`

## Objectif

Constituer au moins **5 000 outcomes réels** portant sur le statut d’orientation d’élèves par filière, avec les labels `ADMIS` ou `RECALE`, afin de lancer la Phase 5 du pipeline ML (`phase5_export_dataset.py` puis `phase5_train_real.py`).

## État actuel

La base ne contient actuellement qu’un seul outcome, encore au statut `EN_COURS`. Les scripts de peuplement (`seed/*.sh`) créent uniquement des données de test — bibliothèque, quiz et utilisateurs — et ne produisent pas de résultats d’orientation réels. Les outcomes apparaissent lorsque les élèves utilisent leur parcours d’orientation, puis sont complétés par les conseillers.

## Points d’entrée API

| Opération | Endpoint | Rôle |
|---|---|---|
| Création ou mise à jour initiale | `POST /api/v1/eleves/{eleveTrackingId}/orientation-outcome` | Enregistrer le résultat du parcours d’orientation |
| Validation du statut | `PATCH /api/v1/eleves/{eleveTrackingId}/orientation-outcome/{outcomeTrackingId}` | Affecter `ADMIS`, `RECALE`, `ABANDON`, `REORIENTE` ou `EN_COURS` |
| Export du dataset | `GET /api/v1/admin/prediction/dataset` | Produire le CSV utilisé par la Phase 5 |

## Sources à collecter

| Source | Données attendues | Usage dans le modèle |
|---|---|---|
| Open Data Togo | Indicateurs d’éducation, établissements supérieurs, lycées et formations techniques | Calibrage des a priori, référentiel des établissements et filières |
| INSEED | Effectifs, nouveaux inscrits et taux de réussite par filière ; annuaire statistique national | Validation des taux d’admission et contrôle de plausibilité |
| Ministère de l’Éducation | Annuaire national 2024–2025 et tableau de bord annuel | Effectifs, taux de scolarisation et taux de réussite officiels |
| Universités de Lomé et de Kara, écoles privées | Listes d’admis, résultats de concours et candidats retenus par filière | Labels réels `ADMIS` et, par comparaison, `RECALE` |
| UNESCO-UIS | Effectifs, taux de réussite et répartition par domaine au niveau tertiaire | Comparaison internationale et contrôle de dérive |
| Office du Baccalauréat | Résultats et taux d’admission par série | A priori par série, notamment A, C et D |
| Application Activ Education | Série, notes, profil RIASEC, engagement et filière choisie | Variables explicatives du modèle |

## Fusion des données

Le dataset final doit réunir trois catégories d’informations. Les statistiques publiques apportent les agrégats nécessaires au calibrage et à la validation. Les listes d’admission des universités et du baccalauréat fournissent les labels de résultat. L’application Activ Education fournit le profil individuel de l’élève et les variables d’entrée du modèle.

> Aucun dataset public prêt à l’emploi ne semble réunir simultanément 5 000 profils individuels d’élèves togolais et leur issue d’admission. Les données d’entraînement devront donc être construites à partir des listes de résultats, puis enrichies par les profils saisis dans l’application.

## Processus recommandé

| Étape | Action | Résultat attendu |
|---:|---|---|
| 1 | Collecter les listes d’admission 2024–2025 auprès des universités, écoles et du baccalauréat | Résultats réels par filière |
| 2 | Saisir ou importer les profils correspondants dans Activ Education | Variables individuelles disponibles |
| 3 | Créer l’outcome par l’API `POST` | Outcome associé à l’élève et à la filière |
| 4 | Faire valider le statut par les conseillers via `PATCH` | Labels `ADMIS` ou `RECALE` |
| 5 | Conduire un pilote dans un ou deux lycées ou établissements supérieurs | Flux de données récurrent et contrôlé |
| 6 | Exporter le CSV une fois le seuil atteint | Dataset réel exploitable |
| 7 | Entraîner et évaluer le modèle | Résultats de la Phase 5 |

## Critères de qualité

Le seuil minimal est de **5 000 outcomes ayant le statut `ADMIS` ou `RECALE`**. Tant que ce seuil n’est pas atteint, il est recommandé de conserver la règle pondérée actuelle **50/35/15** plutôt que d’entraîner un modèle susceptible de surapprendre. Les données synthétiques `models/gb_*.joblib` ne doivent pas être confondues avec les données réelles et ne sont pas utilisées directement par le backend Spring Boot.

Il est également nécessaire de conserver l’année, l’établissement, la filière et la source de chaque résultat. Les profils doivent être pseudonymisés et les listes nominatives ne doivent pas être publiées dans le dataset final. Une séparation entre données d’entraînement, validation et test devra être définie avant l’entraînement.

## Commandes prévues après authentification

```bash
JWT=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@activeducation.tg","motDePasse":"<mdp>"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin).get('token',''))")

python3 phase5_export_dataset.py --token "$JWT" --out real_dataset.csv
python3 phase5_train_real.py --csv real_dataset.csv
```

Le principal point bloquant n’est donc pas l’entraînement du modèle, qui peut s’effectuer rapidement sur une machine ordinaire avec 5 000 lignes, mais la collecte légitime et la mise en correspondance des profils individuels avec les résultats d’admission.

## Références

Le présent document est une restructuration fidèle du fichier fourni par l’utilisateur, `pasted_content.txt`. Les adresses de sites mentionnées dans ce fichier devront être vérifiées directement avant toute collecte ou utilisation opérationnelle des données.
