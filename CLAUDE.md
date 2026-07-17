# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **📖 Source of vérité :** Ce projet contient déjà un `AGENTS.md` riche qui est la référence principale. Le présent fichier n'en est qu'un résumé orienté démarrage rapide — **toujours consulter `AGENTS.md` pour les détails fins** (gotchas, sécurité, conventions précises).

## Vue d'ensemble

**Activ EDUCATION** — plateforme d'orientation scolaire et professionnelle pour le Togo, développée dans le cadre d'un stage à HubCity/Woélab. Trois sous-projets dans le même dépôt :

| Sous-projet | Path | Stack | Port |
|---|---|---|---|
| Backend | `activ-education-backend-main/` | Spring Boot 4.0.5 + Java 21 + Maven | 8080 |
| Mobile (élèves) | `activ-education-fronted-main/activ_education/` | Flutter / Dart (`setState`, pas de state management) | — |
| Backoffice (admins) | `activ-education-fronted-main/backoffice/` | React 19 + TypeScript 6 (strict, `erasableSyntaxOnly`) + Vite + Tailwind v4 | 5174 |
| Seed data | `activ-education-fronted-main/seed/` | 117 universités togolaises en `.md` + scripts shell/SQL | — |

PostgreSQL 16 + pgvector pour la recherche sémantique RAG (2-phase : SQL natif cosinus → JPQL réhydratation). IA : **OpenAI** (migré depuis Gemini) pour les embeddings, **Groq** en fallback, **Ollama local** (`qwen2:0.5b`) pour l'assistant ORIA. CI/CD : 3 workflows GitHub Actions dans `.github/workflows/` (backend / backoffice / flutter).

## Commandes essentielles

### Backend (`activ-education-backend-main/`)
```bash
docker compose up -d db minio redis        # Services only (DB :5433, MinIO :9000/9001, Redis :6379)
docker compose up -d --build               # Full stack (app incluse sur :8080)
./mvnw spring-boot:run                     # Dev local (DB doit tourner sur :5432)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   # Profil dev (rate limit 99999)
./mvnw clean install                       # Build complet + tests
./mvnw package -DskipTests                 # Rebuild rapide
./mvnw -Dtest=AuthServiceTest test         # Un seul test
```
DB locale : `localhost:5432` (5433 en Docker), user `postgres`, pass `abalakata`, db `activ_education`. Swagger : `http://localhost:8080/swagger-ui.html`.

### Flutter mobile (`activ-education-fronted-main/activ_education/`)
```bash
flutter pub get
flutter run
flutter test                              # 3 fichiers de test (smoke, serialization, API)
flutter analyze                           # Doit retourner 0 erreur
adb reverse tcp:8080 tcp:8080             # Android → joindre le backend local
```

### Backoffice (`activ-education-fronted-main/backoffice/`)
```bash
npm install
npm run dev                                # Vite dev server :5174
npm run build                              # tsc -b && vite build
npm run lint                               # ESLint
```
**Pas de framework de test** côté backoffice.

### Seed (à exécuter dans l'ordre, avec JWT admin)
```bash
bash seed/seed_users.sh
bash seed/seed_bibliotheque.sh
bash seed/seed_universites.sh
bash seed/seed_quiz.sh
bash seed/seed_orientation_tests.sh
# Alternative rapide : bash seed/seed_local.sh
```

## Architecture backend (Package by Feature)

Package racine : `tg.edtch.activeducation.*`. **30+ packages** suivant le pattern **Package by Feature** (le chemin dans le code utilise `activEducation` avec un E majuscule, bien que le nom du dossier soit en minuscules).

- **Cœur initial (5 modules)** : `profil`, `bibliotheque`, `diagnostic`, `accompagnement`, `shared`
- **Fonctionnels** : `alumni`, `alums`, `attestations`, `badge`, `cahierdebord`, `calendrier`, `cartemetiers`, `cvgenerateur`, `datahub`, `defis`, `emploi`, `entretien`, `horsligne`, `mentorat`, `parrainage`, `portfolio`, `prediction`, `recommandation`, `reorientation`, `reseau`, `riasec`, `sallevirtuelle`, `simulateur`, `temoignage`, `vae`
- **Héritage JPA** : `BaseEntity` (Long PK + UUID `trackingId` dans toutes les URLs REST)
- **Polymorphisme** : `Fiche` abstraite avec `InheritanceType.JOINED` (Lombok `@SuperBuilder`)
- **RAG vectoriel en 2 phases** : SQL natif pour la similarité cosinus pgvector → JPQL pour réhydrater les bonnes sous-classes (JOINED perd le discriminateur en SQL natif → `clazz_ not found`)
- **Sécurité** : `SecurityConfig.java` est dans `shared/security/config/` ; les SPEL custom sont dans `shared/security/expression/CustomSecurityExpressionRoot.java`

## Architecture Flutter

