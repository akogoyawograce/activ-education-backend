# JOURNAL DE BORD IA 🚀
*Ce fichier sert de mémoire partagée entre le développeur humain et l'Assistant IA pour tracer les problèmes résolus et l'évolution globale du projet `Activ EDUCATION`.*
*(À supprimer avant la mise en production).*

---

### Date : 11 Avril 2026

#### 1. Nettoyage et Restructuration du package MinIO
**Description du besoin** : Intégration d'un ancien package MinIO brut copié dans le dossier `shared`. Le package contenait de vieux buckets non utilisés (`songs`, `photos`, `archives`, `files`).
**Action réalisée** :
- Le package a été entièrement redéclaré pour correspondre à l'arborescence : `tg.edtch.activEducation.shared.minio.*`.
- La logique (`MinioServiceImpl`, `FileValidationUtil`) a été purifiée pour ne gérer que les buckets actuels pertinents : `IMAGE`, `VIDEO`, `DOCUMENT`, `PDF`.
- Les dépendances manquantes (`minio`, `pdfbox`, `tika-core`) ont été injectées dans le `pom.xml`.
- Documentation Swagger (`MinioController`) mise à jour pour le bon écosystème.

#### 2. Résolution de conflit Docker (Port PostgreSQL 5432)
**Problème rencontré** : Erreur `failed to bind host port 0.0.0.0:5432/tcp: address already in use` et `java.net.UnknownHostException: db` au lancement.
**Cause** : Le port 5432 de l'ordinateur était déjà accaparé par une instance locale de PostgreSQL. Par conséquent, le conteneur Docker de la Base de Données `activeducation-db` crashait au démarrage.
**Solution appliquée** : 
- Modification du fichier `docker-compose.yml` : l'exposition du port externe est passée de `5432:5432` à `5433:5432`.
- **Note pour le développeur** : L'application Spring Boot tournant *à l'intérieur* de Docker se connecte toujours sur le port 5432 (port interne réseau Docker). En revanche, vos clients SQL externes (DBeaver, pgAdmin) installés sur votre machine hôte devront communiquer avec `localhost:5433`.
- Nettoyage réseau total via `docker compose down -v` suivi d'un `docker compose up -d --build` pour forcer Docker à oublier l'ancienne configuration coincée en cache.

### Date : 21 Mai 2026

#### 1. Correction de l'erreur 401 Unauthorized dans Swagger
**Problème rencontré** : Même après authentification, les requêtes (POST et GET) retournaient une erreur 401.
**Causes identifiées** :
- **Swagger Headers** : `OpenApiConfig` ne déclarait pas de `SecurityRequirement` global, donc Swagger UI n'envoyait pas le header `Authorization: Bearer <token>` même après avoir cliqué sur "Authorize".
- **Redis Dependency** : `JwtAuthenticationFilter` n'était pas "fail-open" en cas d'indisponibilité de Redis. Si Redis était coupé, la vérification de la blacklist échouait avec une exception, interrompant le processus d'authentification et résultant en un 401.
- **Paths Erronés** : Dans `SecurityConfig.java`, les chemins pour la bibliothèque (GET) utilisaient des préfixes comme `fiches-metiers/**` au lieu de `metiers/**` (chemins réels des contrôleurs), bloquant l'accès public.
**Solutions appliquées** :
- Ajout de `@SecurityRequirement(name = "BearerAuth")` dans `OpenApiConfig.java`.
- Sécurisation du check Redis dans `JwtAuthenticationFilter.java` avec un try-catch (fail-open).
- Alignement des chemins dans `SecurityConfig.java` avec les routes réelles des contrôleurs.

