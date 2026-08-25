# CAHIER DES CHARGES TECHNIQUE — Activ Education

## 1. Architecture générale

```
┌──────────────┐    ┌──────────────┐    ┌─────────────────────┐
│  Flutter App  │    │ React 19 +   │    │   Nginx (Reverse    │
│  (Android /   │    │ TypeScript 6  │    │   Proxy + SSL)      │
│   iOS / Web)  │    │ Backoffice    │    └─────────┬───────────┘
└──────┬───────┘    └──────┬───────┘              │
       │                   │                      │
       └───────────────────┴──────────────────────┘
                           │
                    ┌──────┴──────┐
                    │  Spring      │
                    │  Boot 4.0.5  │
                    │  (Java 21)   │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
         ┌────┴───┐  ┌────┴───┐  ┌────┴───┐
         │Postgres│  │ Redis  │  │ MinIO  │
         │  SQL   │  │Cache/  │  │Objets  │
         │  16    │  │Rate    │  │S3      │
         │+vector │  │Limit   │  │        │
         └────────┘  └────────┘  └────────┘
```

## 2. Stack technique détaillée

### 2.1. Backend — Spring Boot 4.0.5 (Java 21)

**Framework** : Spring Boot 4.0.5 avec Maven
**Java** : 21 (LTS)
**Build** : Maven, multi-module (package by feature)

**Dépendances principales** :
| Dépendance | Version | Usage |
|------------|---------|-------|
| Spring Boot Data JPA | 4.0.5 | ORM Hibernate 6 |
| Spring Boot Security | 4.0.5 | Authentification, autorisation |
| Spring Boot WebMvc | 4.0.5 | API REST |
| Spring Boot WebSocket | 4.0.5 | WebSocket (chat) |
| Spring Boot Actuator | 4.0.5 | Health check, métriques |
| Spring Boot Data Redis | 4.0.5 | Cache, rate limiting, token blacklist |
| Spring Boot Validation | 4.0.5 | Bean Validation (@Valid) |
| jjwt (io.jsonwebtoken) | 0.12.5 | JWT HS512 |
| PostgreSQL Driver | 42.x | Connexion base |
| Hibernate Vector | - | Support pgvector |
| Lombok | - | @Data, @Builder, @SuperBuilder |
| Springdoc OpenAPI | 2.7.0 | Swagger UI |
| MinIO Client | 8.5.17 | Stockage objet S3 |
| PDFBox | 3.0.3 | Extraction texte PDF |
| Tika Core | 2.9.2 | Détection type MIME |
| Micrometer Prometheus | - | Métriques Prometheus |
| Dotenv Java | 3.2.0 | Fichier .env |

### 2.2. Frontend Mobile — Flutter 3.27 (Dart)

**Langage** : Dart 3.x
**Gestion d'état** : setState + singletons statiques (pas de Provider/Riverpod/Bloc)
**HTTP** : Dio 5.7.0 avec intercepteur JWT
**Stockage sécurisé** : flutter_secure_storage 9.2.2 (fallback SharedPreferences + mémoire)
**Mapping** : flutter_map 7.0.2 + latlong2
**Vocale** : speech_to_text 7.4.0 + flutter_tts 4.2.1
**Environnement** : flutter_dotenv 6.0.1
**Pas de base locale** : 100% API-driven

**Architecture Flutter** :
```
lib/
├── main.dart              # Point d'entrée + table de routage
├── models/                # 12 fichiers, ~40 DTOs
├── services/              # 19 services (singletons)
├── screens/               # 55 écrans
├── widgets/               # Composants réutilisables
├── theme/                 # Thème, routes
└── utils/                 # Utilitaires (images, completion)
```

**Écrans** : 55 écrans répartis en auth (11), home (11), explorer (6), search (1), diagnostic (4), messages (5), profile (1), chat (1), modules (9), errors (3)

### 2.3. Frontend Web — React 19 + TypeScript 6

