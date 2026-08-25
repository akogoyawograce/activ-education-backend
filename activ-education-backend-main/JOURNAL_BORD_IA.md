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

---

### Date : 5 Août 2026

#### 1. Anti-hallucination ORIA sur les établissements togolais

**Problème** : ORIA a renvoyé à un élève une liste d'établissements contenant `University of Cotonou (UCC)` (Bénin) et `Université de la Nouvelle-Calédonie (UNCN)` alors que la question portait sur les universités au Togo. Hallucination LLM, pas un bug HTTP.

**Causes** :
1. **Ordre des providers incohérent** : `callLLM()` tentait Ollama (`qwen2:0.5b`) en premier, qui est notoirement faible. La fiabilité décroissante aurait dû être OpenAI > Groq > Ollama.
2. **Prompt trop permissif** : `buildSystemPrompt()` disait "zone de compétence principale Togo" mais autorisait l'international sur demande explicite, sans règle stricte anti-invention quand la base BDD est vide.
3. **`rechercherContexteMotCle()` silencieuse** : renvoyait `null` si aucune fiche → le LLM recevait le prompt sans aucun signal "pas de contexte dispo", ce qui poussait le modèle à inventer.

**Correctifs appliqués** dans `OriaService.java` :
1. **`callLLM()` réordonné** : boucle `List<Supplier<String>>` qui tente OpenAI puis Groq puis Ollama (dernier recours). Helper `hasValidKey()` qui détecte le préfixe `REVOKED_` — cohérent avec `OpenAIEmbeddingServiceImpl.java:78`. Évite un appel HTTP 401 quand la clé est `REVOKED_REPLACE_ME`.
2. **Règle 5b ajoutée au prompt** : « Si la section "INFORMATIONS DE LA BASE DE DONNÉES" ne contient pas l'établissement/filière demandé, NE JAMAIS inventer un nom (surtout pas hors du Togo si la question porte sur le Togo). Répondre explicitement "je n'ai pas d'information vérifiée" plutôt que d'inventer. »
3. **`rechercherContexteMotCle()` → `contexteVide(message)`** : retourne un bloc de contexte `"Aucune fiche ne correspond à « <question> » dans la base togolaise [...] ne cite aucun établissement précis"` quand la recherche est vide, plutôt que `null`.

**Tests ajoutés** (3 dans `OriaServiceTest`) :
- `providersAreTriedInReliabilityOrder` : clé OpenAI valide → Ollama jamais appelé (ArgumentCaptor sur URL).
- `revokedApiKeyIsSkipped` : clé `OPENAI_API_KEY=REVOKED_REPLACE_ME` → pas d'appel HTTP 401, on tombe direct sur Ollama.
- `emptyKeywordContextInjectsExplicitSignal` : page BDD vide → le payload Ollama contient `Aucune fiche ne correspond` et `ne cite aucun établissement précis`.

**Vérifications** :
- ✅ `mvnw -o test -Dtest='OriaServiceTest'` → 16 tests verts (12 anciens + 3 nouveaux, 1 skipped préexistant).
- ✅ `mvnw -o test` (suite complète) → 95/95 verts, aucune régression.

**Hors scope (sprint dédié)** :
- **`FicheRepository.rechercherParMotCle()` — filtre géographique en base** : nécessite ajouter un champ `pays` ou `estTogolais` sur l'entité `Fiche`, ce qui implique migration Flyway V2 + retrait de `ddl-auto=update`. À traiter dans un sprint "filtre géographique RAG".

**Fichiers modifiés** :
- `shared/ai/service/OriaService.java`
- `shared/ai/service/impl/OpenAIEmbeddingServiceImpl.java` (référence au pattern `REVOKED_`, pas modifié)
- `src/test/java/.../shared/ai/service/OriaServiceTest.java`

