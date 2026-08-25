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
- Chat: **dev profile overrides `openai.api.chat.url` → Groq** (`https://api.groq.com/openai/v1/chat/completions`, model `groq/compound-mini`); prod points to OpenAI `gpt-4o-mini`
- **19/08/2026 : le modèle `llama-3.1-8b-instant` n'existe plus sur la clé Groq** → 404 `model_not_found`, tout retombait sur Ollama `qwen2:0.5b` (réponses absurdes). Clé actuelle = `gsk_...` donnant accès à `openai/gpt-oss-120b`, `openai/gpt-oss-20b`, `groq/compound[-mini]`, `qwen/qwen3.6-27b` (ce dernier émet des blocs ` thinking` qui fuient dans la réponse → `nettoyerReponseLlm()` le retire dans `OriaService` et `OpenAIEmbeddingServiceImpl`). Vérifier les modèles dispo via `GET https://api.groq.com/openai/v1/models` avec la clé
- **Modèle chat retenu (19/08/2026) : `groq/compound-mini`** — modèle compound qui route vers des sous-modèles (dont `llama-3.3-70b-versatile`, **TPM réel ~12 000** malgré le « 70K » publié) ; `openai/gpt-oss-120b` = 8 000 TPM. **Réduction tokens/requête** : `MAX_MESSAGES_FOR_LLM=6` (messages tronqués à 300 chars via `abreger`) + contexte DB `PageRequest.of(0, 5)` → ~2 500-3 000 tokens/requête ≈ 4 questions/min sans 429 (au lieu de ~6 700 → 1-2/min)
- **Quota Groq** → `max_tokens` fixé à 400 dans `OriaService` (callOpenAI/callGroq) ; sur 429, `callOpenAI` réessaie une fois après 15 s, et `callLLM` **ne retombe PAS sur Ollama qwen2:0.5b en cas de 429** (réponses absurdes — break + message propre « l'assistant est très sollicité »)
- Fallback chain for chat AND embeddings: **OpenAI → Ollama local (`qwen2:0.5b`)** — set in both `generateAnswer()` and `generateEmbedding()` (missing fallback was a real bug, don't remove)
- **429 (quota) in `generateAnswer()` throws a dedicated exception — NO Ollama fallback** (Ollama won't fix an OpenAI quota)
- Prompt ORIA assoupli (19/08/2026) : règles 5/5b autorisent les connaissances générales du modèle (définitions, parcours, métiers, établissements bien connus) + règle 6d : ignorer les entrées de base hors sujet (ex. « universités publiques » sans citer les fiches privées)
- Chat Flutter (`oria_screen.dart`) : rendu Markdown via `flutter_markdown` (tableaux, gras, listes) ; messages utilisateur en `Text` brut
- **Recommandation IA** (`recommandation_ia_screen.dart`) : affichée en `MarkdownBody` stylé (titres, tableaux avec `tableBorder`/`tableCellsPadding`) — ne pas revenir à un `Text` brut
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
- **Quiz nettoyés (08/2026)** : 29 → **24 quiz** (doublons supprimés ids 1,3,5,2,4 + 400 options + 100 questions) ; les 10 quiz filières réécrits à **6 questions chacun** (60 questions ids 1000+, 240 options ids 5000+, tous actifs). Inactifs restants : Tourisme (8), Cuisinier (9). Tests psychométriques = versions simplifiées des références internationales (RIASEC, MBTI, VARK…), scores déterministes. Vérif DB : `docker exec activeducation-db psql -U postgres -d activ_education`
- **`matieresPreferees`** stored as CSV in `TEXT` column, parsed by `EleveMapper`
- **`GET /api/v1/eleves/{id}/resultats-diagnostic`** returns `Page<>` but Flutter calls without pagination
- **OCR** — image extraction requires `OPENAI_API_KEY` (PDF uses PDFBox, no key)
- **2FA login** — if `requires2fa=true`, client must call `/auth/2fa/validate` with challengeToken
- **OTP inscription obligatoire** — `POST /eleves` et `/parents` exigent `inscriptionToken` (obtenu via `POST /auth/inscription/otp/envoyer` + `/inscription/otp/verify`, Redis, 5 min, usage unique) sinon 400 « Email non vérifié » ; `emailVerifie=true` à la création. **Mot de passe oublié** : déjà OTP (`forgot-password` → `otp/verify` → `resetToken` → `reset-password`). `TestController`/backoffice : `AuthService.genererInscriptionToken(email)` émet un token SANS OTP (jamais exposé publiquement). `InvalidTokenException` → 400 (GlobalExceptionHandler). OTP 4 chiffres, Redis clé `otp:<email>` ; si SMTP (Gmail dans `.env.local`) échoue, le code est loggé « Code (repli dev) »
- **ORIA** — Ollama (`qwen2:0.5b`) local → Groq → OpenAI fallbacks; Flutter Dio timeout set to 120s
- **Maintenance mode** — static in-memory flag (not persistent)
- **Monitoring** — requires `docker compose -f docker-compose.monitoring.yml up -d` separately
- **Flutter web** unstable (`Dart compiler exited unexpectedly`) / DDC doesn't reset new State fields — do full **F5** after adding fields
- **MinIO 500** on `files/download`: 1) `MinioExceptionHandler` needs `@Order(HIGHEST_PRECEDENCE)` before `GlobalExceptionHandler` 2) import the wrapper class, not `java.io.FileNotFoundException` 3) `contentLength()` can NPE if `fileSize` null
- **Simulateur parcours** — bulletin slots auto by level (`_slotsPourNiveau()`), série masquée pour collège/supérieur
- **XLSX import** — `scripts/import_etablissements_xlsx.py` + `Base_Etablissements_Superieurs_Togo.xlsx` (root) → `EtablissementDetailsXlsx*` (bibliotheque) → `/api/v1/bibliotheque/etablissements/{trackingId}/details-xlsx`
- **Enrichissement `offre_formation`** — `scripts/enrichir_offre_formation.py` agrège la feuille `Formations` du XLSX par `ID_ETABLISSEMENT`, joint via `etablissement_details_xlsx.id_source_xlsx`, et remplit `fiches_etablissement.offre_formation` avec un texte structuré par Domaine/Filière/Niveau/Diplôme. **N'écrase que les fiches vides** (NULL, '' ou 'Non disponible'). `--dry-run` pour aperçu. Sur 1361 fiches : 97 liées au XLSX → 44 enrichies en 1ère passe (08/2026) ; 53 étaient déjà remplies. Relancer après import XLSX pour rattrapage.
- **Enrichissement établissements (contacts/GPS/site web)** — `scripts/enrichir_etablissements_infos.py` lit les feuilles `Contacts` et `Localisation` du XLSX, remplit `contacts`, `site_web`, `latitude`, `longitude`, `ville` pour les 97 établissements liés au XLSX. Idempotent (n'écrase rien). ~34 lat/lng + ~14 contacts + ~24 sites web ajoutés en 08/2026.
- **Enrichissement liens etablissement_filiere** — `scripts/enrichir_liens_strict_et_creer_filieres.py` fait 2 passes : (1) matching **strict** (normalisation exacte) XLSX → fiches_filiere existantes, (2) création auto des fiches filières XLSX non-matchées avec fréquence ≥ 3 établissements (16 créées en 08/2026, dont Management, Communication, Comptabilité, Génie logiciel, Commerce international, etc.) + leurs liens. **NE PAS utiliser de fuzzy matching** : vu en 08/2026, le matching par sous-chaîne produit des faux positifs dangereux (ex. `Agroéconomie → Économie` qui sont des formations différentes). Total après enrichissement : 101 liens etab_filiere.
- **Limitation XLSX** — la feuille `Formations` et `Débouchés` ont des colonnes détaillées (`Programme`, `Compétences développées`, `Débouchés`, `Métiers accessibles`) **toutes à "Non disponible"**. Le XLSX sert uniquement d'index de noms. L'enrichissement pédagogique des fiches filières et le peuplement de `filiere_metier` doivent passer par une autre source (saisie backoffice, OCR de documents PDF, ou import futur).
- **Fiches parasites** — 4 fiches avec titre contenant « Test », « Save Button » polluent la base (3 publiées + 1 non publiée). À supprimer via backoffice ou DELETE SQL direct (NE PAS toucher via script — vérifier d'abord qu'aucune FK ne les référence).
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
- **Phase 5 opérationnelle (08/2026)** : dataset calibré 6000 outcomes en DB (ids 10000-15999 eleves, 50000-55999 outcomes, emails `eleve_gen_XXXXX@gen.local`) — source `scripts/generate_calibrated_dataset.py` (seed=42, taux BAC réels 2023/2024 par région×série×sexe, ordre public/privé, DOMAIN_FACTOR par domaine, ATTRACTIVITY filières, RIASEC_PROFILES). Résultats honnêtes : GB ROC-AUC 0.669±0.010 / F1 admis 0.553 / recall réorienté 0.869 ; LR 0.656 / 0.578 / 0.620 (vs 0.87 synthétique gonflé à ne JAMAIS citer comme référence).
- **Colonnes contexte** : `region`, `ordre`, `sexe`, `annee_session` sur `orientation_outcome` (et dans l'export CSV Phase 5) + `filiere` (titre résolu via `FicheFiliereRepository`). Export = 19 colonnes.
- **Bug historique corrigé** : `bac_rate()` testait `serie in BAC_RATES[annee]` mais les clés sont des tuples `(region, serie)` → taux constant 0.60 → aucune différence année/région (2023 et 2024 ~41%). Fix : `(region, serie) in BAC_RATES[annee]` (2023 → 52% ADMIS, 2024 → 32.7%).
- `generated_outcomes.sql` = idempotent (DELETE marqueurs `Généré calibré%` + ids 10000-15999/50000-55999 en tête) — ré-applicable sans conflit.
- `phase5_train_real.py` : `python` → `sys.executable` ; cols conversationnelles neutres (`domaine_declare_match_filiere`=0.5, `constance_ambition`=0.5, `nb_echanges_domaine`=0) ; `region`/`ordre`/`sexe`/`annee_session`/`filiere` dans l'adapt. `train_model.py` : `load_data()` honore `PHASE5_DATASET`, CONFIGS avec CONTEXT_CAT_COLS=[region,ordre,sexe] + CONTEXT_NUM_COLS=[annee_session].
- Login API admin : `admin@activeducation.tg` / `admin123!` (le script d'exemple dans les commentaires contient une typo `activediction.tg`).
- **Intégration ONNX (20/08/2026)** : `models/gb_sans_comportemental.onnx` (export skl2onnx, équivalence validée max diff 6e-8) copié dans `src/main/resources/models/`. `ModeleReussiteService` (`prediction/domain/service/`) charge le modèle au démarrage (fallback 0.5 si indisponible, log `[MODELE-REUSSITE]`). **onnxruntime VERSION PINNÉE 1.20.0** (`pom.xml` `<onnxruntime.version>`) : la 1.29.0 segfault (exit 139, sans hs_err) dès que le classpath contient ≥38 jars réels (bug natif reproductible en isolation : `OrtEnvironment.getEnvironment()`), indépendamment des jars (37 OK / 38 KO, chemins factices OK). ORT 1.20.0 gère l'opset 1 (TreeEnsemble ai.onnx.ml).
- **Fusion ML dans le moteur 3 signaux (v2)** : `Recommandation3SignauxServiceImpl` → `realiteHybride = 0.5·realite + 0.5·ML` (réinjectée dans `combiner` avec les mêmes poids 35/50/15) ; `FiliereScoreeResponse` + `probabiliteReussite` (proba ADMIS brute du modèle) ; `genererRaison` ajoute toujours « ≈ XX% de chances d'admission (modèle 6 000 parcours). » (affiché tel quel sur mobile via `raisonClassement`). `ProfilEleve` + `notesCroissant`.
- **Features ONNX (21 entrées, noms = colonnes)** : 15 float `[1,1]` (riasec_R..C one-hot depuis codeProfil, notes_n2/n1/actuelle, tendance_notes=(actuelle−n2)/2, moyenne_generale, score_60_40=0.6·realite+0.4·aspiration, match_riasec=cosinus RIASEC continu, ecart_notes_seuil=(actuelle−10)/10, annee_session=2024) + 6 strings `[1][1]` (serie=INCONNU, filiere_choisie=titre fiche, niveau_actuel, region/ordre/sexe=INCONNU). Strings de rang 2 obligatoires (sinon InvalidArgument). Tensor par colonne (pas de partage), Map.ofEntries (Map.of ≤10 paires).
- **Piège try-with-resources** : ne pas oublier la parenthèse fermante du bloc `try ( ... )` quand la dernière ressource est `session.run(Map.ofEntries(...))` multi-lignes (javac rapporte alors une erreur trompeuse `')' expected` sur la ligne suivante).
- **Redémarrer le backend** : `pkill -f "[A]ctivEducationApplication --spring"` (le motif `[A]` évite que pkill se tue lui-même — `pkill -f ActivEducationApplication` matche sa propre ligne) puis relancer via `start-backend.sh` (racine projet) avec `setsid -f`.
- Vérification endpoint : `GET /api/v1/eleves/{trackingId}/recommandation-ia/v2` → `top[].probabiliteReussite` non null + `raisonClassement` contient « % de chances ». Élève test complet : 89fd4c6b-1581-42db-af75-39cfd55cd2a6 (proba ~0.22 sur fiches de test).

## Boucle de collecte réelle (Phase 5 — 5000 outcomes réels requis)

Flux : **mobile choix filière → backoffice validation conseiller → entraînement**.
- Mobile : bouton « Choisir cette filière » (`fiche_detail_screen.dart`, uniquement fiches filière) → `ApiService.enregistrerChoixFiliere()` → `POST /api/v1/eleves/{eleveTrackingId}/orientation-outcome` → écran « Prochaines étapes » (`prochaines_etapes_screen.dart` : éligibilité série parsée depuis `conditionsAdmission`, conditions réelles OCRisées, documents, 5 étapes).
- Backend : `OrientationOutcomeRequest` accepte `filiereTrackingId` (UUID, mobile) OU `filiereId` (Long, backoffice) + `region/ordre/sexe/anneeSession` (optionnels) — `@AssertTrue isFiliereRefValide`. POST idempotent par (élève, filière). `OrientationOutcome extends` non-BaseEntity : `id` + `trackingId` manuels.
- Backoffice : écran `/conseiller/orientations` (`OutcomesPage.tsx`) — liste paginée via `GET /api/v1/admin/prediction/orientation-outcomes?statut=&page=` (CONSEILLER/ADMIN/SUPER_ADMIN, enrichi nom élève + titre filière), boutons Admis/Recalé/Réouvrir → `PATCH /api/v1/eleves/{id}/orientation-outcome/{outcomeId}?statut=ADMIS`.
- **Contrainte** : statut final ADMIS/RECALE = vérité terrain. `EN_COURS` n'est PAS entraîné (filtré par `findByStatutIn`).
- Test bout en bout : eleve `testeleve@test.com` (trackingId 94167dc0-3feb-43e3-8ad0-ace52716dcfb).

## Déploiement : Oracle Cloud Always Free (décidé 08/2026)

- **Pourquoi** : seul VPS gratuit permanent suffisant (VM Ampere ARM 4 OCPU / 24 Go RAM) — AWS/Azure/GCP = 12 mois max 1 Go ; Fly.io/Render/Koyeb trop petits (pas de Postgres permanent).
- **2 offres distinctes** : Trial 30 j (300$ crédits, expire sans facturation) vs **Always Free = permanent, 0 € indéfiniment**. Carte bancaire : vérification ~1 € (autorisation annulée sous 2-7 j, JAMAIS débitée). Facturation possible SEULEMENT si dépassement des limites free (VM > shape free, stockage > 200 Go, services payants).
- **Risque réel** : VM Always Free inactive (CPU < ~20% pendant ~7 j) peut être récupérée → backend actif = protégé ; sauvegarder la DB (scripts seed) + e-mail Oracle avant toute facturation.
- Étapes : Ubuntu 24.04 → `curl -fsSL https://get.docker.com | sh` → git clone + `./mvnw clean install` → port 8080 ouvert en ingress → `.env.local` (vrais secrets DB/OpenAI/Supabase) → `docker compose up -d --build` → Caddy/Nginx pour HTTPS → `API_BASE_URL=https://<domaine>` dans le `.env` Flutter + rebuild APK.
- **APK sur téléphone sans backend = ne se connecte PAS** (client-serveur) : `localhost:8080` sur le téléphone = le téléphone. Tests : `adb reverse tcp:8080 tcp:8080` (USB, **méthode fiable actuelle**) OU même Wi-Fi avec IP LAN du PC (exiger `server.address=0.0.0.0`). **Le Wi-Fi LAN local est mort (08/2026) : isolation des clients sur le point d'accès** — ping KO dans les 2 sens, aucune entrée ARP pour le téléphone → seule l'IP USB/localhost marche. PC actuel = `10.0.10.132` (l'ancienne `10.0.10.169` dans `network_security_config.xml` ne répond plus ; garder les 2 + `10.0.2.2`).