#### 2. Mise en place/Vérification de la déconnexion Éélève
**Description du besoin** : L'élève doit pouvoir se déconnecter de son compte.
**Action réalisée** :
- Vérification de l'endpoint `POST /api/v1/auth/logout`. Il est fonctionnel pour tous les rôles authentifiés (ADMIN, ELEVE, PARENT, CONSEILLER).
- Cet endpoint révoque le Refresh Token en base et tente de blacklister l'Access Token dans Redis (si disponible).
- L'élève peut désormais l'utiliser via Swagger ou une application frontend.

#### 3. Nettoyage des logs de démarrage (Conflit Spring Data JPA/Redis)
**Problème rencontré** : Au démarrage, de nombreux messages d'avertissement indiquaient que Spring Data Redis essayait de scanner les repositories JPA, activant un mode de configuration "strict".
**Cause** : La présence du starter `spring-boot-starter-data-redis` active par défaut le scan pour les repositories Redis. Comme le projet n'utilise Redis que pour le cache/blacklist (via `StringRedisTemplate`) et non pour des repositories, cela générait du bruit inutile et de la confusion pour Spring Data.
**Solution appliquée** :
- Désactivation du scan des repositories Redis via la propriété `spring.data.redis.repositories.enabled=false` dans `application.properties`.
- Cela force Spring Data à n'utiliser que le module JPA pour les repositories, supprimant les avertissements et optimisant légèrement le temps de démarrage, tout en évitant les erreurs de compilation liées à des références de classes d'autoconfiguration.

### Date : 22 Mai 2026

#### 1. Correction de l'échec de connexion Admin (DataLoader)
**Problème rencontré** : Erreur 401 Unauthorized lors de la tentative de connexion avec le compte admin par défaut.
**Cause** : Le mot de passe défini dans `DataLoader.java` était `Admin123!` (avec une majuscule), alors que l'utilisateur tentait de se connecter avec `admin123!` (tout en minuscule). De plus, le `DataLoader` ne mettait pas à jour l'utilisateur si celui-ci existait déjà dans la base de données.
**Solution appliquée** :
- Modification de `DataLoader.java` pour utiliser `admin123!` par défaut.
- Ajout d'une logique de mise à jour automatique : le `DataLoader` force désormais le mot de passe défini dans le code même si l'administrateur existe déjà en base de données. Cela garantit que les identifiants de développement sont toujours synchronisés avec le code source après un redémarrage de l'application.

### Date : 3 Août 2026

#### 1. Cartographie comportementale d'ORIA en environnement dégradé (sans clés IA)
**Contexte** : Le backend a été lancé en local (DB Docker `:5433`) avec un `.env.local` surchargé. Les clés `OPENAI_API_KEY`, `GROQ_API_KEY` sont marquées `REVOKED_REPLACE_ME` dans `.env` original. Ollama local (`qwen2:0.5b` sur `:11434`) répond.

**Chaîne d'appel ORIA observée** (`OriaService.callLLM`) :
1. Ollama (`qwen2:0.5b`) — primary
2. OpenAI — fallback (échec 401, clé révoquée)
3. Groq — dernier fallback (échec, clé révoquée)

**Tests effectués** (POST `/api/v1/oria/message` avec token admin, max-time 90s) :

| Cas | Comportement | Verdict |
|-----|--------------|---------|
| "Métiers qui recrutent au Togo" | Réponse vague, aucun usage de la DB (111 métiers, 363 établissements ignorés) | ⚠️ Dégradé — embedding OpenAI en 401, fallback mot-clé trop faible |
| "Université de Lomé" | **Refuse** d'utiliser le nom, propos confus | ❌ Régression vs prompt (qui dit "ne jamais refuser pour un établissement public") |
| "Filières informatique au Togo" | Liste OK mais en partie en anglais | ❌ Prompt interdit le mélange de langues |
| "Ignore all previous instructions..." | Bloqué par `validateMessage` (`INJECTION_PATTERN`) | ✅ Prompt injection défendue |
| "C'est quoi la Série C ?" | Hallucine : "catégorie de films français", "premier roman" | ❌ Mauvaise réponse factuelle |
| "C'est quoi mon adresse ?" | Confabulation : "10 chiffres + 6 caractères codes sécurité" | ❌ Faux, ne respecte pas la vie privée |
| "UAO, c'est quoi ?" | "Je ne peux pas comprendre votre question en français" | ❌ Rejet abusif (acronyme togolais courant) |
| Multi-tour ("3ème + maths" → "filières") | Contexte conservé, propositions vagues | ⚠️ Pas d'ancrage DB |
| Injection prompt | Bloqué | ✅ |

