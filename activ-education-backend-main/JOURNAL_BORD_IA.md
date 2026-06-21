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

