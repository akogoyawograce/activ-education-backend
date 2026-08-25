# 📋 Document de brief — Pour générer le thème du mémoire

> **But** : Ce document est conçu pour être **copié-collé intégralement** dans une conversation Claude (ou tout autre LLM) afin d'obtenir une proposition de thème de mémoire académique pertinente, fidèle au projet réalisé.

---

## 🎓 Section 1 — Contexte académique

- **Type de diplôme** : Licence Professionnelle en **Génie Logiciel**
- **Domaine** : Sciences et Technologies
- **Mention** : Informatique
- **Spécialité** : Génie Logiciel
- **Établissement** : Institut Polytechnique **DEFITECH** (Togo)
- **Année académique** : 2025-2026
- **Structure d'accueil du stage** : **HubCity / Woélab** (centre d'innovation à Lomé, Togo)
- **Contrainte du canevas** : Le thème doit tenir en **2 lignes maximum** (max 200 caractères)
- **Volume du mémoire** : 40-50 pages, police Times New Roman 14, interligne 1.5
- **Langue** : français (sauf abstract en anglais)

---

## 🌍 Section 2 — Problématique métier (contexte togolais)

Au Togo, l'orientation scolaire et professionnelle des élèves souffre de :

1. **Pénurie de conseillers d'orientation** : 1 conseiller pour ~5 000 élèves dans le public, vs un ratio recommandé de 1 pour 300
2. **Information dispersée** sur les **117 établissements supérieurs togolais** (Universités de Lomé, Kara, UCAO, ESA, ESTBA, INJS, IUT-G, etc.)
3. **Choix inadaptés** : orientations décidées sur la seule base de la série scolaire, sans tenir compte du profil psychologique (RIASEC) ni des notes réelles
4. **Fracture numérique** : 80% des jeunes ont un smartphone, mais les interfaces existantes sont peu adaptées au contexte local
5. **Concentration géographique** : services d'orientation uniquement à Lomé et Kara, **zones rurales délaissées**

**Question de recherche centrale** :
> *Comment offrir un accompagnement à l'orientation scolaire personnalisé, accessible gratuitement et pertinent culturellement à chaque jeune Togolais, indépendamment de son lieu de résidence, de son niveau d'études ou de sa situation socio-économique ?*

---

## 💻 Section 3 — Périmètre technique réalisé (basé sur le code)

### Architecture globale

Plateforme **full-stack** composée de **3 applications** coordonnées :

| Application | Stack | Localisation |
|-------------|-------|--------------|
| **Backend API** | Spring Boot 4.0.5 + Java 21 + Maven | `activ-education-backend-main/` |
| **Mobile élèves** | Flutter / Dart (Material 3, setState) | `activ-education-fronted-main/activ_education/` |
| **Backoffice admin** | React 19.2 + TypeScript 6 (strict, `erasableSyntaxOnly`) + Vite + Tailwind v4 | `activ-education-fronted-main/backoffice/` |

### Infrastructure

| Service | Technologie | Rôle |
|---------|-------------|------|
| **Base de données** | PostgreSQL 16 + extension **pgvector** (RAG) | Données + recherche vectorielle |
| **Cache / rate-limit** | Redis | Sessions + throttling API |
| **Stockage objet** | MinIO (S3-compatible) | Images, vidéos, documents |
| **IA principale** | OpenAI (embeddings + génération) | Moteur de recommandation + RAG |
| **IA fallback** | Groq + Ollama local (qwen2:0.5b) | Résilience |
| **Déploiement** | Docker Compose + Nginx + GitHub Actions | CI/CD 3 workflows |

### Architecture backend (Package by Feature)

Le backend contient **29+ packages métier** suivant le pattern **Package by Feature**, avec **23+ entités JPA** utilisant l'héritage **JOINED** (Fiche abstraite → FicheMetier, FicheSerie, FicheFiliere, FicheEtablissement) et **133+ endpoints REST** documentés via Swagger.