**Stack** :
| Technologie | Version |
|-------------|---------|
| React | 19.2.6 |
| TypeScript | 6.0 (erasableSyntaxOnly) |
| Vite | 8.0.12 |
| Tailwind CSS | 4.3 |
| React Router | 7.15.1 |
| TanStack Query | 5.100.11 |
| Zustand | 5.0.13 |
| Axios | 1.16.1 |
| Recharts | 3.8.1 |
| Lucide React | 1.16.0 |
| date-fns | 4.2.1 |

**Pages** : 29 pages (login 1, conseiller 8, admin 17, superadmin 3)

### 2.4. Base de données — PostgreSQL 16

**Extension** : pgvector (768 dimensions)
**DDL** : ddl-auto=update (Hibernate, pas de Flyway/Liquibase)
**Dialecte** : org.hibernate.dialect.PostgreSQLDialect
**Héritage** : JOINED pour Utilisateur → (Eleve, Parent, Conseiller, Administrateur) et Fiche → (FicheSerie, FicheFiliere, FicheMetier, FicheEtablissement)

**Nombre d'entités** : 59 entités JPA réparties dans les modules

### 2.5. Cache et rate limiting — Redis 7

- Blacklist de tokens JWT révoqués
- Rate limiting : login (20/15min), refresh (20/5min), API (200/1min)
- Cache des sessions

### 2.6. Stockage objet — MinIO

- 3 buckets : images, videos, documents
- Upload via `/files/upload/{fileType}`
- Limite : 20 Mo par fichier (25 Mo requête)
- Compatible S3

## 3. API REST — 68 contrôleurs, ~170 endpoints

### 3.1. Structure des URLs

```
/api/v1/{module}/{ressource}/{trackingId}[/{sous-ressource}]
```

### 3.2. Modules API

| Module | Contrôleurs | Endpoints principaux |
|--------|-------------|---------------------|
| profil | 10 | CRUD utilisateurs, auth, documents, notes, historique, consentement, relevé notes |
| bibliotheque | 10 | CRUD fiches (4 types), FAQ, favoris, recherche, graphe liens |
| diagnostic | 9 | CRUD quiz/questions/réponses, résultats, scores, seuils, génération IA |
| accompagnement | 4 | Messages, rendez-vous, tickets, disponibilités |
| shared | 14 | Auth, 2FA/TOTP, OCR, ORIA, vocal, logs, maintenance, stats, paramètres, fichiers |
| feature packages | 21 | Alumni, attestations, badges, cahier bord, calendrier, carte métiers, CV, datahub, défis, emploi, entretien, hors-ligne, mentorat, parrainage, portfolio, prédiction, recommandation, réorientation, réseau, RIASEC, salle virtuelle, simulateur, témoignages, VAE |

### 3.3. Sécurité des endpoints

```
PUBLIC :
  POST /auth/login, /auth/refresh, /auth/forgot-password, /auth/otp/verify, /auth/reset-password
  POST /auth/2fa/validate, /eleves, /parents
  GET  /bibliotheque/* (lecture publique)
  GET  /actuator/health, /files/download/**, /api-docs/**, /swagger-ui/**, /error

AUTHENTICATED (avec @PreAuthorize) :
  PUT  /eleves/*, /parents/*, /conseillers/*
  POST /eleves/*/notes, /eleves/*/documents, /eleves/*/releve-notes, /simulateur/**
  POST /vocal/**, /quiz/generate
  POST/DELETE /bibliotheque/favoris
  POST/DELETE /parents/*/enfants/*

ADMIN :
  POST/PUT/DELETE /eleves/**, /parents/**, /conseillers/**, /administrateurs/**
  POST/PUT/DELETE /bibliotheque/**
  POST/PUT/DELETE /quiz/**, /questions/**, /reponses/**, /score-matrices/**, /seuils-admission/**
```

## 4. Sécurité

### 4.1. Authentification
- JWT HS512 avec clé 512-bit (jjwt 0.12.5)
- Access token : 15 minutes
- Refresh token : 7 jours
- BCrypt coût 12 pour les mots de passe

### 4.2. Double authentification (2FA/TOTP)
- RFC 6238, HMAC-SHA1, Base32
- Entité `TotpSecret` liée à l'utilisateur
- Challenge token temporaire à la connexion
- Setup QR code + vérification à 6 chiffres

