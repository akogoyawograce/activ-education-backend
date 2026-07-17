# Fonctionnalités — Activ EDUCATION

> **Périmètre :** Application mobile (Flutter) · Backoffice (React/TypeScript) · Backend (Spring Boot)
> **Contexte :** Plateforme d'orientation scolaire et professionnelle pour le Togo (HubCity / Woélab)
> **Source de vérité fonctionnelle :** `activ-education-fronted-main/seed/cahier_de_charge.md`

---

## Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Fonctionnalités de l'Application mobile (élèves)](#2-fonctionnalités-de-lapplication-mobile-élèves)
3. [Fonctionnalités du Backoffice (admins / conseillers)](#3-fonctionnalités-du-backoffice-admins--conseillers)
4. [Fonctionnalités du Backend (API REST)](#4-fonctionnalités-du-backend-api-rest)
5. [Fonctionnalités transverses (IA, stockage, sécurité)](#5-fonctionnalités-transverses-ia-stockage-sécurité)

---

## 1. Vue d'ensemble

Activ EDUCATION est une plateforme d'orientation scolaire et professionnelle qui s'articule autour de **5 modules fonctionnels** (cf. cahier des charges) :

| # | Module | Cible principale | Espaces où elle est exposée |
|---|---|---|---|
| 1 | **Gestion des profils utilisateurs** (Profil) | Tous | Mobile, Backoffice, Backend |
| 2 | **Exploration des formations et métiers** (Bibliothèque) | Tous (public + connectés) | Mobile, Backoffice, Backend |
| 3 | **Diagnostic d'orientation** (Quiz + Analyse) | Élèves | Mobile, Backoffice, Backend |
| 4 | **Accompagnement** (FAQ, messagerie, RDV) | Élèves ↔ Conseillers | Mobile, Backoffice, Backend |
| 5 | **Administration & Modération** | Admins | Backoffice, Backend |

**Trois clients** consomment le même backend Spring Boot :

```
┌──────────────────┐     ┌────────────────────┐
│ Mobile (Flutter) │     │ Backoffice (React) │
│  élèves/parents  │     │ admins/conseillers │
└────────┬─────────┘     └────────┬───────────┘
         │                        │
         │  REST + JWT            │  REST + JWT
         │                        │
         └────────────┬───────────┘
                      │
              ┌───────▼────────┐
              │  Spring Boot   │
              │   (Java 21)    │
              └───────┬────────┘
                      │
        ┌─────────┬───┴────┬──────────┐
        │         │        │          │
   PostgreSQL  MinIO    Redis     IA (OpenAI /
   (+pgvector)          (rate     Groq / Ollama)
                        limit)
```

---

## 2. Fonctionnalités de l'Application mobile (élèves)

> **Stack :** Flutter / Dart, `setState` (pas de state management), Dio + intercepteur JWT, polling 4 s pour le chat, polling 120 s côté ORIA.
> **Code :** `activ-education-fronted-main/activ_education/lib/`

### 2.1 Authentification & onboarding

| Fonctionnalité | Détail |
|---|---|
| **Onboarding 3 slides** | Carrousel de présentation avec bouton « Passer » |
| **Profile Setup (étape 1)** | Sélection du rôle (Élève, Parent, Conseiller, etc.), classe, ville |
| **Inscription (étape 2)** | Nom, email, téléphone, mot de passe |
| **Inscription Préférences (étape 3)** | Centres d'intérêt, matières préférées |
| **Login** | Email + mot de passe (Google Sign-In : à implémenter) |
| **Mot de passe oublié** | Formulaire + envoi de lien par email (API simulée) |
| **OTP 2FA** | Saisie du code à 6 chiffres (validation TOTP) |
| **Consentement parental** | Case à cocher pour les mineurs (RGPD) |
| **Splash screen** | Animation logo + barre de progression |
| **Pull-to-refresh** | Sur tous les dashboards pour forcer la sync |

### 2.2 Espace personnel & profil

| Fonctionnalité | Détail |
|---|---|
| **Édition du profil** | Mise à jour des infos, dropdowns (rôle, classe, série, ville) |
| **Photo de profil** | Upload via `image_picker` (try/catch obligatoire — `PlatformException(already_active)`) |
| **Documents personnels** | Bulletins, CV, diplômes, lettres de motivation (PDF/JPEG/PNG) |
| **Upload de bulletins (OCR)** | Reconnaissance automatique des notes via OpenAI Vision (PDFBox pour les PDF sans clé) |
| **Saisie manuelle des notes** | CRUD notes par matière × coefficient (n'est plus exposé sur le mobile, voir § 2.3) |
| **Relevé de notes** | Génération d'un PDF récapitulatif |
| **Favoris** | Liste des fiches (Série/Filière/Métier/Établissement) mises en favori |
| **Centres d'intérêt** | Saisie libre ou par mots-clés pour améliorer les recommandations |
| **Historique d'activités** | Journal de bord : quiz passés, fiches consultées, échanges avec conseillers |
| **Notifications** | Marquer lu, tri par date, suppression (delete API en stub) |
| **Espace Parent** | Vue consolidée des diagnostics / échanges de l'enfant lié |
| **Multi-rôles** | Un utilisateur peut cumuler plusieurs rôles (étudiant + handicap) |

### 2.3 Diagnostic d'orientation (Module 3)

| Fonctionnalité | Détail |
|---|---|
| **Quiz RIASEC** | Arbre de décision adaptatif (6 catégories : Réaliste, Investigateur, Artistique, Social, Entreprenant, Conventionnel) |
| **3 parcours de quiz** | Nouveau Bachelier / Réorientation / Nouveau Bepcien |
| **Rapport de personnalité** | Génération d'un profil (« Créatif », « Technique », « Social », « Méthodique ») |
| **Suggestions automatiques** | Liste de Filières + Métiers correspondant au profil, liens vers fiches |
| **Analyse académique** | *Retirée du mobile* — saisie notes et OCR restent côté backoffice |
| **Recommandations croisées** | Score combinant aspirations (quiz) × performances (notes) |
| **Nuage de recommandations** | Affichage cartes / bulles, filtres (type, durée, ville) |
| **Nuage pondéré 60/40** | Algorithme : 60 % aspirations personnelles, 40 % réalité académique |
| **Export / partage des résultats** | Sauvegarde dans l'historique, impression, partage parent/conseiller |
| **Bouton « Parler à un conseiller »** | Envoie automatiquement le diagnostic au conseiller |
| **Graphique radar** | Visualisation forces/faiblesses par matière (côté backoffice) |

### 2.4 Bibliothèque d'exploration (Module 2)

| Fonctionnalité | Détail |
|---|---|
| **4 types de fiches** | Série (secondaire) · Filière (supérieur) · Métier · Établissement |
| **Accès public** | Catalogue consultable sans compte ; historique sauvegardé si connecté |
| **Recherche globale** | Barre de recherche unique interrogeant les 4 types simultanément |
| **Recherche sémantique (RAG)** | Question en langage naturel (« Je veux travailler dans la nature ») → embeddings OpenAI 768 dim + pgvector cosinus |
| **Filtrage multicritères** | Région, type (public/privé), niveau, durée |
| **3 niveaux de lecture par fiche** | « En bref » (résumé court) · Vidéo explicative · Paragraphes détaillés |
| **Fiches similaires** | Proposition automatique de fiches proches (cosinus sur embeddings) |
| **Fiches tendances** | Top 7 derniers jours |
| **Récemment consultées** | Fiches vues récemment par l'utilisateur |
| **Favoris** | Ajout/suppression, notification si la fiche change |
| **Liens inter-fiches** | Métier → Filières qui y mènent → Établissements qui les proposent |
| **Recherches orphelines** | Mots-clés qui n'ont renvoyé aucune fiche (signalement admin) |
| **Visite virtuelle** | Lien vers une visite 3D / vidéo de l'établissement |
| **Carte interactive** | Localisation de l'établissement (carte intégrée) |

### 2.5 Accompagnement (Module 4)

| Fonctionnalité | Détail |
|---|---|
| **FAQ dynamique** | Base de questions/réponses catégorisées (études, métiers, procédures, situations particulières) |
| **Recherche sémantique FAQ** | Question en langage naturel → réponse générée (RAG) |
| **Bouton « Cette réponse m'a aidé »** | Feedback pour améliorer la pertinence |
| **« Je n'ai pas trouvé ma réponse »** | Redirection vers la messagerie, question anonymisable pour FAQ |
| **Messagerie asynchrone** | Boîte de réception type email, statut (envoyée / consultée / répondue) |
| **Pièces jointes** | Documents contextualisant la question |
| **Contexte automatique** | Le profil + historique de l'élève est joint au message |
| **Chat avec conseiller** | Polling 4 secondes (pas de WebSocket), bulles, auto-scroll, compteurs non-lus |
| **Prise de RDV** | Sélection conseiller + créneau, CRUD complet |
| **Multi-canaux RDV** | Téléphone, visio (Jitsi), physique (locaux partenaires) |
| **Lien visio auto-généré** | Lien de visioconférence envoyé dans la confirmation |
| **Disponibilités conseillers** | Agenda partagé avec créneaux (1-7 = jour de la semaine) |
| **Tickets** | Système de tickets (ouvert, en cours, résolu, fermé) pour les questions |
| **Confirmation & rappels RDV** | Email/SMS immédiat, rappel 24 h avant, annulation/report |
| **Compte-rendu post-RDV** | Synthèse écrite du conseiller, partageable à d'autres conseillers autorisés |
| **Espace situations particulières** | Conseillers spécialisés handicap / décrochage / reconversion |

### 2.6 Intelligence artificielle (ORIA + OCR)

| Fonctionnalité | Détail |
|---|---|
| **ORIA — assistant conversationnel** | Chatbot d'orientation, répond aux questions libres |
| **ORIA — fallback multi-provider** | Ollama local (`qwen2:0.5b`) → Groq → OpenAI (cascade) |
| **ORIA — sessions** | Mémoire de conversation par `sessionId` |
| **OCR de bulletins** | Extraction automatique des notes (OpenAI Vision) |
| **Recommandation IA** | Score de matching d'un élève avec une liste de fiches (pgvector) |
| **Reconnaissance vocale** | Endpoint `/api/v1/vocal` pour la dictée vocale |
| **Recommandation IA par élève** | `GET /api/v1/eleves/{id}/recommandation-ia` — top fiches selon profil |

### 2.7 Fonctionnalités transverses (mobile)

| Fonctionnalité | Détail |
|---|---|
| **Hors-ligne (squelette)** | Mode déconnecté avec cache local (`/api/v1/horsligne`) |
| **Notifications push** | Alertes nouveaux messages, RDV, changements sur fiches favorites |
| **Alumni** | Réseau des anciens élèves pour le mentorat |
| **Badge / gamification** | Badges de progression (quiz terminés, fiches lues, etc.) |
| **Portfolio** | Espace centralisé de productions de l'élève |
| **Carte des métiers** | Visualisation des métiers par secteur / niveau |
| **Simulateur de parcours** | Simulation d'un parcours de formation avec étapes |
| **Témoignages** | Interviews de professionnels, transcriptions longues |
| **VAE (Validation des Acquis)** | Parcours de validation des acquis de l'expérience |
| **Emploi** | Offres d'emploi (post-formation) |
| **Défis** | Challenges gamifiés (quiz, lectures) |
| **Calendrier** | Agenda personnel + RDV + événements (portes ouvertes) |
| **Cahier de bord** | Journal personnel enrichi (réflexions, étapes) |
| **Prédiction** | Modèle prédictif d'adéquation (succès filière) |
| **CV générateur** | Génération automatique d'un CV à partir du profil |
| **Attestations** | Génération d'attestations PDF (parcours, présence) |
| **Réorientation** | Parcours dédié à la réorientation universitaire |
| **Mentorat** | Mise en relation avec un mentor alumni |
| **Parrainage** | Système de parrainage entre élèves |
| **Réseau** | Réseau social d'entraide entre élèves |
| **Réseau de fiches inter-connexions** | Graphe de navigation entre fiches liées |
| **Visio Jitsi** | Lien visio auto-généré pour les RDV à distance |
| **DataHub** | Données ouvertes / indicateurs publics |
| **Paramètres** | Confidentialité, notifications, langue, mode sombre |
| **Mode maintenance** | Bandeau d'information quand la plateforme est en maintenance |
| **Import / Export CSV (élèves)** | *Côté backoffice* — mais le backoffice utilise l'endpoint mobile |

---

## 3. Fonctionnalités du Backoffice (admins / conseillers)

> **Stack :** React 19 + TypeScript 6 (strict, `erasableSyntaxOnly`) + Vite + Tailwind v4
> **State :** Zustand + TanStack Query v5 · **Routing :** react-router-dom v7
> **Code :** `activ-education-fronted-main/backoffice/src/`
> **3 rôles :** `CONSEILLER` · `ADMIN` · `SUPER_ADMIN` (gardé par `<ProtectedRoute>`)

### 3.1 Authentification & layout

| Page | Rôle | Détail |
|---|---|---|
| `LoginPage` | public | Connexion email + mot de passe, redirection selon rôle |
| `NotFoundPage` | public | 404 / page inexistante |
| Layout Admin | ADMIN | Sidebar admin, accès gestion contenus + utilisateurs |
| Layout Conseiller | CONSEILLER | Sidebar conseiller, focus messagerie + RDV |
| Layout SuperAdmin | SUPER_ADMIN | Sidebar superadmin, logs + paramètres système |

### 3.2 Espace ADMIN (`/admin`)

#### Tableau de bord

| Page | Détail |
|---|---|
| `AdminDashboard` | KPIs globaux, graphique d'évolution, alertes (comptes en attente, fiches brouillon, FAQ à modérer) |
| `AdminStatistiquesPage` | Audience, top fiches, diagnostics, accompagnement, export PDF/Excel |
| `AdminProfilPage` | Édition du profil admin, changement de mot de passe, 2FA |

#### Gestion des utilisateurs

| Page | Détail |
|---|---|
| `ElevesPage` | Liste filtrable (rôle, date, niveau, région), vue détaillée, suspension, suppression |
| `ParentsPage` | Liste des parents, gestion des liens parent ↔ enfant |
| `ConseillersPage` | Création de comptes conseillers, spécialités, charge de travail maximale, disponibilités |
| `NotificationsPage` | Envoi de notifications à un utilisateur ou un groupe, templates |

#### Gestion de la Bibliothèque (Module 2)

| Page | Détail |
|---|---|
| `MetiersPage` | CRUD Fiches Métier — workflow brouillon → publication, planif, historique, traduction |
| `SeriesPage` | CRUD Fiches Série (secondaire) — programme détaillé, coefficients, matières |
| `FilieresPage` | CRUD Fiches Filière — conditions admission, programme, poursuites, débouchés |
| `EtablissementsPage` | CRUD Fiches Établissement — type, niveau, contacts, offre de formation |
| `FAQModerationPage` | Modération de la FAQ collaborative — accepter / refuser / modifier les propositions |

#### Gestion du Diagnostic (Module 3)

| Page | Détail |
|---|---|
| `QuizPage` | Liste des quiz, activation / désactivation |
| `QuizEditorPage` | Constructeur de quiz — drag & drop, types de questions (choix unique, multiple, Likert, libre) |
| `QuizEditorPageForm` | Formulaire de quiz alternatif (édition rapide) |
| `ScoreMatricesPage` | Édition des grilles de pondération (R×I×A×S×E×C × filière) |
| `SeuilsPage` | Édition des seuils d'admission (note min par matière pour une filière) |

#### Modération & interaction (Module 4)

| Page | Détail |
|---|---|
| `FAQModerationPage` | (voir ci-dessus) |
| `NotificationsPage` | (voir ci-dessus) |

### 3.3 Espace CONSEILLER (`/conseiller`)

| Page | Détail |
|---|---|
| `ConseillerDashboard` | KPIs conseiller : questions en attente, RDV du jour, taux de réponse |
| `MessagesPage` | File d'attente des questions, éditeur de réponse riche, insertion de liens vers fiches, réponses pré-enregistrées, transfert |
| `RendezVousPage` | Agenda des RDV (vue jour/semaine), validation, annulation, compte-rendu post-RDV |
| `FAQPage` | FAQ conseiller — proposition de nouvelles entrées, statistiques d'utilisation |
| `OriaPage` | Console d'administration ORIA — sessions actives, logs, monitoring des providers |
| `StatistiquesPage` | Stats personnelles : questions traitées, délai moyen, satisfaction |
| `UtilisateursPage` | Liste filtrable des utilisateurs suivis par ce conseiller |
| `ProfilPage` | Profil + disponibilités + spécialités + charge de travail maximale |

### 3.4 Espace SUPERADMIN (`/superadmin`)

| Page | Détail |
|---|---|
| `SuperAdminDashboard` | KPIs plateforme, monitoring Prometheus/Grafana, état des services |
| `ParametresPage` | Paramètres système : templates email/SMS, mots-clés modération, planif backups, timeout session, mode maintenance |
| `LogsPage` | Logs d'audit exhaustifs — qui a fait quoi quand, filtres par utilisateur/action/date |

### 3.5 Fonctionnalités transverses (backoffice)

| Fonctionnalité | Détail |
|---|---|
| **Mode maintenance** | Bascule plateforme offline (in-memory, non persistant) |
| **2FA obligatoire** | TOTP pour tous les rôles backoffice |
| **IP whitelisting** | (Optionnel selon hébergement) restriction d'accès |
| **Logs d'audit** | Toutes les actions tracées et consultables |
| **Notifications configurables** | Templates email/SMS avec variables |
| **Import/Export CSV** | Import en masse de fiches (Séries/Filières/Métiers/Établissements) |
| **Planification de publication** | Fiches en « brouillon » avec date de publication future |
| **Historique des modifications** | Versionning des fiches, retour à une version antérieure |
| **Traductions multilingues** | (Préparé) français + langues locales |
| **Vidéothèque centrale** | Upload, transcodage, association à plusieurs fiches, stats de vues |
| **Recherche intelligente IA** | Prévisualisation de la recherche sémantique RAG |
| **Statistiques exportables** | PDF / Excel pour rapports financeurs |
| **Rapports automatiques** | Programmables (ex : mensuel par email) |
| **Gestion des permissions** | Super admin peut créer de nouveaux rôles, modifier les droits |
| **Sauvegardes automatiques** | Planification des backups DB et fichiers |
| **Monitoring Prometheus** | `/metrics` + dashboards Grafana (docker-compose.monitoring.yml séparé) |
| **Logs centralisés** | Logstash + ELK (config `logstash.conf`) |

---

## 4. Fonctionnalités du Backend (API REST)

> **Stack :** Spring Boot 4.0.5 + Java 21 + Maven
> **Base :** PostgreSQL 16 + pgvector (768 dim) + MinIO + Redis (rate limit)
> **Code :** `activ-education-backend-main/src/main/java/tg/edtch/activEducation/`
> **Architecture :** Package by Feature (30+ packages, ~70 controllers)
> **URL de base :** `http://localhost:8080/api/v1` · Swagger : `/swagger-ui.html`

### 4.1 Sécurité & Authentification (`shared/security/`)

| Endpoint | Description |
|---|---|
| `POST /auth/login` | Connexion email + mot de passe (renvoie JWT + refresh token, ou `requires2fa=true` + `challengeToken`) |
| `POST /auth/refresh` | Rafraîchir le JWT (skip du 401 interceptor) |
| `POST /auth/logout` | Invalider le token (côté Redis/blacklist) |
| `POST /auth/2fa/validate` | Valider le code TOTP (retourne JWT complet) |
| `GET /auth/2fa/setup` | Générer le secret TOTP + QR code (premier login d'un admin/conseiller) |
| `POST /auth/consentement` | Recueil du consentement parental (RGPD) |

**Trois couches de défense :**

1. **`SecurityConfig.java`** — règles de path (publiques vs authentifiées)
2. **`@PreAuthorize`** sur chaque controller — contrôle par rôle
3. **SPEL custom bean `@security`** — règles d'ownership :
   - `isOwner(#trackingId)`
   - `isOwnChild(#eleveTrackingId)`
   - `isOwnConseiller(#conseillerTrackingId)`
   - `isRdvParticipant(#rdvTrackingId)`

**Rate limiting Redis :** login 20/15min · refresh 20/5min · API 200/1min (désactivé en dev).

### 4.2 Gestion des profils (`profil/`)

| Endpoint | Description |
|---|---|
| `POST /eleves` | Inscription élève |
| `GET /eleves/{trackingId}` | Profil élève (soi-même ou son enfant) |
| `GET /eleves` | Liste (admin) — paginée, filtrable |
| `PUT /eleves/{trackingId}` | Mise à jour élève (mot de passe optionnel — pas de `@NotBlank`) |
| `DELETE /eleves/{trackingId}` | Désactivation (soft delete) |
| `GET /eleves/{trackingId}/resultats-diagnostic` | Résultats (Page, **sans pagination côté Flutter**) |
| `POST /parents` | Inscription parent |
| `GET /parents/{trackingId}` | Profil parent |
| `POST /parents/{trackingId}/enfants/{eleveTrackingId}` | Lier un enfant |
| `DELETE /parents/{trackingId}/enfants/{eleveTrackingId}` | Délier |
| `POST /conseillers` | Créer un conseiller (admin) |
| `GET /conseillers/disponibles` | Liste des conseillers avec disponibilités |
| `GET /conseillers/{trackingId}` | Profil conseiller |
| `POST /administrateurs` | Créer un admin (super admin) |
| `GET /administrateurs/{trackingId}` | Profil admin |
| `POST /utilisateurs/{id}/historique` | Ajouter une entrée d'historique |
| `GET /utilisateurs/{id}/historique` | Historique de l'utilisateur |
| `DELETE /utilisateurs/{id}/historique` | Purge l'historique |
| `POST /utilisateurs/{id}/notifications` | Envoyer une notification |
| `GET /utilisateurs/{id}/notifications` | Liste des notifications |
| `GET /utilisateurs/{id}/notifications/non-lues` | Non lues |
| `PATCH /utilisateurs/{id}/notifications/tout-lire` | Tout marquer lu |
| `POST /eleves/{id}/notes` | Ajouter une note |
| `GET /eleves/{id}/notes` | Notes d'un élève |
| `PUT /notes/{trackingId}` | Modifier |
| `DELETE /notes/{trackingId}` | Supprimer |
| `GET /eleves/{id}/releve-notes` | Générer un PDF relevé |
| `POST /eleves/{id}/documents` | Upload d'un document personnel |
| `GET /eleves/{id}/documents` | Liste des documents |
| `DELETE /documents/{trackingId}` | Suppression |

### 4.3 Bibliothèque (`bibliotheque/`)

Architecture polymorphique : `Fiche` abstraite avec `InheritanceType.JOINED`, 4 sous-types, embeddings pgvector 768 dim.

| Endpoint | Description |
|---|---|
| `POST /bibliotheque/metiers` | Créer une fiche métier |
| `GET /bibliotheque/metiers` | Lister (paginé, filtré) |
| `GET /bibliotheque/metiers/{trackingId}` | Détail |
| `PUT /bibliotheque/metiers/{trackingId}` | Modifier |
| `DELETE /bibliotheque/metiers/{trackingId}` | Supprimer (soft) |
| `GET /bibliotheque/metiers/recherche?motCle=...` | Recherche full-text |
| `GET /bibliotheque/metiers/secteurs` | Liste des secteurs |
| `GET /bibliotheque/metiers/{trackingId}/similaires` | Fiches similaires (cosinus) |
| *idem pour* `series` · `filieres` · `etablissements` |
| `GET /bibliotheque/etablissements/ville/{ville}` | Filtrer par ville |
| `GET /bibliotheque/etablissements/niveau/{niveau}` | Filtrer par niveau (Bac, Licence…) |
| `GET /bibliotheque/filieres/domaines` | Liste des domaines |
| `POST /bibliotheque/favoris` | Ajouter aux favoris |
| `GET /bibliotheque/favoris/utilisateur/{trackingId}` | Mes favoris |
| `DELETE /bibliotheque/favoris/{trackingId}` | Retirer |
| `GET /bibliotheque/faq` | FAQ publiées (public) |
| `GET /bibliotheque/faq/categories` | Catégories |
| `GET /bibliotheque/faq/recherche-ia?question=...` | Recherche sémantique FAQ (RAG) |
| `POST /bibliotheque/faq` | Ajouter une entrée (admin) |
| `GET /bibliotheque/recherche-fiche-ia/globale?phrase=...` | Recherche sémantique multi-fiches |
| `GET /bibliotheque/analytics/tendances` | Top 7 derniers jours |
| `GET /bibliotheque/analytics/similaires/{trackingId}` | Fiches similaires |
| `GET /bibliotheque/analytics/orphelines` | Recherches sans résultat (admin) |
| `POST /bibliotheque/lien-inter-fiche` | Créer un lien entre 2 fiches |
| `GET /bibliotheque/lien-inter-fiche/{trackingId}` | Liens d'une fiche |
| `GET /admin/analytics/globales` | Stats admin complètes |
| `GET /admin/analytics/contenus` | Stats contenus (brouillons, publiés) |

### 4.4 Diagnostic (`diagnostic/`)

| Endpoint | Description |
|---|---|
| `POST /quiz` | Créer un quiz |
| `GET /quiz` | Quiz actifs |
| `GET /quiz/{trackingId}` | Détail |
| `PUT /quiz/{trackingId}` | Modifier |
| `DELETE /quiz/{trackingId}` | Désactiver |
| `GET /quiz/{trackingId}/questions` | Questions d'un quiz |
| `POST /quiz/{trackingId}/questions` | Ajouter une question (ordre, type, condition) |
| `PUT /questions/{trackingId}` | Modifier une question |
| `DELETE /questions/{trackingId}` | Supprimer |
| `POST /questions/{trackingId}/reponses` | Ajouter une réponse (texte, catégorie RIASEC, points) |
| `GET /questions/{trackingId}/reponses` | Réponses d'une question |
| `POST /resultats-diagnostic` | Enregistrer un résultat (score + recommandation) |
| `GET /eleves/{trackingId}/resultats-diagnostic` | Résultats d'un élève |
| `GET /resultats-diagnostic/{trackingId}` | Détail d'un résultat |
| `POST /score-matrices` | Créer une matrice de pondération |
| `GET /score-matrices` | Liste des matrices |
| `PUT /score-matrices/{trackingId}` | Modifier |
| `POST /seuils-admission` | Créer un seuil |
| `GET /seuils-admission` | Liste |
| `PUT /seuils-admission/{trackingId}` | Modifier |
| `POST /quiz/generation` | Génération automatique de quiz (IA) |
| `GET /quiz/recommandation/{eleveTrackingId}` | Recommandation de quiz pour un profil |

### 4.5 Accompagnement (`accompagnement/`)

| Endpoint | Description |
|---|---|
| `POST /utilisateurs/{id}/messages` | Envoyer un message |
| `GET /messages/conversation?user1=...&user2=...` | Conversation entre 2 utilisateurs |
| `GET /utilisateurs/{id}/messages/recus` | Messages reçus |
| `GET /utilisateurs/{id}/messages/non-lus/compteur` | Compteur non-lus |
| `PATCH /messages/conversation/lire` | Marquer comme lu |
| `DELETE /messages/{trackingId}` | Supprimer |
| `POST /rendez-vous` | Planifier un RDV (téléphone / visio / physique) |
| `GET /rendez-vous/eleve/{id}` | RDV d'un élève |
| `GET /rendez-vous/conseiller/{id}` | RDV d'un conseiller |
| `PATCH /rendez-vous/{id}/annuler` | Annuler |
| `PATCH /rendez-vous/{id}/terminer` | Terminer + compte-rendu |
| `GET /rendez-vous/{trackingId}` | Détail |
| `POST /tickets` | Créer un ticket (question) |
| `GET /tickets/en-attente` | File d'attente conseiller |
| `PATCH /tickets/{id}/assigner` | Assigner à un conseiller |
| `PATCH /tickets/{id}/statut` | Changer statut (ouvert/en cours/résolu/fermé) |
| `POST /conseillers/{id}/disponibilites` | Ajouter un créneau |
| `GET /conseillers/{id}/disponibilites` | Liste des créneaux |
| `DELETE /disponibilites/{trackingId}` | Retirer un créneau |

### 4.6 Modules fonctionnels additionnels

| Module | Endpoints principaux |
|---|---|
| **`alumni/`** | Réseau des anciens : recherche, profil public, contact |
| **`alums/`** | Variante pour distinguer les promotions |
| **`attestations/`** | Génération PDF d'attestations (parcours, présence) |
| **`badge/`** | CRUD badges + attribution à un utilisateur |
| **`cahierdebord/`** | Journal personnel enrichi (réflexions, étapes) |
| **`calendrier/`** | Agenda unifié : RDV + événements (portes ouvertes) |
| **`cartemetiers/`** | Visualisation des métiers par secteur/niveau |
| **`cvgenerateur/`** | Génération CV PDF à partir du profil |
| **`datahub/`** | Données ouvertes / indicateurs publics |
| **`defis/`** | Challenges gamifiés (CRUD + suivi de progression) |
| **`emploi/`** | Offres d'emploi (post-formation) |
| **`entretien/`** | Préparation à un entretien (banque de questions) |
| **`horsligne/`** | Mode déconnecté : cache local des fiches |
| **`mentorat/`** | Mise en relation avec un mentor alumni |
| **`parrainage/`** | Système de parrainage (code promo, filleul) |
| **`portfolio/`** | Espace centralisé de productions |
| **`prediction/`** | Modèle prédictif d'adéquation (succès filière) |
| **`recommandation/`** | Algorithme 60/40 (aspirations × performances) |
| **`reorientation/`** | Parcours dédié réorientation universitaire |
| **`reseau/`** | Réseau social d'entraide entre élèves |
| **`riasec/`** | Test psychométrique RIASEC (6 dimensions) |
| **`sallevirtuelle/`** | Visite virtuelle 3D / vidéo des établissements |
| **`simulateur/`** | Simulateur de parcours de formation |
| **`temoignage/`** | Interviews de professionnels (texte + vidéo) |
| **`vae/`** | Validation des Acquis de l'Expérience |

### 4.7 Endpoints transverses (`shared/`)

| Endpoint | Description |
|---|---|
| `POST /auth/*` | (voir § 4.1) |
| `POST /admin/logs` | Récupérer les logs d'audit (super admin) |
| `GET /admin/logs?filtres=...` | Filtrage par utilisateur/action/date |
| `GET /stats/globales` | KPIs globaux (admin) |
| `GET /stats/conseiller/{id}` | Stats d'un conseiller |
| `GET /stats/eleve/{id}` | Stats d'un élève |
| `GET /parametres` | Paramètres système (frontend / mobile) |
| `PUT /parametres/{cle}` | Modifier un paramètre (super admin) |
| `GET /maintenance/status` | État du mode maintenance |
| `POST /maintenance/toggle` | Bascule (super admin) |
| `POST /csv/import/eleves` | Import CSV élèves (admin) |
| `GET /csv/export/eleves` | Export CSV élèves (admin) |
| `GET /test/ping` | Endpoint de healthcheck (dev) |
| `GET /actuator/health` | Healthcheck Spring Actuator |
| `GET /actuator/metrics` | Métriques Prometheus |
| `GET /api-docs` | Spécification OpenAPI v3 |
| `GET /swagger-ui.html` | Interface Swagger |

---

## 5. Fonctionnalités transverses (IA, stockage, sécurité)

### 5.1 Stockage de fichiers (`shared/minio/`)

| Endpoint | Description |
|---|---|
| `POST /files/upload/{fileType}` | Upload (fileType = `IMAGE`, `VIDEO`, `PDF`, `DOCUMENT`) |
| `POST /files/upload/multiple/{fileType}` | Upload multiple |
| `GET /files/download/{fileType}/{fileName}` | Téléchargement (attachment) |
| `GET /files/stream/{fileType}/{fileName}` | Téléchargement (inline) |
| `GET /files/url/{fileType}/{fileName}` | URL publique (image) |
| `GET /files/presigned-url/{fileType}/{fileName}` | URL signée avec expiration (default 60 min) |
| `GET /files/metadata/{fileType}/{fileName}` | Métadonnées |
| `GET /files/list/{fileType}` | Liste des fichiers d'un bucket |
| `GET /files/exists/{fileType}/{fileName}` | Existence |
| `DELETE /files/{fileType}/{fileName}` | Suppression |
| `GET /files/pdf/thumbnail/{fileName}` | Génération thumbnail PNG |
| `GET /files/pdf/text/{fileName}` | Extraction texte PDF (PDFBox) |

**3 buckets MinIO :** `images` · `videos` · `documents` · **Max :** 500 MB par fichier.

### 5.2 Intelligence artificielle (`shared/ai/`)

| Endpoint | Description |
|---|---|
| `POST /api/v1/vocal` | Reconnaissance vocale (transcription audio) |
| `POST /api/v1/oria` | Chat avec ORIA (question + sessionId) |
| `GET /api/v1/oria/session/{sessionId}` | Historique d'une session ORIA |
| `POST /api/v1/eleves/{trackingId}/ocr` | OCR d'un bulletin (image ou PDF) |
| `GET /api/v1/eleves/{trackingId}/recommandation-ia` | Recommandation IA basée embeddings |
| `GET /api/v1/recommandation-ia` | Recommandation IA globale |

**Cascade de providers :**

```
Ollama local (qwen2:0.5b)
    ↓ (fallback)
Groq (GROQ_API_KEY)
    ↓ (fallback)
OpenAI (OPENAI_API_KEY)
```

- **Embeddings :** OpenAI `text-embedding-3-small` → 768 dim → pgvector
- **Génération :** Groq (rapide) ou OpenAI (qualité)
- **ORIA :** Ollama local par défaut, fallback Groq, puis OpenAI
- **OCR :** OpenAI Vision pour les images, PDFBox natif pour les PDF

### 5.3 Sécurité opérationnelle

| Fonctionnalité | Détail |
|---|---|
| **JWT stateless** | Filtre toujours actif, CSRF désactivé, CORS multi-origines |
| **Refresh token** | Rotation sécurisée |
| **2FA TOTP** | Obligatoire pour admin/conseiller, optionnel pour élève |
| **Blacklist de tokens** | Invalidation à la déconnexion (Redis) |
| **Rate limiting Redis** | 3 niveaux : login, refresh, API |
| **Chiffrement au repos** | Messages + bulletins sensibles (handicap, médical) |
| **Soft delete** | Conservation des données pour audit (sauf RGPD droit à l'oubli) |
| **Logs d'audit** | Toutes les actions tracées (super admin) |
| **Consentement parental** | Case obligatoire pour les mineurs (RGPD) |
| **Export RGPD** | L'utilisateur peut télécharger l'intégralité de ses données |
| **Suppression RGPD** | Droit à l'oubli : suppression définitive du compte et données |
| **Mode maintenance** | Bascule plateforme offline (in-memory) |
| **Visio Jitsi** | Lien de visioconférence généré automatiquement pour les RDV |
| **Intégration CalDAV** | (Préparé) Synchronisation des agendas conseillers |
| **Monitoring Prometheus** | Métriques `/actuator/metrics` + Grafana |
| **ELK Stack** | Logs centralisés (Logstash + Elasticsearch + Kibana) |
| **CI/CD GitHub Actions** | 3 workflows : `backend.yml` · `backoffice.yml` · `flutter.yml` |

### 5.4 Données & médias

| Fonctionnalité | Détail |
|---|---|
| **PostgreSQL 16 + pgvector** | 768 dim, similarité cosinus (`<=>`) |
| **Recherche RAG 2 phases** | SQL natif cosinus → JPQL réhydratation (JOINED perd le discriminateur) |
| **Polymorphisme JPA JOINED** | `Fiche` abstraite + 4 sous-types, `@SuperBuilder` Lombok |
| **BaseEntity** | Long PK + UUID `trackingId` (exposé dans toutes les URLs REST) |
| **Héritage utilisateur** | `Utilisateur` abstraite → `Eleve` · `Parent` · `Conseiller` · `Administrateur` |
| **Historique unifié** | Journal de bord global partagé inter-modules |
| **Cache LRU mémoire** | (Préparé) pour la recherche RAG |
| **Matières préférées** | Stockées en CSV dans `TEXT` colonne, parsées par `EleveMapper` |
| **117 universités togolaises** | Seed `.md` + scripts `seed_universites.sh` |
| **Vidéothèque** | Upload, transcodage (à venir), association multi-fiches |
| **Carte interactive** | Coordonnées GPS sur les fiches Établissement |
| **Visite virtuelle 3D** | Lien vers visite 3D / vidéo (Module `sallevirtuelle/`) |
| **Recherches orphelines** | Tracking des mots-clés sans résultat (admin analytics) |
| **Fiches tendances** | Calcul sur 7 derniers jours |
| **Fiches récemment consultées** | Basé sur l'historique personnel |
| **Migration de schéma** | **Pas de Flyway/Liquibase** — `ddl-auto=update` (risque de perte de données) |

### 5.5 Internationalisation & accessibilité

| Fonctionnalité | Détail |
|---|---|
| **Multi-rôles utilisateur** | Un utilisateur peut cumuler (étudiant + handicap par ex.) |
| **Multi-langues (préparé)** | Traductions des fiches (français + langues locales) |
| **Adapté mobile-first** | L'application mobile est l'expérience principale |
| **Accessibilité visée** | Interface responsive, mode sombre, contraste élevé |

---

## Annexe : mapping cahier des charges ↔ implémentation

| Exigence du cahier | Statut | Implémenté dans |
|---|---|---|
| 1.1 Architecture & sécurité | ✅ | Backend `shared/security/`, JWT, 2FA, rate limit |
| 1.2 Auth & inscription | ✅ | Mobile `auth/`, Backend `auth/`, `profil/` |
| 1.3 Profil, documents, favoris, historique | ✅ | `profil/`, `bibliotheque/favoris/`, mobile `profile/` |
| 1.4 Comptes liés & familles | ✅ | `ParentController` + lien parent ↔ enfant |
| 1.5 Confidentialité & RGPD | ⚠️ | Export RGPD partiellement, droit à l'oubli en placeholder |
| 1.6 Intégration inter-modules | ✅ | `Historique` partagé |
| 1.7 Système de rôles flexible | ✅ | Héritage JPA, multi-rôles |
| 2. Bibliothèque centrale | ✅ | `bibliotheque/` (4 types de fiches, RAG) |
| 3. Diagnostic | ✅ | `diagnostic/` (RIASEC, OCR, recommandation 60/40) |
| 3.3 Saisie notes mobile | ❌ | Retirée du mobile — côté backoffice uniquement |
| 4. Accompagnement | ✅ | `accompagnement/` (FAQ, messages, RDV, visio Jitsi) |
| 4.7 WebSocket messages | ❌ | Polling 4s à la place |
| 5. Administration | ✅ | Backoffice (admin, conseiller, superadmin) |
| 5.6 Paramétrages système | ✅ | `ParametreController` + `superadmin/ParametresPage` |
| Visio Jitsi | ✅ | Lien auto-généré, à brancher côté mobile |
| OCR bulletins | ✅ | `OcrController` (OpenAI Vision / PDFBox) |
| ORIA assistant | ✅ | `OriaController` (cascade Ollama → Groq → OpenAI) |
| Maintenance mode | ✅ | `MaintenanceController` (in-memory) |
| Monitoring | ✅ | `docker-compose.monitoring.yml` (Prometheus + Grafana) |
| 2FA | ✅ | `TotpController` (TOTP Google Authenticator) |
| Mentorat | ✅ | `mentorat/` module complet |
| CV générateur | ✅ | `cvgenerateur/` |
| Badge gamification | ✅ | `badge/` |
| Multi-rôles utilisateur | ✅ | Héritage JPA |
| Sauvegardes automatiques | ⚠️ | Configuré côté serveur, pas dans l'app |
| Rapports automatiques | ⚠️ | Endpoint stats + export, programmation email à câbler |

> **Légende :** ✅ Implémenté · ⚠️ Partiellement · ❌ Retiré / non implémenté