**Liste des 29 packages backend** (vue réelle du code) :
- **Cœur** : `profil`, `bibliotheque`, `diagnostic`, `accompagnement`, `shared`
- **Fonctionnels** : `alumni`, `alums`, `attestations`, `badge`, `cahierdebord`, `calendrier`, `cartemetiers`, `cvgenerateur`, `datahub`, `defis`, `emploi`, `entretien`, `horsligne`, `mentorat`, `parrainage`, `portfolio`, `prediction`, `recommandation`, `reorientation`, `reseau`, `riasec`, `sallevirtuelle`, `simulateur`, `temoignage`, `vae`

### Sécurité (3 couches)

1. **JWT stateless** + authentification **2FA (TOTP)** généralisée
2. **`@PreAuthorize` par méthode** avec SPEL custom beans (`isOwner(#trackingId)`, `isOwnChild(...)`, etc.)
3. **Rate-limiting Redis** : login 20/15min, refresh 20/5min, API 200/1min
4. **Validation DTOs** via `@Valid` + `GlobalExceptionHandler`

---

## 📘 Section 4 — Les 5 modules contractualisés (cahier des charges)

Le stage a démarré sur la base d'un **cahier des charges fonctionnel** définissant 5 modules :

### Module 1 — Gestion des Profils Utilisateurs
- 9 rôles : Collégien, Lycéen, Étudiant, Parent, Apprenti, Déscolarisé, Personne en situation de handicap, Adulte en reconversion, Conseiller
- Inscription email/téléphone/réseaux sociaux
- Profil progressif, documents personnels (bulletins, CV), favoris
- Comptes famille (parent ↔ enfants), consentement parental, RGPD

### Module 2 — Bibliothèque Centrale (4 types de fiches)
- **Séries** (A, C, D, E, etc.) — secondaire togolais
- **Filières** (BTS, DUT, Licences, Masters) — supérieur
- **Métiers** — réalité du marché de l'emploi au Togo
- **Établissements** — 117 établissements supérieurs documentés

Chaque fiche a une structure à **3 niveaux** : résumé court / vidéo explicative / paragraphes détaillés.

### Module 3 — Diagnostic d'Orientation
- **Quiz psychométrique** avec 3 parcours adaptatifs (Nouveau Bachelier, Réorientation, Nouveau Bepcien)
- **Analyse académique** : saisie manuelle + OCR des bulletins (OpenAI Vision)
- **Algorithme de recommandations croisées** combinant aspirations (Quiz) et réalité (Notes)
- Visualisation radar des points forts/faibles

### Module 4 — Accompagnement Humain
- **FAQ dynamique** avec recherche sémantique
- **Messagerie asynchrone** (système de tickets)
- **Rendez-vous synchrones** : téléphone, **visioconférence Jitsi**, physique
- **Espaces spécifiques** : handicap, décrochage, reconversion professionnelle

### Module 5 — Back-office d'Administration
- 5 rôles : Super Admin, Gestionnaire contenu, Responsable conseillers, Conseiller, Modérateur
- Gestion des fiches, quiz, utilisateurs
- Modération, statistiques d'audience
- **2FA obligatoire**, mode maintenance, logs d'audit

---

## 🚀 Section 5 — Les 23 modules complémentaires (ajoutés pendant le stage)

Au-delà des 5 modules contractualisés, **23 modules additionnels** ont été conçus et implémentés. C'est une **contribution personnelle** au projet, qui démontre la capacité d'extension de l'architecture.

| Catégorie | Modules | Innovation |
|-----------|---------|------------|
| **Engagement** (5) | Badges, Défis, Réseau social interne, Parrainage, Témoignages | Gamification + viralité |
| **Orientation étendue** (4) | RIASEC, Prédiction ML, Simulateur, Réorientation | Algorithmes ML + visualisation |
| **Insertion pro** (3) | Emploi, CV Generator, Portfolio | Pont orientation → emploi |
| **Accompagnement humain** (4) | Mentorat, Alumni, VAE, Salle virtuelle | Réseau + visio Jitsi |
| **Productivité** (4) | Calendrier/Événements, Cahier de bord, Carte métiers, Entretien (simulation) | Organisation + simulation |
| **Transverses** (3) | Attestations, Data Hub, Hors-ligne | PDF + stats + accessibilité |

---

