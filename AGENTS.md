# Activ Education — AGENTS.md

## Repository structure

```
activ-education-backend-main/      # Spring Boot 4.0.5 (Java 21, Maven)
activ-education-fronted-main/
├── activ_education/               # Flutter mobile (Dart, setState), entry: lib/main.dart
├── backoffice/                    # React 19 + TS 6 + Tailwind v4, entry: src/main.tsx
└── seed/                          # SQL + shell scripts + 117 university markdown files
```

## Commands

### Backend (workdir: activ-education-backend-main/)
```
docker compose up -d db minio redis   # Services only (app runs locally)
docker compose up -d --build           # Full stack (db :5433, minio :9000/9001, redis :6379, app :8080)
./mvnw spring-boot:run                 # Local dev (needs DB on :5432)
./mvnw clean install                   # Full build with tests
./mvnw package -DskipTests             # Fast rebuild
```
- DB: `localhost:5432` (5433 in Docker), user `postgres`, pass `abalakata`, db `activ_education`
- `ddl-auto=update` — **no Flyway/Liquibase**, schema changes risk data loss
- Secrets (`JWT_SECRET`, `OPENAI_API_KEY`, `GROQ_API_KEY`) must be set in env or `.env` — **all committed in .env**, rotate before deploying
- Rate limits disabled in dev (`99999` in `application-dev.properties`)
- Swagger: `http://localhost:8080/swagger-ui.html`

### Flutter (workdir: activ-education-fronted-main/activ_education/)
```
flutter pub get
flutter run
flutter test         # 3 test files (widget smoke, model serialization, API integration)
flutter analyze      # dart analyze lib/ — expect 0 errors
```
- `API_BASE_URL` in `.env` — **no `/api/v1` suffix** (backoffice adds it)
- **No state management** — `setState` + static singletons on `BaseService`
- `flutter_secure_storage` fails on web — always wrap in try-catch with in-memory fallback
- 401 interceptor: `_refreshWithLock()` in `base_service.dart`, skips `/auth/login` and `/auth/refresh`
- 4-second polling for chat (no WebSocket)
- Android: `adb reverse tcp:8080 tcp:8080` to reach `localhost:8080`
- `image_picker` must be wrapped in try-catch (`PlatformException(already_active)`)
- **`bottom_nav.dart`** — each `_NavItem` in `Expanded`, never revert to `spaceAround`

### Backoffice (workdir: activ-education-fronted-main/backoffice/)
```
npm install
npm run dev     # Vite dev server :5174
npm run build   # tsc -b && vite build
npm run lint
```
- **No test framework**; TS 6 strict (`erasableSyntaxOnly`: no enums, namespaces, parameterProperties)
- `VITE_API_BASE_URL` includes `/api/v1` suffix
- `@/` → `src/`; react-router-dom v7, @tanstack/react-query v5, Zustand, Tailwind v4, Recharts, Lucide, Axios
- 3 role levels: `CONSEILLER`, `ADMIN`, `SUPER_ADMIN` via `ProtectedRoute`

## Backend architecture

31+ packages (Package by Feature). Original 5 core modules (`profil`, `bibliotheque`, `diagnostic`, `accompagnement`, `shared`) + many feature packages (`alumni`, `badge`, `calendrier`, `defis`, `emploi`, `entretien`, `mentorat`, `portfolio`, `recommandation`, `riasec`, `simulateur`, `temoignage`, `vae`, etc.).

- All entities extend `BaseEntity` (Long PK + UUID `trackingId` in REST URLs)
- All write endpoints use `@Valid` on DTOs — **validation errors produce 400 at `/error`, caught by JWT filter → 401**
- Lombok `@SuperBuilder` on abstract `Fiche` hierarchy with `InheritanceType.JOINED`
- Two-phase pgvector: native SQL for vector search, then JPQL for entity hydration (JOINED loses discriminator in native queries)
- **Séparation `mentorat/` (API) vs `alumni/Mentorat` (entité)** : `alumni.Mentorat` est l'entité JPA racine (table + repository), `mentorat/` est l'API REST dédiée aux programmes de mentorat (statut, séances). `MentoratService` consomme `alumni.MentoratRepository` — pas de duplication de persistance. Confirmer ce pattern avant tout ajout sur l'un ou l'autre.
- **⚠️ pgvector NOT installed locally** (DB Docker `:5433`) — `fiches.embedding` is `real[]`, not `vector`. RAG vectoriel désactivé dans `OriaService.rechercherContexteVectoriel` (retourne `null` → fallback mot-clé). Voir `JOURNAL_BORD_IA.md` §4 pour le plan de réactivation.
- MinIO: 3 buckets (images/videos/documents), upload via `/files/upload/{fileType}`, max 500MB
- `DataLoader.java` seeds default admin (`admin@activeducation.tg`) on startup
- AI: **OpenAI** (migrated from Gemini in Session 4) — `AIEmbeddingService` → `OpenAIEmbeddingServiceImpl`