### 4.3. Contrôle d'accès
- `@PreAuthorize` sur tous les contrôleurs
- Beans SPEL : `@security.isOwner()`, `@security.isOwnChild()`, `@security.isOwnConseiller()`, `@security.isRdvParticipant()`
- Rate limiting Redis par IP
- Filtre JWT + Filtre rate limiting

### 4.4. Protection couche HTTP
- CSP : `default-src 'self'; frame-ancestors 'none'`
- HSTS : 1 an, includeSubDomains
- CORS : origines listées (localhost, *.activeducation.tg)
- CSRF : désactivé (stateless JWT)
- HTTPS : forcé par Nginx

### 4.5. Consentement parental
- Blocage si âge < 15 ans
- Email de validation avec token
- Endpoint public de validation

### 4.6. Mode maintenance
- Filtre servlet statique
- Retour 503 sur routes publiques
- IP privées bypassent le blocage

## 5. IA et traitements vocaux

### 5.1. Modèles IA

| Usage | Fournisseur | Modèle | Fallback |
|-------|-------------|--------|----------|
| Embeddings (vecteurs) | OpenAI | text-embedding-3-small (768d) | LIKE PostgreSQL |
| Chat ORIA | OpenAI | gpt-4o-mini | Groq llama-3.1-8b-instant |
| Vision OCR | OpenAI | gpt-4o-mini (vision) | PDFBox (texte) |
| Quiz génération | OpenAI | gpt-4o-mini | - |
| Recommandation | OpenAI | gpt-4o-mini | Algorithme local |
| STT (vocal) | OpenAI | whisper-1 | - |
| TTS (vocal) | OpenAI | tts-1 | - |
| ORIA local | Ollama | qwen2:0.5b | - |
| Validation relevés | Groq | llama-3.1-8b-instant | Regex |

### 5.2. RAG (Retrieval-Augmented Generation)
- Recherche vectorielle pgvector dans les fiches
- Contexte injecté dans le prompt système d'ORIA
- Le prompt inclut le système éducatif togolais complet

### 5.3. Assistant vocal
- STT : enregistrement → Whisper API → texte
- TTS : réponse texte → OpenAI TTS → audio joué
- Mode hybride texte + vocal

## 6. Stockage et fichiers

### 6.1. Base de données
- 59 tables JPA avec auto-génération (ddl-auto=update)
- Héritage JOINED pour utilisateurs et fiches
- Index sur email, tracking_id (UUID), type_apprenant
- Vector(768) pour embeddings sémantiques

### 6.2. MinIO (stockage objet)
- 3 buckets : activeducation-images, activeducation-videos, activeducation-documents
- Endpoint unique : `/files/upload/{type}`, `/files/download/{id}`
- 20 Mo max par fichier

### 6.3. Redis
- Token blacklist (JWT révoqués)
- Rate limiting (login, refresh, API)
- Cache session

## 7. Tests

### 7.1. Backend (8 fichiers)
| Test | Type | Description |
|------|------|-------------|
| AuthServiceTest | Unitaire | Login, forgot password, OTP, reset |
| AuthControllerTest | Intégration | Endpoints auth |
| EleveServiceTest | Unitaire | CRUD élèves |
| StatsServiceTest | Unitaire | KPIs, inscriptions, quiz |
| StatsControllerTest | Intégration | Endpoints stats |
| TestControllerTest | Intégration | Création utilisateurs test |
| JacksonTest | Unitaire | Sérialisation JSON |
| ApplicationTest | Intégration | Démarrage contexte |

### 7.2. Flutter (3 fichiers)
| Test | Type | Description |
|------|------|-------------|
| widget_test.dart | Widget | Smoke test MaterialApp |
| model_test.dart | Unitaire | Sérialisation JSON |
| api_test.dart | Intégration | Connexion API backend |

### 7.3. Backoffice
- Aucun framework de test installé

## 8. Infrastructure et déploiement

### 8.1. Services Docker

| Service | Image | Port exposé |
|---------|-------|-------------|
| PostgreSQL 16 | postgres:16-alpine | 5432/5433 |
| Redis 7 | redis:7-alpine | 6379 |
| MinIO | minio/minio | 9000 (API), 9001 (Console) |
| Spring Boot | multi-stage build | 8080 |
| Nginx | nginx:alpine | 80, 443 |

