# Analyse des travaux restants — Dataset réel `orientation_outcome`

## 1. Diagnostic général

L’objectif technique est de réunir au moins **5 000 outcomes réels** ayant le statut `ADMIS` ou `RECALE`, avec une filière et un profil d’élève correspondant. La checklist distingue correctement deux familles de données : les **agrégats** qui servent à calibrer et contrôler le modèle, et les **lignes individuelles** qui servent réellement à entraîner la variable cible.

À ce stade, les agrégats nationaux ont commencé à être récupérés, mais le chantier principal — obtenir les résultats d’admission et les relier à des profils individuels — n’est pas encore réalisé. La base de l’application contient toujours un seul outcome `EN_COURS`; elle ne peut donc pas encore alimenter la Phase 5.

## 2. Ce qui est déjà fait

| Élément | État | Résultat |
|---|---|---|
| Annuaire statistique national INSEED 2024 | **Fait** | PDF téléchargé et contrôlé. Il contient des indicateurs de l’enseignement supérieur, des effectifs et des taux d’examens, mais pas un dataset individuel par filière universitaire. |
| Annuaire national du Ministère 2024–2025 | **Fait** | PDF téléchargé et contrôlé. Il contient notamment les taux du BAC par série et région, les effectifs scolaires et des indicateurs nationaux. |
| Fichiers de concours de l’Université de Lomé | **Partiellement fait** | 15 PDF d’avis et de conditions de concours récupérés. Ils décrivent les filières, mais ne sont pas des listes d’admis. |
| Vérification des PDF | **Fait** | Les fichiers ont été convertis en texte et contrôlés par mots-clés et sections. |
| Confirmation du besoin de 5 000 outcomes | **Fait** | Le seuil et le processus API sont définis dans la checklist. |

## 3. Travaux restants classés par priorité

### Priorité P0 — Indispensable pour entraîner le modèle réel

| Travail | Pourquoi c’est indispensable | Livrable attendu |
|---|---|---|
| Collecter les listes officielles d’admis et de retenus | C’est la source des labels `ADMIS`; les recalés doivent être identifiés par une source officielle ou par le registre complet du concours | PDF/CSV par établissement, année, concours et filière |
| Obtenir les listes complètes ou les dénominateurs admis/présents | Une liste d’admis seule ne permet pas toujours de produire des `RECALE` fiables | Table `candidats`, `presents`, `admis`, `recalés` ou source équivalente |
| Collecter les résultats de l’Université de Kara et des écoles privées | Les 15 PDF de Lomé ne suffisent pas pour atteindre 5 000 lignes et couvrent surtout des appels à candidature | Dossier de résultats par établissement et année |
| Définir une procédure légale de pseudonymisation | Les noms des candidats ne doivent pas devenir des données d’entraînement directement identifiantes | Identifiant haché, année, filière, établissement, statut et provenance |
| Faire correspondre les résultats avec les profils Activ Education | Sans profil, un résultat d’admission ne fournit pas les variables explicatives du modèle | Table de correspondance pseudonymisée entre profil et résultat |
| Alimenter l’API des outcomes | Les scripts de la Phase 5 attendent les outcomes présents dans la base ou exportés par l’API | Outcomes `ADMIS`/`RECALE` validés par conseiller |

### Priorité P1 — Indispensable pour calibrer et contrôler le modèle

| Travail | État actuel | Action restante |
|---|---|---|
| Open Data Togo : indicateurs d’éducation | Non obtenu dans la collecte actuelle | Retrouver les fichiers CSV/XLSX officiels ou constater leur indisponibilité et archiver la preuve |
| Open Data Togo : établissements supérieurs | Non obtenu | Importer le référentiel des établissements, filières, régions et statuts |
| Open Data Togo : lycées | Non obtenu | Importer les séries proposées par lycée et la région |
| Open Data Togo : formations techniques | Non obtenu | Importer les filières techniques et professionnelles |
| INSEED : effectifs et nouveaux inscrits par filière | Partiellement couvert | Vérifier si les tableaux détaillés sont publiés séparément du PDF national; sinon utiliser les tableaux disponibles au niveau établissement/type |
| INSEED : taux de réussite du supérieur par filière | Non trouvé | Rechercher la publication statistique dédiée à l’enseignement supérieur; l’annuaire actuel donne surtout des indicateurs globaux et le BTS |
| RGPH : niveau d’instruction par âge, sexe et région | Non réalisé | Télécharger les tableaux ou microdonnées autorisées du RGPH et produire des agrégats pseudonymisés |
| Tableau de bord annuel du Ministère | Non obtenu | Télécharger le tableau de bord correspondant et comparer ses indicateurs avec l’annuaire national |
| MESR : capacité et effectifs par filière | Non réalisé | Rechercher les annuaires/statistiques du supérieur, capacités d’accueil et répartitions par formation |

### Priorité P2 — Enrichissement emploi et recommandations

| Travail | Utilité | Livrable |
|---|---|---|
| ONEF | Mesurer l’insertion et le taux d’emploi par formation ou niveau | Table emploi/insertion par domaine, année et source |
| ANPE Togo | Relier les filières aux offres d’emploi et métiers en tension | Export des offres agrégé par métier/domaine |
| Enquête emploi INSEED | Ajouter chômage des jeunes par niveau de diplôme | Table chômage, âge, sexe, région et diplôme |
| Classification ISCO-08 | Harmoniser les métiers entre sources | Table de correspondance métier–code ISCO |

### Priorité P3 — Sources internationales de contexte

Ces données ne remplacent pas les outcomes togolais. Elles servent à comparer, documenter les tendances et détecter les dérives.

