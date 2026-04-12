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