## 🧠 Section 6 — Innovations techniques principales

### Innovation 1 — Moteur de recommandation multi-signaux

L'innovation algorithmique centrale combine **3 signaux pondérés** :

```
score_final = 0,50 × score_réalité + 0,35 × score_aspiration + 0,15 × score_engagement
```

- **Score réalité (50%)** : notes de l'élève / seuils d'admission des filières + bonus de tendance (progression sur 3 ans)
- **Score aspiration (35%)** : similarité cosinus entre profil RIASEC élève et embedding de la filière (768 dimensions, stockés dans **pgvector**)
- **Score engagement (15%)** : signaux comportementaux (consultations, favoris, recherches RAG)

**Innovation complémentaire** : mécanisme **anti-bulle de filtre** garantissant N découvertes dans le top 10 (filières à fort score mais faiblement consultées).

### Innovation 2 — RAG vectoriel en 2 phases

Architecture technique avancée :
1. **Phase 1** : SQL natif pour la similarité cosinus sur `pgvector`
2. **Phase 2** : JPQL pour réhydrater les bonnes sous-classes JOINED (le polymorphisme JPA est un défi technique important car `JOINED` perd le discriminateur en SQL natif)

### Innovation 3 — Prédiction ML supervisée (Phase 5)

- **Modèle** : Gradient Boosting (joblib) + export ONNX
- **Dataset** : 1200+ lignes synthétiques + données réelles en cours de collecte
- **Features** : notes par matière, score RIASEC, série, historique d'engagement
- **Endpoint** : `/api/v1/eleves/{id}/orientation-outcome`

### Innovation 4 — OCR des bulletins scolaires

- Extraction automatique des notes à partir de photos/scans de bulletins
- Utilise **OpenAI Vision** pour les images, **PDFBox** pour les PDF
- Évite la saisie manuelle fastidieuse

---

## 📊 Section 7 — Données et impact

- **117 établissements supérieurs togolais** documentés dans la base de connaissances
- **13 séries** du secondaire, **32 filières** du supérieur, **42 métiers** référencés
- **12 entrées FAQ** + base RAG
- **7 utilisateurs de seed** (superadmin, modérateur, gestionnaire, conseiller, élève, parent)
- **9 fichiers de tests** (92/92 tests passants)

---

## 🎯 Section 8 — Compétences Génie Logiciel mobilisées

- **Architecture logicielle** : Package by Feature, héritage JPA, polymorphisme
- **Conception UML** : diagrammes de classes, séquence, cas d'utilisation
- **Méthodologie** : cycle en V (cahier des charges → conception → implémentation → tests → déploiement)
- **Sécurité applicative** : JWT, 2FA, OWASP, RBAC, rate-limiting
- **Qualité logicielle** : 92/92 tests, validation DTOs, gestion d'erreurs centralisée
- **DevOps** : Docker, CI/CD GitHub Actions
- **IA appliquée** : embeddings, RAG, similarité cosinus, OCR, ML supervisé
- **Full-stack** : 3 technologies coordonnées, 1 base de code cohérente

---

## 📝 Section 9 — Contraintes pour le thème

Le thème du mémoire doit :

1. ✅ Tenir en **2 lignes maximum** (max ~200 caractères)
2. ✅ Être **académique et professionnel** (niveau Licence Pro)
3. ✅ **Valoriser les aspects Génie Logiciel** (architecture, sécurité, algorithme, ML)
4. ✅ **Ancrer dans le contexte togolais** (pas un projet générique)
5. ✅ **Plaire à un jury de Génie Logiciel** (pas seulement métier)
6. ✅ **Éviter les mots trop génériques** : « application web », « plateforme numérique », « système d'information »
7. ✅ **Mentionner au moins un élément différenciateur** : RAG, IA, recommandation multi-signaux, OCR, prédiction ML, ou un autre défi technique

---

## 🤖 Section 10 — Prompt final à copier-coller

Copier-coller ce qui suit dans une conversation Claude (ou tout autre LLM) :

---

