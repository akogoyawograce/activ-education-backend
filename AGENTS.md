# Activ Education — AGENTS.md

## Repository structure

```
activ-education-backend-main/   # Spring Boot 4.0.5 (Java 21, Maven)
activ-education-fronted-main/
├── activ_education/            # Flutter mobile (Dart, setState), entry: lib/main.dart
├── backoffice/                 # React 19 + TS 6 + Tailwind v4, entry: src/main.tsx
└── seed/                       # SQL + shell scripts + 117 university markdown files
```

## Commands

### Backend (workdir: activ-education-backend-main/)
```
docker compose up -d db minio redis    # Services only (app runs locally)
docker compose up -d --build           # Full stack (db :5433, minio :9000/9001, redis :6379, app :8080)
./mvnw spring-boot:run                  # Local dev (needs DB on :5432)
./mvnw clean install                    # Full build with tests
./mvnw package -DskipTests              # Fast rebuild
```
- DB: `localhost:5432` by default (5433 in Docker), user `postgres`, pass `abalakata`, db `activ_education`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Default admin: `admin@activeducation.tg` / `admin123!`
- `ddl-auto=update` — **no Flyway/Liquibase**, schema changes risk data loss
- **Redis** in docker-compose but missing on some deployments — rate limiting/token blacklist may fail

### Flutter (workdir: activ-education-fronted-main/activ_education/)
```
flutter pub get
flutter run
flutter test         # 3 test files (widget smoke, model serialization, API integration)
flutter analyze      # dart analyze lib/ — expect 0 errors
```
- `API_BASE_URL` in `.env` — **no `/api/v1` suffix** (backoffice adds it)
- **No state management library** — uses `setState` + static singletons on `BaseService`, not Provider/Riverpod/Bloc
- `flutter_secure_storage` **fails on web** — always wrap in try-catch with in-memory fallback
- 401 interceptor: `_refreshWithLock()` in `base_service.dart`, `deleteAll()` if refresh fails
- 4-second polling for chat (no WebSocket)
- Splash screen decodes JWT to check `exp` — expired tokens redirect to onboarding

### Backoffice (workdir: activ-education-fronted-main/backoffice/)
```
npm install
npm run dev     # Vite dev server :5174
npm run build   # tsc -b && vite build
npm run lint
```
- **No test framework**; TS 6 strict (`erasableSyntaxOnly`: no enums, no namespaces, no parameterProperties)
- `VITE_API_BASE_URL` includes `/api/v1` suffix
- `@/` alias maps to `src/` (configured in `vite.config.ts`)
- Stack: react-router-dom v7, @tanstack/react-query v5, Zustand, Tailwind v4, Recharts, Lucide, Axios
- 3 role levels in router: `CONSEILLER`, `ADMIN`, `SUPER_ADMIN` via `ProtectedRoute`

## Seed scripts & data setup

Full details in `activ-education-fronted-main/seed/AGENTS.md`. Deployed DB starts empty; run scripts in order with a JWT from `admin@activeducation.tg`.

## Backend architecture

5 modules (Package by Feature): `profil` (users/auth), `bibliotheque` (library/fiches), `diagnostic` (quizzes), `accompagnement` (appointments/messages), `shared` (MinIO, security, utils). ~27 controllers, ~133 endpoints.

- All entities extend `BaseEntity` (Long PK + UUID `trackingId` exposed in REST URLs)
- All write endpoints use `@Valid` on DTOs — **validation errors produce 400 at `/error`, caught by JWT filter → 401**
- Lombok `@SuperBuilder` on abstract `Fiche` hierarchy with `InheritanceType.JOINED`
- Two-phase pgvector: native SQL for vector search, then JPQL for entity hydration (JOINED loses discriminator in native queries)
- MinIO: 3 buckets (images/videos/documents), upload via `/files/upload/{fileType}`, max 500MB
- `@Async` for orphan search tracking, Gemini embeddings, analytics logging
- `DataLoader.java` seeds the default admin account (`admin@activeducation.tg`) on startup — check/modify there for seed changes