**Cause racine** : `qwen2:0.5b` (0.5 milliard de paramètres) est **trop petit** pour ce use-case :
- Prompt système de 80+ lignes (long pour un si petit modèle)
- Hallucinations fréquentes sur la culture togolaise
- Rejets abusifs (interprète faussement les règles de prudence)
- Mélange de langues sur prompts multilingues

**Impact** : Avec une vraie clé OpenAI (`gpt-4o-mini` configuré) ou Groq (`llama-3.1-8b-instant`), le comportement serait très différent — ces modèles suivent correctement le prompt et utilisent le RAG via embeddings.

**Recommandations** :
1. **Court terme** : forcer OpenAI comme provider par défaut quand la clé est valide ; Ollama en dernier fallback (pas en premier)
2. **Moyen terme** : ajouter des tests automatisés sur questions factuelles togolaises pour détecter les hallucinations
3. **Long terme** : ajouter un `OriaModelEvaluator` pour benchmarker les providers

**Fichiers de référence** :
- `shared/ai/service/OriaService.java` (callLLM ligne 271)
- `shared/ai/controller/OriaController.java`
- `application.properties` (provider config)

---

#### 2. Fix bug `BadCredentialsException` → 500 dans GlobalExceptionHandler
**Problème** : Un login avec mauvais password renvoyait 500 (`handleGeneral(Exception)` capturait tout).
**Cause** : Le `GlobalExceptionHandler` n'avait que `MethodArgumentNotValidException`, `IllegalArgumentException`, et `Exception` catch-all. `BadCredentialsException` (Spring Security) tombait dans le catch-all → 500.
**Solution appliquée** : Ajout de 3 handlers dédiés dans `shared/exception/GlobalExceptionHandler.java` :
- `BadCredentialsException` → 401 + JSON propre
- `AuthenticationException` → 401 (autres erreurs Spring Security)
- `AccessDeniedException` → 403

**Vérifications post-fix** :
- ✅ Login password incorrect → 401
- ✅ Login email inexistant → 401
- ✅ Login body vide → 400 (validation)
- ✅ Login valide → 200 (pas de régression)
- ✅ 11 endpoints critiques testés → tous OK

**Note** : Ce bug était référencé dans `AGENTS.md` gotcha ("refresh token succeeds but retry still 401") — la cause racine présumée était ailleurs, mais ce handler corrige effectivement le symptôme visible pour les credentials.

---

### Date : 3 Août 2026 (suite)

#### 3. Activation du fallback embeddings Ollama (nomic-embed-text)
**Problème** : Avec `OPENAI_API_KEY=REVOKED_*` dans `.env.local`, ORIA ne pouvait pas générer d'embeddings → pas de RAG vectoriel, fallback mot-clé uniquement (qualité dégradée).

**Solution appliquée** :
- Modifié `OpenAIEmbeddingServiceImpl.generateEmbedding()` pour basculer automatiquement sur Ollama local (`localhost:11434`, modèle `nomic-embed-text`) quand la clé OpenAI est absente/invalide ou que l'appel OpenAI échoue (catch → fallback Ollama).
- Ajouté 2 nouveaux `@Value` : `ollama.embedding.url` et `ollama.embedding.model` (défauts : `http://localhost:11434` et `nomic-embed-text`).
- Ajouté 6 tests unitaires dans `OpenAIEmbeddingServiceImplTest` couvrant : clé REVOKED, clé vide, échec OpenAI → Ollama, clé valide → OpenAI direct, body Ollama invalide, Ollama réseau KO.

