# Description du Projet — Activ Education (périmètre complet : 5 modules contractualisés + 23 modules complémentaires)

> Document à fournir à Claude pour générer un thème de mémoire pertinent.
> 
> **Périmètre total réalisé pendant le stage** :
> - **5 modules** du cahier des charges initial (contractualisés)
> - **23 modules complémentaires** ajoutés pendant le stage (cf. `CAHIER_DES_CHARGES_ANNEXE.md`)
> - **Total : 28 modules** répartis en **29+ packages backend** (Package by Feature)

---

## 🎓 Contexte académique

- **Type** : Mémoire de Licence Professionnelle en **Génie Logiciel**
- **Domaine** : Sciences et Technologies
- **Mention** : Informatique
- **Spécialité** : Génie Logiciel
- **Établissement** : Institut Polytechnique **DEFITECH** (Togo)
- **Année académique** : 2025-2026
- **Structure d'accueil** : **HubCity / Woélab** (centre d'innovation au Togo)
- **Stage** : Développement d'une plateforme logicielle en conditions réelles
- **Contrainte canevas** : Le thème ne doit pas dépasser **2 lignes**

---

## 🌍 Problématique métier (tirée du cahier des charges)

Au Togo, l'orientation scolaire et professionnelle des élèves (collège, lycée, bacheliers) souffre de :
- **Manque criant de conseillers d'orientation** (1 conseiller pour plusieurs milliers d'élèves)
- **Information dispersée** sur les 117 établissements supérieurs togolais (Universités de Lomé, Kara, UCAO, ESA, ESTBA, INJS, IUT-G, etc.)
- **Choix souvent inadaptés** au profil réel de l'élève (notes, aptitudes, aspirations)
- **Inadéquation** entre aspirations personnelles et réalité des filières (concours, seuils d'admission)
- **Zones rurales défavorisées** : encore moins d'accès à l'information

---

## 💡 Solution conçue

**Activ EDUCATION** est une plateforme **full-stack** d'orientation scolaire composée au total de **28 modules** :

- **5 modules contractualisés** dans le cahier des charges initial
- **23 modules complémentaires** ajoutés pendant le stage (voir `CAHIER_DES_CHARGES_ANNEXE.md`)

Les 5 modules principaux tels que définis dans le cahier des charges sont :

### Module 1 — Gestion des Profils Utilisateurs

- 9 rôles distincts : Collégien, Lycéen, Étudiant, Parent, Apprenti/Jeune en formation, Jeune déscolarisé, Jeune en situation de handicap, Adulte en reconversion, Conseiller
- **Inscription multi-canal** : email + mot de passe, téléphone (SMS), réseaux sociaux (Google, Facebook)
- **Profil progressif** (complétable au fil de l'eau)
- **Documents personnels** : bulletins, relevés, diplômes, CV (upload PDF/JPEG/PNG, tagging par niveau)
- **Favoris et centres d'intérêt** (Séries, Filières, Métiers, Établissements)
- **Historique des activités** (journal de bord personnel)
- **Comptes famille** (parent ↔ enfants, consentement parental pour les mineurs)
- **RGPD** : export, suppression, gestion du consentement, confidentialité des échanges

### Module 2 — Bibliothèque Centrale (Exploration des Formations et Métiers)

C'est la mémoire vive de la plateforme. 4 types de fiches interconnectées :

- **Fiches « Séries et Spécialités »** : secondaire togolais (6e à Terminale, séries A, C, D, E)
- **Fiches « Filières et Formations »** : supérieur (BTS, DUT, Licences, Masters, Écoles spécialisées)
- **Fiches « Métiers »** : réalité du marché du travail au Togo
- **Fiches « Établissements »** : écoles, universités, centres de formation (publics/privés)

**Structure commune à 3 niveaux** pour chaque fiche :
- **Niveau 1 — « En bref »** : résumé de 3 à 5 lignes (lecture rapide, mobile)
- **Niveau 2 — « Comprendre en vidéo »** : vidéo explicative 3-5 min (YouTube/Vimeo ou upload)
- **Niveau 3 — « Pour aller plus loin »** : paragraphes structurés détaillés (présentation, programme, profil idéal, débouchés, établissements liés)

**Fonctionnalités transverses** :
- Accès public (sans authentification) pour maximiser la diffusion
- **Moteur de recherche global** sur les 4 types de fiches
- **Navigation sémantique** entre fiches (Métier ↔ Filières ↔ Établissements)
- **Filtrage multi-critères** : région, type (public/privé), niveau d'études

### Module 3 — Diagnostic d'Orientation (Moteur Intelligent)

C'est le cœur algorithmique du système. Architecture en 2 sous-modules complémentaires :

**A. Quiz d'Orientation (aspirations personnelles)**

- Moteur de quiz dynamique avec **3 parcours adaptatifs** :
  - **« Nouveau Bachelier »** : choix d'une filière dans le supérieur
  - **« Réorientation Universitaire »** : changement de voie après un premier parcours
  - **« Nouveau Bepcien »** : choix de série après le BEPC
- Questions conditionnelles (arbre de décision)
- Rapport de personnalité (« Profil Créatif », « Technique », « Social », « Méthodique »)
- Suggestions automatiques vers la Bibliothèque (Module 2)

**B. Analyse Académique (réalité scolaire)**

- **Saisie manuelle des notes** par matière et par niveau (6e à Terminale)
- **OCR des bulletins** (scan/photo) — fortement recommandé dans le cahier
- **Moteur d'analyse comparative** : notes vs moyennes vs seuils d'admission
- **Alertes de vigilance** : si l'élève vise une filière hors de portée
- **Visualisation graphique** (radar) : points forts / faibles

**C. Mode combiné (Recommandations croisées)**

- Fusion des deux sources (quiz + notes)
- **Algorithme de pondération** combinant :
  - correspondance avec les goûts (Quiz)
  - probabilité de réussite académique (Notes)
- Affichage par nuages de recommandations filtrables
- Export des résultats (impression, partage parent/conseiller)

### Module 4 — Interaction et Accompagnement (Conseil Personnalisé)

3 niveaux d'accompagnement progressifs :

**A. FAQ dynamique et collaborative**

- Base de connaissances catégorisée (études, métiers, procédures, situations particulières)
- **Moteur de recherche sémantique** en langage naturel
- Feedback utilisateur (« Cette réponse vous a-t-elle aidé ? »)
- Proposition d'ajout si question non trouvée

**B. Messagerie asynchrone**

- Formulaire de contact intelligent (objet, texte, pièces jointes)
- Contexte automatique (profil + historique) transmis au conseiller
- Suivi des échanges (statuts : envoyée, consultée, répondue)
- Notifications email/SMS

**C. Rendez-vous synchrones**

- Agenda partagé des conseillers
- **Multi-canaux** : téléphone, **visioconférence** (Jitsi, Zoom, Google Meet), physique
- Formulaire de préparation (objet, résumé, documents)
- Confirmations, rappels 24h avant, annulation/reporting
- Compte-rendu post-rendez-vous
- Questionnaire de satisfaction

**D. Espaces spécifiques** (publics à besoins particuliers)

- **Handicap** : conseillers spécialisés, ressources dédiées, partage de documents médicaux sécurisé
- **Décrochage / déscolarisation** : approche bienveillante, suivi renforcé
- **Adultes en reconversion** : focus droits/financements, bilan de compétences

### Module 5 — Administration et Back-Office

5 rôles avec permissions distinctes :

- **Super Administrateur** : accès total, gestion des admins, paramétrages système
- **Gestionnaire de contenu** : création/édition fiches + vidéos
- **Responsable des conseillers** : gestion comptes conseillers, suivi activité
- **Conseiller (vue back-office)** : file d'attente questions + RDV
- **Modérateur** : validation FAQ, modération échanges

**Sous-modules** :

- **Gestion des contenus** : fiches (4 types), vidéos (transcodage, stats), quiz (constructeur visuel drag & drop, types de questions, arbres de décision)
- **Gestion utilisateurs** : liste filtrable, suspension, suppression, notifications
- **Gestion conseillers** : spécialités, disponibilités, charge max, suivi activité
- **Modération** : file d'attente FAQ, alertes mots-clés, signalements
- **Statistiques** : audience (vues, provenance, appareils), diagnostics (quiz, profils), accompagnement (FAQ, messagerie, RDV)
- **Paramétrages** : rôles, templates email/SMS, filtres modération, sauvegardes, logs d'audit, **2FA obligatoire**, **mode maintenance**

---

## 🚀 Modules complémentaires (23 modules ajoutés pendant le stage)

Au-delà des 5 modules contractualisés, **23 modules complémentaires** ont été conçus et implémentés pour enrichir la plateforme. Le détail complet est dans `CAHIER_DES_CHARGES_ANNEXE.md`. Voici la liste synthétique :

### Modules d'engagement et gamification (5)
- **Badges** (`badge/`) — 30+ badges débloqués selon les actions utilisateur
- **Défis** (`defis/`) — Challenges individuels et communautaires
- **Réseau social interne** (`reseau/`) — Forum modéré entre élèves
- **Parrainage** (`parrainage/`) — Programme de parrainage viral
- **Témoignages** (`temoignage/`) — Vidéos et textes d'anciens élèves/professionnels

### Modules d'orientation étendue (4)
- **RIASEC** (`riasec/`) — Test de personnalité professionnelle standardisé
- **Prédiction ML** (`prediction/`) — Modèle supervisé de prédiction de réussite
- **Simulateur de parcours** (`simulateur/`) — Visualisation de parcours alternatifs
- **Réorientation** (`reorientation/`) — Accompagnement des changements de voie

### Modules d'insertion professionnelle (3)
- **Emploi** (`emploi/`) — Offres d'emploi, stages, alternance
- **CV Generator** (`cvgenerateur/`) — Génération PDF de CV
- **Portfolio** (`portfolio/`) — Portfolio d'acquis élève

### Modules d'accompagnement humain (4)
- **Mentorat** (`mentorat/`) — Relation mentor/mentoré
- **Alumni** (`alumni/`, `alums/`) — Réseau des anciens élèves
- **VAE** (`vae/`) — Validation des Acquis de l'Expérience
- **Salle virtuelle** (`sallevirtuelle/`) — Visio Jitsi intégrée

### Modules de productivité (4)
- **Calendrier/Événements** (`calendrier/`) — Portes ouvertes, concours, salons
- **Cahier de bord** (`cahierdebord/`) — Journal personnel de réflexion
- **Carte des métiers** (`cartemetiers/`) — Visualisation géographique
- **Entretien (simulation)** (`entretien/`) — Préparation aux entretiens

### Modules transverses (3)
- **Attestations** (`attestations/`) — Génération PDF d'attestations
- **Data Hub** (`datahub/`) — Statistiques avancées pour admins
- **Hors-ligne** (`horsligne/`) — Mode dégradé sans réseau

**Total : 29+ packages backend**, chacun avec ses entités, services, controllers et DTOs.

---

| Couche | Technologie |
|--------|-------------|
| **Backend** | Spring Boot 4.0.5, Java 21, Maven, Hibernate JPA, JWT stateless, 2FA (TOTP), rate-limiting Redis, Swagger |
| **Architecture backend** | **Package by Feature** (29+ packages métier), héritage JPA JOINED (Fiche abstraite → 4 sous-types), 23+ entités, 133+ endpoints REST |
| **Sécurité** | 3 couches : SecurityConfig (chemins) + `@PreAuthorize` (méthodes) + SPEL custom (ownership) |
| **Frontend mobile** | Flutter / Dart (Material 3, Dio, flutter_secure_storage, OCR bulletins, cartographie, assistant vocal) |
| **Backoffice** | React 19.2 / TypeScript 6 strict, Vite, Tailwind v4, Zustand, TanStack Query v5 |
| **Base de données** | PostgreSQL 16 + extension **pgvector** (recherche sémantique vectorielle) |
| **Cache / rate-limit** | Redis |
| **Stockage objet** | MinIO (S3-compatible) — buckets images, vidéos, documents |
| **IA / LLM** | OpenAI (embeddings + génération de texte), Groq et Ollama en fallback |
| **Recherche sémantique** | RAG vectoriel en 2 phases (SQL natif pgvector → JPQL pour réhydratation) |
| **Visioconférence** | Jitsi (intégration API) |
| **Tests** | JUnit 5 (9 fichiers backend, 92/92 passants), Flutter test (3 fichiers mobile) |
| **Déploiement** | Docker Compose (5 services), Nginx, CI/CD GitHub Actions (3 workflows) |

---

## 🧠 Défis techniques et contributions (côté Génie Logiciel)

1. **Architecture Package by Feature** à 29+ modules : conception modulaire cohérente
2. **Héritage JPA JOINED** avec polymorphisme (Fiche abstraite → 4 sous-types) et stratégie de réhydratation
3. **Sécurité multi-couches** : JWT stateless + RBAC + ownership par SPEL custom
4. **Authentification 2FA** (TOTP) généralisée (back-office + utilisateurs sensibles)
5. **Algorithme de recommandation pondéré** combinant aspirations psychométriques (Quiz) et réalité scolaire (Notes) avec alertes de vigilance
6. **Recherche sémantique vectorielle** : embeddings 768 dimensions + similarité cosinus pgvector + RAG 2 phases
7. **OCR des bulletins scolaires** : extraction automatique des notes par vision par ordinateur
8. **Système de tickets** avec traçabilité complète (statuts, notifications, comptes-rendus)
9. **Tests unitaires et d'intégration** (92/92 passants) avec isolation par contexte Spring
10. **Internationalisation** : interface multilingue (français + langues locales togolaises)

---

## 📊 Données et périmètre fonctionnel

- **117 établissements supérieurs togolais** documentés (Universités de Lomé, Kara, UCAO, ESA, ESTBA, INJS, IUT-G, etc.)
- **13 séries du secondaire** togolais (A, C, D, E, etc.)
- **32 filières** du supérieur
- **42 métiers** référencés
- **12 entrées FAQ** initiales
- **7 utilisateurs de seed** (superadmin, modérateur, gestionnaire, conseiller, élève, parent)

---

## 📦 Livrables du stage

1. **Code source complet** des 3 sous-projets (backend + mobile + backoffice)
2. **Scripts de seed ordonnés** (utilisateurs → bibliothèque → quiz → universités)
3. **Documentation technique** (Swagger, AGENTS.md, JOURNAL_BORD_IA.md, DEPLOY.md)
4. **Application déployable** via Docker Compose (5 services)
5. **Mémoire de 40-50 pages** + abstract en anglais + page de garde DEFITECH

---

## 🎯 Compétences Génie Logiciel mobilisées

- **Architecture logicielle** : Package by Feature, héritage JPA, polymorphisme
- **Conception UML** : diagrammes de classes, séquence, cas d'utilisation
- **Méthodologie** : cycle en V (cahier des charges → conception → implémentation → tests → déploiement)
- **Sécurité applicative** : JWT, 2FA, OWASP, RBAC, chiffrement, RGPD
- **Qualité logicielle** : tests unitaires/intégration, validation DTOs, gestion d'erreurs centralisée
- **DevOps** : Docker, CI/CD, monitoring
- **IA appliquée** : embeddings, RAG, similarité cosinus, OCR, reconnaissance vocale
- **Full-stack** : 3 technologies coordonnées, 1 base de code cohérente

---

## 💎 Différenciateurs par rapport à un projet CRUD classique

1. **Algorithme de recommandation pondéré** croisant aspirations psychométriques et réalité scolaire
2. **Recherche sémantique vectorielle** (RAG pgvector) sur 117 établissements et 4 types de fiches
3. **OCR des bulletins scolaires** par vision par ordinateur
4. **Système de tickets + visio Jitsi** pour l'accompagnement humain
5. **3 applications coordonnées** (mobile + web + backoffice) sur 1 backend
6. **Contexte togolais réel** : 117 établissements, données terrain, problématique sociétale documentée
7. **Sécurité de niveau production** : 2FA, rate-limiting, audit, RBAC

---

## 📝 Prompt à coller dans Claude (pour générer un thème)

> **Contexte** : Mémoire de Licence Professionnelle en Génie Logiciel à l'Institut Polytechnique DEFITECH (Togo), stage à HubCity/Woélab, année 2025-2026. Le canevas impose un thème en **2 lignes maximum**.
>
> **Projet** : « Activ EDUCATION » est une plateforme full-stack d'orientation scolaire et professionnelle pour les élèves togolais. Le périmètre total réalisé est de **28 modules** répartis en deux catégories :
>
> **5 modules contractualisés** (cahier des charges initial) : (1) Gestion des profils utilisateurs avec 9 rôles et comptes famille, (2) Bibliothèque centrale de 4 types de fiches (Séries, Filières, Métiers, Établissements) avec structure 3 niveaux (résumé / vidéo / détaillé) et recherche sémantique, (3) Diagnostic d'orientation par quiz psychométrique (3 parcours adaptatifs) et analyse des notes avec OCR, recommandations croisées pondérées, (4) Accompagnement par FAQ dynamique, messagerie asynchrone, rendez-vous synchrones (visioconférence Jitsi) et espaces spécifiques (handicap, décrochage, reconversion), (5) Back-office d'administration avec 5 rôles, modération, statistiques et 2FA obligatoire.
>
> **23 modules complémentaires** ajoutés pendant le stage (cf. annexe) : Alumni, Attestations, Badges (gamification), Cahier de bord, Calendrier/Événements, Carte des métiers, CV Generator, Data Hub, Défis, Emploi (offres), Entretien (simulation), Hors-ligne, Mentorat, Parrainage, Portfolio, Prédiction ML supervisée, Réorientation, Réseau social interne, RIASEC, Salle virtuelle (visio), Simulateur de parcours, Témoignages, VAE.
>
> **Stack technique** : Spring Boot 4.0.5 / Java 21 (29+ packages métier, héritage JPA JOINED, 133+ endpoints REST, sécurité 3 couches), Flutter (mobile, OCR bulletins), React 19 / TypeScript 6 strict (backoffice), PostgreSQL 16 + pgvector (RAG vectoriel), Redis (rate-limiting), MinIO (stockage), OpenAI + Groq + Ollama (IA), Docker Compose (déploiement), 9 fichiers de tests (92/92 passants).
>
> **Défis techniques** : architecture Package by Feature à 29+ modules, polymorphisme JPA, sécurité multi-couches, 2FA, algorithme de recommandation pondéré, recherche sémantique vectorielle (RAG), OCR, ML supervisé (prédiction réussite), 117 établissements togolais documentés.
>
> **Consignes** : Propose-moi **5 variantes de thème** académiques, classées par pertinence, tenant chacune en **2 lignes maximum**. Valorise les aspects **Génie Logiciel** (architecture, sécurité, algorithme) et reste ancré dans le **contexte togolais**. Indique ton coup de cœur avec une justification courte. Évite les formulations trop génériques (« application web », « plateforme numérique »).