```
Tu es un expert en mémoires de Licence Professionnelle en Génie Logiciel. 
Le jury est composé d'enseignants en architecture logicielle, bases de 
données, IA, et génie logiciel. Le mémoire se déroule à l'Institut 
Polytechnique DEFITECH (Togo), encadré par HubCity/Woélab, pour 
l'année académique 2025-2026.

PROJET : "Activ EDUCATION" — plateforme full-stack d'orientation 
scolaire et professionnelle pour les élèves togolais.

PÉRIMÈTRE RÉALISÉ (basé sur le code) :
- 3 applications : Backend Spring Boot 4.0.5 / Java 21 (29+ packages 
  Package by Feature, héritage JPA JOINED, 133+ endpoints REST), 
  Mobile Flutter (OCR bulletins, chat conseillers, cartographie), 
  Backoffice React 19 / TypeScript 6 strict
- Infrastructure : PostgreSQL 16 + pgvector (RAG), Redis (rate-limit), 
  MinIO (stockage), OpenAI + Groq + Ollama (IA), Docker Compose
- 5 modules contractualisés (cahier des charges) : (1) Gestion profils 
  avec 9 rôles, (2) Bibliothèque centrale de 4 types de fiches 
  (Séries/Filières/Métiers/Établissements) avec structure 3 niveaux, 
  (3) Diagnostic d'orientation par quiz psychométrique (3 parcours 
  adaptatifs) et analyse des notes avec OCR, recommandations croisées, 
  (4) Accompagnement par FAQ, messagerie, visio Jitsi, (5) Back-office 
  avec 5 rôles + 2FA
- 23 modules complémentaires ajoutés (contribution personnelle) : 
  RIASEC, Prédiction ML, Simulateur, Mentorat, Alumni, Badges, etc.
- Innovations techniques : moteur de recommandation multi-signaux 
  (0,50×réalité + 0,35×aspiration + 0,15×engagement) avec anti-bulle 
  de filtre, RAG vectoriel pgvector en 2 phases, ML supervisé de 
  prédiction de réussite, OCR bulletins
- 117 établissements supérieurs togolais documentés, 92/92 tests 
  passants

CONTRAINTE : le thème doit tenir en 2 LIGNES MAXIMUM (≤ 200 caractères).

DEMANDE : Propose-moi 5 variantes de thème académiques, classées par 
pertinence (la meilleure en premier). Pour chaque variante :
- Le thème en lui-même (2 lignes max)
- Une justification courte (2-3 phrases) expliquant pourquoi ce thème 
  est pertinent et quels aspects du projet il valorise

Indique ton COUP DE CŒUR avec une raison claire.

ÉVITE les formulations trop génériques ("application web", "plateforme 
numérique", "système d'information"). PRIVILÉGIE les formulations 
académiques qui mentionnent : architecture multi-modules, IA, RAG, 
recommandation, sécurité, Package by Feature, ou un autre défi 
technique spécifique au Génie Logiciel.
```

---

## ✅ Checklist d'utilisation

- [ ] Copier le contenu de la **Section 10** (Prompt final)
- [ ] Le coller dans une nouvelle conversation Claude (ou autre LLM)
- [ ] Récupérer les 5 propositions + le coup de cœur
- [ ] Soumettre le thème retenu à votre **directeur de mémoire à DEFITECH**
- [ ] Une fois validé, mettre à jour la page de garde du mémoire

---

## 📎 Annexes disponibles (références complémentaires)

Si le LLM a besoin de plus de contexte, vous pouvez aussi lui partager :

- `docs/memoire/01-cahier-charges-fonctionnel.md` — détail des 5 modules
- `docs/memoire/02-cahier-charges-technique.md` — architecture technique cible
- `docs/memoire/04-problematique-recherche.md` — sous-problèmes + hypothèses
- `docs/memoire/05-valeur-ajoutee-memoire.md` — 7 idées innovantes
- `docs/memoire/07-description-pour-theme.md` — description synthétique
- `docs/memoire/08-annexe-modules-complementaires.md` — détails des 23 modules ajoutés
- `docs/projet/01-description-projet.md` — description technique complète

> **Conseil** : Ne donnez **que la Section 10** dans un premier temps. Si le LLM pose des questions précises, partagez ensuite l'annexe concernée.
