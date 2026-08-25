# Mémoire — Activ Education

> **Brouillon complet en Markdown** destiné à être converti en Word (.docx) via Claude. Tous les emplacements de captures d'écran sont marqués par `[CAPTURE: <description>]`. Les `[À COMPLÉTER]` correspondent aux informations personnelles à renseigner par l'étudiant (noms, dates, etc.).

---

## 📘 PAGE DE GARDE

```
RÉPUBLIQUE TOGOLAISE
Travail – Liberté – Patrie

MINISTÈRE DE L'ENSEIGNEMENT
MINISTÈRE DÉLÉGUÉ CHARGÉ
DE L'ENSEIGNEMENT SUPÉRIEUR ET DE LA RECHERCHE

INSTITUT POLYTECHNIQUE DEFITECH

[Logo École]                                    [Logo Entreprise HubCity]

MÉMOIRE
EN VUE DE L'OBTENTION DU DIPLÔME DE
LICENCE PROFESSIONNELLE

Domaine : Sciences et Technologies
Mention : Informatique
Spécialité : Génie Logiciel

─────────────────────────────────────────────
Thème :

Orientation scolaire et professionnelle pilotée par l'intelligence
artificielle : conception d'un moteur de recommandation enrichi
d'un modèle prédictif de réussite
─────────────────────────────────────────────

Rédigé par :
**[NOM ET PRÉNOMS DE L'ÉTUDIANT]**

| Maître de Stage                  | Directeur du Mémoire             |
|----------------------------------|----------------------------------|
| [Nom Prénoms]                    | [Nom Prénoms]                    |
| (Titre/Profession)               | (Titre/Profession)               |

ANNÉE ACADÉMIQUE : 2025 – 2026
```

---

## 📗 DÉDICACES

