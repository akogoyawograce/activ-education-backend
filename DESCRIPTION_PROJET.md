# Activ Education — Description du Projet

> Plateforme innovante d'orientation scolaire et professionnelle au Togo.
> Accompagne les élèves et étudiants dans leurs choix d'avenir grâce à un accompagnement personnalisé et des outils technologiques de pointe.

---

## Architecture globale

```
Projet-activ-education/
├── activ-education-backend-main/       # Backend Spring Boot 4.0.5 (Java 21, Maven)
├── activ-education-fronted-main/
│   ├── activ_education/                # App mobile Flutter (Dart, setState)
│   ├── backoffice/                     # App web React 19 + TypeScript 6 + Tailwind v4
│   └── seed/                           # Scripts de seed (SQL, shell, 117 universités)
├── AGENTS.md / CLAUDE.md               # Mémoires partagées assistants IA
├── memoire_activ_education.md          # Mémoire de projet (Génie Logiciel)
├── generate_synthetic_data.py          # Génération données synthétiques ML
├── train_model.py / phase5_*.py        # Entraînement modèle prédictif
└── orientation_outcome_synthetic.csv   # Dataset synthétique orientation
```

---

## Backend — Spring Boot 4.0.5 / Java 21

### Stack technique

| Couche | Technologie |
|--------|-------------|
| Framework | Spring Boot 4.0.5 (Spring MVC, Data JPA) |
| Langage | Java 21 |
| Base de données | PostgreSQL 16 + pgvector |
| Cache / Rate-limiting | Redis |
| Stockage objet | MinIO (S3-compatible) |
| IA | OpenAI (embeddings + texte), Groq, Ollama (qwen2:0.5b) |
| Build | Maven |
| Tests | 9 fichiers (Spring context, Jackson, services, controllers) |

### Architecture — Package by Feature (29+ packages)

```
tg.edtch.activEducation/
├── shared/            # Configuration, sécurité JWT, MinIO, IA, WebSocket, BaseEntity
├── profil/            # Utilisateurs (Élève, Parent, Conseiller, Administrateur)
├── bibliotheque/      # Fiches (Série, Filière, Métier, Établissement), FAQ, Favoris
├── diagnostic/        # Quiz RIASEC, Questions, Réponses, Résultats, Matrices de score
├── accompagnement/    # Messages, RDV, Disponibilités
├── prediction/        # Module prédiction & recommandation IA (Phase 1-5)
├── alumni/            # Réseau des anciens
├── badge/             # Système de badges
├── calendrier/        # Calendrier / événements
├── cvgenerateur/      # Génération de CV
├── defis/             # Défis / challenges
├── emploi/            # Offres d'emploi
├── entretien/         # Simulations d'entretien
├── mentorat/          # Mentorat
├── portfolio/         # Portfolio élève
├── recommandation/    # Recommandations (LLM)
├── riasec/            # Test RIASEC (personnalité)
├── simulateur/        # Simulateur de parcours
├── temoignage/        # Témoignages
├── vae/               # Validation des Acquis de l'Expérience
└── ... (horsligne, parrainage, reseau, reorientation, sallevirtuelle,
         datahub, attestations, cahierdebord, cartemetiers, alums)
```

### Entités principales (23+ JPA)

- **Profil :** `Utilisateur` (abstraite) → `Eleve`, `Parent`, `Conseiller`, `Administrateur`, `Role`, `Document`, `Notification`, `Historique`, `NoteSaisiManuel`
- **Diagnostic :** `Quiz`, `Question`, `Reponse`, `ResultatDiagnostic`, `ScoreMatrice`, `SeuilAdmission`
- **Bibliothèque :** `Fiche` (abstraite, `InheritanceType.JOINED`, embedding pgvector) → `FicheMetier`, `FicheSerie`, `FicheFiliere`, `FicheEtablissement`, `Favori`, `EntreeFAQ`, `RechercheOrpheline`
- **Accompagnement :** `RendezVous`, `Message`, `Disponibilite`
- **Prédiction (nouveau) :** `NiveauFiliere`, `NotesHistorique`, `OrientationOutcome`, `EngagementSignal`

### API REST (133+ endpoints)

- `/api/v1/auth/*` — Authentification (login 2FA, refresh, logout)
- `/api/v1/eleves/*`, `/api/v1/parents/*`, `/api/v1/conseillers/*`, `/api/v1/administrateurs/*` — CRUD utilisateurs
- `/api/v1/quiz/*`, `/api/v1/questions/*`, `/api/v1/resultats-diagnostic` — Diagnostic RIASEC
- `/api/v1/bibliotheque/*` — CRUD fiches, recherche, favoris, FAQ, IA sémantique
- `/api/v1/rendez-vous/*`, `/api/v1/messages/*` — Accompagnement
- `/files/upload/*`, `/files/download/*` — MinIO (images, vidéos, documents)
- `/api/v1/eleves/*/notes-historique`, `/api/v1/eleves/*/orientation-outcome` — Prédiction
- `/api/v1/eleves/*/recommandation-ia/v2` — Moteur 3 signaux