## Security model

- JWT filter always active (stateless, CSRF disabled, CORS all origins)
- `@PreAuthorize` on every controller with custom SPEL bean `@security`:
  - `@security.isOwner(#trackingId)` — resource ownership
  - `@security.isOwnChild(#eleveTrackingId)` — Parent access to child data
  - `@security.isOwnConseiller(#conseillerTrackingId)` — Conseiller self-access
  - `@security.isRdvParticipant(#rdvTrackingId)` — RendezVous participant check
- Role comparison in Flutter must use `.toUpperCase()` (backend returns `"Parent"` PascalCase)

## Before any modification

1. **Read `seed/cahier_de_charge.md`** to confirm the feature is specified
2. **Check existing controllers + `SecurityConfig.java`** before creating new endpoints
3. **Do not duplicate routes** — verify the exact path isn't already mapped

## Features ajoutées (sprint CDC)

### 1. 2FA/TOTP (juin 2026)
- **Backend**: `TotpSecret` entity, `TotpService` (RFC 6238, HMAC-SHA1, Base32), `TotpController` (`/api/v1/auth/2fa/*`)
- Login flow: si TOTP activé → `requires2fa: true` + challengeToken → client doit appeler `/auth/2fa/validate`
- **Flutter**: `TotpSetupScreen`, `TotpVerifyScreen`, routes `/totp-setup`, `/totp-verify`
- **Backoffice**: `LoginPage.tsx` avec étape TOTP, `authStore.ts` avec `completeTotpLogin()`
- Configuration : aucune clé externe nécessaire (HMAC-SHA1 natif Java)

### 2. OCR bulletins
- `OcrService` : PDFBox pour PDF, Gemini Vision pour images
- `GeminiEmbeddingService.extractTextFromImage()` — envoie l'image en base64 à Gemini
- Endpoint: `POST /api/v1/eleves/{trackingId}/ocr` → retourne `[{matière, note, coefficient}]`
- 2 parsers : regex pour PDF texte, JSON parsing pour réponse Gemini

### 3. Système de tickets
- `Ticket` entity (statuts: OUVERT→ASSIGNE→EN_COURS→RESOLU→FERME)
- `TicketService` : création, assignation round-robin, changement statut, ajout message
- Endpoints: `POST /api/v1/tickets`, `PATCH /{id}/statut`, `POST /{id}/messages`, etc.
- `TicketController` avec statistiques par statut

### 4. WYSIWYG editor
- `RichTextEditor.tsx` component React (contentEditable + execCommand)
- Barre d'outils : gras, italique, souligné, listes, titres, liens
- Utilisable dans les pages admin backoffice (QuizEditor, fiches, FAQ)

### 5. Versioning fiches (paper_trail)
- `VersionHistorique` entity + `VersionHistoriqueRepository`
- `VersioningService` : `enregistrerCreation()`, `enregistrerModification()`, `enregistrerSuppression()`
- Calcul automatique des changements (diff JSON entre ancien/nouvel état)
- Table `versions_historique` : item_type, item_tracking_id, event, whodunnit, object_data, object_changes

### 6. Consentement parental
- `ConsentementParental` entity : `eleve_id`, `email_parent`, `token_validation`, `consenti`, `ip_validation`
- `ConsentementParentalService` : `demanderConsentement()`, `validerConsentement(token)`, `necessiteConsentement(dateNaissance)`
- Règle : si âge < 15 ans → bloquer rôles + demander consentement
- Endpoint public: `GET /api/v1/consentement/valider?token=xxx`

### 7. Notifications push
- `NotificationPushService` : envoi FCM (Firebase Cloud Messaging) + sauvegarde en DB
- Fallback silencieux si `push.fcm.server-key` non configuré
- Notification sauvegardée dans la table `notifications` existante

### 8. Rappels SMS
- `SmsService` : support Twilio + Orange API
- Configuration via `sms.provider`, `sms.api-key`, `sms.sender`
- Mode simulation loggué si aucun provider configuré