- **Pas de state management** : `setState` + singletons statiques sur `BaseService`
- **`BaseService`** (dans `lib/services/base_service.dart`) : Dio + intercepteur 401 avec `_refreshWithLock()` (skip `/auth/login` et `/auth/refresh`)
- **Endpoints** : `API_BASE_URL` dans `.env` **sans** suffixe `/api/v1` (c'est le backoffice qui l'ajoute)
- **Chat** : polling 4 secondes (pas de WebSocket). ORIA : timeout Dio à 120s
- **Stockage** : `flutter_secure_storage` (toujours `try/catch` — plante sur web, fallback mémoire)
- **2FA** : si `requires2fa=true` dans la réponse login, appeler `/auth/2fa/validate` avec `challengeToken`
- **Comparaison de rôles** : **toujours `.toUpperCase()`** — le backend renvoie du PascalCase (`"Parent"`, `"Conseiller"`)

## Architecture Backoffice

- **TS 6 strict** avec `erasableSyntaxOnly` : pas d'`enum`, pas de `namespace`, pas de `parameterProperties`
- **Alias** : `@/` → `src/`
- **State** : Zustand + TanStack Query v5
- **Routing** : react-router-dom v7, `ProtectedRoute` pour les 3 rôles (`CONSEILLER`, `ADMIN`, `SUPER_ADMIN`)
- **`VITE_API_BASE_URL`** inclut le suffixe `/api/v1`

## Sécurité

- **JWT filter toujours actif** : stateless, CSRF désactivé, CORS toutes origines
- **`@PreAuthorize`** sur chaque controller avec des SPEL custom beans :
  - `isOwner(#trackingId)`, `isOwnChild(#eleveTrackingId)`, `isOwnConseiller(#conseillerTrackingId)`, `isRdvParticipant(#rdvTrackingId)`
- **`SecurityConfig.java`** (133+ endpoints) : vérifier avant d'ajouter une route
- **Validation** : tous les write endpoints ont `@Valid` sur les DTOs ; `GlobalExceptionHandler` traduit en 400 JSON (le `/error` du filtre JWT peut renvoyer du 401 par erreur sur des cas de validation mal routés)
- **Rate limiting Redis** : login (20/15min), refresh (20/5min), API (200/1min) — désactivé en dev

## ⚠️ Pièges critiques (lire avant de modifier)

1. **Secrets commités dans `.env`** : `OPENAI_API_KEY`, `JWT_SECRET`, `GROQ_API_KEY` — **à rotationner avant tout déploiement**
2. **Pas de Flyway/Liquibase** : `ddl-auto=update` — tout changement de schéma risque une perte de données
3. **DB déployée vide** : exécuter les scripts `seed/*.sh` dans l'ordre (avec JWT admin)
4. **Ne pas dupliquer les routes** : vérifier dans les controllers existants + `SecurityConfig.java` avant d'en créer
5. **`ParentRequest.java` / `ConseillerRequest.java`** : `motDePasse` `@NotBlank`/`@Size` retirés (causaient des 401 sur update)
6. **`matieresPreferees`** : stocké en CSV dans une colonne `TEXT`, parsé par `EleveMapper`
7. **OCR** : nécessite `OPENAI_API_KEY` pour images (PDF → PDFBox sans clé)
8. **`image_picker`** : toujours wrapper dans `try/catch` (Flutter `PlatformException(already_active)`)
9. **Mode maintenance** : flag in-memory statique (non persistant)
10. **Monitoring** : `docker compose -f docker-compose.monitoring.yml up -d` séparé
11. **Flutter web instable** : `Dart compiler exited unexpectedly`
12. **`bottom_nav.dart`** : chaque `_NavItem` dans un `Expanded`, ne jamais revenir à `spaceAround`
13. **9 fichiers de test backend** (pas 3) : `ActivEducationApplicationTests`, `JacksonTest`, `EleveServiceTest`, `AuthServiceTest`, `AuthControllerTest`, `StatsControllerTest`, `StatsServiceTest`, `TestControllerTest`, `TestBeansConfig`

## Avant toute modification

1. **Lire `activ-education-fronted-main/seed/cahier_de_charge.md`** pour vérifier que la feature est spécifiée
2. **Vérifier les controllers existants + `activ-education-backend-main/src/main/java/tg/edtch/activEducation/shared/security/config/SecurityConfig.java`** avant de créer un nouvel endpoint (133+ endpoints déjà mappés)
3. **Consulter `AGENTS.md`** pour le détail des conventions (sécurité, gotchas, services Flutter)

## Fichiers de référence clés

- `AGENTS.md` — référence technique complète (commandes précises, architecture, sécurité, gotchas) — **à consulter avant toute modif non triviale**
- `activ-education-backend-main/README.md` — architecture et modules backend
- `activ-education-backend-main/CHANGELOG.md` — historique des versions
- `activ-education-backend-main/JOURNAL_BORD_IA.md` — journal des décisions IA du stage
- `activ-education-backend-main/DEPLOY.md` — procédure de déploiement
- `activ-education-fronted-main/seed/cahier_de_charge.md` — spécifications fonctionnelles
- `activ-education-fronted-main/seed/etat-projet.md` — état d'avancement détaillé
- `instructions_claude.md` — contexte métier (mémoire de stage, structure d'accueil)
