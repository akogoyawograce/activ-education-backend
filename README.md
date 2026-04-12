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
- Fiches de métiers, filières, séries et établissements spécifiques au Togo (stratégie d'héritage d'entités avec `JOINED`).
- **FAQ Intelligente :** Moteur de recherche assisté par IA grâce aux embeddings et à la recherche par similarité cosinus.

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