### Sécurité

- **JWT filter toujours actif** (stateless, CSRF désactivé, CORS multi-origines)
- **3 couches :** SecurityConfig (chemins) → `@PreAuthorize` (méthodes) → SPEL bean `@security` (ownership)
- **Rôles :** `CONSEILLER`, `ADMIN`, `SUPER_ADMIN`
- **2FA** — si `requires2fa=true`, appel à `/auth/2fa/validate`
- **Rate-limiting** via Redis : login (20/15min), refresh (20/5min), API (200/1min)
- **Validation** `@Valid` sur DTOs → 400 JSON géré par `GlobalExceptionHandler`

### IA / Moteur de recommandation

- **Embeddings :** pgvector (768 dimensions) via OpenAI
- **RAG sémantique :** FAQ + recherche globale avec similarité cosinus (`<=>`)
- **Recommandation v2 (3 signaux) :**
  ```
  score_final = 0.50·score_realite + 0.35·score_aspiration + 0.15·score_engagement
  ```
  - `score_aspiration` : similarité cosinus RIASEC élève ↔ filière
  - `score_realite` : notes / seuil admission + bonus tendance
  - `score_engagement` : consultations, favoris, recherches RAG
- **Découvertes garanties** : N filières à fort score mais faible engagement (anti bulle de filtre)

### Intégrations

- **MinIO :** 3 buckets (images, vidéos, documents), upload max 500MB
- **OpenAI :** Embeddings + génération de texte
- **Groq / Ollama :** Fallback IA (qwen2:0.5b local)
- **ORIA :** Assistant vocal IA local

---

## Frontend Mobile — Flutter / Dart

### Stack technique

| Couche | Technologie |
|--------|-------------|
| Framework | Flutter (Material 3) |
| Langage | Dart (SDK >=3.3.0) |
| HTTP | Dio (5.7.0) |
| Stockage token | flutter_secure_storage |
| State management | ❌ Aucun (setState + singletons statiques) |
| Cartes | flutter_map + latlong2 |
| Graphiques | fl_chart |
| Assistant vocal | speech_to_text + flutter_tts |
| Polices | Inter, Poppins |

### Structure du code Flutter

```
lib/
├── main.dart                        # Entry point
├── models/                          # 15 fichiers (badge, bulletin, diagnostic, prédiction...)
├── screens/                         # 22 dossiers de fonctionnalités + main_scaffold
│   ├── auth/                        # Login, Register, OTP, Forgot Password
│   ├── onboarding/                  # Splash + Onboarding (3 slides)
│   ├── home/                        # Dashboard, profil, notifications, FAQ
│   ├── explorer/                    # Bibliothèque (fiches, recherche, favoris)
│   ├── diagnostic/                  # Quiz RIASEC, résultats, notes
│   ├── chat/                        # Messagerie (polling 4s)
│   ├── orientation/                 # Sélection niveau, bulletins, recommandation v2
│   ├── entretien/                   # Simulations d'entretien
│   ├── simulateur/                  # Simulateur de parcours
│   ├── portfolio/                   # Portfolio
│   ├── datahub/                     # Data hub
│   └── ...
├── services/                        # 21 fichiers (API, auth, diagnostic, prédiction...)
├── theme/                           # Routes + thème
├── utils/                           # Image utils, profile completion
└── widgets/                         # bottom_nav, skeleton, recommendations
```

### Fonctionnalités clés

- **Dashboard** bachelier avec stats, notes, RDV, messages, actions rapides
- **Explorer** (bibliothèque) : 5 onglets, recherche, grille, favoris
- **Diagnostic RIASEC** : Quiz avec arbre de décision, résultats avec recommandations
- **Messagerie** : Chat avec polling 4s, liste groupée, swipe-to-delete
- **Prise de RDV** : CRUD, sélection conseiller
- **Recommandation v2 (3 signaux)** : Parcours de sélection niveau → saisie bulletins → top 10 filières
- **Assistant vocal** (ORIA) : Questions par voix
- **Badges, Alumni, Défis, Mentorat**

### Tests

3 fichiers : widget smoke, model serialization, API integration

---

## Backoffice — React 19 + TypeScript 6 + Tailwind v4

### Stack technique

| Couche | Technologie |
|--------|-------------|
| Framework | React 19.2 |
| Langage | TypeScript 6.0 (strict, erasableSyntaxOnly) |
| Bundler | Vite 8 |
| Routing | react-router-dom v7 |
| Data fetching | @tanstack/react-query v5 |
| State management | Zustand v5 |
| UI | Tailwind v4, Lucide icons |
| Graphiques | Recharts |
| HTTP | Axios |
| Tests | ❌ Aucun framework de test |

### Structure du code

