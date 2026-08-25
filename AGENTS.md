# Activ Education — AGENTS.md

## Repository structure

```
activ-education-backend-main/      # Spring Boot 4.0.5 (Java 21, Maven) — root pkg tg.edtch.activEducation
activ-education-fronted-main/
├── activ_education/               # Flutter mobile (Dart, setState), entry: lib/main.dart
├── backoffice/                    # React 19 + TS 6 + Tailwind v4, entry: src/main.tsx
└── seed/                          # SQL + shell scripts + 118 university markdown files
scripts/                           # Python imports (ex. import_etablissements_xlsx.py)
```

## Commands

### Backend (workdir: activ-education-backend-main/)
```
docker compose up -d db minio redis   # Services only (app runs locally)
docker compose up -d --build           # Full stack (db :5433, minio :9000/9001, redis :6379, app :8080)
./start-backend.sh                     # **canonical local dev** — sources .env.local, else DB localhost:5433
BACKEND_PROD=1 ./start-backend.sh      # rebuild JAR + java -jar
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw clean install                   # Full build with tests
./mvnw -Dtest=AuthServiceTest test     # Single test
```
- DB: `localhost:5432` (5433 in Docker), user `postgres`, pass `abalakata`, db `activ_education`
- `ddl-auto=update` — **no Flyway/Liquibase**, schema changes risk data loss
- **Secrets are placeholders now**: `.env` (tracked) contains `REVOKED_REPLACE_ME` / throwaway values; real keys live in **`.env.local`** (gitignored) — `start-backend.sh` loads it. Both are reviewed before any deploy.
- Rate limits disabled in dev (`99999` in `application-dev.properties`)
- Swagger: `http://localhost:8080/swagger-ui.html`

