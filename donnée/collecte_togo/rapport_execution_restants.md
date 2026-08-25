# Rapport d’exécution — travaux restants de collecte

## Résumé

Les travaux restants accessibles publiquement ont été exécutés. Le dossier contient maintenant des sources nationales déjà récupérées, des indicateurs internationaux pour le Togo, un référentiel géographique des établissements et un référentiel O*NET pour les métiers et intérêts professionnels. Ces fichiers enrichissent la calibration, le contexte et le diagnostic, mais **ils ne créent pas les 5 000 outcomes individuels `ADMIS`/`RECALE`**.

## Nouvelles données effectivement collectées

| Source | Fichiers | Contenu | Usage |
|---|---|---|---|
| UNESCO UIS via HDX | `unesco_sdg_data_tgo.csv`, `unesco_opri_data_tgo.csv`, `unesco_dem_data_tgo.csv` et listes d’indicateurs | Données UNESCO pour le Togo : indicateurs SDG 4, indicateurs de politique éducative et données démographiques | Calibration et comparaison internationale |
| Banque mondiale WDI | Trois fichiers JSON sur l’inscription tertiaire, l’achèvement tertiaire et les dépenses d’éducation | Séries temporelles nationales | Variables contextuelles, contrôle de cohérence et évolution temporelle |
| HOT/HDX | `togo_education_facilities_geojson.zip` et métadonnées | Établissements d’éducation géolocalisés issus d’OpenStreetMap | Référentiel géographique et contrôle de couverture |
| O*NET 30.3 | ZIP complet et fichiers structurés d’occupations, intérêts et mappings | 1 016 occupations, 8 307 lignes de types d’intérêts et 73 062 lignes de domaines d’intérêts | Mapping filière–métier–intérêts et validation du module RIASEC |
| Portail de données du Togo | Catalogue vérifié | Le catalogue affiche 155 jeux de données Education et 22 jeux Emploi | Sélection ultérieure de jeux CSV/XLSX précis |

Les données UNESCO téléchargées sont organisées dans `structured/` avec une extraction ciblée des indicateurs contenant des termes liés à l’enseignement supérieur, l’achèvement, l’inscription et les dépenses d’éducation. Les données géographiques HOT/HDX comportent une réserve importante : la couverture dépend de l’activité des contributeurs OpenStreetMap et n’est pas exhaustive.

## Sources nationales déjà disponibles

Les fichiers nationaux suivants restent dans le dossier : l’Annuaire statistique national INSEED 2024, l’Annuaire national ministériel 2024–2025 et les PDF de concours de l’Université de Lomé. Les annuaires contiennent des agrégats, notamment des effectifs et des taux de réussite par série ou examen. Les PDF de Lomé sont des avis de concours et des conditions d’accès, pas des listes d’admis.

## Gabarits créés

Deux fichiers prêts à être alimentés ont été créés : `admission_results_template.csv` pour normaliser les résultats de concours et `orientation_outcome_template.csv` pour préparer le dataset final de la Phase 5. Les identifiants doivent être pseudonymisés et les noms en clair doivent rester dans une table séparée et protégée.

## Ce qui reste réellement bloquant

La collecte publique n’a pas permis d’obtenir un fichier ouvert contenant 5 000 profils togolais avec une issue d’admission. Les tâches suivantes nécessitent encore une source institutionnelle, une autorisation, un accès au portail ou une collecte dans l’application :

| Blocage | Pourquoi il reste nécessaire |
|---|---|
| Résultats complets des concours de Lomé, Kara et écoles privées | Produire les labels `ADMIS` et `RECALE` par filière |
| Dénominateurs des concours ou listes complètes de candidats | Éviter de considérer à tort l’absence d’un nom comme un échec |
| Profils individuels Activ Education | Relier le résultat à la série, aux notes, au RIASEC et à l’engagement |
| Validation par les conseillers | Confirmer le statut final via `PATCH` |
| Accès aux tables détaillées MESR/ONEF/ANPE | Ajouter capacité d’accueil, insertion et marché du travail |
| ESCO, PASEC, MICS et DHS | Enrichir les référentiels et diagnostics sous réserve des conditions d’accès |

Le portail officiel de résultats des examens et concours du Togo a été identifié, mais son accès public semble orienté vers la consultation de résultats plutôt que vers un export massif. Il ne faut pas automatiser une collecte nominative ni contourner une authentification ou un OTP.

## Conclusion opérationnelle

La partie **calibration et enrichissement** est maintenant bien avancée. La partie **variable cible et profils individuels** ne peut pas être fabriquée à partir des agrégats. La prochaine action réellement décisive est de lancer un pilote avec un lycée ou une université, obtenir une liste autorisée de résultats, faire saisir les profils correspondants dans Activ Education et valider les outcomes. Tant que le nombre d’outcomes `ADMIS`/`RECALE` reste inférieur à 5 000, il faut conserver la règle pondérée existante et ne pas présenter un entraînement réel comme terminé.

## Références

[1]: https://data.humdata.org/dataset/unesco-data-for-togo "HDX — Togo Education Indicators, source UNESCO UIS"
[2]: https://data.humdata.org/dataset/hotosm_tgo_education_facilities "HDX/HOT — Education Facilities of Togo"
[3]: https://www.onetcenter.org/database.html "O*NET 30.3 Database"
[4]: https://esco.ec.europa.eu/en/use-esco/download "ESCO Dataset Download"
[5]: https://togo.opendataforafrica.org/data/ "Portail de données du Togo"
[6]: https://resultats.service-public.gouv.tg/ "Portail officiel des résultats des examens et concours du Togo"
[7]: https://api.worldbank.org/v2/country/TGO/indicator/SE.TER.ENRL?format=json&per_page=1000 "Banque mondiale — inscription dans le supérieur, Togo"