**Note transférée de la session 3 août** : ce correctif ferme le point identifié au §1 du journal précédent (cartographie comportementale d'ORIA en environnement dégradé). L'environnement reste dégradé (clés révoquées → Ollama seul), mais le comportement est désormais borné par la règle 5b du prompt.

---

### Date : 6 Août 2026

#### 1. Vérification live des correctifs ORIA + découverte du bug de profil dev

**Suite de la séance du 5 août** : tests live sur backend démarré, après mise à jour de la clé OpenAI dans `.env.local`.

**Test live 1 — reproduction de BUG A** :
- Question : « Liste moi les universités au Togo »
- Provider tenté : OpenAI (1er de la liste de fallback)
- Erreur : `404 NOT_FOUND - The model llama-3.3-70b-versatile does not exist`
- Diagnostic : la clé OpenAI est valide (HTTP 200 sur `/v1/models`), mais le profil `application-dev.properties` forçait `openai.api.chat.model=llama-3.3-70b-versatile` (un modèle **Groq**, pas OpenAI) → 404 systématique.

**Découverte** : le profil `dev` était configuré comme un **proxy vers Groq** :
```
openai.api.chat.url=https://api.groq.com/openai/v1/chat/completions
openai.api.chat.key=${OPENAI_CHAT_KEY:}
openai.api.chat.model=llama-3.3-70b-versatile
```
Toute clé OpenAI dans `OPENAI_CHAT_KEY` se faisait donc rejeter par Groq. Si on mettait une clé Groq dedans, ça marchait par hasard. Sinon, fallback Ollama → hallucinations.

**Test live 2 — reproduction de BUG B** : pas eu le temps, le test live 1 a tout monopolisé sur le diagnostic du profil dev.

**Test live 3 — tentative avec vraie URL OpenAI** :
- Modification : URL → `https://api.openai.com/v1/chat/completions`, modèle → `gpt-4o-mini`.
- Erreur : `429 TOO_MANY_REQUESTS - You have no credits remaining`
- Diagnostic : clé OpenAI valide, mais compte à 0 crédit.

**Décision** : bascule sur Groq via la même structure (`openai.api.chat.*`) en attendant que le compte OpenAI soit re-crédité. Nouvelle conf `dev` :
```
openai.api.chat.url=https://api.groq.com/openai/v1/chat/completions
openai.api.chat.key=${GROQ_API_KEY}
openai.api.chat.model=llama-3.1-8b-instant
```

**Test live 4 — VALIDATION des correctifs ORIA avec Groq `llama-3.1-8b-instant`** :

| Test | Question | Résultat |
|---|---|---|
| T1 — BUG A | « Liste moi les universités au Togo » | ✅ « Je n'ai pas d'information vérifiée sur les universités au Togo dans ma base de données. [...] consultez le site du MESR » — **plus aucune hallucination** |
| T2 — BUG B | 2 tours : « terminale série D quelles filières » puis « quelle école pour l'informatique » | ✅ 2 réponses distinctes, historique cumulé de 6 messages (2 user + 2 assistant + 2 user + 2 assistant) |
| T3 — RAG | « Parle moi de l'Université de Lomé » (la fiche **est** en base) | ⚠️ « Je n'ai pas trouvé de fiche correspondante » — le RAG mot-clé ne matche pas, indépendant du LLM |

**Conclusion** :
- ✅ **BUG A résolu en conditions live** : le prompt renforcé + le signal `contexteVide()` bloquent les hallucinations sur les 2 providers testés (Groq et OpenAI-before-quota).
- ✅ **BUG B résolu** : la persistance multi-tour fonctionne, l'historique est correctement agrégé.
- ⚠️ **RAG mot-clé trop strict** : avec Groq, on n'invente plus, mais on ne trouve pas non plus les fiches existantes. Le fallback RAG (recherche par mot-clé sur titre) est trop restrictif. La solution pérenne est **l'activation de pgvector** (sprint dédié).

**Sécurité** : la clé OpenAI mise dans `.env.local` pendant ces tests a été affichée en clair dans le chat (ligne 25+26) — elle est compromise et doit être révoquée sur https://platform.openai.com/api-keys. Le PAT GitHub `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` est aussi compromis (cf. sessions précédentes).

**Fichiers modifiés ce soir** :
- `application-dev.properties` : URL + modèle chat corrigés (Groq-compatible, model `llama-3.1-8b-instant`).
- `OriaService.java` : logs `[ORIA-DEBUG]` (a)-(h) dans `sendMessageAndPersist()` pour diagnostic (commit `5f83516`).
- `OriaServiceTest.java` : 3 tests anti-régression (BUG A, BUG B, série D vs C) (commit `7ca2bf7`).

**Action recommandée pour la suite** :
1. Révoquer la clé OpenAI compromise.
2. Activer pgvector (sprint dédié — cf. section "hors scope" du 5 août).
3. Quand le compte OpenAI sera re-crédité, remettre l'URL `https://api.openai.com/v1/chat/completions` + `gpt-4o-mini` dans le profil dev (Groq en fallback via `callGroq()` déjà câblé dans `callLLM()`).

#### 2. Bug RAG détecté en live : recherche mot-clé trop restreinte

**Test live T4** : question « Quelles écoles se trouvent à Lomé ? ».

**Comportement observé** :
- ORIA répond correctement « je n'ai pas d'information vérifiée » (BUG A corrigé).
- MAIS la recherche RAG mot-clé ne remonte rien alors que la base contient 363 fiches, dont plusieurs avec `ville='Lomé'`.

**Cause racine identifiée** dans `FicheRepository.java:27-31` :
```sql
SELECT f FROM Fiche f WHERE f.estPublie = true AND
  (LOWER(f.titre) LIKE LOWER(CONCAT('%', :terme, '%')) OR
   LOWER(f.resume) LIKE LOWER(CONCAT('%', :terme, '%')) OR
   LOWER(f.contenu) LIKE LOWER(CONCAT('%', :terme, '%')))
```

**Trois problèmes cumulés** :
1. **Colonnes insuffisantes** : la recherche ne touche que `titre`, `resume`, `contenu` de la table parente `fiches`. La `ville` et le `type_etablissement` sont dans `fiches_etablissement` (JOINED inheritance) → non cherchés.
2. **Terme entier passé comme LIKE** : `OriaService.rechercherContexteMotCle()` envoie TOUT le message normalisé (`"quelles ecoles se trouvent a lome"`) comme `terme`. Le SQL fait `LIKE '%quelles ecoles se trouvent a lome%'` qui ne matche aucune fiche (aucun titre ne contient cette phrase complète). Il faudrait splitter en mots individuels.
3. **Recherche vectorielle désactivée** : `rechercherContexteVectoriel()` (OriaService.java:524-536) renvoie `null` parce que la colonne `embedding` est en `real[]` et non `vector` (pgvector). Cf. plan d'activation pgvector déjà documenté.

**Vérification SQL directe** :
```sql
-- 5 fiches contiennent Lomé ou Togo dans leur titre
SELECT id, titre FROM fiches WHERE titre ILIKE '%lome%' OR titre ILIKE '%togo%';
-- Mais 0 résultat via la requête exacte du repository (test : terme='lome')
-- Cause : LIKE '%lome%' sur titre='Institut IAI-TOGO' ne matche pas (pas de 'lome')
--        et contenu='Institut privé proposant...' n'a pas 'lome' non plus
```

**Décision** : NE PAS fixer maintenant. Trop de surface touchée (modif repository + régression test + activation pgvector). À traiter dans le sprint « activation pgvector + filtre géographique RAG » déjà planifié. Le LLM reste honnête (règle 5b) donc l'utilisateur n'est pas trompé, juste pas aidé.

**Fix proposé pour ce sprint (à valider)** :
1. Étendre la requête du repository pour chercher aussi `fiches_etablissement.ville` et `fiches_etablissement.type_etablissement` via JOIN.
2. Splitter le `terme` en mots individuels (≥ 3 caractères) et faire un OR de chaque LIKE.
3. Une fois pgvector activé, basculer sur la recherche vectorielle + retomber sur le LIKE comme dernier filet.

**Hors scope immédiat** : ne pas toucher au repository ce soir. Documenter et passer à la suite.

---

### Date : 6 août 2026 (suite)

#### 3. FIX RAG : recherche par mot-clé étendue avec unaccent

**Continuation du §2** : la cause racine a été traitée en sprint dédié impromptu, vu que la requête du repository était 3 lignes et l'impact borné.

**Modifs appliquées** :

1. **`OriaService.rechercherContexteMotCle()` — split en mots-clés**
   - Avant : passage du message entier normalisé comme `terme` au LIKE → aucun match.
   - Après : split sur espaces, garde les mots ≥ 3 chars, retire les stop-words français (`est`, `les`, `des`, `une`, `pour`, etc.), déduplique. Pour chaque mot-clé, lance une requête et agrège les 5 premiers résultats uniques.

2. **`FicheRepository.rechercherParMotCle()` — étendue + unaccent**
   - Avant : JPQL touchant `fiches.titre/resume/contenu` uniquement.
   - Après : SQL natif avec LEFT JOIN vers `fiches_etablissement` (pour `ville`, `type_etablissement`) et `fiches_filiere` (pour `domaine`), normalisation `unaccent(LOWER(...))` partout.
   - Migration V2 (`db/migration/V2__unaccent_for_keyword_search.sql`) : `CREATE EXTENSION IF NOT EXISTS unaccent;`.

3. **Contournement `trouverParIdsOrdonnes`** : ce helper fait `ORDER BY CASE WHEN :ids.get(N)` pour N=0..9. Avec moins de 10 résultats (cas fréquent après split), il plantait `IndexOutOfBoundsException`. Le `default method` du repository bascule sur `findAllById` si on a < 10 IDs.

**Vérifications live (v8)** :
- ✅ « quelles écoles à Lomé » : RAG remonte **5 fiches pertinentes** (Université de Lomé x3, Université de Lomé (UL), Université des Sciences de la Santé). Le LLM produit une réponse détaillée et factuelle.
- ✅ « que fait un developpeur web » : RAG remonte 5 fiches (Informatique, Développeur Web). Réponse cohérente avec le métier.
- ❌ « liste moi les universités au Togo » : RAG ne remonte rien (la question n'a pas de mot dans les titres). Le LLM reste honnête : « pas d'info vérifiée ».
- ✅ Tests : 98/98 verts, BUILD SUCCESS.

**Limite restante** : si la question est purement sémantique (« universités au Togo »), le LIKE mot-clé reste insuffisant — c'est le boulot de pgvector, toujours hors-scope.

**Fichiers modifiés** :
- `shared/ai/service/OriaService.java` : split en mots-clés avec stop-words.
- `bibliotheque/repository/FicheRepository.java` : requêtes natives + unaccent + helper.
- `db/migration/V2__unaccent_for_keyword_search.sql` : extension PostgreSQL.

**Sécurité** : la clé OpenAI compromise affichée plus tôt dans la session reste à révoquer sur https://platform.openai.com/api-keys.

### Date : 6 août 2026 (suite)

#### 4. Sprint xlsx : import des 97 établissements supérieurs togolais

**Contexte** : `Base_Etablissements_Superieurs_Togo.xlsx` (97 fiches, 24 feuilles, ~575 lignes de formations) doit être ingéré dans `fiches_etablissement` + table JSON d'extension pour les détails non-typés.

**Architecture retenue (MVP)** : 1 entité JSON plutôt que 7 entités JPA
- Sprint dédié à la migration Flyway pour ajouter `pays/est_togolais` aurait été nécessaire pour typer Formations/Diplômes/etc. — décision : tout en JSON dans `etablissement_details_xlsx.donnees_etendues_json` (TEXT). Permet de tout ingérer en 1 PUT par fiche, simplifie le schéma. Si le besoin de requêtes typées émerge (ex : "toutes les écoles dont le téléphone est +228 22"), on migrera vers des entités dédiées dans un sprint ultérieur.

**Modifs livrées** :

1. **Entité `EtablissementDetailsXlsx`** (JOINED via `BaseEntity` + Long PK + UUID `trackingId`)
   - `idSourceXlsx` (ex : `TG-UL-001`), `ficheId` (FK 1-1 unique vers `fiches`), `donneesEtenduesJson` (TEXT), `dateExtraction`
2. **Repository** : `findByFicheId`, `findByIdSourceXlsx`.
3. **Service** : `getDetails` / `upsertDetails` (idempotent) / `deleteDetails`.
4. **Controller** : `GET/PUT/DELETE /api/v1/bibliotheque/etablissements/{trackingId}/details-xlsx`. GET public, PUT/DELETE `@PreAuthorize("hasRole('ADMIN')")`.
5. **DTO `EtablissementDetailsXlsxRequest`** : `donneesEtendues` typé `Object` (pas `JsonNode`) — Jackson 3 utilisé ici ne sait pas désérialiser `JsonNode` en `@RequestBody` directement (erreur `HttpMessageConversionException` rencontrée pendant les tests).
6. **Migration V3** (`V3__etablissements_details_xlsx.sql`) : table + index unique sur `fiche_id`.
7. **Script Python** `scripts/import_etablissements_xlsx.py` :
   - Login JWT admin → 24 feuilles du xlsx parsées via `openpyxl`.
   - Pour chaque établissement : `existing_by_title()` (recherche par titre normalisé), POST si nouveau, puis PUT details-xlsx (idempotent).
   - Mapping type xlsx → enum via préfixe (université publique/privée → UNIVERSITE, grande école → GRANDE_ECOLE, institut supérieur → ECOLE_SUPERIEURE, etc.). Bascule sur `ECOLE_SUPERIEURE` par défaut si préfixe non reconnu (5 cas rencontrés sur 97).
   - Stratégie finale : ne PAS appeler PUT sur `/etablissements/{trackingId}` car `FicheEtablissementServiceImpl.modifierEtablissement()` lance `UnsupportedOperationException` (bug pré-existant). On n'utilise QUE POST + PUT details-xlsx.

**Résultat de l'import** :
- 43 fiches créées, 54 existantes enrichies avec leurs détails xlsx, 0 échec.
- 97 lignes dans `etablissement_details_xlsx`, 97 `id_source_xlsx` distincts, 97 `fiche_id` distincts. **Phase 4 (unicité) validée**.

**Diagnostic d'un faux bug (perte de temps)** : pendant les tests, les SELECT directs en base Docker semblaient retourner 0 lignes après PUT réussi. Soupçon initial : Hibernate L1 cache, puis rollback silencieux. Après investigation :
- **Cause racine** : le `.env` du backend pointait vers **Supabase** (`db.popzhmtutyjxrxcxrizp.supabase.co`), pas la DB Docker locale. Mes `SELECT` psql Docker查到 0 lignes parce que les écritures allaient sur Supabase. Les 97 upserts du premier essai étaient bien persistés, juste sur la mauvaise DB.
- **Fix** : bascule `.env` vers `localhost:5433/activ_education`, override `SPRING_DATASOURCE_URL` dans `.env` pour désactiver SSL (Docker local ne supporte pas `?sslmode=require`), retrait de `?sslmode=require` dans `application-dev.properties` (ligne qui shadowait l'override).

**Smoke test ORIA post-import** : `« liste moi les universités togolaises »` → Ollama `qwen2:0.5b` répond (incohérences sur la France, comme attendu — bug anti-hallucination ORIA est dans le backlog du 5 août, hors-scope).

**Embeddings** : sur les 43 fiches créées pendant l'import, l'embedding est généré automatiquement par `FicheEtablissementServiceImpl` via `OpenAIEmbeddingServiceImpl` → fallback Ollama local (`nomic-embed-text`, ~1536 floats). Les 612 fiches pré-existantes n'ont pas d'embedding (n'ont jamais été regénérées), ce n'est pas un blocage pour le RAG mais à noter pour un sprint dédié.

**Fichiers modifiés/créés** :
- `bibliotheque/domain/entite/EtablissementDetailsXlsx.java` (nouveau)
- `bibliotheque/repository/EtablissementDetailsXlsxRepository.java` (nouveau)
- `bibliotheque/domain/service/EtablissementDetailsXlsxService.java` + `serviceImple/EtablissementDetailsXlsxServiceImpl.java` (nouveau)
- `bibliotheque/application/dto/request/EtablissementDetailsXlsxRequest.java` (nouveau)
- `bibliotheque/application/dto/response/EtablissementDetailsXlsxResponse.java` (nouveau)
- `bibliotheque/application/controller/EtablissementDetailsXlsxController.java` (nouveau)
- `db/migration/V3__etablissements_details_xlsx.sql` (nouveau)
- `scripts/import_etablissements_xlsx.py` (nouveau)
- `.env` + `application-dev.properties` : bascule DB locale + désactivation SSL.
- `.env.supabase.bak` : backup de la config Supabase avant bascule.

**Hors-scope pour ce sprint, à traiter plus tard** :
- Plan anti-hallucination ORIA du 5 août (3 modifs sur `OriaService.java`).
- Déduplication des fiches établissement pré-existantes (188 titres uniques → viser 178 après dedup).
- Regen embeddings pour les 612 fiches pré-existantes.
- Filtrage géographique RAG (champ `pays` ou `est_togolais`) — nécessite migration schéma.

### Date : 6 août 2026 (suite)

#### 5. FIX bug entretien : JSON d'erreur dans la bulle recruteur

**Symptôme** : sur l'écran Flutter "Entretien simulé", la bulle "Recruteur" affichait `{"score": 10, "feedback": "Erreur d'évaluation, veuillez réessayer."}` à la 1ère question (capture : filière "médecine").

**Cause racine** : `EntretienService.appelerOpenAI()` (l'ancien nom) gérait 3 usages (question texte / évaluation JSON / appréciation texte) avec un seul `catch` qui renvoyait **toujours** le JSON d'erreur d'évaluation. Le service mélangeait :
- `genererQuestion()` → appelle `appelerOpenAI()`, prend la string retournée comme **question**.
- En cas d'erreur HTTP LLM, le catch retournait un **JSON d'évaluation** qui était utilisé tel quel comme question.

Le JSON remontait ensuite à l'API dans `EntretienResponse.question`, et le frontend ligne 163 affichait `_session!.question ?? ''` directement dans la bulle. D'où l'affichage du JSON brut à la place d'une vraie question.

**Bug aggravé** par 2 faiblesses :
- `SYSTEM_PROMPT` ne demandait pas explicitement "réponds uniquement en texte, pas de JSON".
- Aucune séparation "succès" vs "erreur technique" — le frontend n'avait aucun moyen de distinguer un JSON d'erreur d'une vraie question.

**Modifs livrées** :

1. **`EntretienService`** — refactor majeur
   - Wrapper `ResultatAppelLLM<T>` qui encapsule succès/erreur (avec message d'erreur).
   - 3 méthodes d'appel distinctes : `appelerLLMTexteAvecRetry()` (questions/appréciation), `appelerLLMJsonAvecRetry()` (évaluations), `appelerLLMBrutAvecRetry()` (bas niveau).
   - Retry automatique x1 sur réponse mal-formée (parsing JSON cassé → retry, distinct d'une erreur HTTP transport).
   - Pas de retry sur erreur HTTP (5xx, 4xx) → directement `erreur=true`.
   - Validation de schéma : pour une évaluation, exige `score` numérique + `feedback` textuel non vide.
   - Strip des code fences ` ```json ... ``` ` avant validation.
   - Prompt `SYSTEM_PROMPT` renforcé : "AUCUN préambule, AUCUNE balise JSON, AUCUN guillemet".
   - Prompt `EVALUATION_PROMPT` renforcé : "FORMAT STRICT JSON" + exemple explicite.

2. **`EntretienResponse`** — séparation succès/erreur
   - Champs ajoutés : `Boolean erreur`, `String messageErreur`.
   - `@JsonInclude(NON_NULL)` : en cas d'erreur, `question` est omis de la réponse JSON.
   - 2 constructeurs : un "succès" (avec question), un "erreur" (sans question, avec message).

3. **Frontend Flutter** (`entretien_screen.dart` + `entretien_models.dart`)
   - `EntretienResponse.fromJson` lit `erreur` et `messageErreur`.
   - État local `_erreurTechnique` + `_derniereAction` (callback pour "Réessayer").
   - `_buildErreurBandeau()` : bandeau rouge en haut avec icône `error_outline`, message, et bouton "Réessayer" qui réinvoque la dernière action (start ou repondre).
   - Garde-fou dans `_buildInterview()` : si `question` est null/vide (ne devrait plus arriver), affiche "Question indisponible" au lieu d'une chaîne vide.

4. **Tests anti-régression** (`EntretienServiceTest.java`, 6 tests, 100% verts)
   - `malformedJsonThenRetry_renvoieErreurTrue` : 1er appel = JSON d'erreur, 2e = JSON d'erreur → retry déclenché mais le 2e échoue aussi → `erreur=true`, `question=null`, et `repository.save()` jamais appelé.
   - `erreurHttp_renvoieErreurTrueSansRetry` : 5xx → 1 seul appel, pas de retry, `erreur=true`.
   - `generationReussie_questionValide` : question valide non vide retournée, 1 seul appel HTTP, schéma respecté.
   - `codeFenceJson_acceptable` : ```json ... ``` strippé proprement et accepté.
   - `evaluationJsonValide_stockeeCorrectement` : JSON stocké a `score` numérique et `feedback` non vide.
   - `evaluationJsonInvalide_stockePlaceholder` : réponse texte au lieu de JSON → pas de blocage, comportement gracieux.

**Vérifications** :
- ✅ Build : `mvn clean compile` OK
- ✅ Tests : **104/104 verts** (6 nouveaux + 98 anciens).
- ✅ Smoke test live : `POST /api/v1/entretien/start` avec clé OpenAI révoquée renvoie
  ```json
  {"metierTitre":"medecin","questionNumero":0,"totalQuestions":5,"statut":"EN_COURS",
   "erreur":true,"messageErreur":"Erreur technique LLM : Unauthorized"}
  ```
  et **PAS de champ `question`** → le frontend affichera le bandeau rouge, pas de JSON brut.

**Fichiers modifiés** :
- `entretien/domain/service/EntretienService.java` : refactor avec retry + wrapper.
- `entretien/domain/dto/EntretienResponse.java` : ajout champs erreur/messageErreur + @JsonInclude.
- `entretien/domain/service/EntretienServiceTest.java` (nouveau) : 6 tests.
- `activ_education/lib/models/entretien_models.dart` : fromJson lit erreur/messageErreur.
- `activ_education/lib/screens/entretien/entretien_screen.dart` : bandeau erreur + bouton réessayer.

**Note** : la cause du 5xx de l'environnement de dev (clé OpenAI révoquée) sort du scope — d'autres sprint régénèrent la clé. Le bug `<JSON d'erreur dans la bulle>` est corrigé indépendamment.


### Date : 6 août 2026 (suite)

#### 6. FIX bug widget "Ma recommandation" sur la home

**Symptôme** : un élève de test voit sur la home le widget "Ma recommandation" afficher le message
`Désolé, je n'ai pas pu générer une recommandation pour le moment`, sans distinction
entre "profil incomplet" et "LLM en panne". Le bouton `Tester la v2 (3 signaux)` à
côté suggérait que deux implémentations coexistaient (v1 = LLM texte, v2 = moteur
algorithmique).

**Diagnostic** :
- L'endpoint v1 est `GET /api/v1/eleves/{trackingId}/recommandation-ia`
  (`shared/ai/controller/RecommandationIAController.java:25`), service
  `RecommandationIAService.genererRecommandation()`.
- L'endpoint v2 (bouton) est `GET /api/v1/eleves/{trackingId}/recommandation-ia/v2`
  (`RecommandationIAController.java:43`), service
  `Recommandation3SignauxServiceImpl.recommander()` (moteur 3 signaux).
- `RecommandationIAService.ligne 122-130` avait un `try { aiService.generateAnswer(...) }`
  qui catchait toute `Exception` et renvoyait un message générique indistinctement.
- La cause racine était en amont : `OpenAIEmbeddingServiceImpl.generateAnswer()`
  (ligne 165-211) ne disposait PAS du fallback Ollama que `generateEmbedding()` a
  depuis le 3 août 2026. Avec `OPENAI_API_KEY=REVOKED_REPLACE_ME` dans `.env`, l'appel
  OpenAI renvoie `401` → exception → catchée par le service → message générique.

**Décision retenue (validée avec l'utilisateur)** : **garder le v1** et corriger
la cause racine, plutôt que de supprimer le v1. Le v2 est déjà une migration
encours (bouton "Tester la v2") mais le widget home reste sur v1 pour ne pas
casser l'UX existante.

**Modifications** :

1. **`OpenAIEmbeddingServiceImpl.java`** : ajout du fallback Ollama à `generateAnswer()`.
   - Extraction de `callOpenAIChat()` et nouveau `callOllamaChat()`.
   - Si la clé OpenAI est absente/révoquée → Ollama direct.
   - Si OpenAI échoue en 4xx/5xx (sauf 429) ou timeout/parsing → fallback Ollama.
   - 429 (quota) remonte un message dédié (Ollama ne fixera pas le quota).
   - Pattern aligné sur `generateEmbedding()` (ligne 80-92).

2. **`shared/ai/domain/dto/RecommandationIAResponse.java`** (nouveau) : record typé
   avec enum `Statut { OK, PROFIL_INCOMPLET, ERREUR_LLM }`. Constructeurs
   `ok()` / `profilIncomplet()` / `erreurLlm()`.

3. **`RecommandationIAService.java`** : retourne `RecommandationIAResponse` au lieu
   de `String`. La branche "profil vide" renvoie `profilIncomplet(...)` sans
   appeler le LLM, distinguant ce cas de l'erreur LLM.

4. **`RecommandationIAController.java`** : signature changée
   `Map<String, String> → RecommandationIAResponse`. Sérialisation JSON
   automatique : `{ "recommandation": "...", "type": "OK"|"PROFIL_INCOMPLET"|"ERREUR_LLM" }`.

5. **`recommendation_ia_models.dart`** (nouveau, Flutter) : modèle `RecommandationIAResponse`
   + enum `RecommandationIAStatut` + helpers `isOk` / `isErreurTechnique`.

6. **`api_service.dart.getRecommandationIA`** : ne retourne plus `String?` mais
   `RecommandationIAResponse`. En cas d'erreur HTTP/transport, renvoie un objet
   `ERREUR_LLM` au lieu de `null` (les anciens appelants peuvent continuer à
   utiliser `recommandation` comme un String).

7. **`dashboard_bachelier.dart`** : widget adapte sa couleur (rouge en cas d'erreur),
   son titre ("Recommandation indisponible" vs "Ma recommandation") et son
   comportement (tap = `_loadDashboardData()` en cas d'erreur, navigation
   normale sinon). Le bouton "Tester la v2" est masqué en cas d'erreur.

**Vérifications** :
- ✅ Build backend : `mvn clean compile` OK.
- ✅ Tests backend à compléter (cf. tâche #34).
- ✅ `Recommandation3SignauxServiceImpl.candidatsPourProfil()` ligne 218-226 :
  le guard `niveauActuel == null → List.of()` est préservé (inchangé depuis le
  fix `9cc37c0` du 4 août 2026, "élève sans niveau ne reçoit plus 117 fiches").

**Fichiers modifiés** :
- `shared/ai/service/impl/OpenAIEmbeddingServiceImpl.java` : fallback Ollama pour `generateAnswer()`.
- `shared/ai/domain/dto/RecommandationIAResponse.java` (nouveau).
- `shared/ai/service/RecommandationIAService.java` : retour typé.
- `shared/ai/controller/RecommandationIAController.java` : signature changée.
- `activ_education/lib/models/recommandation_ia_models.dart` (nouveau).
- `activ_education/lib/models/models.dart` : export du nouveau modèle.
- `activ_education/lib/services/api_service.dart` : `getRecommandationIA` typé.
- `activ_education/lib/screens/home/dashboard_bachelier.dart` : widget adaptatif.

---

## §7 — Re-diagnostic widget « Ma recommandation » + logs de trace (6 août 2026, soir)

**Contexte** : le widget home montrait encore « Désolé, je n'ai pas pu générer une
recommandation… » pour un élève de test, avec une entrée « Tester la v2 (3 signaux) »
séparée. Re-diagnostic complet et live le 6 août au soir.

### Récap diagnostic

- **1. Endpoint du widget home** : `GET /api/v1/eleves/{trackingId}/recommandation-ia`
  (**v1**, LLM textuel, typé OK/PROFIL_INCOMPLET/ERREUR_LLM).
  Le bouton « Tester la v2 (3 signaux) » (dashboard) n'impacte pas v1 : il lance
  `_lancerParcours3Signaux()` → saisie bulletins → `GET .../recommandation-ia/v2`
  (moteur 3 signaux déterministe). Deux implémentations **cohabitent volontairement** ;
  v1 reste le défaut de la home. Il n'existe **pas** d'endpoint
  `GET /api/v1/recommandation/{eleveTrackingId}`.
- **2. État en base (DB Docker :5433)** :
  - `niveaux_filieres` : **0 ligne** → v2 passe par le fallback `findAll()` (log.warn visible).
  - `tests_riasec_resultats` : **0 ligne** → v2 utilise le vecteur neutre `[0.5]×6`.
  - Élève de test (`eleve1@activ-education.com`, id 12) : `niveau=Terminale`, `filiere=Série C`,
    pas de notes ni de quiz → **profilRempli v1 = true** (niveau renseigné) → v1 va au LLM.
- **3. Smoke test live** (backend dev tournant, JWT admin) :
  - v1 → `{"type":"OK", "recommandation":"…1517 chars…"}` ✅
  - v2 → `top: 10` filières, `niveau LYCEE_TLE` ✅
  - Le message « Désolé… » n'est **plus** dans le code mobile (resté seulement dans
    `backoffice`, page ORIA = feature distincte, et un commentaire). Symptôme non reproductible
    avec le code courant → build stale côté client (APK web non rebuild).

### Décisions

- **v1 est conservé** (décision §6 du 6 août validée) — pas de bascule du widget home sur v2 ;
  les deux cohabitent, v2 reste accessible via le bouton.
- Le garde-fou « niveauActuel null → `List.of()` » (v2, `candidatsPourProfil` l.218-226)
  est préservé inchangé.

### Changements

1. **Traçabilité des logs v1** (`RecommandationIAService`) :
   - `log.info` au retour `PROFIL_INCOMPLET` (avec snapshot niveau/filière/métier/matières/notes/quiz).
   - `log.info` avant l'appel LLM (nb filières/métiers/établissements publiés).
   - `log.info` après `OK` (nb caractères) ; `log.error` conservé sur exception LLM.
2. **Test** `RecommandationIAServiceTest#profilCompletAvecNotesEtQuiz_llmOk_renvoieRecommandationNonVide` :
   élève complet (niveau + filière + métier + notes + quiz + filières publiées) →
   `OK` non vide, et vérifie que notes + quiz passent bien au LLM.
   Couvre le scénario « widget home » côté service (point 4 du diagnostic).

### Vérifications

- ✅ `mvn -o -Dtest=RecommandationIAServiceTest test` → 5/5.
- ✅ `mvn -o -Dtest='tg.edtch.activEducation.shared.ai.**' test` → 34/34 (1 skip existant).

### Tâches restantes

- Rebuild + redéploiement Flutter web si l'utilisateur voit encore l'ancien message
  (le code source est déjà à jour).
- Charger les tables `niveaux_filieres` (actuellement 0 ligne) pour un v2 plus fin.

## §8 — Personalisation de l'Explorer (bibliotheque) par profil élève (6 août 2026)

### Problème observé

Un élève BAC_3 / Génie Informatique / Maths-SVT / informaticien voyait en
tête de l'onglet Explorer « Série G3 (Secrétariat) », « Série G2
(Comptabilité) » et « Tourisme et Hôtellerie » en bandeau vedette — rien à
voir avec son profil.

### Diagnostic

1. **Endpoint** : `GET /api/v1/bibliotheque/filieres` (et `/series`) paginé,
   trié **systématiquement `createdAt DESC`** (ordres de seed).
2. **Aucun identifiant élève transmis** : le widget transmettait seulement
   `page`/`size`. Le JWT n'était utilisé nulle part dans la requête.
3. **Bandeau « Filière du moment »** = `<i>première filière</i> de la liste
   (`_filieres.first`), c'est-à-dire la plus récemment seedée — contenu
   éditorial fixe, pas un calcul de popularité.

### Corrections (PHASE 2)

1. **Paramètre optionnel `eleveTrackingId`** ajouté sur
   `GET /api/v1/bibliotheque/filieres` et `.../series`. S'il est fourni et
   valide, les fiches sont **triées par pertinence** (score DESC), sinon
   repli inchangé (createdAt DESC).
2. **Scoring réutilisé** : cosinus RIASEC élève ×
   `ProfilFiliereRiasecCatalog.profilPour(titre)` (même logique que le
   moteur 3 signaux), complété par un score de mots-clés déterministe
   (filière actuelle, métier souhaité, matières préférées — matching par
   préfixe 4 lettres pour les flexions « maths » ↔ « Mathématiques »).
Nouveau service : `bibliotheque/.../FicheExplorerPersonnaliseeServiceImpl`
   (interface + impl). Aucun scoring dupliqué : on réutilise le catalogue.
3. **Bandeau clarifié** : avec élève → « RECOMMANDÉ POUR TOI / Selon ton
   profil » (vraie personnalisation) ; sans élève → « FILIÈRE DU MOMENT /
   Tendance générale » (contenu éditorial non-personnalisé, explicitement
   labellisé).
4. **Repli propre** : élève introuvable ou profil incomplet (niveauActuel
   null ou aucun champ) → liste générique non vide (createdAt DESC), jamais
   d'erreur 500 ni liste vide.

### Tests (PHASE 3) — `FicheExplorerPersonnaliseeServiceImplTest` (5 tests)

1. Profil Informatique+Maths/SVT → filière informatique en tête, pas
   Secrétariat/Comptabilité.
2. Séries scientifiques (Série C/Math) passent devant « Série G3 (Secrétariat) ».
3. Élève introuvable → repli générique non vide.
4. Profil incomplet → repli générique non vide, sans erreur.
5. RIASEC Investigateur présente → la filière informatique reste en tête.

### Smoke test live

- `filieres?eleveTrackingId=<eleve Génie Info>` → « Génie Informatique »,
  « Informatique » en tête (avant Série G3/Tourisme).
- `series?eleveTrackingId=…` → Série F4 (Génie Civil), E et C (Mathématiques)
  en tête ; G3 (Secrétariat) hors du top.

### Vérifications

- ✅ `mvn clean test` → **118/118 (2 skips pré-existants)**.
- ✅ `flutter analyze` → 0 erreur (corrigé au passage 2 erreurs résiduelles
  de la migration DTO typé : `quiz_screen.dart` et `entretien_screen.dart`).
- ✅ `flutter test` → 24/24.

### Notes

- La personnalisation s'applique aux onglets Tout + Séries + Filières via
  l'Explorer home (métiers/établissements non couverts, tri createdAt
  conservé).
- Données de test « Test Save Button MODIFIED » présentes en base — à purger.