### Flutter (workdir: activ-education-fronted-main/activ_education/)
```
flutter pub get
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
- Role comparison must use `.toUpperCase()` (backend returns PascalCase like `"Parent"`)
- **`bottom_nav.dart`** — each `_NavItem` in `Expanded`, never revert to `spaceAround`
- Dart `docker compose` services **must be up first**: backend down → every Dio call fails ERR_CONNECTION_REFUSED (`./start-backend.sh`)

### Backoffice (backoffice/)
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

Package by Feature (~35 feature packages). Core modules: `profil`, `bibliotheque`, `diagnostic`, `accompagnement`, `shared` + many features (`alumni`, `badge`, `calendrier`, `defis`, `emploi`, `entretien`, `mentorat`, `portfolio`, `recommandation`, `riasec`, `simulateur`, `temoignage`, `vae`, `prediction`, `attestations`, ...).

- All entities extend `BaseEntity` (Long PK + UUID `trackingId` in REST URLs)
- All write endpoints use `@Valid` on DTOs — validation errors produce 400 at `/error`, caught by JWT filter → 401
- Lombok `@SuperBuilder` on abstract `Fiche` hierarchy with `InheritanceType.JOINED` (native SQL loses discriminator → two-phase: native SQL vector search, then JPQL hydration)
- **Séparation `mentorat/` (API) vs `alumni/Mentorat` (entité JPA)**: `alumni.Mentorat` est l'entité racine (table + repository), `mentorat/` est l'API REST dédiée (statut, séances). `MentoratService` consomme `alumni.MentoratRepository`. Confirmer ce pattern avant tout ajout.
- **pgvector NOT installed locally** (DB Docker `:5433`) — `fiches.embedding` est `real[]`, pas `vector`. RAG vectoriel désactivé dans `OriaService.rechercherContexteVectoriel` (retourne `null` → fallback mot-clé). Voir `JOURNAL_BORD_IA.md`.
- MinIO: 3 buckets (images/videos/documents), upload via `/files/upload/{fileType}`, max 500MB
- `DataLoader.java` seeds default admin (`admin@activeducation.tg`) on startup

### AI / LLM chain (highly relevant in dev)
- Chat: **dev profile overrides `openai.api.chat.url` → Groq** (`https://api.groq.com/openai/v1/chat/completions`, model `llama-3.1-8b-instant`); prod points to OpenAI `gpt-4o-mini`
- Fallback chain for chat AND embeddings: **OpenAI → Ollama local (`qwen2:0.5b`)** — set in both `generateAnswer()` and `generateEmbedding()` (missing fallback was a real bug, don't remove)
- **429 (quota) in `generateAnswer()` throws a dedicated exception — NO Ollama fallback** (Ollama won't fix an OpenAI quota)
- Typically OpenAI key in `.env` is `REVOKED_REPLACE_ME` → chat falls back to Groq (dev) or Ollama (local) — see `RECAP_SESSION_2026-08-06.md`

## Security

- JWT filter always active (stateless, CSRF disabled, CORS multi-origins)
- **Three-layer defense**: SecurityConfig (path-based) → `@PreAuthorize` (method-based) → SPEL `@security` bean (ownership)
- All unprotected paths default to `.authenticated()` (last rule in SecurityConfig)
- `@PreAuthorize` on controllers with custom SPEL beans:
  - `isOwner(#trackingId)`, `isOwnChild(#eleveTrackingId)`, `isOwnConseiller(#conseillerTrackingId)`, `isRdvParticipant(#rdvTrackingId)`
- Rate limiting Redis: login (20/15min), refresh (20/5min), API (200/1min)
- Files: IMAGE downloads public, DOCUMENT/PDF downloads require authentication
- CI/CD: GitHub Actions with 3 workflows (backend build+test, backoffice build+lint, flutter analyze)

## Before any modification

1. **Read `seed/cahier_de_charge.md`** to confirm the feature is specified
2. **Check existing controllers + `security/config/SecurityConfig.java`** before creating new endpoints — **270+ already mapped** (`@RequestMapping` count)
3. **Do not duplicate routes** — verify exact path isn't already mapped, e.g. via `seed/api-endpoints.md`

## Typed AI response DTOs (recent convention)

- `RecommandationIAResponse` (record with enum `type ∈ {OK, PROFIL_INCOMPLET, ERREUR_LLM}`) — widget « Ma recommandation » (`dashboard_bachelier.dart`) is color-coded per status; SERIALIZED `{recommandation, type}` — do NOT return raw `Map`.
- `EntretienResponse` now has `erreur`/`messageErreur` fields + `@JsonInclude(NON_NULL)` — Flutter shows a red band + "Réessayer" button (see `entretien_screen.dart`)

## Décisions d'architecture (mobile)

- **Fonctionnalité NOTE retirée du mobile** : saisie manuelle des notes, upload OCR de bulletins, et analyse des résultats scolaires exclus du mobile (restent côté backoffice)

## Key gotchas

- **`.env` contains `REVOKED_REPLACE_ME` / old secrets** — rotate before any deploy; real keys in ignored `.env.local`
- **22 backend test files** — not 9 (see list below for names). Run one via `./mvnw -Dtest=? test`
- **Deployed DB is empty** — run `seed/*.sh` scripts in order (need JWT from `admin@activeducation.tg`, pass otherwise in `start-backend.sh` docs)
- **`ParentRequest.java` / `ConseillerRequest.java`** — `motDePasse` `@NotBlank`/`@Size` removed (was causing 401 on update)
- **Backoffice admin pages** (quiz editor FAQ moderation, stats, ...) are partially stubs/mock data
- **`matieresPreferees`** stored as CSV in `TEXT` column, parsed by `EleveMapper`
- **`GET /api/v1/eleves/{id}/resultats-diagnostic`** returns `Page<>` but Flutter calls without pagination
- **OCR** — image extraction requires `OPENAI_API_KEY` (PDF uses PDFBox, no key)
- **2FA login** — if `requires2fa=true`, client must call `/auth/2fa/validate` with challengeToken
- **ORIA** — Ollama (`qwen2:0.5b`) local → Groq → OpenAI fallbacks; Flutter Dio timeout set to 120s
- **Maintenance mode** — static in-memory flag (not persistent)
- **Monitoring** — requires `docker compose -f docker-compose.monitoring.yml up -d` separately
- **Flutter web** unstable (`Dart compiler exited unexpectedly`) / DDC doesn't reset new State fields — do full **F5** after adding fields
- **MinIO 500** on `files/download`: 1) `MinioExceptionHandler` needs `@Order(HIGHEST_PRECEDENCE)` before `GlobalExceptionHandler` 2) import the wrapper class, not `java.io.FileNotFoundException` 3) `contentLength()` can NPE if `fileSize` null
- **Simulateur parcours** — bulletin slots auto by level (`_slotsPourNiveau()`), série masquée pour collège/supérieur
- **XLSX import** — `scripts/import_etablissements_xlsx.py` + `Base_Etablissements_Superieurs_Togo.xlsx` (root) → `EtablissementDetailsXlsx*` (bibliotheque) → `/api/v1/bibliotheque/etablissements/{trackingId}/details-xlsx`
- **Explorer personnalisé** — `GET /api/v1/bibliotheque/filieres` et `/series` acceptent `?eleveTrackingId=` : tri par pertinence profil (cosinus RIASEC `ProfilFiliereRiasecCatalog` + mots-clés), repli createdAt DESC si profil incomplet. Flutter transmet le trackingId connecté (`explorer_screen.dart`)

## ML Pipeline (workdir: projet racine)

```bash
# 1. Générer les données synthétiques (Phase 0)
python3 generate_synthetic_data.py

# 2. Entraîner (LogisticRegression + GradientBoosting)
python3 train_model.py          # → models/gb_*.joblib + results_prototype.json

# 3. (Quand ≥ 5000 orientation_outcome réels) Phase 5 :
JWT=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@activediction.tg","motDePasse":"abalakata"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin).get('token',''))")
python3 phase5_export_dataset.py --token "$JWT" --out real_dataset.csv
python3 phase5_train_real.py --csv real_dataset.csv   # → results_phase5.json
```
- Modèles : `LogisticRegression` (class_weight=balanced) + `GradientBoostingClassifier`
- Configs : `sans_comportemental` (RIASEC + notes + 60/40) et `avec_comportemental` (+ comportemental)
- 7 niveaux : COLLEGE → BAC_3, prédiction ADMIS vs REORIENTE par filière
- Phase 5 nécessite ≥ 5 000 orientation_outcome réels (DB vide actuellement)