```
src/
├── main.tsx / App.tsx                # Entry point
├── api/                              # 15 modules API (auth, eleves, conseillers, quiz...)
├── components/
│   ├── layout/                       # Layout principal, sidebar, header
│   ├── shared/                       # Composants réutilisables
│   └── ui/                           # UI primitives
├── pages/
│   ├── login/                        # Connexion
│   ├── conseiller/                   # Espace conseiller
│   ├── admin/                        # Espace administrateur
│   └── superadmin/                   # Espace super-administrateur
├── stores/                           # authStore (Zustand)
├── types/                            # Types partagés
└── lib/                              # Utilitaires
```

### Rôles et accès

- **CONSEILLER** : Gestion des élèves, RDV, messagerie
- **ADMIN** : Modération contenu, quiz, FAQ, statistiques
- **SUPER_ADMIN** : Gestion des administrateurs, configuration globale

---

## Seed & Données

### Scripts de seed (à exécuter dans l'ordre)

1. `seed_users.sql` — 7 utilisateurs (superadmin, moderateur, gestionnaire, conseiller, eleve, parent)
2. `seed_bibliotheque.sh` — 13 séries, 32 filières, 42 métiers, 22 établissements, 12 FAQ
3. `seed_etablissement_images.sh` — Images placeholder via multipart
4. `seed_quiz.sh` — Quiz RIASEC (30Q) + Personnalité (5Q)
5. `seed_universites.sh` — 117 établissements supérieurs togolais

### Base documentaire

- `seed/universites/` — 117 dossiers avec fichiers `.md` décrivant chaque établissement
- Cahier des charges, état du projet, prompts d'IA, documentation des endpoints

---

## Module Prédiction (Phase 1-5)

### Modèle de données (4 nouvelles tables)

| Table | Description |
|-------|-------------|
| `niveaux_filieres` | Mapping niveau ↔ filière éligible |
| `notes_historique` | Moyennes annuelles sur 3 ans (trajectoire) |
| `orientation_outcome` | Suivi des choix d'orientation (entraînement supervisé) |
| `engagement_signal` | Signaux comportementaux agrégés |

### Algorithme de recommandation v2 (3 signaux)

- **Pondération :** 50% réalité académique, 35% aspirations RIASEC, 15% engagement
- **Anti bulle de filtre :** N découvertes garanties dans le top 10
- **Endpoint :** `GET /api/v1/eleves/{id}/recommandation-ia/v2`

### Pipeline ML (Phase 5)

- Dataset synthétique généré (`orientation_outcome_synthetic.csv`, 1200+ lignes)
- Modèle ML pour prédire la réussite en filière
- Entraînement sur données réelles en cours

---

## Déploiement

### Services Docker

| Service | Port (hôte) |
|---------|-------------|
| Spring Boot API | 8080 |
| PostgreSQL | 5432 (5433 en Docker) |
| MinIO API | 9000 |
| MinIO Console | 9001 |
| Redis | 6379 |
| Monitoring (optionnel) | Stack Grafana/Prometheus |

### Commandes principales

```bash
# Services seulement (dev local)
docker compose up -d db minio redis

# Stack complète
docker compose up -d --build

# Build backend
./mvnw clean install
./mvnw spring-boot:run

# Frontend mobile
cd activ-education-fronted-main/activ_education
flutter pub get && flutter run

# Backoffice
cd activ-education-fronted-main/backoffice
npm install && npm run dev
```

---

## Points d'attention (gotchas)

- **ddl-auto=update** — Pas de Flyway/Liquibase, risque de perte de données
- **Secrets commités** dans `.env` (JWT_SECRET, OPENAI_API_KEY, GROQ_API_KEY) — à rotationner avant déploiement
- **401 sur update** — `@Valid` sur DTOs sans mot de passe → erreur 400 capturée par le filtre JWT → 401
- **MinIO 500** — `MinioExceptionHandler` doit avoir `@Order(HIGHEST_PRECEDENCE)`
- **Flutter web instable** — Dart compiler peut planter, F5 plein nécessaire
- **RIASEC catalogue en dur** — 15 profils typiques seulement
- **Pas de tests backoffice** — Aucun framework installé
- **Redis manquant dans docker-compose** — Rate limiting / blacklist cassé en Docker

---

## Roadmap

- [x] Phase 1 : Data Definition Layer (entités JPA, repositories)
- [x] Phase 2 : Business Logic (algorithmes, services)
- [x] Phase 3 : Interface API & Sécurité (contrôleurs REST, JWT, 2FA)
- [x] Phase 4 : Front-end Flutter + Backoffice
- [ ] Phase 5 : Module Prédiction & ML (modèle entraîné en production)
- [ ] Déploiement Cloud & QA
- [ ] Tests backend + frontend
- [ ] WebSocket pour le chat (vs polling 4s)
- [ ] Migrations DB (Flyway/Liquibase)

---

> *"Réussir votre orientation commence par le bon diagnostic."* — L'équipe Activ Education
