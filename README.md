# Activ EDUCATION 🎓🚀

![Plateforme v2.0](https://img.shields.io/badge/Version-v2.0-blue?style=for-the-badge&logo=appveyor)
![Java 21](https://img.shields.io/badge/Java-21-E34F26?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2+-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-pgvector-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)

<div align="center">
  <img src="https://via.placeholder.com/150" alt="Logo Activ EDUCATION" />
</div>

---

## 🎯 À propos du projet

**Activ EDUCATION** est une plateforme innovante dédiée à l'orientation scolaire et professionnelle au Togo. Notre mission est d'accompagner les élèves et étudiants dans leurs choix d'avenir grâce à un accompagnement personnalisé et des outils technologiques de pointe.

La plateforme se distingue par :
- Un **algorithme de recommandation intelligent (60/40)** qui pondère les aspirations personnelles (goûts) et la réalité académique.
- L'intégration d'**Intelligences Artificielles** pour une FAQ sémantique réactive.
- Une **bibliothèque d'exploration riche** et contextualisée au marché togolais.
- Un pont direct vers une **consultation avec des conseillers spécialisés**.

---

## 🏗️ Architecture et Modules

Le projet est conçu selon une architecture **Monolithe Modulaire ("Package by Feature")**, facilitant la maintenance, la testabilité et une potentielle future migration vers des microservices.

### 1. 🔐 Gestion des Profils & Sécurité (`profil`)
- **Acteurs :** Élèves, Parents, Conseillers, Administrateurs.
- **Fonctionnalités :** Authentification (2FA), gestion des rôles, documents scolaires et historique d'activités.

### 2. 📚 Bibliothèque d'exploration (`bibliotheque`)

**A. Rôle du Module**
Ce module est le cœur informationnel de la plateforme. Il centralise, structure et expose aux élèves toutes les informations sur l'orientation au Togo : quelles sont les **Séries** existantes, à quelles **Filières** elles mènent, quels sont les **Métiers** possibles et dans quels **Établissements** se former.

**B. Fonctionnalités Clés**
- **CRUD Avancé** : Gestion de contenu asynchrone avec attachements enrichis (Images, Vidéos, Documents hébergés sur `MinIO`).
- **Recherche Multidimensionnelle** : Filtres classiques par secteurs, profils recherchés.
- **Système de Favoris** : Sauvegarde de fiches par l'utilisateur.
- **Analytics & Tendances** : Suivi des consultations pour mettre en avant les fiches "Tendances" (sur les 7 derniers jours) et générer l'historique de contenu "Récemment consulté" par l'élève.
- **Moteur IA Sémantique ("RAG")** : Permet à l'utilisateur de chercher "Je veux travailler dans la nature" et d'obtenir des Filières ou Métiers grâce à une recherche vectorielle profonde.

**C. Architecture Technique & Choix de Conception**
L'implémentation de ce module regorge de patterns de conception importants pour conjuguer flexibilité et performance :

1. **Polymorphisme et Héritage JPA (`InheritanceType.JOINED`)**
   Toutes nos fiches héritent d'une entité mère abstraite `Fiche.java`. Chaque sous-type (`FicheMetier`, `FicheFiliere`, etc.) possède sa propre table liées par clé étrangère. Cela garantit l'intégrité des données tout en nous permettant d'effectuer des recherches génériques.

2. **Génération d'Embeddings (Pipeline RAG avec Google Gemini)**
   - Dès qu'une `Fiche`  ou une `EntreeFAQ` est créée ou modifiée, le `GeminiEmbeddingService` interroge secrètement l'API Gemini (`gemini-embedding-2`) pour transformer son contenu en un vecteur de *768 dimensions*.
   - Ce vecteur est sauvegardé dans l'attribut `float[] embedding` natif à PostgreSQL.

3. **Recherche Vectorielle Natif (PostgreSQL `pgvector`)**
   - Au lieu d'une simple recherche LIKE, la recherche IA calcule la "similarité Cosinus" (`<=>`) entre la phrase de l'utilisateur (vectorisée) et toutes les embeddings de la base de données.

4. **Contournement des limites Hibernate (Stratégie en 2 Étapes)**
   - L'héritage *JOINED* couplé aux requêtes SQL Natives provoque une perte du discriminateur chez Hibernate (`clazz_ not found`).
   - Pour l'IA et l'Analytics, nous utilisons un **Pipeline à 2 étapes** : 
     1) Requête Native SQL pure pour extraire uniquement les identifiants `List<Long> id` (avec pagination/limites).
     2) Requête JPQL classique pour instancier les bonnes sous-classes (`SELECT f FROM Fiche f WHERE f.id IN ... ORDER BY CASE`) en préservant l'ordre mathématique ou chronologique.