## Security

- JWT filter always active (stateless, CSRF disabled, CORS multi-origins)
- **Three-layer defense**: SecurityConfig (path-based) → `@PreAuthorize` (method-based) → SPEL `@security` bean (ownership)
- All unprotected paths default to `.authenticated()` (last rule in SecurityConfig)
- `@PreAuthorize` on controllers with custom SPEL bean `@security`:
  - `isOwner(#trackingId)`, `isOwnChild(#eleveTrackingId)`, `isOwnConseiller(#conseillerTrackingId)`, `isRdvParticipant(#rdvTrackingId)`
- Role comparison in Flutter must use `.toUpperCase()` (backend returns PascalCase like `"Parent"`)
- `.env` secrets (JWT, OpenAI, Groq) excluded via `.gitignore` — rotate before deploying
- Rate limiting via Redis: login (20/15min), refresh (20/5min), API (200/1min)
- Files: IMAGE downloads public, DOCUMENT/PDF downloads require authentication
- GlobalExceptionHandler handles `@Valid` validation errors → 400 JSON (not `/error` 401)
- CI/CD: GitHub Actions with 3 workflows (backend build+test, backoffice build+lint, flutter analyze)

## Before any modification

1. **Read `seed/cahier_de_charge.md`** to confirm the feature is specified
2. **Check existing controllers + `SecurityConfig.java`** before creating new endpoints — 133+ endpoints exist
3. **Do not duplicate routes** — verify exact path isn't already mapped

## Décisions d'architecture (mobile)

- **Fonctionnalité NOTE retirée du mobile** : la saisie manuelle des notes, l'upload OCR de bulletins, et l'analyse des résultats scolaires sont exclus de l'application mobile. Ces fonctionnalités restent disponibles côté backoffice si nécessaire.

## Key gotchas

- **Secrets committed** in `.env` (OPENAI_API_KEY, JWT_SECRET, GROQ_API_KEY) — rotate before any deploy
- **9 backend test files** exist (ActivEducationApplicationTests, JacksonTest, EleveServiceTest, AuthServiceTest, AuthControllerTest, StatsControllerTest, StatsServiceTest, TestControllerTest, TestBeansConfig) — not just 3
- **Deployed DB is empty** — run `seed/*.sh` scripts in order (need JWT from admin account)
- **`ParentRequest.java` / `ConseillerRequest.java`** — `motDePasse` `@NotBlank`/`@Size` removed (was causing 401 on update)
- **Backoffice admin pages** (quiz editor, FAQ moderation, stats) are partially stubs/mock data
- **`matieresPreferees`** stored as CSV in `TEXT` column, parsed by `EleveMapper`
- **`GET /api/v1/eleves/{id}/resultats-diagnostic`** returns `Page<>` but Flutter calls without pagination
- **OCR** requires `OPENAI_API_KEY` for image extraction (PDF uses PDFBox without key)
- **2FA login** — if `requires2fa=true`, client must call `/auth/2fa/validate` with challengeToken
- **ORIA** uses Ollama (`qwen2:0.5b`) locally, falls back to Groq, then OpenAI; Flutter Dio timeout set to 120s
- **Maintenance mode** — static in-memory flag (not persistent)
- **Monitoring** — requires `docker compose -f docker-compose.monitoring.yml up -d` separately
- **Flutter web** unstable (`Dart compiler exited unexpectedly`) — DDC hot reload ne réinitialise pas les nouveaux champs State, faire F5 plein
- **`bottom_nav.dart`** — each `_NavItem` in `Expanded`, never revert to `spaceAround`
- **MinIO 500 bug** — si `files/download` retourne 500 : 1) `MinioExceptionHandler` doit avoir `@Order(HIGHEST_PRECEDENCE)` pour passer avant `GlobalExceptionHandler` 2) l'import du handler doit être la classe custom pas `java.io.FileNotFoundException` 3) `contentLength()` peut NPE si `fileSize` null
- **Simulateur parcours** — les slots de bulletins sont automatiques selon le niveau (`_slotsPourNiveau()`), la série scolaire est masquée pour collège/supérieur
- **`bottom_nav.dart`** — each `_NavItem` in `Expanded`, never revert to `spaceAround`