| Source | Action restante | Usage |
|---|---|---|
| UNESCO UIS | Télécharger les indicateurs tertiaires du Togo via l’interface ou l’API | Comparaison des effectifs, domaines d’études et indicateurs supérieurs |
| Banque mondiale EdStats/WDI | Télécharger les indicateurs Togo liés à la scolarisation, l’achèvement, l’emploi et les ratios scolaires | Variables contextuelles et comparaison temporelle |
| OECD Education at a Glance | Extraire uniquement les références comparables | Ordres de grandeur internationaux, sans les utiliser comme vérité togolaise |
| OIT ILOSTAT | Télécharger chômage des jeunes, emploi par secteur et niveau d’éducation | Contexte emploi et mapping ISCO-08 |
| HDX | Vérifier les jeux « Togo education » et leurs métadonnées | Données scolaires et géographiques complémentaires |
| PASEC | Vérifier les données accessibles pour le Togo 2014/2019 et les conditions d’accès | Calibrage des performances en français et mathématiques |
| MICS/DHS | Vérifier les microdonnées anonymisées accessibles | Niveau d’éducation, âge, sexe et scolarisation des ménages |

### Priorité P2/P3 — Référentiels du diagnostic et des métiers

| Source | Action restante | Résultat attendu |
|---|---|---|
| O*NET | Télécharger les fichiers d’intérêts, compétences et codes RIASEC | Mapping métier–RIASEC–compétences |
| ESCO | Télécharger la base française des professions et compétences | Référentiel multilingue métier–compétence |
| ONISEP | Sélectionner les fiches formations/métiers utiles comme documentation francophone | Descriptions de métiers et débouchés |
| WEF | Extraire les tendances de compétences émergentes | Mise à jour qualitative des recommandations |
| O*NET Interest Profiler | Comparer le quiz RIASEC de l’application au questionnaire de référence | Rapport de cohérence du diagnostic |
| MBTI et autres tests | Ne pas intégrer automatiquement sans licence, validité et justification | À traiter comme documentation, pas comme source principale |

## 4. Ordre d’exécution recommandé

La séquence efficace est la suivante. Il faut d’abord établir le référentiel des établissements et des filières, puis collecter les résultats d’admission. En parallèle, les agrégats INSEED, Ministère et MESR peuvent fournir des contrôles de cohérence. Les sources internationales et les référentiels métiers viennent ensuite, car elles n’augmentent pas le nombre d’outcomes réels.

| Phase | Action | Condition de sortie |
|---:|---|---|
| A | Référentiel établissements/filières | Une table unique avec code établissement, filière, région, niveau et année |
| B | Résultats de concours et admissions | Résultats par année, concours, filière, présents, admis et source |
| C | Pseudonymisation et contrôle juridique | Aucun nom en clair dans le dataset ML |
| D | Collecte des profils Activ Education | Série, notes, RIASEC, engagement et filière liés à un identifiant pseudonyme |
| E | Import et validation conseiller | Outcomes avec statut final `ADMIS` ou `RECALE` |
| F | Contrôle qualité | Doublons, valeurs manquantes, cohérence année/filière et provenance vérifiés |
| G | Export Phase 5 | Au moins 5 000 lignes valides et équilibrage documenté |
| H | Entraînement et évaluation | `real_dataset.csv` et `results_phase5.json` produits |

## 5. Schéma minimal du dataset réel

Le dataset devrait au minimum contenir un identifiant pseudonyme, l’année et la source du résultat, l’établissement et la filière, la série du baccalauréat, les notes disponibles, les scores RIASEC, les indicateurs d’engagement et le statut final. Les champs d’identité directe — nom, téléphone, adresse et numéro de candidat — doivent rester dans une table séparée et protégée.

| Groupe | Champs recommandés |
|---|---|
| Provenance | `source`, `annee`, `document_source`, `date_validation` |
| Orientation | `etablissement`, `filiere`, `niveau`, `concours` |
| Profil scolaire | `serie_bac`, `note_moyenne`, `notes_matieres`, `region` |
| Diagnostic | `riasec_R`, `riasec_I`, `riasec_A`, `riasec_S`, `riasec_E`, `riasec_C` |
| Engagement | `quiz_completion`, `activite`, `assiduite`, `score_engagement` |
| Cible | `orientation_outcome` avec seulement `ADMIS` ou `RECALE` |

## 6. Ce qui ne doit pas être fait

Il ne faut pas transformer les taux nationaux de réussite en faux profils individuels. Un taux de 42,2 % en série D est un agrégat utile pour la calibration, mais il ne crée pas des lignes `ADMIS` et `RECALE`. De même, les PDF d’appels à candidature de Lomé ne doivent pas être traités comme des résultats d’admission.

Il ne faut pas non plus utiliser les données O*NET, UNESCO, Banque mondiale, OECD ou OIT pour remplacer les résultats togolais. Elles peuvent enrichir le contexte, le diagnostic et la recommandation, mais elles ne répondent pas à l’objectif principal de 5 000 outcomes togolais associés à des profils.

## 7. Conclusion opérationnelle

Le blocage principal reste la collecte des **résultats individuels ou des tableaux complets de candidats par filière**, puis leur liaison avec les profils Activ Education. Les travaux sur Open Data, RGPH, MESR et les sources internationales sont importants, mais secondaires par rapport à ce blocage. La priorité immédiate est donc de produire un premier pilote vérifié dans un ou deux établissements, avec une procédure claire de collecte, de consentement, de pseudonymisation et de validation par conseiller.