### 9. Mode maintenance
- `MaintenanceFilter` (Servlet Filter @Order(1)) : vérifie flag statique `maintenanceMode`
- IPs privées (127.0.0.1, 10.*, 172.16.*, 192.168.*) bypassent le blocage
- `MaintenanceController` : `GET/POST /api/v1/admin/maintenance` (admin only)
- Retourne 503 avec message JSON sur toutes les routes publiques

### 10. Monitoring
- `docker-compose.monitoring.yml` : Prometheus (:9090), Grafana (:3000), ELK (Elasticsearch :9200, Kibana :5601, Logstash :5000/9600)
- `prometheus.yml` : scrape Spring Boot (/actuator/prometheus), PostgreSQL, Redis
- Ajouter `spring-boot-starter-actuator` + `micrometer-registry-prometheus` dans pom.xml si nécessaire

### 11. Tests (backend)
- 3 classes de test existantes : AuthServiceTest, AuthControllerTest, StatsControllerTest
- `activ-education-backend-main/src/test/`

## Testing status

- **Backend**: 3 test files (AuthServiceTest, AuthControllerTest, StatsControllerTest)
- **Flutter**: 3 files (widget smoke, model serialization, API integration)
- **Backoffice**: no test framework

## Session log (ongoing)

### Session 4 — Gemini → OpenAI migration
- Remplacé Gemini par OpenAI (interface `GeminiEmbeddingService` → `AIEmbeddingService`)
- Créé `OpenAIEmbeddingServiceImpl` avec `text-embedding-3-small` (768 dims, compatible pgvector) et `gpt-4o-mini` pour chat/vision
- Supprimé `GeminiEmbeddingServiceImpl.java`
- Mis à jour `application.properties`, `application-dev.properties`, `docker-compose.yml` (`GEMINI_API_KEY` → `OPENAI_API_KEY`)
- Corrigé port DB dans `application-dev.properties` : 5432 → 5433 (Docker)
- Nettoyé volume PostgreSQL (incompatibilité 15→16) et relancé les services Docker
- Backend démarré avec Spring Boot 4.0.5, health UP, 171 endpoints, Swagger OK
- Clé OpenAI configurée : `sk-proj-d...` (valide, 112 modèles)
- OCR : utilise désormais OpenAI Vision au lieu de Gemini Vision
- FAQ RAG : utilise OpenAI Chat au lieu de Gemini

### Session 1 — CDC gaps + responsive + adaptive quiz
- Tous les écrans Flutter conformes multi-tailles : Flexible/Wrap/TextOverflow.ellipsis sur 14 screens
- Nouveaux dashboards : `DashboardDecrocheur` (orange, parcours pas à pas, ressources réinsertion), `DashboardReconversion` (financements CPF/VAE, bilan compétences)
- Routing par `typeApprenant` : `PROFESSIONNEL` → reconversion, `AUTRE` → décrocheur, autres → bachelier
- API Gemini intégrée : `getRecommandationIA()`, plus de fake matchs
- Quiz adaptatif : re-tri des questions par domaine sous-testé + `prochaineQuestionTrackingId`
- Stats endpoints backend : distribution type-apprenant, fiches modifiées récentes, KPIs
- Audit logs : `AuditLog` entity + filter auto-log POST/PUT/DELETE + `AdminLogsController`
- Backoffice connecté aux vraies API : AdminDashboard, LogsPage, SuperAdminDashboard
- FAQ feedback : vote Utile/Pas utile avec compteur (Flutter + backoffice)
- OCR→Seuils matching : normalisation accents/tirets dans `SeuilAdmissionService`
- Rappels RDV SMS : `@EnableScheduling` + CRON 8h00 quotidien
- Poids quiz configurables : `ParametreApplication` entity + `ParametreController`
- Import/Export CSV : `CsvController` + boutons backoffice
- Graphe inter-fiches : `LienInterFiche` entity + CRUD controller
- Nouvelles entités (ddl-auto) : `audit_logs`, `parametres_application`, `liens_inter_fiches`