5. **Interopérabilité Trans-Modules**
   - Le système d'Analytics enregistre les vues en s'appuyant sur l'entité globale `Historique` du module *Profil*. Les requêtes SQL de tendance effectuent un pont asynchrone (`CAST(f.tracking_id AS text) = h.details`) sans générer de couplage fort en base de données.

### 3. 🧭 Diagnostic d'orientation (`diagnostic`)
- Évaluation à travers des quiz thématiques.
- Calcul de recommandation via des matrices de score et confrontation avec les seuils d'admission des différentes filières.

### 4. 🤝 Accompagnement (`accompagnement`)
- Mise en relation entre les élèves et les professionnels.
- Prise de rendez-vous, suivi via messagerie interne et gestion fine des disponibilités des conseillers.

### 5. ⚙️ Administration (`administration`)
- Back-office complet pour la gestion de la plateforme.
- Modération des inscriptions, analyse des KPIs et gestion globale des ressources pédagogiques.

---

## 🛠️ Stack Technique

- **Langage principal :** Java 21
- **Framework Core :** Spring Boot
- **Accès aux données :** Spring Data JPA / Hibernate
- **Productivité :** Lombok (Entités avec `@SuperBuilder`)
- **Base de données principale :** PostgreSQL
- **Module IA / Vectorisation :** Extension `pgvector` (Recherche sémantique)

---

## 📁 Structure du Code

Nous avons adopté des conventions architecturales strictes pour préserver la propreté du code :

- **Package by Feature :** Chaque module encapsule son propre domaine métier. Par exemple : `tg.edtch.activeducation.[module]`.
- **Classe `BaseEntity`:** Une classe mère globale qui gère le **JPA Auditing** de manière uniforme (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`). Toutes nos entités intègrent la traçabilité sans redondance.
- **Isolations des Enums :** Les énumérations sont centralisées dans des sous-packages stricts : `domain.enums` au sein de chaque module.
- **Clés Primaires Sécurisées :** Utilisation standardisée de l'ID système en `Long` couplée un `trackingId` (`UUID`) public pour sécuriser nos requêtes et nos API.

---

## 🚀 Prérequis & Installation

### 1. Outils nécessaires
- [Java 21+](https://jdk.java.net/21/)
- [PostgreSQL 15+](https://www.postgresql.org/)
- [Docker](https://www.docker.com/) (Optionnel mais recommandé pour la DB)
- Extension `pgvector`

### 2. Configuration PostgreSQL et pgvector
Lance PostgreSQL avec l'extension activée (via Docker) :
```bash
docker run --name activeducation-db \
  -e POSTGRES_PASSWORD=secret \
  -e POSTGRES_DB=activeducation \
  -p 5432:5432 \
  -d pgvector/pgvector:pg16
```

Puis dans psql ou pgAdmin :
```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

### 3. Cloner et Lancer
```bash
# Cloner le dépôt
git clone https://github.com/votre-organisation/activ-education.git
cd activ-education

# Configurer les identifiants DB dans src/main/resources/application.yml

# Compiler les sources (sans lancer les tests, optionnel)
./mvnw clean compile

# Lancer l'application
./mvnw spring-boot:run
```

---

## 🗺️ État du projet / Roadmap

- [x] **Phase 1 : Data Definition Laye**r — Architecture mise en place, implémentation des entités JPA et configuration des Repositories terminée (Module 1 à 4).
- [ ] **Phase 2 : Business Logic (En cours)** — Implémentation de la couche Service (Algorithmes de recommandation, workflows métiers).
- [ ] **Phase 3 : Interface API & Sécurité** — Développement des REST Controllers, interfaçage avec Spring Security et intégration du JWT / 2FA.
- [ ] **Phase 4 : Front-end & Tests Globaux** — Déploiement Cloud et QA.

---

> _"Réussir votre orientation commence par le bon diagnostic."_ — L'équipe Tech Activ EDUCATION