### 8.2. Monitoring (docker-compose.monitoring.yml)
| Service | Port |
|---------|------|
| Prometheus | 9090 |
| Grafana | 3000 |
| Elasticsearch | 9200 |
| Kibana | 5601 |
| Logstash | 5000/9600 |

### 8.3. Sécurité réseau
- Tous les services internes en 127.0.0.1
- Nginx expose uniquement 80/443
- SSL Let's Encrypt avec renouvellement automatique

## 9. Configuration et variables d'environnement

Fichier .env requis :
```
DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
JWT_SECRET (64-byte base64)
MINIO_ACCESS_KEY, MINIO_SECRET_KEY
REDIS_HOST, REDIS_PORT
OPENAI_API_KEY
GROQ_API_KEY
OLLAMA_URL, OLLAMA_MODEL
JPA_DDL_AUTO, SERVER_PORT
RATE_LIMIT_*
FCM_SERVER_KEY (optionnel)
SMS_PROVIDER, SMS_API_KEY (optionnel)
```

## 10. Structure du code source

```
activ-education-backend-main/          # Backend Spring Boot
├── docker-compose.yml                 # Services Docker
├── docker-compose.prod.yml            # Production stack
├── Dockerfile.prod                    # Build multi-stage
├── nginx.conf                         # Reverse proxy config
├── pom.xml                            # Dépendances Maven
└── src/main/java/tg/edtch/activEducation/
    ├── profil/                        # Module profils
    ├── bibliotheque/                  # Module bibliothèque
    ├── diagnostic/                    # Module diagnostic
    ├── accompagnement/                # Module accompagnement
    ├── shared/                        # Module partagé (sécurité, IA, config)
    ├── alumni/                        # Réseau alumni
    ├── attestations/                  # Attestations
    ├── badge/                         # Badges
    ├── cahierdebord/                  # Cahier de bord
    ├── calendrier/                    # Calendrier
    ├── cartemetiers/                  # Carte des métiers
    ├── cvgenerateur/                  # CV generator
    ├── datahub/                       # DataHub
    ├── defis/                         # Défis
    ├── emploi/                        # Emploi
    ├── entretien/                     # Simulation entretien
    ├── horsligne/                     # Mode hors ligne
    ├── mentorat/                      # Mentorat
    ├── parrainage/                    # Parrainage
    ├── portfolio/                     # Portfolio compétences
    ├── prediction/                    # Prédiction
    ├── recommandation/                # Recommandation globale
    ├── reorientation/                 # Réorientation
    ├── reseau/                        # Réseau social
    ├── riasec/                        # RIASEC
    ├── sallevirtuelle/                # Visites virtuelles
    ├── simulateur/                    # Simulateur de parcours
    ├── temoignage/                    # Témoignages
    └── vae/                           # VAE

activ-education-fronted-main/
├── activ_education/                   # Flutter mobile
│   └── lib/
│       ├── main.dart
│       ├── models/                    # DTOs
│       ├── services/                  # API services
│       ├── screens/                   # 55 écrans
│       ├── widgets/                   # Composants
│       ├── theme/                     # Thème + routes
│       └── utils/                     # Utilitaires
├── backoffice/                        # React backoffice
│   └── src/
│       ├── api/                       # 14 services API
│       ├── components/                # 13 composants
│       ├── pages/                     # 29 pages
│       ├── stores/                    # Zustand auth
│       └── types/                     # Types TS
└── seed/                              # Scripts de seed
```

## 11. Contraintes techniques

- **Dart analyze** : 0 erreur, 0 warning
- **TypeScript strict** : erasableSyntaxOnly (pas d'enums, pas de parameterProperties)
- **Flutter web** : flutter_secure_storage fail → fallback SharedPreferences + mémoire
- **Image picker** : PlatformException already_active → try-catch obligatoire
- **Chat** : polling 4s (pas de WebSocket malgré dépendance)
- **API Base URL** : Flutter sans `/api/v1`, backoffice avec `/api/v1`
- **Backend startup** : nécessite OPENAI_API_KEY dans l'environnement