### Session 2 — Flutter run + fixes mobiles
- Backend health check `GET /actuator/health` → 200
- `flutter run` sur moto g pure (Android 720p) : RenderFlex overflow 11px dans `bottom_nav.dart:44`
- `flutter run` sur Chrome : `Dart compiler exited unexpectedly` (instable)
- `PlatformException(already_active)` dans `profile_screen.dart:617` (image_picker)
- **Corrections** :
  - `bottom_nav.dart` : `Expanded` sur chaque `_NavItem` au lieu de `FittedBox` → répartition égale, zéro overflow, fonctionne sur tous les écrans
  - `profile_screen.dart` : `try-catch` autour de `picker.pickImage()` avec fallback SnackBar pour l'erreur `already_active`
- `flutter analyze` : 0 erreur sur les fichiers modifiés

### Session 3 — Fix crash ficheDetail route + pagination quiz
- Backend health check `GET /actuator/health` → 200
- `resultats_screen.dart` : compilation error `class-in-class` — la méthode `_loadResultats()` n'était pas fermée (brace manquant après `catch`), ce qui imbriquait tout le code suivant. Rajouté `  }` pour fermer le corps de méthode.
- Crash `String is not a subtype of Map<String, dynamic>?` dans le routeur `ficheDetail` (`main.dart:133`) : `dashboard_reconversion.dart` passait `metier.trackingId` (String) comme arguments alors que le handler attendait `Map`. Handler rendu robuste avec vérification `raw is Map` avant cast ; fallback vers écran d'erreur.
- `diagnostic_enfant_screen.dart:653` : arguments `{'ficheId': ..., 'type': 'SERIE'}` corrigés en `{'fiche': rec.serie!}` pour correspondre au handler.
- `flutter run` sur moto g pure (Android 720p) : RenderFlex overflow 11px dans `bottom_nav.dart:44`
- `flutter run` sur Chrome : `Dart compiler exited unexpectedly` (instable)
- `PlatformException(already_active)` dans `profile_screen.dart:617` (image_picker)
- **Corrections** :
  - `bottom_nav.dart` : `Expanded` sur chaque `_NavItem` au lieu de `FittedBox` → répartition égale, zéro overflow, fonctionne sur tous les écrans
  - `profile_screen.dart` : `try-catch` autour de `picker.pickImage()` avec fallback SnackBar pour l'erreur `already_active`
- `flutter analyze` : 0 erreur sur les fichiers modifiés

## Critical gotchas

- `OPENAI_API_KEY` **committed** in `application-dev.properties` — rotate before deploying
- No CI/CD, no pre-commit hooks
- Deployed DB is empty — run `seed/*.sh` scripts in order (need JWT from `admin@activeducation.tg`)
- `GET /api/v1/eleves/{id}/resultats-diagnostic` returns `Page<>` but Flutter calls without pagination
- `ParentRequest.java` / `ConseillerRequest.java` have `@NotBlank`/`@Size` removed from `motDePasse` (was causing 401 on update)
- Backoffice admin pages (quiz editor, FAQ moderation, stats) are partially stubs/mock data
- `matieresPreferees` stored as CSV in `TEXT` column, parsed by `EleveMapper`
- Login 2FA : si `requires2fa=true`, le client doit appeler `/auth/2fa/validate` avec le challengeToken
- OCR : nécessite `OPENAI_API_KEY` valide pour l'extraction sur images (PDF utilise PDFBox sans clé)
- Mode maintenance : flag statique en mémoire (pas persistant). À remplacer par flag DB si besoin de persistance
- Monitoring : nécessite `docker compose -f docker-compose.monitoring.yml up -d` séparément
- `flutter run` sur Android : `adb reverse tcp:8080 tcp:8080` nécessaire pour que l'app atteigne `localhost:8080`
- `flutter run` sur Chrome : instable (`Dart compiler exited unexpectedly`)
- `bottom_nav.dart` : chaque `_NavItem` wrapé dans `Expanded` — ne jamais revenir à `spaceAround` sur la Row directe
- `profile_screen.dart` : image_picker doit être dans un `try-catch` pour éviter le crash `already_active`