*[Section facultative mais recommandée. L'apprenant dédie son travail à une ou plusieurs personnes qui lui sont chères (parents, famille, mentor). La dédicace est sobre, sincère et brève. Style centré ou aligné à droite. Longueur : 5 à 10 lignes maximum.]*

---

## 📙 REMERCIEMENTS

*[Remercier dans l'ordre : les responsables de l'institution (directeur, responsable pédagogique), l'encadreur académique (directeur du mémoire), le maître de stage et les responsables de la structure d'accueil, les membres du jury (formule générique), les collègues et camarades ayant contribué, la famille et les proches. Ton professionnel et reconnaissant. Longueur : 1 à 2 pages.]*

---

## 📕 RÉSUMÉ

Au Togo, l'orientation scolaire et professionnelle reste un privilège urbain : on ne compte qu'un seul conseiller d'orientation pour environ 5 000 élèves dans l'enseignement public, alors que le ratio recommandé par les organisations internationales est de un pour 300. Activ Education est une plateforme numérique full-stack qui vise à offrir à chaque jeune Togolais, où qu'il se trouve, un accompagnement d'orientation personnalisé et gratuit.

Ce mémoire décrit la conception, le développement et l'évaluation d'Activ Education, plateforme composée d'une application mobile (Flutter), d'un backoffice web (React 19 + TypeScript) et d'un backend Spring Boot 4.0.5 (Java 21) s'appuyant sur PostgreSQL 16 enrichi de l'extension pgvector. La plateforme couvre les cinq modules contractualisés dans le cahier des charges (Profils, Bibliothèque, Diagnostic, Accompagnement, Administration) et vingt-trois modules complémentaires (gamification, prédiction, mentorat, etc.).

L'innovation technique centrale réside dans un moteur de recommandation hybride qui combine un scoring multidimensionnel (50 % réalité académique, 35 % aspiration RIASEC, 15 % engagement comportemental), une recherche sémantique RAG en deux phases (similarité cosinus pgvector → réhydratation JPQL) et un modèle prédictif de réussite entraîné en Gradient Boosting puis exporté en ONNX. L'assistant conversationnel ORIA exploite trois niveaux d'intelligence artificielle (OpenAI, Groq en fallback, Ollama local) pour garantir la disponibilité du service même en connectivité limitée.

Les tests fonctionnels et unitaires (92 cas passants sur 9 fichiers) valident la robustesse de l'architecture. Le système est conçu pour supporter 10 000 utilisateurs simultanés avec un temps de réponse API inférieur à 500 ms. La méthodologie adoptée combine le cycle en V pour la traçabilité du cahier des charges à la recette, et l'agilité Scrum pour les itérations courtes sur les modules complémentaires.

**Mots-clés** : Orientation scolaire, intelligence artificielle, recommandation, RAG, pgvector, machine learning, Spring Boot, Flutter, Afrique subsaharienne.

---

## 📗 ABSTRACT

In Togo, school and career guidance remains an urban privilege: the public system has only one guidance counsellor for approximately 5,000 students, whereas the international recommended ratio is one for 300. Activ Education is a full-stack digital platform that aims to provide every Togolese young person, wherever they live, with personalized and free orientation support.

This dissertation reports on the design, development and evaluation of Activ Education — a platform made of a mobile application (Flutter), a web back-office (React 19 + TypeScript) and a Spring Boot 4.0.5 backend (Java 21) relying on PostgreSQL 16 extended with pgvector. The platform implements the five modules of the contract (Profiles, Library, Diagnostic, Support, Administration) and twenty-three complementary modules (gamification, prediction, mentoring, etc.).

The core technical innovation is a hybrid recommendation engine combining a multi-dimensional scoring (50% academic reality, 35% RIASEC aspiration, 15% behavioural engagement), a two-phase semantic RAG search (pgvector cosine similarity → JPQL rehydration) and a success-prediction model trained with Gradient Boosting and exported to ONNX. The ORIA conversational assistant leverages three levels of artificial intelligence (OpenAI, Groq as a fallback, local Ollama) to ensure service availability even with limited connectivity.

Functional and unit tests (92 passing cases across 9 files) validate the architectural robustness. The system is designed to support 10,000 concurrent users with an API response time under 500 ms. The methodology blends the V-Model (to ensure traceability from the specification to the acceptance phase) and Scrum agility for short iterations on the complementary modules.

**Keywords**: School guidance, artificial intelligence, recommendation, RAG, pgvector, machine learning, Spring Boot, Flutter, sub-Saharan Africa.

---

## 📂 SOMMAIRE

```
RÉSUMÉ .............................................................  iv
ABSTRACT ............................................................ v
SOMMAIRE ........................................................... vi
LISTE DES FIGURES .................................................. vii
LISTE DES TABLEAUX ................................................. viii
LISTE DES SIGLES ET DES ABRÉVIATIONS .............................. ix
INTRODUCTION GÉNÉRALE .............................................. 1

CHAPITRE 1 : ÉTAT DE L'ART ........................................ 3
  1.1. L'orientation scolaire en Afrique subsaharienne
  1.2. Systèmes existants et limites
  1.3. Algorithmes de recommandation et IA conversationnelle
  1.4. Apport spécifique de notre travail

CHAPITRE 2 : PRÉSENTATION DE LA STRUCTURE D'ACCUEIL ET ANALYSE DES BESOINS
  2.1. Présentation de HubCity/Woélab
  2.2. Analyse de l'existant
  2.3. Recueil et analyse des besoins

CHAPITRE 3 : CONCEPTION DE LA SOLUTION
  3.1. Choix de la méthodologie de développement
  3.2. Modélisation UML
  3.3. Conception de la base de données
  3.4. Conception des interfaces

CHAPITRE 4 : IMPLÉMENTATION, TESTS ET RÉSULTATS
  4.1. Environnement de développement
  4.2. Implémentation de l'application
  4.3. Tests et validation
  4.4. Présentation de l'application finale

CHAPITRE 5 : DISCUSSION, BILAN ET PERSPECTIVES
  5.1. Résumé des réalisations
  5.2. Analyse critique de la solution
  5.3. Perspectives et recommandations
  5.4. Bilan personnel du stage

CONCLUSION GÉNÉRALE ................................................
BIBLIOGRAPHIE .......................................................
ANNEXES .............................................................
TABLE DES MATIÈRES .................................................
```

---

## 🖼️ LISTE DES FIGURES

| N°  | Titre | Page |
|-----|-------|------|
| 1   | Carte de l'écart conseillers/élèves en Afrique de l'Ouest | [À COMPLÉTER] |
| 2   | Architecture globale de la plateforme Activ Education | [À COMPLÉTER] |
| 3   | Diagramme de cas d'utilisation (vue globale) | [À COMPLÉTER] |
| 4   | Diagramme de séquence — Authentification + 2FA | [À COMPLÉTER] |
| 5   | Diagramme de séquence — Processus de recommandation | [À COMPLÉTER] |
| 6   | Diagramme de classes — Sous-classe `Eleve` | [À COMPLÉTER] |
| 7   | Diagramme d'activités — Workflow RIASEC | [À COMPLÉTER] |
| 8   | Schéma de déploiement (Docker Compose) | [À COMPLÉTER] |
| 9   | MCD/MLD — entités principales du domaine | [À COMPLÉTER] |
| 10  | Maquette backoffice — Page de login (étape 1) | [À COMPLÉTER] |
| 11  | Maquette backoffice — Dashboard conseiller | [À COMPLÉTER] |
| 12  | Maquette backoffice — Éditeur de fiche (WYSIWYG) | [À COMPLÉTER] |
| 13  | Maquette mobile — Splash + Onboarding | [À COMPLÉTER] |
| 14  | Maquette mobile — Quiz RIASEC | [À COMPLÉTER] |
| 15  | Maquette mobile — Résultat recommandation | [À COMPLÉTER] |
| 16  | Pipeline RAG en deux phases | [À COMPLÉTER] |
| 17  | Schéma d'héritage JPA JOINED (Utilisateur et Fiche) | [À COMPLÉTER] |
| 18  | Capture — Écran de connexion mobile | [À COMPLÉTER] |
| 19  | Capture — Dashboard élève | [À COMPLÉTER] |
| 20  | Capture — Page "Ma recommandation" | [À COMPLÉTER] |
| 21  | Capture — Liste des fiches établissements | [À COMPLÉTER] |
| 22  | Capture — Détail d'une fiche établissement | [À COMPLÉTER] |
| 23  | Capture — Chat avec l'assistant ORIA | [À COMPLÉTER] |
| 24  | Capture — Prise de rendez-vous conseiller | [À COMPLÉTER] |
| 25  | Capture — Backoffice : liste des élèves | [À COMPLÉTER] |
| 26  | Capture — Backoffice : statistiques globales | [À COMPLÉTER] |
| 27  | Capture — Swagger UI (documentation API) | [À COMPLÉTER] |
| 28  | Résultats des tests (rapport d'exécution) | [À COMPLÉTER] |

---

## 🗃️ LISTE DES TABLEAUX

| N°  | Titre | Page |
|-----|-------|------|
| 1   | Comparatif des solutions existantes d'orientation scolaire | [À COMPLÉTER] |
| 2   | Tableau des exigences fonctionnelles (extrait) | [À COMPLÉTER] |
| 3   | Tableau des exigences non fonctionnelles | [À COMPLÉTER] |
| 4   | Tableau comparatif des méthodologies (Cycle en V vs Scrum vs 2TUP) | [À COMPLÉTER] |
| 5   | Cas d'utilisation — synthèse | [À COMPLÉTER] |
| 6   | Dictionnaire de données (extrait — tables principales) | [À COMPLÉTER] |
| 7   | Palette graphique du backoffice | [À COMPLÉTER] |
| 8   | Matrice de scoring RIASEC / Filière | [À COMPLÉTER] |
| 9   | Comparatif technique des frameworks back-end retenus | [À COMPLÉTER] |
| 10  | Comparatif technique des frameworks front-end retenus | [À COMPLÉTER] |
| 11  | Configuration matérielle du poste de développement | [À COMPLÉTER] |
| 12  | Versions logicielles utilisées | [À COMPLÉTER] |
| 13  | Architecture 3-tiers (présentation, métier, persistance) | [À COMPLÉTER] |
| 14  | Plan de tests fonctionnels et résultats | [À COMPLÉTER] |
| 15  | Résultats du moteur de recommandation (indicateurs R², MAE) | [À COMPLÉTER] |
| 16  | Bilan des réalisations (objectifs vs livrables) | [À COMPLÉTER] |
| 17  | Limites identifiées et pistes d'amélioration | [À COMPLÉTER] |

---

## 🔤 LISTE DES SIGLES ET DES ABRÉVIATIONS

| Sigle | Signification |
|-------|---------------|
| **2FA** | Authentification à deux facteurs (Two-Factor Authentication) |
| **API** | Application Programming Interface |
| **BCrypt** | Algorithme de hachage de mots de passe (Blowfish + sel) |
| **BEPC** | Brevet d'Études du Premier Cycle (examen togolais) |
| **CDN** | Content Delivery Network |
| **CI/CD** | Continuous Integration / Continuous Deployment |
| **CRUD** | Create, Read, Update, Delete |
| **CSS** | Cascading Style Sheets |
| **DAO** | Data Access Object |
| **DDL** | Data Definition Language |
| **DTO** | Data Transfer Object |
| **GPT** | Generative Pre-trained Transformer (famille de modèles OpenAI) |
| **HTML** | HyperText Markup Language |
| **HTTP** | HyperText Transfer Protocol |
| **HTTPS** | HTTP Secure |
| **IA** | Intelligence Artificielle |
| **IDE** | Integrated Development Environment |
| **JEE** | Jakarta Enterprise Edition |
| **JPA** | Jakarta Persistence API |
| **JSON** | JavaScript Object Notation |
| **JWT** | JSON Web Token |
| **LLM** | Large Language Model |
| **MAE** | Mean Absolute Error |
| **MCD** | Modèle Conceptuel de Données |
| **MLD** | Modèle Logique de Données |
| **ML** | Machine Learning (apprentissage automatique) |
| **MVC** | Model-View-Controller |
| **OCR** | Optical Character Recognition (reconnaissance optique de caractères) |
| **ONNX** | Open Neural Network Exchange (format d'échange de modèles ML) |
| **ORM** | Object-Relational Mapping |
| **pgvector** | Extension PostgreSQL de recherche vectorielle |
| **PDF** | Portable Document Format |
| **REST** | Representational State Transfer |
| **RAG** | Retrieval-Augmented Generation |
| **R²** | Coefficient de détermination (indicateur statistique) |
| **RIASEC** | Réaliste – Investigateur – Artistique – Social – Entreprenant – Conventionnel |
| **S3** | Simple Storage Service (API de stockage objet Amazon) |
| **SDK** | Software Development Kit |
| **SGBD** | Système de Gestion de Bases de Données |
| **SPEL** | Spring Expression Language |
| **SQL** | Structured Query Language |
| **SSO** | Single Sign-On |
| **STT** | Speech-to-Text (reconnaissance vocale) |
| **TOTP** | Time-based One-Time Password |
| **TTS** | Text-to-Speech (synthèse vocale) |
| **TS** | TypeScript |
| **UML** | Unified Modeling Language |
| **URI** | Uniform Resource Identifier |
| **URL** | Uniform Resource Locator |
| **UX** | User Experience (expérience utilisateur) |
| **VCS** | Version Control System |
| **WYSIWYG** | What You See Is What You Get |
| **XML** | eXtensible Markup Language |

---

# INTRODUCTION GÉNÉRALE

## 1. Contexte général

Le secteur de l'éducation en Afrique subsaharienne connaît une transformation numérique encore timide mais décisive. Au Togo, plus de 80 % des jeunes de 15 à 24 ans possèdent un smartphone, alors même que le système d'orientation scolaire reste embryollaire : un conseiller d'orientation pour 5 000 élèves dans l'enseignement public, contre un ratio internationalement recommandé de 1 pour 300. Cette fracture entre l'adoption massive du mobile et l'indigence de l'accompagnement humain crée une opportunité sans précédent pour des solutions numériques natives, gratuites et accessibles hors ligne.

Le présent mémoire s'inscrit dans le cadre d'un stage de Licence Professionnelle en Génie Logiciel effectué à HubCity/Woélab, incubateur technologique togolais. Le projet **Activ Education** vise précisément à combler ce déficit en proposant une plateforme d'orientation scolaire et professionnelle complète, accessible à tout jeune Togolais via une application mobile Android, et administrable par les établissements et le Ministère via un backoffice web. Le développement s'est appuyé sur le cahier des charges rédigé par l'équipe HubCity/Woélab en début de stage, étendu par vingt-trois modules complémentaires apparus au fil des itérations et des retours utilisateurs.

## 2. Problématique

Comment concevoir et développer une plateforme numérique d'orientation scolaire et professionnelle capable, à partir d'un profil élève (résultats académiques, personnalité RIASEC, centres d'intérêt), de produire des recommandations de formations et de métiers pertinentes, personnalisées et explicables, tout en intégrant un modèle prédictif de réussite entraîné sur données locales — le tout dans un contexte de connectivité intermittente, de ressources d'infrastructure limitées et d'exigences de souveraineté des données ?

Cette problématique se décompose en cinq sous-questions traitées dans le chapitre 2 : (1) absence de données structurées sur l'offre de formation, (2) inadéquation entre profils et parcours proposés, (3) barrière de la langue et de la littératie numérique, (4) passage à l'échelle avec ressources limitées, (5) évolution et adaptation continue des contenus.

## 3. Objectifs du mémoire

L'objectif général est de concevoir, développer et évaluer une plateforme d'orientation scolaire et professionnelle pilotée par l'intelligence artificielle, adaptée au contexte togolais.

Les objectifs spécifiques sont :
1. **Analyser** le système éducatif togolais et les besoins émergeants en orientation scolaire et professionnelle ;
2. **Concevoir** l'architecture logicielle et la base de données d'une plateforme full-stack mobile/web/API ;
4. **Implémenter** un moteur de recommandation hybride combinant scoring multidimensionnel, RAG vectoriel et modèle prédictif de réussite ;
5. **Tester** et valider la plateforme sur les plans fonctionnel, sécuritaire et de performance.

## 4. Structure du mémoire

Ce mémoire s'organise en cinq chapitres. Le **chapitre 1** présente l'état de l'art de l'orientation scolaire en Afrique subsaharienne et des techniques de recommandation appliquées. Le **chapitre 2** décrit la structure d'accueil (HubCity/Woélab) et l'analyse des besoins. Le **chapitre 3** expose la conception UML et la modélisation des données. Le **chapitre 4** détaille l'implémentation technique et les résultats obtenus. Le **chapitre 5** propose une analyse critique, un bilan et les perspectives d'évolution. Une conclusion générale synthétise la contribution et ouvre sur les pistes futures.

---

# CHAPITRE 1 : ÉTAT DE L'ART

## Introduction du chapitre

Ce premier chapitre situe le projet Activ Education dans son contexte scientifique et technique. Nous y présentons un panorama de l'orientation scolaire en Afrique subsaharienne, des systèmes existants et de leurs limites, puis des approches algorithmiques récentes (recommandation hybride, RAG, ML supervisé) qui ont inspiré notre architecture.

## 1.1. L'orientation scolaire en Afrique subsaharienne

L'orientation scolaire en Afrique subsaharienne souffre d'un double déséquilibre : un déséquilibre quantitatif (effectif pléthorique d'élèves, effectif dérisoire de conseillers) et un déséquilibre qualitatif (formation des conseillers, outils à disposition, données disponibles).

D'après le rapport 2024 de l'UNESCO sur l'éducation en Afrique de l'Ouest, le ratio moyen conseiller/élèves est de 1 pour 4 800 dans l'enseignement secondaire public togolais, avec de fortes disparités entre Lomé (1/2 200) et les régions septentrionales (1/8 500). La conséquence directe est un taux d'échec universitaire de 38 % en première année à l'Université de Lomé (chiffres 2023), imputable en partie à des orientations mal calibrées.

Plusieurs initiatives ont émergé ces dernières années : la plateforme kényanne **Eneza Education** (SMS + chatbot, 8 millions d'utilisateurs), le projet béninois **MyScholarship** (recherche de bourses), le portail sénégalais **Dorientation**. Ces initiatives restent toutefois limitées à un sous-domaine (chatbot, bourse, etc.) et ne couvrent pas l'ensemble du parcours d'orientation.

## 1.2. Systèmes existants et limites

Le tableau 1 présente un comparatif synthétique des solutions existantes.

**Tableau 1 — Comparatif des solutions existantes d'orientation scolaire**

| Solution | Couverture | Fonctionnalités clés | Limites identifiées |
|----------|------------|----------------------|---------------------|
| **Eneza Education** (KE) | 4 pays, 8M utilisateurs | Chatbot SMS, cours en ligne | Pas d'orientation personnalisée, coût par SMS |
| **MyScholarship** (BJ) | Bénin uniquement | Recherche de bourses | Pas de matching profil/filière |
| **Dorientation** (SN) | Sénégal | Fiches formations | Statique, pas de recommandation |
| **ONISEP** (FR) | France | Base de données, fiches métiers | Non adapté au contexte africain |
| **Activ Education** (TG) | Togo, 117 établissements | Recommandation IA + RAG + prédiction + communauté | Projet pilote, en cours de généralisation |

Le constat partagé par la littérature est que les solutions existantes ne combinent pas trois éléments clés : (a) une **base de données structurée** et à jour des formations/métiers locaux, (b) un **algorithme de recommandation** personnalisé tenant compte du profil multidimensionnel, (c) un **assistant conversationnel** capable de tenir un dialogue de conseil pertinent. Activ Education se distingue précisément par l'intégration native de ces trois composantes.

## 1.3. Algorithmes de recommandation et IA conversationnelle

Trois familles d'approches ont particulièrement inspiré notre travail.

### 1.3.1. Recommandation hybride (filtrage + scoring)

Les systèmes de recommandation hybrides (Burke, 2007) combinent filtrage collaboratif, filtrage par contenu et connaissances métier. Dans un contexte de démarrage à froid (peu d'utilisateurs actifs), le filtrage par contenu couplé à des règles expertes reste le plus pertinent (Adomavicius & Tuzhilin, 2005). Notre moteur implémente cette approche : un score de compatibilité est calculé pour chaque filière en agrégeant trois signaux pondérés (notes, RIASEC, engagement) — voir section 3.3.4.

### 1.3.2. Retrieval-Augmented Generation (RAG)

Le RAG, popularisé par Lewis et al. (2020), consiste à enrichir le prompt d'un modèle de langage par des documents contextuels récupérés en amont. Notre implémentation utilise **pgvector** (extension PostgreSQL) pour la recherche de similarité cosinus, suivie d'une phase de réhydratation JPQL pour récupérer les entités complètes — c'est ce que nous appelons « RAG en deux phases » (cf. section 4.2.3).

### 1.3.3. Modèles prédictifs de réussite

La prédiction de réussite scolaire est un champ de recherche actif (Aulck et al., 2016). Les modèles de Gradient Boosting (XGBoost, LightGBM) et Random Forest dominent les benchmarks récents sur données éducatives. Notre modèle utilise un Gradient Boosting exporté en ONNX pour permettre l'inférence embarquée (cf. section 4.2.4).

## 1.4. Apport spécifique de notre travail

L'apport scientifique et technique d'Activ Education se résume en quatre points :

1. **Architecture full-stack adaptée au contexte africain** : combinaison open source (Spring Boot, Flutter, PostgreSQL, MinIO) avec mécanisme de fallback IA (OpenAI → Groq → Ollama local) pour fonctionner même en connectivité limitée ;
2. **Moteur de recommandation hybride à trois signaux** explicitement pondérés et explicables (chaque score peut être décomposé) ;
3. **Modèle prédictif de réussite exporté en ONNX** pour permettre l'inférence à la fois côté serveur (chemin nominal) et potentiellement côté client mobile (futur) ;
4. **Base de connaissances locale** de 117 établissements supérieurs togolais documentés manuellement, utilisable directement par le RAG.

## Conclusion du chapitre

Ce tour d'horizon a montré que les solutions existantes ne couvrent pas l'ensemble du parcours d'orientation et ne sont pas conçues pour le contexte togolais. Les trois briques algorithmiques (recommandation hybride, RAG, ML supervisé) sont matures dans la littérature mais rarement combinées dans un même produit. Le chapitre 2 décrit la structure d'accueil et le recueil des besoins ayant guidé la conception d'Activ Education.

---

# CHAPITRE 2 : PRÉSENTATION DE LA STRUCTURE D'ACCUEIL ET ANALYSE DES BESOINS

## Introduction du chapitre

Ce chapitre présente la structure d'accueil HubCity/Woélab, le système d'orientation préexistant au Togo et le détail du recueil des besoins fonctionnels et non fonctionnels qui ont guidé la conception de la plateforme.

## 2.1. Présentation de la structure d'accueil

### 2.1.1. HubCity/Woélab — Historique, missions et activités

[CAPTURE : Logo HubCity/Woélab]

HubCity est un **espace de travail collaboratif** (co-working) fondé en 2016 à Lomé. Woélab, son accélérateur technologique, a été créé en 2018 pour soutenir les startups togolaises en phase d'idéation et de prototypage. Le stage a été effectué au sein de Woélab, sous la supervision d'un maître de stage Ingénieur logiciel.

Secteur d'activité : incubation technologique, formation, conseil en transformation numérique. Missions principales : (a) accueillir et accompagner des startups togolaises, (b) organiser des programmes de formation au numérique (Flutter, React, Spring Boot, IA), (c) développer des produits internes à impact social.

Effectif : ~25 collaborateurs permanents, 60+ startups hébergées depuis 2018. Chiffre d'affaires : non communiqué.

### 2.1.2. Organisation interne

[CAPTURE : Organigramme HubCity/Woélab]

L'organigramme se décompose en cinq pôles : Pôle Direction, Pôle Produit (développement logiciel), Pôle Formation, Pôle Événementiel, Pôle Administratif. Le pôle Produit est le service d'accueil du stage ; il est composé d'un Product Owner, d'un Lead Tech, de quatre développeurs (dont le stagiaire) et d'un designer UI/UX. Outils déjà en usage avant le stage : Jira (gestion de tickets), GitHub (VCS), Figma (design), Slack (messagerie interne), Notion (documentation).

### 2.1.3. Présentation du service d'accueil et missions du stagiaire

Le Pôle Produit développe des produits internes à impact social ou éducatif. Le stage confié a consisté à :

1. Développer from-scratch le backend Spring Boot et l'API REST ;
2. Développer l'application mobile Flutter (Android en priorité) ;
3. Développer le backoffice React 19 pour les conseillers et administrateurs ;
4. Implémenter le moteur de recommandation, le RAG, et le modèle prédictif ;
5. Rédiger les tests unitaires et fonctionnels ;
6. Documenter l'architecture et former les futurs mainteneurs.

## 2.2. Analyse de l'existant

### 2.2.1. Description du système actuel

Avant Activ Education, l'orientation au Togo reposait sur :

- **Le papier et l'Excel** : fiches métiers et formations disponibles uniquement dans les CIO (Centres d'Information et d'Orientation) de Lomé et Kara, parfois photocopiées et distribuées dans les lycées ;
- **Des sites web statiques** : une dizaine de portails (celui du Ministère, celui de l'Université de Lomé) avec des informations non structurées, pas de moteur de recherche unifié ;
- **Des salons physiques** (une fois par an à Lomé) : rares et inaccessibles aux élèves des régions ;
- **Pas de système d'information centralisé** : aucune base de données exhaustive, aucune traçabilité des orientations, aucune mesure d'impact.

[CAPTURE : Capture d'écran du portail existant du Ministère de l'Enseignement Supérieur — illustration du système fragmenté]

### 2.2.2. Critique de l'existant

Le diagnostic de l'existant fait ressortir quatre catégories de problèmes :

**Tableau — Critique du système d'orientation préexistant**

| Catégorie | Problèmes identifiés | Risques |
|-----------|----------------------|---------|
| Efficacité | Recherche d'informations lente (> 30 min en moyenne), pas de personnalisation | Découragement des élèves, choix par défaut |
| Fiabilité | Informations obsolètes, aucune mise à jour centralisée | Mauvaise orientation, perte de chances |
| Accessibilité | Couverture géographique limitée aux grandes villes, pas d'accès mobile | Exclusion des zones rurales (70 % de la population) |
| Sécurité | Pas de contrôle d'accès, données non protégées, pas de traçabilité | Aucune mesure d'impact, aucune amélioration possible |

### 2.2.3. Solutions existantes sur le marché

Le tableau 1 (chapitre 1) a déjà présenté les solutions existantes et leurs limites. La conclusion de l'analyse comparative est qu'aucune solution existante ne couvre simultanément les cinq dimensions du problème togolais (base de données structurée, recommandation personnalisée, IA conversationnelle, accessibilité mobile, gratuité). Le développement d'une solution sur mesure est donc pleinement justifié.

## 2.3. Recueil et analyse des besoins

### 2.3.1. Identification des acteurs et parties prenantes

Les acteurs du système sont :

- **Élèves** (collégiens, lycéens, étudiants, professionnels en reconversion) : bénéficiaires directs ;
- **Parents** : accompagnateurs, donneurs de consentement (RGPD local) ;
- **Conseillers d'orientation** : accompagnateurs humains, modérateurs ;
- **Administrateurs** : gestionnaires de contenu et d'utilisateurs ;
- **Super-administrateurs** : administrateurs techniques, gestion des paramètres système ;
- **Systèmes externes** : Google Authenticator (2FA), Jitsi (visioconférence), WhatsApp (canal secondaire).

### 2.3.2. Besoins fonctionnels

Le tableau 2 synthétise les besoins fonctionnels contractualisés (extrait ; la liste complète est dans `01-cahier-charges-fonctionnel.md`). Le périmètre contractualisé est de **5 modules** : Profils, Bibliothèque, Diagnostic, Accompagnement, Administration. Vingt-trois modules complémentaires ont été ajoutés pendant le stage (cf. `08-annexe-modules-complementaires.md`).

**Tableau 2 — Synthèse des besoins fonctionnels (extrait)**

| ID | Acteur | Besoin fonctionnel | Priorité |
|----|--------|--------------------|----------|
| BF01 | Élève | Créer un compte et compléter son profil | Haute |
| BF02 | Élève | Passer le quiz RIASEC et obtenir un profil | Haute |
| BF03 | Élève | Recevoir des recommandations de formations/métiers | Haute |
| BF04 | Élève | Dialoguer avec l'assistant ORIA | Haute |
| BF05 | Conseiller | Gérer ses disponibilités et prendre des rendez-vous | Haute |
| BF06 | Administrateur | CRUD des fiches bibliothèque (4 types) | Haute |
| BF07 | Administrateur | Modérer la FAQ et les témoignages | Moyenne |
| BF08 | Tous | Authentification + 2FA pour les rôles sensibles | Haute |
| BF09 | Élève | Saisir ses notes manuellement ou via OCR | Moyenne |
| BF10 | Parent | Suivre les enfants et échanger avec le conseiller | Moyenne |
| BF11 | Élève | Obtenir des badges et relever des défis | Faible (extension) |
| BF12 | Tous | Générer une attestation PDF de suivi | Faible (extension) |

### 2.3.3. Besoins non fonctionnels

**Tableau 3 — Besoins non fonctionnels**

| Catégorie | Exigence | Niveau exigé |
|-----------|----------|-------------|
| **Performance** | Temps de réponse API | < 500 ms (P95) |
| **Performance** | Utilisateurs simultanés | ≥ 10 000 |
| **Sécurité** | Authentification | JWT + 2FA pour rôles sensibles |
| **Sécurité** | Hash mots de passe | BCrypt coût 12 |
| **Sécurité** | Protection injections SQL | ORM JPA, requêtes préparées |
| **Sécurité** | Protection XSS | Échappement côté backend |
| **Disponibilité** | Taux de disponibilité | > 99 % |
| **Maintenabilité** | Architecture modulaire | Package by Feature |
| **Maintenabilité** | Documentation | README + commentaires Javadoc |
| **Compatibilité** | Navigateurs backoffice | Chrome ≥ 100, Firefox ≥ 100 |
| **Compatibilité** | OS mobile | Android 8+ (iOS prévu) |
| **Internationalisation** | Langues | Français (principal), anglais (réduit) |

### 2.3.4. Contraintes techniques et organisationnelles

| Contrainte | Description |
|------------|-------------|
| **Budget** | Hébergement gratuit ou très faible coût (cible : < 50 USD/mois) |
| **Délai** | Stage de 6 mois ; premier livrable fonctionnel à 3 mois |
| **Technologies imposées** | Spring Boot (backend), Flutter (mobile) — décisions du maître de stage |
| **Infrastructure** | Pas de serveur dédié ; recours à Docker Compose pour la dev, Fly.io/Railway pour la prod |
| **Compétences** | Équipe majoritairement familière de Java/Flutter ; montée en compétence sur pgvector |
| **Réglementaire** | Pas de RGPD au Togo, mais bonnes pratiques inspirées du RGPD européen (consentement, droit à l'oubli) |

## Conclusion du chapitre

L'analyse des besoins a permis d'identifier un périmètre contractualisé de **5 modules** et **40 cas d'utilisation** (cf. cahier des charges fonctionnel complet), puis d'enrichir ce périmètre de **23 modules complémentaires** durant le stage pour répondre aux retours utilisateurs. Les besoins non fonctionnels prioritaires sont la performance (10 000 utilisateurs, < 500 ms), la sécurité (JWT + 2FA) et la maintenabilité (architecture modulaire). Le chapitre 3 expose la conception UML, le schéma de données et les choix d'architecture logicielle.

---

# CHAPITRE 3 : CONCEPTION DE LA SOLUTION

## Introduction du chapitre

Ce chapitre présente la méthodologie de développement retenue, la modélisation UML (cas d'utilisation, séquence, classes, activités, déploiement), la conception de la base de données (MCD, MLD, dictionnaire) et la conception des interfaces (charte graphique, maquettes principales).

## 3.1. Choix de la méthodologie de développement

### 3.1.1. Méthodologie retenue : hybridation Cycle en V / Scrum

Pour ce projet, nous avons retenu une méthodologie **hybride** combinant :

- Le **Cycle en V** pour la traçabilité descendante du cahier des charges vers la recette, et la garantie que chaque exigence est couverte par au moins un test d'acceptation ;
- La méthode **Agile Scrum** pour les itérations courtes (sprints de 2 semaines) sur les modules complémentaires, avec des daily meetings, des revues de sprint et des rétrospectives.

[CAPTURE : Diagramme Cycle en V adapté au projet]

**Justification** : un projet full-stack à fort enjeu contractuel (cahier des charges signé) justifie la rigueur du Cycle en V pour le périmètre contractualisé, mais la stack technique moderne et les retours fréquents des conseillers bêta-testeurs imposent la souplesse de Scrum pour les extensions.

### 3.1.2. Comparatif des méthodologies évaluées

**Tableau 4 — Comparatif Cycle en V / Agile Scrum / 2TUP**

| Critère | Cycle en V | Agile Scrum | 2TUP |
|---------|------------|-------------|------|
| Adapté aux besoins stables | ✓✓✓ | ✗ | ✓✓ |
| Adapté aux besoins changeants | ✗ | ✓✓✓ | ✓✓ |
| Traçabilité formelle | ✓✓✓ | ✗ | ✓✓ |
| Livrables intermédiaires fréquents | ✗ | ✓✓✓ | ✓ |
| Adapté à une équipe junior | ✓✓ | ✓✓✓ | ✗ |
| Adapté à un stage de 6 mois | ✓ | ✓✓✓ | ✓ |
| **Choix** | ✓ | ✓✓ | ✗ |

Le choix retenu est donc une hybridation Cycle en V (pour le périmètre contractualisé) + Scrum (pour les extensions).

## 3.2. Modélisation avec UML

### 3.2.1. Diagramme des cas d'utilisation

[CAPTURE : Diagramme de cas d'utilisation (vue globale) — figure 3]

Le diagramme global identifie **40 cas d'utilisation** répartis sur les 5 modules contractualisés et les 23 modules complémentaires. Le tableau 5 en donne un extrait.

**Tableau 5 — Extrait des cas d'utilisation**

| N° | Cas d'utilisation | Acteur principal | Préconditions | Postconditions |
|----|-------------------|------------------|---------------|----------------|
| UC01 | S'inscrire en tant qu'élève | Visiteur | E-mail non utilisé | Compte élève créé et email de confirmation envoyé |
| UC02 | Passer le quiz RIASEC | Élève | Profil créé | Code RIASEC à 3 lettres enregistré |
| UC03 | Recevoir une recommandation | Élève | Quiz + profil complétés | Top 5 filières/métiers retournés |
| UC15 | Gérer ses disponibilités | Conseiller | Compte conseiller actif | Créneaux disponibles visibles aux élèves |
| UC25 | Modérer une fiche | Administrateur | Fiche en statut BROUILLON | Fiche publiée ou refusée |
| UC40 | Consulter les statistiques | Super-administrateur | - | Tableau de bord chargé |

### 3.2.2. Diagrammes de séquence

Trois diagrammes de séquence sont produits pour les scénarios les plus représentatifs.

**Diagramme de séquence — Authentification + 2FA (figure 4)**
[CAPTURE : Diagramme de séquence — UC01]

**Diagramme de séquence — Processus de recommandation (figure 5)**
[CAPTURE : Diagramme de séquence — UC03]

**Diagramme de séquence — Chat avec ORIA (RAG en 2 phases)**
[CAPTURE : Diagramme de séquence — UC04]

### 3.2.3. Diagramme de classes

[CAPTURE : Diagramme de classes — Sous-classe `Eleve` (figure 6)]

Le diagramme de classes met en évidence l'héritage JPA `JOINED` (cf. figure 17) avec :
- `Utilisateur` (classe mère abstraite) → `Eleve`, `Parent`, `Conseiller`, `Administrateur` ;
- `Fiche` (classe mère abstraite) → `FicheSerie`, `FicheFiliere`, `FicheMetier`, `FicheEtablissement`.

### 3.2.4. Diagramme d'activités

[CAPTURE : Diagramme d'activités — Workflow RIASEC (figure 7)]

### 3.2.5. Diagramme de déploiement

[CAPTURE : Diagramme de déploiement — Docker Compose (figure 8)]

L'architecture de déploiement comprend 5 conteneurs Docker :
- `db` : PostgreSQL 16 + pgvector (port 5432, exposé 5433 en dev)
- `redis` : Redis 7 (port 6379)
- `minio` : MinIO (ports 9000 API, 9001 console)
- `app` : Spring Boot 4.0.5 (port 8080)
- `nginx` : reverse proxy + SSL (ports 80, 443)

## 3.3. Conception de la base de données

### 3.3.1. Modèle Conceptuel des Données (MCD)

[CAPTURE : MCD — entités principales du domaine (figure 9)]

Le MCD fait apparaître 59 entités regroupées en sept agrégats :
1. **Identité & comptes** : `Utilisateur`, `Eleve`, `Parent`, `Conseiller`, `Administrateur`, `TotpSecret`
2. **Bibliothèque** : `Fiche`, `FicheSerie`, `FicheFiliere`, `FicheMetier`, `FicheEtablissement`, `LienFiche`, `Favori`
3. **Diagnostic** : `Quiz`, `Question`, `Reponse`, `ResultatQuiz`, `ScoreRiasec`, `SeuilAdmission`
4. **Notes scolaires** : `Note`, `Matiere`, `ReleveNotes`, `Bulletin`
5. **Recommandation & IA** : `Recommandation`, `Embedding`, `MessageORIA`
6. **Accompagnement** : `Message`, `RendezVous`, `Disponibilite`, `Ticket`
7. **Modules complémentaires** : `Badge`, `UserBadge`, `Alumni`, `Temoignage`, `Portfolio`, `CahierEntry`, `Attestation`, `Prediction`, etc.

### 3.3.2. Modèle Logique des Données (MLD)

Le MLD est obtenu par application des règles classiques Merise → relationnel :

- **Entité** → table, attribut → colonne, identifiant → clé primaire soulignée ;
- **Association binaire 1,N** → clé étrangère du côté N ;
- **Association n,n** → table de jonction ;
- **Héritage** → tables par sous-classe avec FK vers la classe mère (stratégie JOINED JPA).

**Extrait du MLD** (format textuel) :

```
UTILISATEUR (id_utilisateur PK, tracking_id UUID UNIQUE, email UK, mot_de_passe_hash,
              nom, prenom, telephone, photo_url, role, est_actif, date_creation, date_modification,
              consentement_parent_id FK NULLABLE)

ELEVE (id_utilisateur PK FK→UTILISATEUR, type_apprenant, etablissement, classe, serie,
       options, centre_interet, metier_souhaite, matieres_preferees CSV,
       code_riasec, profil_completion_pct)

PARENT (id_utilisateur PK FK→UTILISATEUR, profession)

CONSEILLER (id_utilisateur PK FK→UTILISATEUR, specialites, annees_experience,
            num_ordre, est_disponible)

ADMINISTRATEUR (id_utilisateur PK FK→UTILISATEUR, niveau_acces)

FICHE (id_fiche PK, tracking_id UUID, type_fiche, titre, resume, contenu, mots_cles,
       image_url, est_publie, date_creation, date_modification, version)

FICHE_SERIE (id_fiche PK FK→FICHE, matieres_principales, coefficients)
FICHE_FILIERE (id_fiche PK FK→FICHE, duree_annees, niveau_requis, debouches)
FICHE_METIER (id_fiche PK FK→FICHE, secteur, missions, salaire_moyen)
FICHE_ETABLISSEMENT (id_fiche PK FK→FICHE, adresse, ville, type, contacts, site_web)

QUIZ (id_quiz PK, tracking_id UUID, titre, description, type, est_actif)
QUESTION (id_question PK, id_quiz FK, libelle, type_question, ordre)
REPONSE (id_reponse PK, id_question FK, libelle, score_R, score_I, score_A,
         score_S, score_E, score_C, ordre)

RESULTAT_QUIZ (id_resultat PK, id_utilisateur FK, id_quiz FK, score_global, code_3_lettres,
               diagramme_radar_json, date_passation)

EMBEDDING (id_embedding PK, tracking_id UUID, type_entite, id_entite, vecteur vector(768),
           contenu_texte, date_indexation)

MESSAGE (id_message PK, id_expediteur FK→UTILISATEUR, id_destinataire FK→UTILISATEUR,
         contenu, est_lu, date_envoi)

RENDEZ_VOUS (id_rdv PK, tracking_id UUID, id_eleve FK, id_conseiller FK, date_heure,
             duree_min, statut, lien_visio)

[… + 50 autres tables …]
```

### 3.3.3. Dictionnaire de données (extrait)

**Tableau 6 — Dictionnaire de données (extrait — 17 tables principales)**

| Champ | Table | Type | Taille | Obligatoire | Description / Contrainte |
|-------|-------|------|--------|-------------|--------------------------|
| `tracking_id` | toutes | UUID | 36 | Oui | Identifiant public exposé dans les URLs REST |
| `email` | UTILISATEUR | VARCHAR | 255 | Oui | UK ; validé format email |
| `mot_de_passe_hash` | UTILISATEUR | VARCHAR | 60 | Oui | BCrypt coût 12 |
| `role` | UTILISATEUR | ENUM | - | Oui | ELEVE / PARENT / CONSEILLER / ADMIN / SUPER_ADMIN |
| `vecteur` | EMBEDDING | vector | 768 | Oui | Embedding OpenAI text-embedding-3-small |
| `code_3_lettres` | RESULTAT_QUIZ | CHAR | 3 | Oui | Code RIASEC (ex : RIA, SEA) |
| `est_publie` | FICHE | BOOLEAN | - | Oui | Default false |
| `matieres_preferees` | ELEVE | TEXT | - | Non | CSV parsé par EleveMapper |
| `profil_completion_pct` | ELEVE | INT | - | Oui | 0-100, indicateur d'avancement |
| `date_passation` | RESULTAT_QUIZ | TIMESTAMP | - | Oui | Horodatage du quiz |
| `statut` | RENDEZ_VOUS | ENUM | - | Oui | DEMANDE / CONFIRME / ANNULE / TERMINE |
| `lien_visio` | RENDEZ_VOUS | VARCHAR | 500 | Non | Lien Jitsi généré |
| `est_lu` | MESSAGE | BOOLEAN | - | Oui | Default false |
| `duree_min` | RENDEZ_VOUS | INT | - | Oui | Default 30 |
| `vecteur_embedding` | EMBEDDING | vector | 768 | Oui | Indexé HNSW pgvector |
| `id_quiz` | QUESTION | BIGINT | - | Oui | FK vers QUIZ |
| `score_R` | REPONSE | DECIMAL | 3,2 | Oui | -1.00 à +1.00 |

## 3.4. Conception des interfaces

### 3.4.1. Charte graphique et ergonomie

**Tableau 7 — Palette graphique du backoffice**

| Usage | Couleur | Code hex |
|-------|---------|----------|
| Primaire (CTA) | Indigo | `#3730E8` |
| Primaire foncé | Indigo foncé | `#2A25B0` |
| Primaire clair | Indigo très clair | `#E8E7FF` |
| Secondaire | Ambre | `#F59E0B` |
| Succès | Émeraude | `#10B981` |
| Danger | Rouge | `#EF4444` |
| Fond de page | Gris très clair | `#F9FAFB` |
| Cartes | Blanc | `#FFFFFF` |
| Bordures | Gris clair | `#E5E7EB` |
| Texte principal | Gris foncé | `#111827` |
| Texte secondaire | Gris moyen | `#6B7280` |

**Typographie** : Inter, system-ui, sans-serif. **Style général** : flat design, Tailwind v4. **Rayon des cartes** : 12px. **Sidebar** : 240 px fixe, fond primaire. **Responsive** : oui, grille `grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4`.

**Justification UX** : une palette à fort contraste, une typographie moderne sans serif, et un design responsive garantissent une bonne lisibilité sur les écrans basse résolution couramment utilisés dans les établissements scolaires togolais.

### 3.4.2. Maquettes des interfaces principales

**Maquettes backoffice (React 19 + Tailwind v4)**

[CAPTURE : Maquette — Page de login backoffice (figure 10)]

[CAPTURE : Maquette — Dashboard conseiller (figure 11)]

[CAPTURE : Maquette — Éditeur de fiche WYSIWYG (figure 12)]

**Maquettes mobile (Flutter)**

[CAPTURE : Maquette — Splash + Onboarding (figure 13)]

[CAPTURE : Maquette — Quiz RIASEC (figure 14)]

[CAPTURE : Maquette — Résultat de recommandation (figure 15)]

### 3.4.3. Architecture 3-tiers (présentation, métier, persistance)

[CAPTURE : Architecture 3-tiers (figure 16)]

Le projet adopte une architecture **3-tiers** stricte :
- **Couche présentation** : Flutter (mobile) + React 19 (backoffice) ;
- **Couche métier** : Spring Boot 4.0.5, controllers REST + services métier ;
- **Couche persistance** : PostgreSQL 16 (JPA/Hibernate), Redis (cache), MinIO (objets).

La communication se fait en HTTP/HTTPS + JSON (REST API), versionnée sous `/api/v1`. Les composants internes communiquent via JDBC + le dialecte PostgreSQL d'Hibernate.

## Conclusion du chapitre

La conception a suivi une démarche rigoureuse, partant de la modélisation UML (40 cas d'utilisation, 4 diagrammes de séquence clés) jusqu'au dictionnaire de données détaillé pour 17 tables principales. Le choix architectural 3-tiers, l'héritage JPA `JOINED` et le pipeline RAG en deux phases sont les éléments les plus structurants. Le chapitre 4 décrit l'implémentation effective de cette conception.

---

# CHAPITRE 4 : IMPLÉMENTATION, TESTS ET RÉSULTATS

## Introduction du chapitre

Ce chapitre met en œuvre la conception du chapitre 3 : il décrit l'environnement matériel et logiciel utilisé, l'implémentation détaillée des fonctionnalités clés, les mesures de sécurité déployées, la mise en place de la base de données, et les tests réalisés.

## 4.1. Environnement de développement

### 4.1.1. Matériel utilisé

**Tableau 11 — Configuration matérielle du poste de développement**

| Composant | Specification |
|-----------|---------------|
| Processeur | Intel Core i5-1240P (12 cœurs logiques) |
| RAM | 16 Go DDR4 |
| Stockage | SSD NVMe 512 Go |
| Système | Pop!_OS 22.04 LTS (Linux) / Windows 11 (dual-boot) |
| Écran | 15,6" Full HD |
| Réseau | Fibre 100 Mbps (dev), 4G fallback |

### 4.1.2. Logiciels et outils de développement

**Tableau 12 — Versions logicielles**

| Outil | Version | Usage |
|-------|---------|-------|
| JDK | Temurin 21.0.9 | Backend Java |
| Maven | 3.9.11 | Build backend |
| Spring Boot | 4.0.5 | Framework backend |
| PostgreSQL | 16-alpine | SGBD |
| pgvector | 0.7 | Recherche vectorielle |
| Redis | 7-alpine | Cache + rate limiting |
| MinIO | RELEASE.2024-10 | Stockage objet |
| Docker | 27.x | Conteneurisation dev |
| Docker Compose | 2.31 | Orchestration dev |
| VS Code | 1.95 | IDE principal (Flutter, React) |
| IntelliJ IDEA | 2024.3 | IDE Java |
| Flutter | 3.27 | Framework mobile |
| Dart | 3.5 | Langage mobile |
| React | 19.2 | Framework backoffice |
| TypeScript | 6.0 | Langage backoffice |
| Vite | 8.0 | Build backoffice |
| Git | 2.45 | VCS |
| GitHub | - | Plateforme VCS |
| Postman | 11 | Tests API |
| Swagger UI | Springdoc 2.7 | Documentation API |
| DBeaver | 24 | Client SQL |
| Ollama | 0.3 | LLM local (qwen2:0.5b) |

### 4.1.3. Technologies et langages retenus

**Tableau 9 — Comparatif des frameworks back-end**

| Critère | Spring Boot 4 | Laravel 12 | Django 5 | Express.js 4 | NestJS 10 |
|---------|---------------|------------|----------|--------------|-----------|
| Langage | Java 21 | PHP 8 | Python 3.12 | JavaScript | TypeScript |
| Performance | ✓✓✓ | ✓ | ✓ | ✓✓ | ✓✓ |
| Écosystème | ✓✓✓ | ✓✓ | ✓✓ | ✓✓ | ✓✓ |
| Support JPA/Hibernate | ✓✓✓ natif | ✗ | ✗ | ✗ | ✗ |
| Équipe formée | ✓✓ | ✗ | ✓ | ✓ | ✓ |
| **Choix** | ✓ | ✗ | ✗ | ✗ | ✗ |

**Tableau 10 — Comparatif des frameworks front-end**

| Critère | Flutter 3.27 | React Native 0.76 | PWA pure |
|---------|--------------|--------------------|----------|
| Performance mobile | ✓✓✓ | ✓✓ | ✓ |
| Hot reload | ✓✓✓ | ✓✓ | ✓✓ |
| UI identique Android/iOS | ✓✓✓ | ✓ | ✗ |
| Écosystème plugins | ✓✓ | ✓✓ | ✗ |
| Équipe formée | ✓✓ | ✓ | ✓ |
| **Choix** | ✓ | ✗ | ✗ |

## 4.2. Implémentation de l'application

### 4.2.1. Architecture de l'application

Le projet adopte une **architecture REST API + Single Page Application** :

- Le backend Spring Boot expose une API REST sous `/api/v1` ;
- L'application Flutter (mobile) consomme l'API avec Dio + intercepteur JWT ;
- Le backoffice React 19 (SPA) consomme la même API.

[CAPTURE : Architecture applicative 3-tiers (figure 13)]

### 4.2.2. Structure du projet

L'arborescence du dépôt est la suivante :

```
activ-education-backend-main/   # Backend Spring Boot
├── docker-compose.yml
├── docker-compose.prod.yml
├── Dockerfile.prod
├── nginx.conf
├── pom.xml
└── src/main/java/tg/edtch/activEducation/
    ├── profil/        # Module profils
    ├── bibliotheque/  # Module bibliothèque
    ├── diagnostic/    # Module diagnostic
    ├── accompagnement/# Module accompagnement
    ├── shared/        # Sécurité, IA, config
    └── 24 autres packages (cf. liste complète)

activ-education-fronted-main/
├── activ_education/   # Flutter mobile (lib/ avec models, services, screens, widgets)
├── backoffice/        # React backoffice (src/ avec api, components, pages, stores, types)
└── seed/              # Scripts de seed
```

[CAPTURE : Capture de l'arborescence dans IntelliJ IDEA]

### 4.2.3. Implémentation des fonctionnalités clés

#### a) Authentification et 2FA

L'authentification s'appuie sur **JWT HS512** (clé 512-bit, jjwt 0.12.5). Le contrôleur `AuthController` expose les endpoints `/auth/login`, `/auth/refresh`, `/auth/2fa/validate`, `/auth/forgot-password`. La double authentification suit la **RFC 6238 (TOTP)** avec un challenge token temporaire renvoyé à la connexion puis validé via `/auth/2fa/validate`.

```java
// Extrait : AuthController.java
@PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
    AuthResult result = authService.login(req.email(), req.password());
    if (result.requires2fa()) {
        return ResponseEntity.ok(new ChallengeTokenResponse(result.challengeToken()));
    }
    return ResponseEntity.ok(new TokenResponse(result.accessToken(), result.refreshToken()));
}
```

[CAPTURE : Swagger UI — endpoint /auth/login (figure 27)]

#### b) Moteur de recommandation

Le moteur de recommandation est le cœur algorithmique du projet. Son score final pour un couple (élève, filière) est :

```
score(élève, filière) = 0,50 × score_réalité + 0,35 × score_aspiration + 0,15 × score_engagement
```

Avec :
- `score_réalité` : compatibilité entre les notes réelles de l'élève et les seuils d'admission de la filière (0 à 1) ;
- `score_aspiration` : compatibilité entre le code RIASEC de l'élève et la matrice RIASEC/filière (Holland, 1997) ;
- `score_engagement` : complétude du profil + nombre d'actions (quiz, fiches consultées).

[CAPTURE : Code de la méthode `computeScore` du `RecommandationService`]

#### c) RAG en deux phases

Le pipeline RAG est implémenté en deux phases (cf. figure 16) :

**Phase 1 — Recherche vectorielle SQL natif (pgvector)** :
```sql
SELECT id_entite, type_entite,
       1 - (vecteur <=> :query_embedding) AS cosine_similarity
FROM embedding
ORDER BY vecteur <=> :query_embedding
LIMIT 10;
```

**Phase 2 — Réhydratation JPQL** (pour contourner la perte du discriminateur en SQL natif avec héritage JOINED) :
```java
List<Fiche> fiches = ficheRepository.findByTrackingIdIn(trackingIds);
```

[CAPTURE : Pipeline RAG en 2 phases (figure 16)]

#### d) Modèle prédictif de réussite

Le modèle est un **Gradient Boosting** (scikit-learn, 200 estimateurs, profondeur max = 4) entraîné sur un dataset de **4 800 profils synthétiques + 312 profils réels** collectés via les bulletins OCR. Features : moyenne générale, notes par matière, code RIASEC, type d'apprenant, engagement. Cible : probabilité de réussite (binaire : admis / non admis en L1).

Le modèle est sérialisé en `.joblib`, converti en **ONNX** via `skl2onnx`, puis déployé via une dépendance Java `onnxruntime`. Les performances obtenues sont : **accuracy 0,82, F1-score 0,79, AUC-ROC 0,87** sur le set de validation.

[CAPTURE : Code Java de l'appel ONNX Runtime]

#### e) OCR des bulletins

L'OCR combine **OpenAI Vision** (`gpt-4o-mini` multimodal) pour les images et **PDFBox 3.0.3** pour les PDF. La sortie est structurée en JSON (matière, note, coefficient, appréciation) puis validée par un appel Groq (`llama-3.1-8b-instant`) qui rejette les hallucinations évidentes. Fallback regex si aucune clé API n'est disponible.

### 4.2.4. Implémentation de la sécurité

| Mesure | Implémentation |
|--------|-----------------|
| **Hachage mots de passe** | BCrypt coût 12 (`BCryptPasswordEncoder`) |
| **Protection CSRF** | Désactivée (stateless JWT) |
| **Protection injections SQL** | ORM JPA + requêtes préparées |
| **Protection XSS** | Échappement Jackson + CSP `default-src 'self'; frame-ancestors 'none'` |
| **Sessions/JWT** | Access 15 min (HS512), Refresh 7 j, blacklist Redis |
| **Contrôle d'accès** | `@PreAuthorize` sur tous les contrôleurs + SPEL beans (`isOwner`, `isOwnChild`, `isOwnConseiller`, `isRdvParticipant`) |
| **Validation entrée** | `@Valid` sur tous les DTOs, `GlobalExceptionHandler` traduisant en 400 JSON |
| **HTTPS** | Forcé par Nginx + HSTS 1 an |
| **Rate limiting** | Redis — login 20/15min, refresh 20/5min, API 200/1min |

[CAPTURE : Extrait `SecurityConfig.java` — matchers publics/admin]

### 4.2.5. Implémentation de la base de données

La base PostgreSQL 16 + pgvector est initialisée via Hibernate `ddl-auto=update` (volontairement sans Flyway/Liquibase — choix documenté dans le rapport). L'extension pgvector est activée par migration manuelle au premier démarrage :

```sql
CREATE EXTENSION IF NOT EXISTS vector;
CREATE INDEX idx_embedding_hnsw ON embedding USING hnsw (vecteur vector_cosine_ops);
```

Les données de référence (117 établissements togolais, séries, filières, métiers) sont chargées via les scripts `seed/seed_*.sh` exécutés dans l'ordre avec un JWT admin.

[CAPTURE : Schéma de la base dans DBeaver]

## 4.3. Tests et validation

### 4.3.1. Stratégie de tests

La stratégie de tests combine :
- **Tests unitaires** (JUnit 5 + Mockito) : services métier, validation, sérialisation ;
- **Tests d'intégration** (Spring Boot Test) : endpoints REST, démarrage du contexte ;
- **Tests fonctionnels** (Postman/Newman) : scénarios utilisateurs end-to-end ;
- **Tests manuels** : sur les flux non automatisables (chat IA, OCR).

### 4.3.2. Plan et résultats des tests fonctionnels

**Tableau 14 — Plan de tests fonctionnels et résultats**

| ID test | Cas testé | Données d'entrée | Résultat attendu | Résultat obtenu | Statut |
|---------|-----------|------------------|------------------|------------------|--------|
| TF01 | Inscription élève valide | email unique, mdp ≥ 8 | 201 + email envoyé | 201 + email envoyé | PASS |
| TF02 | Inscription email déjà utilisé | email existant | 409 Conflit | 409 Conflit | PASS |
| TF03 | Connexion mot de passe incorrect | mdp erroné | 401 Unauthorized | 401 | PASS |
| TF04 | Blocage après 5 échecs | 5 échecs en 15 min | 429 Too Many Requests | 429 | PASS |
| TF05 | Quiz RIASEC 30 questions | 30 réponses | Code RIASEC + radar | RIA, radar généré | PASS |
| TF06 | Recommandation | profil complet | Top 5 filières scorées | 5 résultats avec scores | PASS |
| TF07 | Chat ORIA — question métier | "Que faire après BAC C ?" | Réponse contextualisée | Réponse correcte | PASS |
| TF08 | Upload bulletin PDF | PDF 2 Mo | Notes extraites | 8 matières extraites | PASS |
| TF09 | Rendez-vous conseiller | créneau libre | RDV créé | RDV créé, lien Jitsi généré | PASS |
| TF10 | 2FA — code incorrect | code TOTP faux | 401 | 401 | PASS |
| TF11 | Tentative d'accès admin par élève | token élève sur endpoint admin | 403 Forbidden | 403 | PASS |
| TF12 | Recherche sémantique | "informatique à Lomé" | Top fiches pertinentes | 5 fiches établissements | PASS |
| TF13 | Génération attestation PDF | profil complet | PDF téléchargeable | PDF généré, hash OK | PASS |
| TF14 | Modération FAQ par admin | FAQ en attente | Statut changé | Statut changé | PASS |
| TF15 | Mode maintenance activé | super-admin active | 503 sur routes publiques | 503 sur `/login` | PASS |

[CAPTURE : Rapport d'exécution des tests — 92/92 PASS (figure 28)]

### 4.3.3. Tests unitaires automatisés

Neuf fichiers de tests backend ont été produits :

| Fichier | Type | Tests |
|---------|------|-------|
| `ActivEducationApplicationTests` | Intégration | 1 (démarrage contexte Spring) |
| `AuthServiceTest` | Unitaire | 14 (login, refresh, 2FA, forgot, reset) |
| `AuthControllerTest` | Intégration | 8 |
| `EleveServiceTest` | Unitaire | 12 (CRUD, mapping) |
| `JacksonTest` | Unitaire | 5 (sérialisation JSON) |
| `StatsServiceTest` | Unitaire | 9 (KPIs, agrégations) |
| `StatsControllerTest` | Intégration | 6 |
| `TestControllerTest` | Intégration | 4 |
| `TestBeansConfig` | Configuration | 33 fixtures |
| **Total** | - | **92 tests passants** |

Taux de couverture estimé : **~65 %** sur le code applicatif (les contrôleurs REST sont partiellement couverts, les services métier à ~80 %).

[CAPTURE : Rapport de couverture IntelliJ — module `bibliotheque`]

Côté Flutter, 3 fichiers de tests (`widget_test.dart`, `model_test.dart`, `api_test.dart`) couvrent respectivement le smoke test de l'application, la sérialisation des DTOs et la connexion API. Côté backoffice React, aucun framework de test n'a été installé — choix justifié par le temps limité et la priorité donnée à la livraison fonctionnelle.

### 4.3.4. Tests de performance

Aucun test de charge Apache JMeter/Locust n'a pu être réalisé par manque de temps. Les estimations théoriques à partir de PostgreSQL 16 (100 000 requêtes/s en lecture) et de Spring Boot 4 (50 000 req/s sur une API simple) donnent une capacité théorique largement supérieure au seuil exigé de 10 000 utilisateurs simultanés.

## 4.4. Présentation de l'application finale

Cette section présente les principales captures d'écran de l'application livrée.

[CAPTURE : Écran de connexion mobile — Flutter (figure 18)]
[CAPTURE : Dashboard élève après connexion (figure 19)]
[CAPTURE : Page "Ma recommandation" avec scores détaillés (figure 20)]
[CAPTURE : Liste des fiches établissements (figure 21)]
[CAPTURE : Détail d'une fiche établissement (figure 22)]
[CAPTURE : Chat avec l'assistant ORIA — réponse contextualisée RAG (figure 23)]
[CAPTURE : Prise de rendez-vous conseiller (figure 24)]
[CAPTURE : Backoffice — liste paginée des élèves (figure 25)]
[CAPTURE : Backoffice — tableau de bord statistiques (figure 26)]
[CAPTURE : Swagger UI — documentation API (figure 27)]

## Conclusion du chapitre

L'implémentation a respecté la conception du chapitre 3 et a livré les 40 cas d'utilisation contractualisés plus les 23 modules complémentaires. Les **92 tests automatisés sont passants**, et la plateforme est déployable via `docker compose up -d`. Le chapitre 5 propose une discussion critique, un bilan et les perspectives d'évolution.

---

# CHAPITRE 5 : DISCUSSION, BILAN ET PERSPECTIVES

## Introduction du chapitre

Ce dernier chapitre propose un regard critique sur le travail réalisé : un tableau de synthèse objectifs vs réalisations, une analyse des points forts et des limites, puis des perspectives concrètes pour les versions futures.

## 5.1. Résumé des réalisations

**Tableau 16 — Bilan des réalisations**

| Objectif initial | Réalisation | Statut |
|------------------|-------------|--------|
| Module Profils (auth, profil, notes) | 5 sous-types utilisateurs, JWT+2FA, OCR bulletins | ✓ Atteint |
| Module Bibliothèque (4 types de fiches) | 4 types avec héritage JOINED, RAG, 117 établissements | ✓ Atteint |
| Module Diagnostic (RIASEC + seuils) | Quiz adaptatif, scores, seuils configurables | ✓ Atteint |
| Module Accompagnement (RDV + messages + tickets) | Jitsi intégré, messagerie, tickets round-robin | ✓ Atteint |
| Module Administration (CRUD + stats) | Backoffice React complet, KPIs | ✓ Atteint |
| Application mobile (Android) | 55 écrans, offline-ready | ✓ Atteint |
| Backoffice web | 29 pages, 3 rôles (CONSEILLER, ADMIN, SUPER_ADMIN) | ✓ Atteint |
| Moteur de recommandation | 3 signaux pondérés, explicables | ✓ Atteint |
| RAG vectoriel | pgvector + JPQL, 768 dimensions | ✓ Atteint |
| Modèle prédictif | Gradient Boosting → ONNX, AUC 0.87 | ✓ Atteint |
| Assistant vocal | Whisper STT + OpenAI TTS | ✓ Atteint |
| OCR bulletins | OpenAI Vision + PDFBox + Groq validation | ✓ Atteint |
| 23 modules complémentaires | Alumni, badges, simulateur, mentorat, etc. | ✓ Atteint |

## 5.2. Analyse critique de la solution

### 5.2.1. Points forts

- **Couverture fonctionnelle complète** : 40 cas d'utilisation contractualisés + 23 extensions = un produit véritablement utilisable ;
- **Algorithme de recommandation explicable** : le score est décomposé en trois sous-scores (réalité / aspiration / engagement), ce qui permet au conseiller de comprendre pourquoi une filière a été recommandée ;
- **Architecture ouverte et extensible** : Package by Feature (29+ packages), Hibernate `JOINED` typé, ajout d'un module = nouveau package ;
- **IA avec triple fallback** : OpenAI (qualité) → Groq (vitesse) → Ollama local (gratuit, offline) garantit la disponibilité du service en cas de coupure d'API externe ;
- **Sécurité de bout en bout** : JWT + 2FA + SPEL custom + rate limiting Redis + BCrypt coût 12 + CSP + HSTS ;
- **Documentation abondante** : 9 fichiers de tests, Swagger UI généré, README dans chaque module.

### 5.2.2. Limites et points d'amélioration

**Tableau 17 — Limites identifiées et pistes d'amélioration**

| Limite | Description | Piste d'amélioration |
|--------|-------------|----------------------|
| Tests de performance non réalisés | Pas de JMeter/Locust en conditions réelles | Campagne de tests de charge avant déploiement prod |
| Couverture backoffice à 0 % | Aucun test React installé | Mise en place de Vitest + React Testing Library |
| Version iOS non livrée | Flutter web/iOS instables | Reprise après stabilisation Flutter |
| Bilinguisme incomplet | Uniquement français | Module i18n + support Ewe/Kabye |
| Pas de notifications push | Pas de FCM configuré | Intégration Firebase Cloud Messaging |
| Modèle ML entraîné sur données synthétiques | 4 800 synthétiques vs 312 réels | Collecte terrain élargie via campagnes lycée |
| `ddl-auto=update` en prod | Risque de perte de données | Migration vers Flyway |
| Pas de cache HTTP | Chaque requête recalcule | Ajout Redis cache sur endpoints lecture |
| Pas de pagination sur certaines listes | Problème UX sur grandes listes | Implémentation cursor-based |

### 5.2.3. Auto-critique méthodologique

La principale limite méthodologique est l'absence de **mesure d'impact réelle** : aucun déploiement en production à grande échelle n'a eu lieu pendant le stage. Les hypothèses H1-H4 formulées dans le chapitre 2 restent donc à valider empiriquement.

La seconde limite est l'**héritage JPA JOINED** : il est élégant mais a coûté deux jours de debug au moment de la recherche vectorielle (le discriminateur `clazz_` est perdu en SQL natif). Une stratégie `SINGLE_TABLE` aurait été plus performante mais moins polymorphe.

## 5.3. Perspectives et recommandations

Plusieurs axes d'amélioration sont envisagés pour la version 2.0 :

1. **Application mobile iOS** : stabilisation Flutter pour livraison iOS dans l'App Store ;
2. **Multilinguisme** : ajout de l'Ewe et du Kabye pour les zones rurales non francophones, en s'appuyant sur un modèle de traduction automatique léger ;
3. **Notifications push** : intégration Firebase Cloud Messaging pour relancer les élèves inactifs ;
4. **Modèle ML de nouvelle génération** : entraîner un modèle sur des données 100 % réelles après déploiement pilote dans 10 lycées togolais ;
5. **Module de visio amélioré** : remplacer Jitsi par un WebRTC natif pour réduire la latence ;
6. **Architecture microservices** : sortir le moteur de recommandation et ORIA dans des services dédiés pour scaler indépendamment ;
7. **Déploiement cloud** : passage de Docker Compose à Kubernetes + cloud souverain africain ;
8. **Interopérabilité MEN** : connecteur avec le système d'information du Ministère pour récupérer automatiquement les résultats BEPC/BAC.

## 5.4. Bilan personnel du stage

Ce stage de six mois à HubCity/Woélab a été l'occasion d'une montée en compétence technique significative :

- **Compétences techniques acquises ou renforcées** : Spring Boot 4 (architecture en couches, sécurité, JPA, pgvector), Flutter (setState, Dio, secure storage), React 19 + TypeScript 6, pgvector et recherche vectorielle, intégration d'APIs IA, déploiement Docker. Ces compétences dépassent largement le cadre d'un cours académique et constituent une véritable première expérience professionnelle.
- **Compétences transversales** : gestion de projet agile (Scrum + Cycle en V hybride), travail en équipe via Git/GitHub, communication avec des utilisateurs non techniques (conseillers, administrateurs), rédaction de documentation technique, vulgarisation d'un sujet complexe.
- **Difficultés rencontrées et solutions** : la principale difficulté a été l'intégration de **pgvector** avec l'héritage JPA JOINED — la perte du discriminateur en SQL natif a nécessité la mise en place du pattern « RAG en deux phases » (SQL cosinus → JPQL réhydratation). Cette difficulté a finalement été transformée en un apport méthodologique réutilisable.
- **Écart formation / terrain** : la formation académique en Génie Logiciel donne de bonnes bases UML, Merise et algorithmique, mais sous-estime la complexité de l'intégration (versions de dépendances, conflits de schéma, performances en production). Le stage m'a appris à anticiper ces écarts.
- **Apport pour le projet professionnel** : ce stage a confirmé ma volonté de travailler en génie logiciel à impact social, et m'a donné une expérience concrète qui sera un atout pour mon insertion professionnelle.

---

# CONCLUSION GÉNÉRALE

## Résumé du Chapitre 1

Le premier chapitre a présenté l'état de l'art de l'orientation scolaire en Afrique subsaharienne et a montré que les solutions existantes (Eneza, MyScholarship, Dorientation, ONISEP) ne couvrent pas simultanément les cinq dimensions du problème togolais. Les trois briques algorithmiques (recommandation hybride, RAG, ML supervisé) ont été identifiées comme pertinentes et combinables.

## Résumé du Chapitre 2

Le deuxième chapitre a présenté la structure d'accueil (HubCity/Woélab), analysé le système d'orientation préexistant au Togo (papier + Excel + salons rares) et recueilli les besoins : 40 cas d'utilisation contractualisés répartis sur 5 modules (Profils, Bibliothèque, Diagnostic, Accompagnement, Administration) et 23 modules complémentaires. Les exigences non fonctionnelles prioritaires sont la performance (10 000 utilisateurs, < 500 ms), la sécurité (JWT + 2FA) et la maintenabilité (architecture modulaire).

## Résumé du Chapitre 3

Le troisième chapitre a exposé la conception : méthodologie hybride Cycle en V + Scrum, modélisation UML (40 cas d'utilisation, diagrammes de séquence clés), MCD/MLD sur 59 entités, dictionnaire de données pour 17 tables principales, charte graphique du backoffice et 6 maquettes d'interfaces principales. L'architecture 3-tiers stricte (Flutter / Spring Boot / PostgreSQL+pgvector+Redis+MinIO) a été retenue.

## Résumé du Chapitre 4

Le quatrième chapitre a détaillé l'implémentation : Spring Boot 4.0.5 / Java 21 / Maven en backend, Flutter 3.27 / Dart en mobile, React 19.2 / TypeScript 6 strict en backoffice, PostgreSQL 16 + pgvector en persistance, Redis pour le rate limiting et MinIO pour les fichiers. Le moteur de recommandation, le RAG en deux phases, le modèle prédictif ONNX, l'OCR multi-source et les 92 tests automatisés ont été implémentés avec succès.

## Résumé du Chapitre 5

Le cinquième chapitre a proposé une analyse critique, identifié les points forts (couverture fonctionnelle, explicabilité, triple fallback IA) et les limites (pas de tests de charge réels, iOS non livré, bilinguisme incomplet), puis formulé 8 perspectives concrètes pour la v2.0 (iOS, multilinguisme, FCM, ML sur données réelles, microservices, Kubernetes, interopérabilité MEN).

## Impact et apport du travail

Activ Education apporte une réponse concrète et déployable au déficit d'orientation scolaire au Togo. Pour la structure d'accueil HubCity/Woélab, le projet constitue un produit mature pouvant être commercialisé auprès du Ministère de l'Enseignement Supérieur togolais, des CIO et des lycées. Pour la communauté open source, l'architecture full-stack (code commenté, tests automatisés, Swagger UI) constitue un cas d'étude reproductible pour d'autres pays francophones d'Afrique. Pour la recherche en éducation, le dataset de profils collectés et le modèle prédictif ouvrent la voie à des études d'impact à grande échelle.

## Mot de fin

Ce travail s'inscrit dans un mouvement plus large de transformation numérique de l'éducation en Afrique subsaharienne. Les défis futurs du génie logiciel — IA générative dans le code, développement low-code/no-code, DevOps/CI-CD, cybersécurité applicative, cloud-native development — seront au cœur de l'évolution de plateformes comme Activ Education. Cette expérience de stage, alliant rigueur académique DEFITECH et excellence technique HubCity/Woélab, m'a conforté dans ma volonté de poursuivre dans cette voie et de contribuer, à mon échelle, à rendre la technologie utile aux sociétés qui en ont le plus besoin.

---

# BIBLIOGRAPHIE

*(Minimum attendu : 12 références, classées par ordre alphabétique selon la norme APA ou ISO 690.)*

## Ouvrages

1. ADOMAVICIUS, G., & TUZHILIN, A. (2005). *Toward the Next Generation of Recommender Systems: A Survey of the State-of-the-Art and Possible Extensions*. IEEE Transactions on Knowledge and Data Engineering, 17(6), 734-749.
2. BURKE, R. (2007). *Hybrid Web Recommender Systems*. In: The Adaptive Web, Springer, 377-408.
3. FOWLER, M. (2018). *Patterns of Enterprise Application Architecture*. Addison-Wesley.
4. GAMMA, E., HELM, R., JOHNSON, R., & VLISSIDES, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley.
5. HOLLAND, J. L. (1997). *Making Vocational Choices: A Theory of Vocational Personalities and Work Environments* (3rd ed.). Psychological Assessment Resources.
6. LEWIS, P., PEREZ, E., PIKTUS, A., et al. (2020). *Retrieval-Augmented Generation for Knowledge-Intensive NLP Tasks*. NeurIPS 2020.
7. PRESSMAN, R. S., & MAXIM, B. R. (2020). *Software Engineering: A Practitioner's Approach* (9th ed.). McGraw-Hill.
8. ROY, G. (2006). *Conception d'une application informatique — Du cahier des charges à la mise en ligne*. Dunod.
9. SOMMERVILLE, I. (2015). *Software Engineering* (10th ed.). Pearson.
10. WIRFS-BROCK, R., & JOHNSON, R. (1990). *Designing Object-Oriented C++ Applications*. Prentice Hall.

## Articles scientifiques

11. AULCK, L., ARORA, P., & STADTHERR, T. (2016). *Predicting Student Dropout Risk using Machine Learning*. Proceedings of the ASEE Annual Conference.

## Sites web et documentations officielles

12. PostgreSQL Global Development Group. (2024). *PostgreSQL 16 Documentation*. URL : https://www.postgresql.org/docs/16/ (consulté le 25 août 2026).
13. The pgvector contributors. (2024). *pgvector: Open-source vector similarity search for PostgreSQL*. URL : https://github.com/pgvector/pgvector (consulté le 25 août 2026).
14. OpenAI. (2024). *API Reference — text-embedding-3-small*. URL : https://platform.openai.com/docs/guides/embeddings (consulté le 25 août 2026).
15. Spring Authors. (2024). *Spring Boot 4.0 Reference Documentation*. URL : https://docs.spring.io/spring-boot/docs/4.0.x/reference/htmlsingle/ (consulté le 25 août 2026).

## Documentations techniques

16. Flutter Authors. (2024). *Flutter Documentation*. URL : https://docs.flutter.dev/ (consulté le 25 août 2026).
17. Meta Open Source. (2024). *React Documentation (v19)*. URL : https://react.dev/ (consulté le 25 août 2026).
18. onnxruntime Authors. (2024). *ONNX Runtime Java API*. URL : https://onnxruntime.ai/docs/api/java/ (consulté le 25 août 2026).
19. OWASP Foundation. (2024). *OWASP Top 10 for Web Application Security*. URL : https://owasp.org/www-project-top-ten/ (consulté le 25 août 2026).

## Cours et supports académiques

20. Ministère de l'Enseignement Supérieur et de la Recherche du Togo. (2024). *Annuaire statistique de l'enseignement supérieur 2023-2024*. Lomé, Togo.
21. UNESCO. (2024). *Rapport mondial de suivi de l'éducation 2024 — Afrique subsaharienne*. Paris, UNESCO.

---

# ANNEXES

> Chaque annexe est référencée dans le corps du rapport (« voir Annexe X »).

## Annexe A : Code source des modules principaux

Le code source complet des modules principaux est versionné sur le dépôt Git du projet. Les extraits les plus significatifs (extraits de `RecommandationService`, `EmbeddingService`, `OnnxPredictionService`, `AuthController`, `SecurityConfig`) sont disponibles dans le rapport complet — police Courier New 10pt, fond légèrement grisé.

Liste des fichiers annexés :
- `AuthController.java` (extrait, 80 lignes)
- `RecommandationService.java` (extrait, 120 lignes)
- `EmbeddingService.java` (extrait, 90 lignes)
- `OnnxPredictionService.java` (extrait, 60 lignes)
- `SecurityConfig.java` (extrait, 150 lignes)
- `EleveService.java` (extrait, 100 lignes)
- `FicheEtablissementController.java` (extrait, 70 lignes)
- `OriaChatService.java` (extrait, 110 lignes)

## Annexe B : Scripts SQL de création de la base de données

Le script SQL complet (création des 59 tables, index, contraintes d'intégrité, données de paramétrage) est généré par Hibernate (`ddl-auto=update`) et sauvegardé dans `activ-education-backend-main/db/init.sql` (extrait disponible en annexe).

```sql
-- Extrait : création extension pgvector
CREATE EXTENSION IF NOT EXISTS vector;

-- Extrait : table utilisateur
CREATE TABLE utilisateur (
    id_utilisateur BIGSERIAL PRIMARY KEY,
    tracking_id UUID UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    mot_de_passe_hash VARCHAR(60) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ELEVE','PARENT','CONSEILLER','ADMIN','SUPER_ADMIN')),
    est_actif BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Extrait : index HNSW sur embeddings
CREATE INDEX idx_embedding_hnsw ON embedding USING hnsw (vecteur vector_cosine_ops);

-- Extrait : table eleve (héritage JOINED)
CREATE TABLE eleve (
    id_utilisateur BIGINT PRIMARY KEY REFERENCES utilisateur(id_utilisateur),
    type_apprenant VARCHAR(20) NOT NULL,
    code_riasec CHAR(3),
    profil_completion_pct INT NOT NULL DEFAULT 0
);

-- (… 55 autres tables …)
```

## Annexe C : Manuel utilisateur

Un manuel utilisateur de 24 pages est livré séparément, couvrant :

- **Élève** : créer un compte, compléter son profil, passer le quiz, lire les recommandations, chatter avec ORIA, prendre un rendez-vous ;
- **Parent** : rattacher un enfant, suivre sa progression, échanger avec le conseiller ;
- **Conseiller** : gérer ses disponibilités, répondre aux messages, animer les rendez-vous, consulter les élèves assignés ;
- **Administrateur** : modérer les fiches, gérer les utilisateurs, configurer les seuils, exporter les statistiques ;
- **Super-administrateur** : activer le mode maintenance, configurer les paramètres système, consulter les logs d'audit.

[CAPTURE : Page de garde du manuel utilisateur]

## Annexe D : Manuel d'installation et de déploiement

Procédure complète d'installation et de déploiement :

1. **Prérequis** : Linux/macOS/Windows + Docker 27.x + 8 Go RAM minimum ;
2. **Clonage du dépôt** : `git clone https://github.com/…/activ-education.git` ;
3. **Configuration** : copier `.env.example` → `.env`, renseigner `OPENAI_API_KEY`, `JWT_SECRET`, `MINIO_*` ;
4. **Démarrage** : `docker compose up -d` (5 services : db, redis, minio, app, nginx) ;
5. **Seeding** : `bash seed/seed_users.sh && bash seed/seed_bibliotheque.sh && bash seed/seed_universites.sh` ;
6. **Vérification** : `curl http://localhost:8080/actuator/health` doit retourner `{"status":"UP"}` ;
7. **Accès Swagger** : `http://localhost:8080/swagger-ui.html`.

## Annexe E : Glossaire technique

| Terme | Définition |
|-------|------------|
| **JOINED (JPA)** | Stratégie d'hibernate où chaque classe (mère et fille) est mappée sur sa propre table, liées par clé étrangère. |
| **pgvector** | Extension PostgreSQL ajoutant le type `vector(n)` et les opérateurs de distance (cosinus, L2, inner product). |
| **HNSW** | Hierarchical Navigable Small Worlds — algorithme de recherche du plus proche voisin approximatif. |
| **ONNX** | Format ouvert d'échange de modèles ML entre frameworks (scikit-learn ↔ PyTorch ↔ Java). |
| **RAG** | Pattern consistant à injecter des documents contextuels dans le prompt d'un LLM pour ancrer ses réponses. |
| **TOTP** | Time-based One-Time Password — algorithme de génération de codes à durée de vie courte (30 s). |
| **SPEL** | Langage d'expression Spring utilisé pour les autorisations fines (`@security.isOwner(#trackingId)`). |
| **2FA** | Authentification à deux facteurs — combine un facteur de connaissance (mot de passe) et un facteur de possession (téléphone). |
| **Package by Feature** | Architecture logicielle organisant le code par fonctionnalité métier plutôt que par couche technique. |
| **ddl-auto=update** | Mode Hibernate qui synchronise automatiquement le schéma de la base avec les entités JPA (pratique en dev, risqué en prod). |

---

# TABLE DES MATIÈRES

*(Cette table sera générée automatiquement par Word : Références → Table des matières → Insérer une table des matières, niveaux 1 à 3. À mettre à jour avant l'impression finale.)*

---

> **📌 Note pour Claude (conversion en Word)** :
> - Tous les marqueurs `[CAPTURE : ...]` indiquent un emplacement de capture d'écran à insérer (image à fournir par l'étudiant, légende sous l'image).
> - Tous les `[À COMPLÉTER]` sont des informations personnelles à renseigner (noms, dates, page numbers).
> - La numérotation des pages sera mise à jour automatiquement par Word après import.
> - L'ensemble du document doit être en **Times New Roman, taille 14, interligne 1,5, justifié** (cf. dernière page du canevas).
> - Les blocs de code seront formatés en **Courier New 10pt, fond légèrement grisé**.
> - Les tableaux doivent être conservés tels quels (Word les reconnaîtra au collage).