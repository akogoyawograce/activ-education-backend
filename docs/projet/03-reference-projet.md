# Activ EDUCATION — Document de référence projet

> **Usage** : ce document est destiné à être fourni comme contexte à un assistant
> IA (Claude, etc.) à chaque conversation pour qu'il comprenne le projet sans
> repartir de zéro. Il privilégie la **vérité d'implémentation** (code source
> actuel) plutôt que la spec idéale.
>
> **Date de dernière mise à jour** : 5 août 2026.

---

## Table des matières

1. [Pitch & contexte métier](#1-pitch--contexte-métier)
2. [Architecture globale](#2-architecture-globale)
3. [Modèle de données commun](#3-modèle-de-données-commun)
4. [Sécurité & authentification](#4-sécurité--authentification)
5. [Backend — 29 packages (présentation)](#5-backend--29-packages)
6. [Backend — modules transverses](#6-backend--modules-transverses)
7. [Fonctionnalités pédagogiques (focus)](#7-fonctionnalités-pédagogiques-focus)
8. [Mobile Flutter](#8-mobile-flutter)
9. [Backoffice React](#9-backoffice-react)
10. [Données seed](#10-données-seed)
11. [Tests](#11-tests)
12. [API publique — conventions](#12-api-publique--conventions)
13. [Commandes essentielles](#13-commandes-essentielles)
14. [Gotchas & pièges](#14-gotchas--pièges)
15. [État du projet & dette technique](#15-état-du-projet--dette-technique)

---

## 1. Pitch & contexte métier

### Problème

Au Togo (et dans la sous-région), le choix d'orientation après le collège ou le
lycée est un parcours du combattant pour l'élève et sa famille :

- **Pas de cartographie claire** des 117 universités/écoles togolaises accréditées
  par le MESR (Ministère de l'Enseignement Supérieur et de la Recherche).
- **Pas de profilage structuré** : les tests d'orientation (RIASEC Holland,
  intérêts professionnels) ne sont ni traduits ni adaptés au système éducatif
  local.
- **Conseillers d'orientation submergés** (1 conseiller pour ~800 élèves dans
  certaines préfectures) — pas le temps de suivi individuel.
- **Données dispersées** entre le MENJS (secondaire), le MESR (supérieur),
  les écoles elles-mêmes, et des sources informelles.
- **Parents éloignés** du numérique — l'orientation se passe souvent sans eux
  alors qu'ils sont décisionnaires.
- **Décrochage post-orientation** : ~30 % des élèves basculent dans une filière
  qui ne leur correspond pas (donnée projet stage, à valider).

### Solution

**Activ EDUCATION** est une plateforme multi-canal qui couvre le parcours
d'orientation de la 3e à la fin du cycle universitaire :

| Acteur | Valeur apportée |
|---|---|
| **Élève** | Test RIASEC adapté, recommandation 3 signaux, simulateur de parcours, chat IA ORIA 24/7, dépôt de bulletins (OCR), suivi personnel. |
| **Parent** | Visibilité sur les choix, notifications, RDV avec conseillers. |
| **Conseiller** | Dashboard élèves, messagerie, RDV, recommandations pré-calculées. |
| **École / Administration** | Statistiques d'orientation, suivi cohortes, export. |

### Différenciateurs techniques

- **Multi-tenant mobile/web** avec backoffice admin (React).
- **IA hybride** : OpenAI + Groq + Ollama (mode dégradé dev), prompt durci contre
  les hallucinations sur les établissements (cf. `JOURNAL_BORD_IA.md` 5 août).
- **RAG vectoriel 2-phase** (SQL natif pgvector + JPQL pour réhydratation JOINED).
- **Héritage JOINED** sur `Fiche` (abstraite) avec 4 sous-types (Série,
  Filière, Métier, Établissement).
- **OCR multimodal** sur bulletins (PDF via PDFBox, images via OpenAI Whisper).
- **Sécurité PKI UUID** : toutes les URLs REST exposent un `trackingId` UUID
  public, pas d'ID numérique interne.

### Périmètre du stage

Développé dans le cadre d'un stage à **HubCity / Woélab** (Lomé, Togo). Le
projet couvre l'intégralité de la plateforme (backend Spring Boot complet,
mobile Flutter complet, backoffice React complet, seed initial), avec un focus
particulier sur le module **prédiction/recommandation** et l'**assistant IA ORIA**.

---

## 2. Architecture globale

```
┌────────────────────────┐         ┌────────────────────────┐
│  Mobile Flutter        │         │  Backoffice React      │
│  (élève/parent)        │         │  (admin/conseiller)    │
│  - setState            │         │  - Zustand + TanStack  │
│  - Dio + 401 refresh   │         │  - TS 6 strict         │
│  - port dynamique      │         │  - Vite + Tailwind v4  │
└──────────┬─────────────┘         └──────────┬─────────────┘
           │ HTTPS /api/v1                     │ HTTPS /api/v1
           │ Bearer JWT                        │ Bearer JWT
           ▼                                   ▼
        ┌──────────────────────────────────────────┐
        │ Spring Boot 4.0.5 (Java 21, Maven, :8080)
        │ JwtAuthenticationFilter │
        │ RateLimitingFilter (Redis)              │
        │ @PreAuthorize (SPEL custom)             │
        │ 76 controllers / 80 services            │
        │ Spring Data JPA + Hibernate             │
        └──────┬───────────┬──────────┬────────────┘
               │           │          │
               ▼           ▼          ▼
        ┌──────────┐ ┌─────────┐ ┌─────────┐
        │ Postgres │ │ MinIO   │ │ Redis   │
        │ :5432    │ │ :9000   │ │ :6379   │
        │ +pgvector│ │ S3 API  │ │ cache + │
        │          │ │         │ │ rate-limit
        └──────────┘ └─────────┘ └─────────┘
                 ┌─────────────┐
                 │ Ollama local│ ← mode dev (qwen2:0.5b)
                 │ :11434      │
                 └─────────────┘

        Services IA externes (config via env vars):
        - OpenAI api.openai.com (gpt-4o-mini, text-embedding-3-small, whisper-1, tts-1)
        - Groq api.groq.com (llama-3.1-8b-instant, fallback Ollama désactivé)
```

### Stack résumée

| Sous-projet | Stack | Détails |
|---|---|---|
| **Backend** | Spring Boot 4.0.5 + Java 21 + Maven | 540 fichiers Java, port 8080 |
| **Base** | PostgreSQL 16 + pgvector | `localhost:5432` (Docker `:5433` ext) |
| **Cache/Rate** | Redis 7 | blacklist JWT + rate-limiting |
| **Objet** | MinIO | buckets images/vidéos/documents |
| **Mobile** | Flutter/Dart | 103 fichiers, port dynamique, polling 4s |
| **Backoffice** | React 19 + TypeScript 6 strict + Vite | port 5174, pas de framework de test |
| **IA** | OpenAI / Groq / Ollama | ordre de fallback : OpenAI > Groq > Ollama |

---

## 3. Modèle de données commun

### BaseEntity (héritage racine)

Toutes les entités héritent de `BaseEntity` dans `shared/domain/entite/` :

```java
public abstract class BaseEntity {
    @Id @GeneratedValue Long id;              // PK interne (jamais exposé)
    @Column(unique=true, nullable=false) String trackingId;  // UUID v4, exposé dans toutes les URLs REST
    Instant createdAt;                        // auditing
    String createdBy;
    Instant updatedAt;
    String updatedBy;
}
```

**Règle absolue** : les controllers ne retournent JAMAIS `id` directement.
`trackingId` (UUID v4) est l'identifiant public.

### Fiche (héritage JOINED polymorphique)

```java
@Entity @Inheritance(strategy = InheritanceType.JOINED)
public abstract class Fiche extends BaseEntity {
    String titre; String resume; String contenu;
    Boolean estPublie; String imageUrl;
}

@Entity public class FicheSerie      extends Fiche { ... }  // BAC séries
@Entity public class FicheFiliere    extends Fiche { ... }  // ~117 fiches
@Entity public class FicheMetier     extends Fiche { ... }  // ~111 fiches
@Entity public class FicheEtablissement extends Fiche {  // universités/écoles
    String ville; TypeEtablissement type; Boolean estPublic;
    String niveau; String offreFormation; String siteWeb;
    String contactEmail; String contactTel;
}
```

**Pourquoi JOINED** : permet des jointures propres par sous-type, et un
repository polymorphe (`FicheRepository`) qui sert tous les modules.

**Conséquence SQL** : la recherche vectorielle en SQL natif doit passer par
**deux phases** : (1) SQL natif pour calculer la similarité cosinus sur
pgvector, (2) JPQL pour réhydrater la bonne sous-classe (JOINED perd le
discriminateur en SQL natif → `clazz_ not found`).

### UUID + DTOs

Toutes les URLs exposent `{trackingId}`. Les DTOs request/response n'exposent
jamais l'ID numérique — uniquement `trackingId` + champs métier.

---

## 4. Sécurité & authentification

### Pipeline HTTP

```
HTTP request
  ├─ JwtAuthenticationFilter (déjà actif — voir §6)
  ├─ SupabaseJwtAuthenticationFilter (squelette — credentials absents)
  ├─ RateLimitingFilter (Redis : login 20/15min, refresh 20/5min, API 200/1min)
  ├─ CORS (toutes origines — dev)
  ├─ CSRF désactivé (stateless)
  ├─ @PreAuthorize (SPEL custom : isOwner, isOwnChild, isOwnConseiller, isRdvParticipant)
  └─ Controller → Service → Repository
```

### Mots de passe & hachage

- BCrypt avec strength 10 (défaut Spring Security).
- `EleveRequest.motDePasse` : `@NotBlank` + `@Size(min=8)` RETIRÉS (causaient
  des 401 silencieux sur update partiel en Flutter). Validation déplacée dans
  `EleveServiceImpl.inscrireEleve()`.
- Idem pour `ParentRequest` et `ConseillerRequest`.

### Authentification 2FA (TOTP)

- Flag `requires2fa` dans `/auth/login` response.
- Si true : POST `/auth/2fa/validate` avec `challengeToken`.
- Stockage secret TOTP par utilisateur (champ `totpSecret`).

### JWT

- Algorithme : HS512 (défaut Spring).
- `JWT_SECRET` dans `.env` (à rotationner avant déploiement).
- Claims : `sub` (email), `roles[]`, `typeUtilisateur`, `trackingId`,
  `jti` (UUID unique), `iat`, `exp`.
- Refresh token : 7 jours, single-use, blacklist Redis.
- Access token : 15 min (900 000 ms), blacklist Redis.

### Rôles (TypeUtilisateur enum)

| Valeur stockée | Signification |
|---|---|
| `Administrateur` | SUPER_ADMIN, accès total |
| `Conseiller` | Conseiller d'orientation |
| `Eleve` | Élève (rôle principal) |
| `Parent` | Parent lié à un ou plusieurs élèves |

> ⚠️ **Convention critique** : en Flutter, **toujours `.toUpperCase()`** avant
> de comparer le rôle retourné par le backend (PascalCase) avec des chaînes
> locales.

### SPEL custom (beans)

Dans `shared/security/expression/CustomSecurityExpressionRoot` :
- `@security.isOwner(#trackingId)` — l'utilisateur authentifié est propriétaire
- `@security.isOwnChild(#eleveTrackingId)` — le parent est lié à cet élève
- `@security.isOwnConseiller(#conseillerTrackingId)` — le conseiller est assigné
- `@security.isRdvParticipant(#rdvTrackingId)` — participe au RDV

---

## 5. Backend — 29 packages

Tous les packages applicatifs suivent **Package by Feature** :
`{package}/{domain|application|infrastructure}/{entite|service|controller|dto|repository|mapper}/`.

Hiérarchie type (exemple `profil/`) :
```
profil/
├── application/
│   ├── controller/    # REST endpoints
│   ├── dto/request/   # validation @Valid
│   ├── dto/response/  # exposition API
│   └── mapper/        # MapStruct ou manuel
├── domain/
│   ├── entite/        # JPA entities
│   └── service/       # interfaces + impl/
└── repository/        # Spring Data JPA
```

### Vue d'ensemble des 29 packages

| Package | Rôle | Endpoints clés |
|---|---|---|
| **shared/** | Sécurité JWT, IA partagée, MinIO, configuration WebSocket, exception handler | `/auth/*`, `/oria/*`, `/minio/*`, `/ws/chat` |
| **profil/** | Comptes utilisateurs (Élève, Parent, Conseiller, Admin) | `/eleves/*`, `/parents/*`, `/conseillers/*` |
| **bibliotheque/** | Catalogue fiches polymorphes (Série/Filière/Métier/Établissement), FAQ, favoris | `/fiches/*`, `/etablissements/*`, `/faq/*` |
| **diagnostic/** | Test RIASEC Holland, quiz génériques, questions/réponses, scoring | `/diagnostic/*`, `/quiz/*` |
| **accompagnement/** | RDV conseillers, disponibilités, messagerie, tickets | `/rendezvous/*`, `/messages/*`, `/tickets/*` |
| **prediction/** | Module IA prédiction/recommandation (3 signaux), dataset, lookup, outcomes | `/prediction/*`, `/orientations/*` |
| **recommandation/** | Algorithme de scoring, top-K par profil | `/recommandation/*` |
| **riasec/** | Spécialisation scoring RIASEC (matrices, calculs, lettres Holland) | `/riasec/*` |
| **simulateur/** | Simulateur de parcours scénarios A/B, comparaison | `/simulateur/*`, `/scenarios/*` |
| **alumni/** | Anciens élèves, mentorat inter-générationnel | `/alumni/*` |
| **mentorat/** | API REST mince sur `alumni.Mentorat` (vue dédiée) | `/mentorat/*` |
| **attestations/** | Génération PDF d'attestations (orientation, présence, réussite) | `/attestations/*` |
| **badge/** | Système de gamification (badges, progression, niveaux) | `/badges/*` |
| **cahierdebord/** | Journal de bord conseiller/élève | `/cahier-de-bord/*` |
| **calendrier/** | Calendrier d'événements (salons, JPO, deadlines) | `/calendrier/*` |
| **cartemetiers/** | Carte des métiers togolais + données régionales | `/carte-metiers/*` |
| **cvgenerateur/** | Génération CV élève (depuis profil + bulletins) | `/cv/*` |
| **datahub/** | Plateforme de données ouvertes (admin) — stats et exports | `/datahub/*` |
| **defis/** | Défis d'orientation gamifiés | `/defis/*` |
| **emploi/** | Offres d'emploi/débuts de carrière liés aux fiches métiers | `/emploi/*` |
| **entretien/** | Préparation aux entretiens (questions types, simulation) | `/entretien/*` |
| **horsligne/** | Mode hors-ligne (cache local, sync différée) | `/sync/*` |
| **parrainage/** | Système de parrainage entre élèves | `/parrainage/*` |
| **portfolio/** | Portfolio élève (réalisations, projets, badges) | `/portfolio/*` |
| **reeorientation/** | Réorientation en cours de parcours (changement de filière) | `/reorientation/*` |
| **reseau/** | Réseau social interne (posts, comments, follows entre élèves/conseillers) | `/reseau/*` |
| **sallevirtuelle/** | Salles virtuelles pour RDV/visio (intégration WebRTC) | `/salles/*` |
| **temoignage/** | Témoignages d'anciens élèves (lecture/écriture) | `/temoignages/*` |
| **vae/** | Validation des acquis de l'expérience | `/vae/*` |

### Détail des packages critiques

#### `profil/` (comptes utilisateurs)

**Entités** : `Utilisateur` (abstraite, commune à tous), `Eleve`, `Parent`,
`Conseiller`, `Administrateur`. Champs clés :
- `email` (unique, login)
- `motDePasse` (BCrypt strength 10)
- `trackingId` (UUID v4, exposé)
- `roles[]` (String[])
- `typeUtilisateur` (enum)
- `enabled` (bool, soft-delete via `enabled=false`)
- `dateInscription`
- `derniereConnexion`

**Endpoints clés** :
- `POST /api/v1/auth/register` — inscription élève
- `POST /api/v1/auth/login` — connexion (retourne JWT + refresh)
- `POST /api/v1/auth/refresh` — rafraîchissement token
- `POST /api/v1/auth/logout` — révocation
- `POST /api/v1/auth/2fa/validate` — validation 2FA
- `GET /api/v1/eleves/{trackingId}` — profil élève (owner ou parent/conseiller autorisé)
- `PUT /api/v1/eleves/{trackingId}` — mise à jour (sans mot de passe obligatoire)
- `POST /api/v1/eleves/{trackingId}/bulletins/upload` — upload bulletin (multipart)
- `POST /api/v1/eleves/{trackingId}/bulletins/preview` — preview OCR sans persist
- `POST /api/v1/eleves/{trackingId}/bulletins/confirm` — confirmation persist
- `POST /api/v1/eleves/{trackingId}/bulletins/batch` — upload multiple

#### `bibliotheque/` (fiches polymorphes)

**Entités** : `Fiche` (abstraite, JOINED), `FicheSerie`, `FicheFiliere`,
`FicheMetier`, `FicheEtablissement`. Champs communs : titre, resume, contenu,
estPublie, imageUrl. Spécifiques à `FicheEtablissement` : ville, typeEtablissement
(enum), estPublic, niveau, offreFormation, siteWeb, contactEmail, contactTel.

**Endpoints clés** :
- `GET /api/v1/fiches/{trackingId}` — détail fiche
- `GET /api/v1/fiches` — recherche paginée + filtres (type, niveau, ville)
- `POST /api/v1/etablissements` — création (admin)
- `GET /api/v1/etablissements/niveau/{niveau}` — filtre par niveau
- `GET /api/v1/filieres` — liste paginée
- `GET /api/v1/metiers` — liste paginée
- `POST /api/v1/fiches/{trackingId}/favoris` — toggle favori utilisateur
- `GET /api/v1/fiches/series/bac` — séries du bac

**Repository polymorphe** : `FicheRepository extends JpaRepository<Fiche, Long>`
avec méthodes discriminées par `clazz_` (Hibernate JOINED).

**RAG** : `rechercherParMotCle(motCle, pageable)` — LIKE sur titre/resume/contenu.
Filtre `estPublie=true`. Pagination par défaut `PageRequest.of(0, 5)`.
Pas de filtre géographique par pays (cf. dette technique §15).

#### `prediction/`

Module central du projet, structuré en **5 phases** :
1. **Données** : import cohortes réelles via `PredictionDatasetService`
2. **Modèle** : entraînement scikit-learn (`train_model.py` à la racine)
3. **Prédiction** : appel modèle + scoring 3 signaux (cf. §7)
4. **Recommandation** : agrégation + top-K
5. **Suivi** : `OrientationOutcome` (statut post-orientation)

**Entités clés** : `Prediction`, `PredictionDataset`, `PredictionLookup`
(cache des prédictions), `OrientationOutcome` (succès/échec post-orientation).

**Endpoints clés** :
- `POST /api/v1/predictions/generer/{eleveTrackingId}` — déclenche génération
- `GET /api/v1/predictions/{trackingId}` — détail
- `GET /api/v1/recommandation/{eleveTrackingId}` — top-K filière
- `POST /api/v1/orientations/{trackingId}/outcome` — enregistrement résultat
- `GET /api/v1/orientations/{eleveTrackingId}` — historique

**Service central** : `Recommandation3SignauxServiceImpl.candidatsPourProfil()`
combine 3 signaux :
1. **RIASEC** : similarité cosinus entre profil élève et profil filière (catalogue RIASEC)
2. **Académique** : score basé sur notes bulletin
3. **Affinité géographique** : mock pour l'instant

⚠️ **Bug déjà corrigé** : quand `niveauActuel==null`, retourne `List.of()` (pas
`findAll()` qui sortait les 117 fiches). Test anti-régression dans
`Recommandation3SignauxServiceTest`.

#### `diagnostic/` + `riasec/`

**Test RIASEC Holland** en 6 dimensions :
- **R**éaliste, **I**nvestigateur, **A**rtistique, **S**ocial, **E**ntrepreneurial, **C**onventionnel

**Entités** : `TestRIASECResultat` (résultat calculé avec lettres dominantes),
`DiagnosticQuestion`, `DiagnosticReponse`, `DiagnosticSession`.

**Endpoints** :
- `GET /api/v1/diagnostic/riasec/questions` — questions du quiz (randomisées)
- `POST /api/v1/diagnostic/riasec/soumettre` — soumission réponses → score
- `GET /api/v1/diagnostic/resultats/{trackingId}` — résultat
- `GET /api/v1/riasec/profil/{eleveTrackingId}` — profil dimensionnel R/I/A/S/E/C

**Catalogue** : `ProfilFiliereRiasecCatalog` (40+ filières togolaises mappées
sur les 6 dimensions). Méthode `profilFilierePour(nom)` retourne `double[6]`.

#### `simulateur/`

**Concept** : l'élève simule 2 scénarios (A/B) en modifiant ses notes
(hypothétiques) ou matières, et compare les recommandations.

**Entités** : `SimulateurScenario`, `SimulateurParcours`,
`ScenarioTemplate` (modèles pré-configurés).

**Endpoints** :
- `POST /api/v1/simulateur/scenarios` — création scénario
- `GET /api/v1/simulateur/{trackingId}/comparer/{autreId}` — comparaison A/B
- `GET /api/v1/simulateur/templates` — templates (Mon exploration, maths excellentes, etc.)

---

## 6. Backend — modules transverses

### 6.1 `shared/security/`

#### `JwtAuthenticationFilter`
- Filtre servlet qui extrait le `Bearer` header, parse le JWT, charge
  `UtilisateurDetailsService`, peuple le `SecurityContextHolder`.
- Fail-open sur Redis (try/catch sur blacklist check).
- Vérifie `jti` non blacklisté.
- Configuré dans `SecurityConfig` via `addFilterBefore(jwtAuthFilter, ...)`.

#### `SupabaseJwtAuthenticationFilter` (squelette non activé)
- Délègue à `SupabaseJwtService` (validation JWKS distant).
- Configuré dans `SecurityConfig` via `addFilterAfter(...)`.
- Credentials absents — le filtre ne fait rien pour l'instant.

#### `SupabaseJwtService`
- Téléchargement JWKS `https://{supabase.url}/auth/v1/.well-known/jwks.json`.
- Cache des clés publiques avec TTL.
- Méthode `validateToken(jwt)` retourne claims ou throw.

#### `RateLimitingFilter`
- Bucket par IP+endpoint via Redis.
- Limites (dévérrouillables en profil dev) :
  - `POST /auth/login` : 20/15 min
  - `POST /auth/refresh` : 20/5 min
  - autres API : 200/1 min

#### `SecurityConfig.java`
- 133+ endpoints mappés explicitement avant `anyRequest().authenticated()`.
- Activation conditionnelle de Supabase filter (sans clé → no-op).
- CORS toutes origines (dev).
- CSRF désactivé (stateless).

#### `UtilisateurDetailsService` + `CustomSecurityExpressionRoot`
- `UserDetailsService` Spring standard.
- SPEL custom beans (cf. §4).

### 6.2 `shared/ai/` (ORIA)

#### `OriaService` (le plus important)

Service principal de l'assistant IA. ~570 lignes.

**Méthodes publiques** :
- `sendMessage(req, userId)` — mode RAM (cache conversation), **non utilisé en prod**
- `sendMessageAndPersist(req, userId)` — mode BDD (utilisé par tous les callers
  réels : `OriaController` et `VocalService`)

**Persistance multi-tour** :
- Table `oria_messages` : `(id, session_id, role, contenu, message_timestamp, user_id)`
- `ProfilOrientation` (table) accumule mots-clés par userId (informatique, médecine…)
  et `resumeParcours` synthétisé.
- Session ID : `conv-{userId}` (un user = une session persistante).

**Pipeline d'appel (`callLLM()`)** — ordre de fiabilité décroissante :
1. **OpenAI** si `hasValidKey(openaiApiKey)` (non vide, ne commence pas par `REVOKED_`)
2. **Groq** si `hasValidKey(groqApiKey)`
3. **Ollama** en dernier recours (toujours tenté)

Helper clé : `hasValidKey(key)` détecte le préfixe `REVOKED_` pour éviter
HTTP 401. Cohérent avec `OpenAIEmbeddingServiceImpl.java:78`.

**Validation entrée** :
- Taille max 2000 caractères
- `INJECTION_PATTERN` regex (ignore/forget/system instructions...)
- `BLOCKED_WORDS = {hack, pirate, exploit, vuln, malware, virus, phish}`

**Recherche contexte RAG** :
1. `rechercherContexteVectoriel()` → `null` (pgvector absent de la DB Docker
   locale, désactivé volontairement — voir `JOURNAL_BORD_IA.md` 3 août §4).
2. `rechercherContexteMotCle()` : LIKE sur `titre/resume/contenu`, top 5.
3. Si vide → injecte `contexteVide(message)` (texte signalant au LLM
   l'absence de résultats, anti-hallucination).

**Prompt système** (~65 lignes dans `buildSystemPrompt()`) :
- Identité ORIA, zone Togo + international sur demande
- Règles strictes : ne pas refuser pour info publique, ne pas inventer d'établissement/filière/chiffre, anti-mélange de langues
- **Règle 5b (5 août 2026)** : si la base ne contient pas l'établissement
  demandé, dire explicitement "je n'ai pas d'information vérifiée" plutôt que d'inventer.

**Max tokens Ollama** : 150 (limite pour rapidité locale). **OpenAI/Groq** : 800.

#### `OpenAIEmbeddingServiceImpl`
- Génère vecteurs 1536-dim via `text-embedding-3-small`.
- **Fallback Ollama** (`nomic-embed-text`, 768-dim) si clé OpenAI absente/révoquée
  ou échec HTTP. Pattern `!key.startsWith("REVOKED_")` à la ligne 78.
- Stockage en colonne `real[]` dans la table `fiches` (limitations pgvector
  absent — voir §15 dette).

#### `OriaController` + `VocalService`
- `POST /api/v1/oria/message` — chat textuel
- `GET /api/v1/oria/sessions/{sessionId}` — historique
- `POST /api/v1/vocal/transcribe` — STT OpenAI Whisper
- `POST /api/v1/vocal/synthesize` — TTS OpenAI

### 6.3 `shared/minio/`

#### `MinioServiceImpl`
- Service principal (images, vidéos, documents).
- Buckets : `activeducation-images`, `activeducation-videos`, `activeducation-documents`.
- Création auto des buckets au boot si absents.
- Validation taille/type via `FileValidationUtil`.

#### `SupabaseStorageServiceImpl` (squelette)
- Implémentation Supabase Storage via REST API S3-compat.
- Délégué à `SupabaseStorageProperties` (URL + service-role-key).
- Pas activé en prod.

#### `MinioController`
- `POST /minio/upload/{bucket}` — multipart, retourne URL pré-signée
- `GET /minio/{bucket}/{filename}` — téléchargement
- `DELETE /minio/{bucket}/{filename}` — suppression (admin)
- `GET /minio/presigned-url/{bucket}/{filename}` — URL signée temporaire

### 6.4 `shared/config/`

- `WebMvcConfig` — CORS, message converters (deprecated Jackson, à migrer).
- `OpenApiConfig` — config Swagger (`@SecurityRequirement(name="BearerAuth")`).
- `DataLoader` — crée met à jour le compte admin par défaut (`admin@activeducation.tg`,
  mot de passe `admin123!`). **Le mot de passe est forcé à chaque démarrage**.
- `MaintenanceFilter` + `MaintenanceInitializer` — mode maintenance in-memory
  (flag statique, non persistant).
- `FlywayConfig` — Flyway 10 activé, `baseline-on-migrate=true`, `V1__baseline.sql`
  créé pour ne rien casser (migration destructive interdite).
- `MaintenanceProperties` — config rate-limit et maintenance.

### 6.5 `shared/exception/`

`GlobalExceptionHandler` (tous catch-all + handlers dédiés) :
- `MethodArgumentNotValidException` → 400 JSON propre
- `BadCredentialsException` → 401
- `AuthenticationException` → 401
- `AccessDeniedException` → 403
- `IllegalArgumentException` → 400
- Catch-all `Exception` → 500

### 6.6 `shared/websocket/`

- Endpoint `/ws/chat` (chat temps réel — alternative au polling mobile 4s).
- Pas activé par défaut dans la version actuelle (mobile utilise polling).

---

## 7. Fonctionnalités pédagogiques (focus)

### 7.1 Parcours élève typique

```
Inscription (EleveRequest) → Email confirmation
  → Connexion (JWT)
2FA opt-in
  → Test RIASEC (TestRIASECResultat créé)
  → Profil généré (R/I/A/S/E/C dimension scores)
  → Recommandations initiales (top-K filières)
  → Compléter profil (matières préférées, ambitions, niveau)
  → Upload bulletins (OCR PDF/PNG → notes structurées)
  → Recalcul prédictions basées sur notes réelles
  → Simulateur scénarios A/B
  → Chat ORIA pour questions ponctuelles
  → RDV conseiller (calendrier)
  → Choix définitif (OrientationOutcome)
  → Suivi post-orientation (notes université)
```

### 7.2 Algorithme "3 signaux" (cœur du projet)

`Recommandation3SignauxServiceImpl.candidatsPourProfil(profil)` retourne un
top-K de `FicheFiliere` scorées :

```
score(filiere, eleve) = poids_r * sim_cosinus_riasec(profilEleve, profilFiliere)
                       + poids_a * score_academique(moyenne, matieresPreferees)
                       + poids_g * affinite_geographique(ville, typeEtablissement)
```

Poids par défaut : `poids_r = 0.5`, `poids_a = 0.35`, `poids_g = 0.15`.

**Garde-fous** :
- Si `profil.niveauActuel == null` → retour `List.of()` + warn log. **Pas** de
  `findAll()` qui ferait sortir les 117 fiches au profil neutre.
- Si pas de liaisons `niveaux_filieres` → fallback `findAll()` avec warn.

### 7.3 Chat ORIA — détails techniques

**Modèle de conversation multi-tour persistant** :

1. User `POST /api/v1/oria/message {"message": "..."}`
2. `validateMessage()` rejette injection/mots bannis
3. `resolveSessionId()` retourne `"conv-" + userId` (1 session persistante par user)
4. `updateProfilOrientation()` extrait mots-clés (DOMAIN_KEYWORDS set) et
   accumule dans `profilOrientationRepository`
5. `rechercherContexte()` tente RAG, tombe sur mot-clé, ou `contexteVide()`
6. `callLLM(session, contexte, resumeParcours)` retourne réponse
7. `saveMessage()` en BDD (user + assistant)
8. Réponse JSON `{message, sessionId, historique[]}`

**Profil orientation accumulé** :
- `domainesInteret` : Set<String> accumulé (CSV), ex: `"informatique, mathématiques, médecine"`
- `premiereAmbition` : premier domaine détecté
- `dernierDomaine` : dernier message classifié
- `resumeParcours` : synthétisé pour injection prompt

**WebSocket** : configuré (`/ws/chat`) mais **pas activé** dans la version
courante — Flutter utilise polling toutes les 4 secondes (`BaseService` chat).

### 7.4 Upload bulletins (OCR multimodal)

`BulletinUploadOrchestrator` orchestre :

1. Réception multipart (PDF ou image)
2. Si PDF : extract via **PDFBox** (gratuit, sans clé API)
3. Si image : envoi OpenAI **Whisper** vision (nécessite clé OpenAI valide)
4. Parsing du texte extrait → notes structurées `{matiere, note, bareme, appreciation}`
5. Calcul moyenne + recalcul prédictions
6. Persistance `Bulletin` + notes

### 7.5 RIASEC — détail

**6 dimensions** : R (Réaliste) / I (Investigateur) / A (Artistique) /
S (Social) / E (Entrepreneurial) / C (Conventionnel).

**Méthode de scoring** :
- Chaque question a 6 poids (un par dimension)
- Score d'une dimension = somme des poids des réponses cochées / somme max
- Profil = vecteur `double[6]` normalisé [0, 1]
- Code Holland à 3 lettres (ex: "RIA") = tri décroissant des dimensions

**Catalogue `ProfilFiliereRiasecCatalog`** :
- 40+ filières mappées sur les 6 dimensions
- Méthode `profilFilierePour(String nom)` fait `containsIgnoreCase`
- Retour par défaut `NEUTRE = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5}`

### 7.6 Simulateur de parcours

Concepts métier :
- **Scénario** : ensemble de notes hypothétiques
- **Simulation** : exécution de l'algo 3 signaux sur ce scénario
- **Comparaison A/B** : différence entre 2 simulations

Templates disponibles : "Mon exploration", "maths excellentes",
"maths juste au seuil", "Seul", etc.

---

## 8. Mobile Flutter

### Stack & conventions

- **Pas de state management** : `setState` + singletons statiques sur `BaseService`
- **Dio** + intercepteur 401 (`_refreshWithLock()`)
- `flutter_secure_storage` (toujours `try/catch` — plante sur web, fallback mémoire)
- **2FA** : si `requires2fa=true` → POST `/auth/2fa/validate`
- **Comparaison de rôles** : **toujours `.toUpperCase()`**
- **Polling 4s** pour le chat (pas de WebSocket)
- **ORIA** : timeout Dio **120s** (modèles lents)

### Structure `lib/`

```
lib/
├── main.dart            # MaterialApp + routes initiales
├── services/            # BaseService + sous-services (Auth, Oria, Bulletin, ...)
├── screens/             # Écrans par feature
│   ├── auth/
│   ├── home/
│   ├── diagnostic/
│   ├── profil/
│   ├── oria/
│   ├── upload/
│   ├── calendrier/
│   └── ...
├── widgets/             # Composants partagés (cartes, drawers, bottom nav)
├── models/              # Sérialisation JSON Dart
├── utils/               # Constantes, validations
└── theme/               # app_theme.dart (palette cf. CHARTE_GRAPHIQUE.md)
```

### `BaseService` (cœur technique)

```dart
class BaseService {
  static final Dio _dio = Dio(BaseOptions(baseUrl: API_BASE_URL));
  static String? _accessToken;
  static String? _refreshToken;

  // Intercepteur 401 → tente refresh, sinon redirige /login
  // Skip /auth/login et /auth/refresh
  static Future<void> _refreshWithLock() async { ... }
}
```

### Endpoints Flutter consomme

Base path lu depuis `.env` (sans suffixe `/api/v1` ajouté — **c'est le
backoffice qui l'ajoute, pas Flutter**).

```
API_BASE_URL=https://activ-education-backend.onrender.com
# ou en local : http://10.0.2.2:8080 (émulateur Android)
```

### Écrans principaux

| Écran | Route | Description |
|---|---|---|
| Login | `/login` | Email/password, gère 2FA challenge |
| Home | `/` | Dashboard selon rôle (élève/parent/conseiller) |
| Test RIASEC | `/diagnostic/riasec` | Quiz questions, score calculé |
| Profil | `/profil` | Données user, matières préférées, ambitions |
| Recommandations | `/recommandations` | Top-K filières avec scores |
| Simulateur | `/simulateur` | Comparaison scénarios |
| Chat ORIA | `/oria` | Polling 4s, historique |
| Upload bulletin | `/bulletin/upload` | Caméra ou fichier |
| Calendrier RDV | `/rdv` | Prise RDV conseiller |
| Paramètres | `/settings` | 2FA toggle, notifications, logout |

### Widgets importants

- `bottom_nav.dart` : chaque `_NavItem` dans un `Expanded` (**ne jamais
  revenir à `spaceAround`** — bug historique)
- `app_theme.dart` : palette `CHARTE_GRAPHIQUE.md §2`

---

## 9. Backoffice React

### Stack

- **React 19** + **TypeScript 6 strict** + `erasableSyntaxOnly`
- **Vite** (port 5174)
- **Tailwind v4**
- **Zustand** + **TanStack Query v5**
- **react-router-dom v7** avec `ProtectedRoute`

### Alias & règles TS

- `@/` → `src/`
- Pas d'`enum`, pas de `namespace`, pas de `parameterProperties` (TS 6 erasable)
- Composants en `.tsx`, hooks en `.ts`

### Structure

```
src/
├── main.tsx
├── App.tsx
├── routes.tsx          # Configuration react-router
├── components/         # Composants partagés
├── pages/              # Une page par route
│   ├── auth/Login.tsx
│   ├── dashboard/Dashboard.tsx
│   ├── eleves/...
│   ├── fiches/...
│   ├── recommandation/...
│   └── admin/...
├── api/                # Clients TanStack Query
├── stores/             # Zustand (auth, ui)
├── hooks/              # Custom hooks
├── types/              # Types TS partagés
└── utils/              # Constantes, validations
```

### Routes & rôles

`ProtectedRoute` pour les 3 rôles : `CONSEILLER`, `ADMIN`, `SUPER_ADMIN`.
Redirection automatique vers `/login` si pas authentifié.

| Route | Rôle requis | Description |
|---|---|---|
| `/login` | public | Connexion |
| `/dashboard` | tous | KPIs selon rôle |
| `/eleves` | CONSEILLER+ | Liste élèves, recherche, filtres |
| `/eleves/{trackingId}` | owner | Détail élève, profil, prédictions |
| `/fiches` | ADMIN+ | CRUD fiches catalogue |
| `/recommandation` | CONSEILLER+ | Reco par élève |
| `/admin/users` | ADMIN | Gestion utilisateurs |
| `/admin/datahub` | SUPER_ADMIN | Exports, stats agrégées |

### Conventions API

`VITE_API_BASE_URL=http://localhost:8080/api/v1` (suffixe `/api/v1`
**inclus dans le backoffice**, contrairement au mobile).

---

## 10. Données seed

### 117 fiches établissements togolais

Emplacement : `activ-education-fronted-main/seed/` (Markdown structuré).
Source de vérité : scripts de seed (à exécuter dans l'ordre avec JWT admin).

### Scripts de seed (à exécuter dans l'ordre)

| Script | Cible |
|---|---|
| `seed_users.sh` | Compte admin + utilisateurs de démo |
| `seed_bibliotheque.sh` | FAQ, séries, catégories |
| `seed_universites.sh` | 117 fiches établissements (.md) |
| `seed_quiz.sh` | Questions/réponses quiz RIASEC |
| `seed_orientation_tests.sh` | Sessions de test démo |

Alternative rapide : `bash seed/seed_local.sh` (tout-en-un).

⚠️ **Les seeds nécessitent un JWT admin valide**. L'ordre est important
(dépendances FK).

### Métadonnées seed

- Compte admin par défaut : `admin@activeducation.tg` / `admin123!` (forcé par
  `DataLoader` au boot)
- Suivi via les tables `flyway_schema_history` (V1 baseline)

---

## 11. Tests

### Backend — 19 fichiers de tests

Tous sous `activ-education-backend-main/src/test/java/tg/edtch/activEducation/`.

| Fichier | Rôle |
|---|---|
| `ActivEducationApplicationTests` | Smoke test Spring context (skip si profil dev) |
| `JacksonTest` | Sérialisation JSON DTOs |
| `EleveServiceTest` | Inscription, soft-delete, validation password |
| `AuthServiceTest` | Login, mot de passe oublié, OTP |
| `AuthControllerTest` | Endpoints REST auth |
| `StatsControllerTest` / `StatsServiceTest` | Statistiques |
| `TestControllerTest` | Contrôleur de test (création users) |
| `TestBeansConfig` | Config beans pour tests |
| `OpenAIEmbeddingServiceImplTest` | Fallback Ollama quand clé révoquée |
| `OriaServiceTest` | Validation, multi-tour, profil orientation, anti-hallucination |
| `BulletinUploadOrchestratorImplTest` | OCR pipeline (PDF + images) |
| `BulletinUploadControllerTest` | Endpoints upload bulletins |
| `NoteTrajectoireServiceTest` | Calcul trajectoire depuis notes |
| `PredictionLookupServiceTest` | Cache lookup prédictions |
| `PredictionServiceTest` | Génération prédictions |
| `Recommandation3SignauxServiceTest` | Algorithme 3 signaux + anti-régression |
| `OrientationOutcomeServiceTest` | Statut post-orientation |
| `ScenarioTemplateControllerTest` | Templates simulateur |
| `SimulateurParcoursServiceTest` | Comparaison A/B |

### Commandes

```bash
cd activ-education-backend-main
./mvnw test                                              # tous
./mvnw test -Dtest='OriaServiceTest'                     # un fichier
./mvnw test -Dtest='OriaServiceTest#multiTurnKeepsHistory'  # une méthode
./mvnw -o test                                           # hors ligne
```

### Frontend (Flutter) — 3 fichiers

`activ-education-fronted-main/activ_education/test/` :
- Smoke tests
- Sérialisation
- Appels API mockés

```bash
flutter test
flutter analyze  # 0 erreur requise
```

### Backoffice — pas de framework de test

Tests visuels via `npm run build` (TS strict + lint).

---

## 12. API publique — conventions

### Base path

```
/api/v1  (préfixe global dans SecurityConfig)
```

### Format réponse

- Success : entité ou `Page<T>` Spring Data
- Erreur : `{ "message": "...", "code": "...", "fieldErrors": [...] }`
- 400 (validation), 401 (auth), 403 (forbidden), 404, 500

### Identifiants

- `{trackingId}` (UUID v4) dans toutes les URLs
- JAMAIS d'ID numérique exposé

### Swagger UI

`http://localhost:8080/swagger-ui.html` (dev). `BearerAuth` global security
requirement activé.

### OpenAPI spec

Générée auto par SpringDoc. Téléchargeable `/v3/api-docs`.

### CORS

Toutes origines (dev) — à restreindre en prod via `CorsConfigurationSource`.

---

## 13. Commandes essentielles

### Backend

```bash
cd activ-education-backend-main
docker compose up -d db minio redis      # Services uniquement
docker compose up -d --build             # Stack complète (app incluse :8080)
./mvnw spring-boot:run                   # Dev local (DB doit être sur :5432)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   # Profil dev (rate-limit désactivé)
./mvnw clean install                     # Build complet + tests
./mvnw package -DskipTests               # Rebuild rapide
./mvnw test -Dtest='OriaServiceTest'     # Un seul fichier de test
./start-backend.sh  # Helper de lancement (depuis la racine)
```

### Flutter mobile

```bash
cd activ-education-fronted-main/activ_education
flutter pub get
flutter run
flutter test
flutter analyze
adb reverse tcp:8080 tcp:8080           # Android → joindre backend local
```

### Backoffice

```bash
cd activ-education-fronted-main/backoffice
npm install
npm run dev          # Vite dev server :5174
npm run build        # tsc -b && vite build
npm run lint
```

### Seed (avec JWT admin)

```bash
cd activ-education-fronted-main/seed
bash seed_users.sh
bash seed_bibliotheque.sh
bash seed_universites.sh
bash seed_quiz.sh
bash seed_orientation_tests.sh
# Alternative rapide : bash seed_local.sh
```

---

## 14. Gotchas & pièges

> ⚠️ **Section critique** : ces pièges ont coûté du temps. À lire avant
> toute modification non triviale.

### Sécurité

1. **Secrets commités dans `.env`** : `OPENAI_API_KEY`, `JWT_SECRET`,
   `GROQ_API_KEY` — à **rotationner** avant tout déploiement production.
2. **Pas de Flyway sur schéma existant** : `ddl-auto=update` est resté.
   Migration vers Supabase demandera une vraie stratégie (Flyway V2 minimum).
3. **Compte admin par défaut forcé** : `DataLoader` réinitialise le mot de
   passe à `admin123!` **à chaque démarrage**. OK pour dev, **bloqueur prod**.

### Configuration / schéma

4. **DB Docker port 5433** (externe) → 5432 (interne). Clients SQL externes
   (DBeaver/pgAdmin) → `localhost:5433`.
5. **DDL auto-update** : tout changement d'entité peutdrop des données.
   Préférer Flyway pour évolutions sérieuses.
6. **DB seedée vide** après `docker compose down -v` : relancer `bash seed_*.sh`.

### Backend

7. **Ne pas dupliquer les routes** : vérifier dans les controllers existants
   + `SecurityConfig.java` avant d'en créer (133+ endpoints déjà mappés).
8. **`ParentRequest.motDePasse` / `ConseillerRequest.motDePasse`** :
   `@NotBlank`/`@Size` retirés (causaient des 401 silencieux sur update Flutter).
9. **`matieresPreferees`** : CSV dans une colonne `TEXT`, parsé par `EleveMapper`.
10. **OCR PDF** : `PDFBox` sans clé API. **OCR images** : nécessite
    `OPENAI_API_KEY` valide (Whisper vision).
11. **Mode maintenance** : flag in-memory statique (non persistant en DB).
12. **Monitoring** : `docker compose -f docker-compose.monitoring.yml up -d`
    séparé.
13. **`AUTH` BadCredentialsException** : tombait en 500 avant — corrigé
    dans `GlobalExceptionHandler` (5 août 2024).
14. **RIA hallucination établissement hors Togo** : protégé par règle 5b du
    prompt système + détection `REVOKED_` dans `OriaService` (5 août 2026).
15. **Élève sans niveau scolaire** : `Recommandation3SignauxServiceImpl`
    retournait `findAll()` → 117 fiches ; corrigé pour retourner `List.of()` + warn.

### Mobile Flutter

16. **`flutter_secure_storage`** : toujours `try/catch` — plante sur web,
    fallback mémoire.
17. **`image_picker`** : toujours wrapper dans `try/catch` (PlatformException
    `already_active`).
18. **Flutter web instable** : `Dart compiler exited unexpectedly` connu,
    pas encore résolu.
19. **`bottom_nav.dart`** : chaque `_NavItem` dans un `Expanded`, ne jamais
    revenir à `spaceAround`.

### Backoffice

20. **TS 6 strict + `erasableSyntaxOnly`** : pas d'`enum`, pas de `namespace`,
    pas de `parameterProperties`.
21. **VITE_API_BASE_URL inclut `/api/v1`** (différence avec le mobile).

### Sécurité applicative

22. **Webhook réflexion** : `oracle_web_security` détecte XSS dans Swagger UI.
23. **Référencement circulaire Lazy** : Hibernate — éviter les `toString()`
    qui chargent des `@OneToMany` dans les transactions.

---

## 15. État du projet & dette technique

### Couverture actuelle

- ✅ Backend complet (33 modules, 95/95 tests verts au 5 août 2026)
- ✅ Mobile Flutter complet (3 écrans principaux, 103 fichiers)
- ✅ Backoffice React complet (53 fichiers, rôles 3 niveaux)
- ✅ 117 universités togolaises seedées
- ✅ Chat ORIA fonctionnel (avec fallback Ollama en dev)
- ✅ 33 modules, 76 controllers, 80 services, 540 fichiers Java
- ⚠️ Migration Supabase : squelette compilable, activation réelle en attente credentials
- ⚠️ RAG vectoriel : désactivé volontairement (pgvector absent de DB Docker locale)

### Tests verts récents (sélection)

- Recommandation 3 signaux (anti-findAll aveugle)
- Bulletin upload orchestrator (PDF + image OCR)
- Statistiques (controllers + services)
- Oria multi-tour persistant + accumulation profil
- OpenAI embedding fallback Ollama
- **ORIA anti-hallucination (5 août 2026)** : ordre providers + règle 5b + contexteVide

### Dette technique connue (sprints futurs)

| Item | Priorité | Effort |
|---|---|---|
| **pgvector absent** : réactiver `rechercherContexteVectoriel` une fois pgvector installé + migration `real[] → vector` | Haute | ~2 jours |
| **Filtre géographique RAG** : ajouter champ `pays` ou `estTogolais` à `Fiche` + migration Flyway V2 | Haute | ~3 jours |
| **Activation Supabase réelle** : credentials + migration DB + tuning JWKS cache | Haute | ~1 semaine |
| **Mise en place stratégie** : tests de charge, monitoring Prometheus/Grafana | Moyenne | ~1 semaine |
| **Migrer WebMvcConfig** (Jackson deprecated) | Basse | ~2h |
| **`@Builder.Default`** : 8 classes ont des warnings à corriger | Très basse | ~1h |
| **`alooms/` package mort supprimé** (déjà fait) | — | — |
| **Doc `mentorat/` vs `alumni.Mentorat`** : clarifier pattern, déjà documenté | — | — |

### Architecture particulière

- **ORM** : Hibernate 7.2.7 avec `JOINED` sur `Fiche` → requêtes SQL natives
  perdent le discriminateur (`clazz_ not found`). Solution actuelle :
  **2-phase RAG** (SQL natif pgvector + JPQL réhydratation).
- **Soft-delete** : via `enabled=false` sur `Utilisateur`, pas via
  `@SQLDelete` (choix projet pour éviter la complexité de l'héritage).
- **WSL** : backend développé/testé sur WSL Ubuntu 22.04, pas Windows
  (problèmes de volumes Docker).

### Journal des décisions

Voir `activ-education-backend-main/JOURNAL_BORD_IA.md` pour l'historique
complet des décisions techniques du stage (cartographie, dette, correctifs,
tests anti-régression).

---

**Fin de la version de référence. Toute modification majeure du projet
doit s'accompagner d'une mise à jour de ce document.**