## ML Pipeline : entraînement jusqu'à la fin (workdir: projet racine)

```bash
# 1. Générer les données synthétiques (Phase 0)
python3 generate_synthetic_data.py

# 2. Entraîner les modèles (LogisticRegression + GradientBoosting)
python3 train_model.py          # → models/gb_*.joblib + results_prototype.json

# 3. (Quand ≥ 5000 orientation_outcome réels) Phase 5 :
JWT=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@activeducation.tg","motDePasse":"abalakata"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin).get('token',''))")
python3 phase5_export_dataset.py --token "$JWT" --out real_dataset.csv
python3 phase5_train_real.py --csv real_dataset.csv   # → results_phase5.json
```

- Modèles : `LogisticRegression` (class_weight=balanced) + `GradientBoostingClassifier`
- Configs : `sans_comportemental` (RIASEC + notes + 60/40) et `avec_comportemental` (+ comportemental)
- 7 niveaux : COLLEGE → BAC_3, prédiction ADMIS vs REORIENTE par filière
- Phase 5 nécessite ≥ 5 000 orientation_outcome réels (DB vide actuellement)

## Session 2026-07-25 — Bug Flutter web : ERR_CONNECTION_REFUSED

### Symptôme
- App Flutter web tente de joindre `http://localhost:8080/api/v1/...` mais toutes les requêtes Dio échouent avec `net::ERR_CONNECTION_REFUSED`
- Stack traces massives dans la console navigateur lors du chargement de `splash_screen.dart`, `main_scaffold.dart`, `dashboard_bachelier.dart`, `profile_screen.dart`, `login_screen.dart`
- `LOGIN ERROR: DioException [connection error]` à chaque tentative de POST `/api/v1/auth/login`
- Endpoints appelés en boucle (probable retry automatique via `_refreshWithLock()`) : `/eleves/{id}`, `/rendez-vous/eleve/{id}`, `/utilisateurs/{id}/messages/non-lus/compteur`, `/eleves/{id}/resultats-diagnostic`, `/eleves/{id}/recommandation-ia`, `/bibliotheque/favoris/utilisateur/{id}`, `/utilisateurs/{id}/historique`, `/eleves/{id}/documents/count`

### Cause racine identifiée
- Le backend Spring Boot sur `localhost:8080` n'était pas démarré
- Le JAR compilé existe : `activ-education-backend-main/target/activEducation-0.0.1-SNAPSHOT.jar`
- Script de lancement disponible : `start-backend.sh` à la racine (exporte `DB_PASSWORD`, `DB_HOST`, `DB_PORT`, etc. puis lance le JAR)

### Fichiers clés
- `activ-education-fronted-main/activ_education/lib/services/base_service.dart:108` — `dioGet()` (auth interceptor ligne 145, error handler ligne 191)
- `activ-education-fronted-main/activ_education/lib/services/auth_service.dart:142` — `getEleve()`, `:11` `login()`
- `activ-education-fronted-main/activ_education/lib/services/api_service.dart` — définitions endpoints (lignes 57, 126, 133, 167, 172, 245)
- `activ-education-fronted-main/activ_education/lib/main.dart:61` — charge `.env` via dotenv avant `runApp`
- `activ-education-backend-main/.env` — config backend (DB, JWT_SECRET, OPENAI_API_KEY, GROQ_API_KEY, etc.)
- `start-backend.sh` — wrapper de lancement avec variables DB

### Workaround temporaire
- Le frontend ne peut rien faire tant que le backend n'est pas lancé
- Pas de fallback offline implémenté dans `BaseService`