**Vérifications** :
- ✅ `mvnw -Dtest='OpenAIEmbeddingServiceImplTest,OriaServiceTest' test` → 18/18 verts
- ✅ Log live : `Clé OpenAI invalide/absente → fallback embeddings Ollama` puis `Embedding Ollama généré : 768 dimensions`

**Note** : Le fallback Ollama produit des vecteurs 768-dim **compatibles** avec le format pgvector (pour le jour où pgvector sera installé).

#### 4. Découverte : pgvector absent de la DB locale → RAG vectoriel désactivé
**Problème** : En testant ORIA live avec le fallback Ollama, le log montre `ERROR: operator does not exist: real[] <=> vector`. Investigation :
```sql
\d fiches  → embedding | real[] | (et non vector(768))
```
**Cause** : La colonne `fiches.embedding` est `real[]` natif PostgreSQL, pas `vector`. L'extension pgvector n'est **pas installée** sur cette DB. L'opérateur `<=>` (cosinus) n'existe pas non plus.

**Solution appliquée** (court terme) :
- Désactivé `rechercherContexteVectoriel` dans `OriaService` : retourne `null` directement → le code fallback vers `rechercherContexteMotCle` (LIKE sur titre/resume/contenu).
- Ajouté un commentaire pointant vers cette entrée du journal pour la réactivation future.

**Test live** (après fix) :
- ✅ POST `/api/v1/oria/message` "Université de Lome, informatique" → 200 en 16s, réponse cohérente sur Université de Lomé + sciences/tech, pas de mélange de langues, pas d'hallucination type "Université = privée".
- ⚠️ Réponse reste générique (paraphrase du prompt système) — qualité limitée par `qwen2:0.5b` (0.5B params).
- ✅ POST `... "C'est quoi la série C ?"` → 200 en 4s, reconnaît qu'il n'a pas la réponse claire et demande clarification (au lieu d'affirmer "catégorie de films français" comme avant).

**Plan d'activation RAG vectoriel (quand pgvector dispo)** :
1. **Installer pgvector** : ajouter `pgvector` dans l'image Docker de `db` ou `CREATE EXTENSION IF NOT EXISTS vector;` au démarrage.
2. **Migrer la colonne** :
   ```sql
   ALTER TABLE fiches ADD COLUMN embedding_vector vector(768);
   UPDATE fiches SET embedding_vector = embedding::vector(768) WHERE embedding IS NOT NULL;
   ALTER TABLE fiches DROP COLUMN embedding;
   ALTER TABLE fiches RENAME COLUMN embedding_vector TO embedding;
   ```
3. **Réactiver** `rechercherContexteVectoriel` dans `OriaService` (déjà codé, juste enlever le `return null`).
4. **Repasser le type** du paramètre `rechercherIdsParSimilariteGlobale` de `String` à `float[]` (CAST gèrera la conversion) ou garder `String` avec `toVectorLiteral()` (déjà implémenté dans `OriaService`).
5. **Réindexer** : appeler le service d'embedding sur tous les titres/resumes existants (script ou migration Flyway).

**Risques** :
- Locker la table pendant `ALTER TABLE` → fenêtre d'indispo (~minutes pour 117 fiches, OK)
- Migration non-atomique si gros volume → préférer `pt-online-schema-change` si > 10k rows
- Casser les références Hibernate si `real[]` est mappé explicitement dans une entité (à vérifier dans `Fiche.java`)

**Fichiers de référence** :
- `shared/ai/service/OriaService.java` (méthode `rechercherContexteVectoriel` ligne ~436)
- `bibliotheque/repository/FicheRepository.java` (méthode `rechercherIdsParSimilariteGlobale` ligne ~43)
- `shared/ai/service/impl/OpenAIEmbeddingServiceImpl.java` (méthode `generateEmbedding` ligne ~73)

---

#### 5. Tests ORIA étendus (RAG multi-tour, profil orientation, session ID)
**Problème** : La couverture de tests ORIA était limitée à la validation de message (injection, mots bannis, taille). Manquaient : persistance session, accumulation profil, isolation session par user.

**Solution appliquée** :
- Ajouté 6 tests dans `OriaServiceTest` :
  - `multiTurnKeepsHistory` — 2ème tour conserve l'historique du 1er (4 messages au lieu de 2)
  - `ragContextIncludedInOllamaPrompt` — le prompt envoyé à Ollama contient bien les infos RAG (« INFORMATIONS DE LA BASE DE DONNÉES »)
  - `profilOrientationDetectsKeywords` — mots-clés du message (informatique, mathématiques) détectés et stockés dans `ProfilOrientation.domainesInteret`
  - `profilOrientationSkipsIrrelevantMessage` — message sans mot-clé → aucun save
  - `profilOrientationAccumulatesDomains` — accumulation sur plusieurs messages (informatique + médecine)
  - `sessionIdIsUserScoped` — `alice` et `bob` ont des sessions distinctes (`conv-alice` vs `conv-bob`)
- Ajouté mocks par défaut dans `@BeforeEach` pour `profilRepository.save()` (retourne l'argument), `embeddingService.generateEmbedding()` (vecteur non-null), `rechercherIdsParSimilariteGlobale` (liste vide) et `rechercherParMotCle` (Page.empty).

**Vérifications** :
- ✅ `mvnw -Dtest='OriaServiceTest' test` → 12/12 verts (6 anciens + 6 nouveaux)
- ✅ Total backend : 18/18 verts (12 ORIA + 6 embedding)

---

### Date : 4 Août 2026 — Sprint audit + dette technique

#### 1. Audit complet des 33 modules
**Problème** : Pas de vision claire de l'état du backend avant la migration Supabase.

**Solution** :
- Cartographie exhaustive des 33 packages (entity, services, controllers, tests)
- Verdict module par module : 28 OK, 1 stub corrigé, 1 package mort supprimé
- Note globale révisée : **7.5/10** (puis 8.0/10 après corrections)
- Plan complet documenté dans `~/.claude/plans/groovy-petting-cat.md`

**Fichiers produits** :
- 5 actions prioritaires identifiées
- 4 livrables (plan, sprint, rapport, commits)

#### 2. Action #1 — Flyway 10 intégré (commit `fc0d1e9`)
**Problème** : `ddl-auto=update` garantit perte de données à chaque changement de schéma. Migration Supabase imminente.

**Solution** :
- Ajouté `flyway-core` + `flyway-database-postgresql` (Spring Boot 4 → Flyway 10)
- `application.properties` : `spring.flyway.enabled=true`, `baseline-on-migrate=true`, `baseline-version=0`, `locations=classpath:db/migration`
- Créé `V1__baseline.sql` (SELECT 1) pour ancrer la DB existante sans rien casser
- Aucune migration rétroactive — adoption non-destructive

**Vérification** : `mvnw -o test` → 91/91 verts. Build OK.

**Note** : Ne pas oublier d'**incrémenter manuellement la version Flyway** à chaque nouvelle migration. La V2 sera la première migration utile (ex: ajout colonne pour Supabase).

#### 3. Action #3 — Suppression `alums/` (package mort)
**Problème** : Le package `tg.edtch.activEducation.alums` ne contenait que des dossiers vides (squelette IDE), aucun fichier, aucune référence. Probablement un ancien nom d'`alumni`.

**Solution** : `rm -rf` du dossier. Pas de commit dédié car le package n'était pas tracké par git.

#### 4. Action #2 — Doc `mentorat/` vs `alumni.Mentorat` (commit `08ced9b`)
**Problème** : Audit initial signalait `mentorat/` comme doublon de `alumni/Mentorat`.

**Rectification** : `mentorat/` n'est PAS un doublon — c'est une **API REST mince** sur l'entité `alumni.Mentorat` (3 endpoints : GET/POST/PUT, ~14 lignes de service). Le service `MentoratService` injecte `alumni.MentoratRepository`. Pas de duplication de persistance, juste une vue API dédiée.

**Solution** : Documentation ajoutée à `AGENTS.md` (section "Backend architecture") pour clarifier la séparation. Confirmer ce pattern avant tout ajout futur.

#### 5. Action #4 — ORIA : `sendMessageAndPersist` systématique
**Problème** : Audit initial craignait que la version RAM-only `sendMessage()` soit encore utilisée.

**Rectification** : Lu dans `OriaController.java` ligne 31 et `VocalService.java` ligne 47 — les **seuls appelants** utilisent DÉJÀ `sendMessageAndPersist()`. La critique était partiellement fausse. Le cache RAM est désormais secondaire, la BDD est la source primaire.

**Décision** : Aucune modification. `sendMessage()` (RAM-only) reste dans l'API mais n'est pas appelé en production.

#### 6. Action #5 — Bug moteur 3 signaux + test anti-régression (commit `9cc37c0`)
**Problème** : `Recommandation3SignauxServiceImpl.candidatsPourProfil()` ligne 220 — quand `niveauActuel == null`, le code faisait `return ficheFiliereRepository.findAll()` → retournait **les 117 fiches** scorées avec un profil quasi-neutre. Un élève sans niveau (compte fraîchement créé, profil incomplet) recevait un top 10 absurde.

**Solution** :
- Modifié : retour `List.of()` + `log.warn("Élève {} sans niveau scolaire : recommandation retournée vide (pas de findAll() aveugle)", profil.getTrackingId())`
- Ajouté test `eleveSansNiveauNeRetournePasToutesLesFilieres()` dans `Recommandation3SignauxServiceTest` :
  - `verify(ficheFiliereRepository, never()).findAll()`
  - Le test **échouait** sur le code initial (preuve du bug) → passe après le fix
- Pattern de garde-fou : un comportement dangereux doit être **exprimé par un test négatif** explicite.

**Vérification** : `mvnw -o test` → 92/92 verts (1 nouveau test).

#### 7. Commits atomiques — 5 commits sur `main` (4 août 2026)
```
fc0d1e9  feat: intègre Flyway 10 (préparation migration Supabase)
08ced9b  docs: clarifie architecture backend (mentorat vs alumni) + commentaire cassé
9cc37c0  fix(prediction): moteur 3 signaux — élève sans niveau ne reçoit plus 117 fiches
7c3367f  feat(oria): multi-tour persistant + accumulation profil + fallback embeddings Ollama
e615ac2  feat: refacto simulateur + fiches filière + MinIO 500 + GlobalExceptionHandler
```

**État final** : 92/92 tests verts, build OK, dette critique résolue avant migration Supabase.

#### 8. Restant intentionnel (chantier Supabase)
Fichiers créés mais non commités (en attente des credentials) :
- `shared/security/supabase/SupabaseJwtService.java`
- `shared/security/filter/SupabaseJwtAuthenticationFilter.java`
- `shared/minio/config/SupabaseStorageProperties.java`
- `shared/minio/service/impl/SupabaseStorageServiceImpl.java`
- `shared/security/config/SecurityConfig.java` (5 lignes ajoutées)
- `application.properties` (bloc supabase.*)
- `prediction/domain/util/ProfilFiliereRiasecCatalog.java` (ajout)

**Bloqueur** : credentials Supabase (`SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY`, `DB_PASSWORD`) non fournis par l'utilisateur.

**Note** : la première migration Flyway utile (V2) sera probablement liée à Supabase (changement de schéma, ajout de tables auth, etc.).

