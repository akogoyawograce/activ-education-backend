# Cahier des Charges — Annexe : Extensions et Modules Complémentaires

> **Document annexe au cahier des charges initial** listant les modules et fonctionnalités ajoutés pendant le stage, au-delà du périmètre contractualisé. Ces extensions ont été conçues et implémentées en accord avec la structure d'accueil (HubCity/Woélab) pour enrichir la valeur ajoutée de la plateforme Activ Education.

---

## Préambule

Le présent document constitue une **annexe technique** au cahier des charges fonctionnel validé en début de stage. Il recense les **modules complémentaires** développés pendant la période de stage, en réponse à :
- Des **besoins émergents** identifiés par l'équipe HubCity/Woélab
- Des **opportunités techniques** détectées lors de l'implémentation
- Des **retours utilisateurs** collectés pendant les phases de test
- Des **bonnes pratiques de Génie Logiciel** (qualité, modularité, extensibilité)

Ces extensions respectent l'architecture **Package by Feature** et s'intègrent de manière cohérente avec les 5 modules du cahier initial.

---

## 6. Module Réseau Alumni (Anciens Élèves)

### 6.1. Contexte et objectifs
Le Togo souffre d'un déficit de **modèles d'identification** pour les jeunes en orientation. La mise en relation avec des anciens élèves ayant suivi des parcours similaires constitue un levier puissant de motivation et d'information concrète.

### 6.2. Fonctionnalités
- **Profils d'anciens élèves** : parcours, formation suivie, métier actuel, ville
- **Annuaire searchable** : recherche par filière, métier, génération, région
- **Témoignages structurés** : retours d'expérience liés aux fiches du Module 2
- **Demande de contact** : un utilisateur peut demander à entrer en relation avec un alumnus
- **Modération** : validation des profils par les administrateurs

### 6.3. Implémentation
- Package : `alumni/` et `alums/`
- Entités : `Alumni`, `AlumniProfile`, `AlumniContactRequest`

---

## 7. Module Attestations

### 7.1. Contexte et objectifs
Les élèves togolais ont régulièrement besoin d'**attestations officielles** (scolarité, présence, orientation) pour diverses démarches administratives. La dématérialisation de ces documents est un gain de temps majeur.

### 7.2. Fonctionnalités
- **Génération automatique d'attestations PDF** à partir des données du profil
- **Modèles personnalisables** : orientation, suivi, présence
- **Signature numérique** (hash de validation)
- **Téléchargement direct** et historique des attestations émises
- **Vérification d'authenticité** via QR code ou trackingId

### 7.3. Implémentation
- Package : `attestations/`
- Génération PDF via bibliothèque dédiée (PDFBox sans clé API / OpenAI pour images)

---

## 8. Module Badges (Gamification)

### 8.1. Contexte et objectifs
La gamification est un levier reconnu pour **maintenir l'engagement** des jeunes sur les plateformes éducatives. Le système de badges valorise les progrès de l'élève dans son parcours d'orientation.

### 8.2. Fonctionnalités
- **30+ badges** débloqués selon des critères précis :
  - Quiz complété, score parfait, séries de connexions
  - Fiches consultées, favoris ajoutés
  - RDV pris, messages échangés avec un conseiller
  - Découverte de métiers variés
- **Niveaux de rareté** : commun, rare, épique, légendaire
- **Notifications** lors de l'obtention d'un badge
- **Affichage public** sur le profil utilisateur (optionnel)
- **Tableau de bord** des badges obtenus / à débloquer

### 8.3. Implémentation
- Package : `badge/`
- Entités : `Badge`, `UserBadge`, `BadgeCriteria`
- Moteur d'évaluation déclenché par événements (EventBus interne)

---

## 9. Module Cahier de Bord

### 9.1. Contexte et objectifs
Permettre à l'élève de **consigner ses réflexions**, ses évolutions personnelles et ses choix d'orientation dans un journal privé, utile pour les séances avec un conseiller.

### 9.2. Fonctionnalités
- **Entrées texte** datées et horodatées
- **Catégorisation** : réflexion, question, choix, doute
- **Possibilité d'attacher** des fiches (Métier, Filière) à une entrée
- **Export PDF** du journal complet
- **Confidentialité** : visible uniquement par l'élève et les conseillers autorisés
- **Recherche full-text** dans les entrées

### 9.3. Implémentation
- Package : `cahierdebord/`
- Entité : `CahierEntry`, `CahierCategory`

---

## 10. Module Calendrier et Événements

### 10.1. Contexte et objectifs
Les élèves togolais manquent d'informations centralisées sur les **événements d'orientation** : journées portes ouvertes, salons, dates de concours, sessions d'inscription.

### 10.2. Fonctionnalités
- **Agenda partagé** des événements d'orientation au Togo
- **Types d'événements** : portes ouvertes, salons, concours, inscriptions, conférences
- **Géolocalisation** : visualisation sur carte
- **Rappels automatiques** avant l'événement
- **Ajout à l'agenda personnel** de l'utilisateur
- **Inscription** à certains événements (limite de places)
- **Modération admin** des événements ajoutés

### 10.3. Implémentation
- Package : `calendrier/`
- Entités : `Event`, `EventType`, `EventRegistration`

---

## 11. Module Carte des Métiers

### 11.1. Contexte et objectifs
Visualisation **géographique** des opportunités professionnelles au Togo, en complément de la fiche Métier textuelle.

### 11.2. Fonctionnalités
- **Carte interactive** des métiers avec densité d'offres par zone
- **Filtrage par secteur** (agriculture, TIC, santé, éducation, artisanat, etc.)
- **Indicateurs socio-économiques** : salaire moyen, demande, perspectives
- **Liens vers les fiches métiers** du Module 2
- **Vue comparative** entre régions

### 11.3. Implémentation
- Package : `cartemetiers/`
- Intégration : `flutter_map` côté mobile
- Données : agrégées depuis la table `FicheMetier`

---

## 12. Module CV Generator

### 12.1. Contexte et objectifs
Les élèves bacheliers et étudiants n'ont souvent pas de CV structuré. La plateforme peut générer un **CV de base** à partir des informations du profil.

### 12.2. Fonctionnalités
- **Génération PDF** d'un CV formaté
- **Sections pré-remplies** : profil, parcours scolaire, compétences, centres d'intérêt
- **Modèles** : classique, moderne, créatif
- **Personnalisation** : ajout d'expériences, langues, certifications
- **Export** PDF haute qualité + partage lien

### 12.3. Implémentation
- Package : `cvgenerateur/`
- Templates LaTeX ou HTML/CSS → PDF
- Données issues du profil utilisateur

---

## 13. Module Data Hub

### 13.1. Contexte et objectifs
Centraliser les **statistiques avancées** et les **données analytiques** pour les administrateurs, afin de piloter la plateforme de manière data-driven.

### 13.2. Fonctionnalités
- **Tableaux de bord** multi-axes : audience, engagement, conversion
- **Filtres dynamiques** : période, région, profil, parcours
- **Export** CSV/Excel pour analyses externes
- **Indicateurs clés** : taux de complétion du quiz, score moyen, taux de prise de RDV
- **Visualisations** : graphiques, heatmaps, courbes d'évolution
- **Prédictions** : tendances d'orientation par cohorte

### 13.3. Implémentation
- Package : `datahub/`
- Agrégations SQL optimisées + cache Redis
- API dédiée consommée par le backoffice

---

## 14. Module Défis (Challenges)

### 14.1. Contexte et objectifs
Les **défis gamifiés** encouragent l'exploration et la découverte de nouveaux horizons d'orientation.

### 14.2. Fonctionnalités
- **Défis individuels** : « Découvre 5 métiers du numérique en 7 jours »
- **Défis communautaires** : « La classe qui complète le plus de quiz ce mois-ci »
- **Récompenses** : badges, points, attestations de participation
- **Classements** anonymisés
- **Suggestions personnalisées** selon le profil RIASEC
- **Notifications push** pour les nouveaux défis

### 14.3. Implémentation
- Package : `defis/`
- Entités : `Challenge`, `UserChallenge`, `Leaderboard`

---

## 15. Module Emploi (Offres d'emploi)

### 15.1. Contexte et objectifs
Mise en relation entre **étudiants/diplômés** et **offres d'emploi/stage** au Togo, créant un pont entre l'orientation et l'insertion professionnelle.

### 15.2. Fonctionnalités
- **Catalogue d'offres** : emploi, stage, alternance
- **Recherche et filtrage** : secteur, ville, type de contrat, niveau d'études
- **Candidature en ligne** : CV + lettre de motivation
- **Suivi des candidatures** : statut (envoyée, vue, retenue, refusée)
- **Suggestions d'offres** basées sur le profil RIASEC + historique
- **Espace recruteur** (administrateurs modération)

### 15.3. Implémentation
- Package : `emploi/`
- Entités : `JobOffer`, `JobApplication`, `JobCategory`

---

## 16. Module Entretien (Simulation)

### 16.1. Contexte et objectifs
Préparation des élèves aux **entretiens d'admission** dans les établissements supérieurs ou aux entretiens d'embauche.

### 16.2. Fonctionnalités
- **Banque de questions** par type d'entretien (concours, admission, emploi)
- **Simulation interactive** : questions/réponses chronométrées
- **Évaluation IA** des réponses (OpenAI/Groq) avec feedback
- **Catégories** : motivation, projet professionnel, connaissances générales
- **Historique** des simulations et progression
- **Recommandations** pour s'améliorer

### 16.3. Implémentation
- Package : `entretien/`
- Intégration IA : analyse des réponses textuelles
- Statuts : en cours, terminé, évalué, erreur (cf. fix bug du 6 août)

---

## 17. Module Hors-ligne

### 17.1. Contexte et objectifs
Les élèves en zone rurale ont un accès Internet limité. La plateforme doit fonctionner **partiellement hors-ligne** pour rester accessible.

### 17.2. Fonctionnalités
- **Cache local** des fiches consultées (Flutter)
- **Mode dégradé** : consultation des fiches + quiz sans réseau
- **Synchronisation différée** des actions hors-ligne (favoris, notes)
- **Indicateur visuel** du mode hors-ligne
- **Stratégie de cache** configurable par type de contenu

### 17.3. Implémentation
- Package : `horsligne/`
- Cache Flutter : `shared_preferences` + `sqflite` pour les données structurées
- File d'attente des actions à synchroniser

---

## 18. Module Mentorat

### 18.1. Contexte et objectifs
Mise en relation entre **mentors** (étudiants avancés, professionnels) et **mentorés** (élèves en orientation) pour un accompagnement personnalisé.

### 18.2. Fonctionnalités
- **Profils mentors** : parcours, expertise, disponibilités
- **Demande de mentorat** : l'élève formule ses besoins
- **Matching algorithmique** selon profil RIASEC et objectifs
- **Sessions de mentorat** : chat dédié, visio Jitsi
- **Suivi longitudinal** : objectifs, progression, bilan
- **Modération** des profils mentors par les administrateurs

### 18.3. Implémentation
- Package : `mentorat/`
- Entités : `Mentor`, `Mentee`, `MentoringSession`, `MentoringGoal`

---

## 19. Module Parrainage

### 19.1. Contexte et objectifs
Programme de **parrainage** pour accélérer l'adoption de la plateforme via le bouche-à-oreille.

### 19.2. Fonctionnalités
- **Code de parrainage unique** par utilisateur
- **Lien de partage** partageable (WhatsApp, SMS, réseaux sociaux)
- **Récompenses** pour le parrain et le filleul (badges, attestations)
- **Tableau de bord** des parrainages actifs
- **Statistiques** du programme (taux de conversion)

### 19.3. Implémentation
- Package : `parrainage/`
- Entités : `Sponsorship`, `SponsorshipReward`

---

## 20. Module Portfolio Élève

### 20.1. Contexte et objectifs
Permettre à l'élève de **constituer un portfolio** de ses acquis, projets, expériences, valorisable pour les admissions ou candidatures.

### 20.2. Fonctionnalités
- **Sections personnalisables** : projets scolaires, expériences, compétences, certifications
- **Upload de fichiers** : images, PDF, vidéos
- **Visibilité** : privé, partagé avec conseillers, public
- **Export PDF** du portfolio complet
- **Liaison** avec les fiches Métiers/Formations visées
- **Mise à jour** au fil du parcours

### 20.3. Implémentation
- Package : `portfolio/`
- Entités : `PortfolioItem`, `PortfolioSection`

---

## 21. Module Prédiction ML (Phase 5)

### 21.1. Contexte et objectifs
Appliquer le **machine learning supervisé** pour prédire les chances de réussite d'un élève dans une filière donnée, en complément du moteur de recommandation.

### 21.2. Fonctionnalités
- **Modèle ML supervisé** : prédiction de réussite en filière
- **Dataset synthétique** de 1200+ lignes pour entraînement initial
- **Features** : notes par matière, score RIASEC, série, historique
- **Endpoint prédictif** : `/api/v1/eleves/{id}/orientation-outcome`
- **Données réelles** en cours de collecte pour ré-entraînement
- **Explainability** : features importantes dans la décision

### 21.3. Implémentation
- Package : `prediction/`
- Entités : `NiveauFiliere`, `NotesHistorique`, `OrientationOutcome`, `EngagementSignal`
- Scripts Python : `phase5_train_real.py`, `generate_synthetic_data.py`
- Modèle exporté (joblib/pickle) chargé au démarrage

> ⚠️ **Statut** : Phase 1-4 implémentées ; Phase 5 (production ML) en cours d'enrichissement avec données réelles.

---

## 22. Module Réorientation

### 22.1. Contexte et objectifs
Accompagner spécifiquement les étudiants qui souhaitent **changer de voie** en cours de parcours universitaire.

### 22.2. Fonctionnalités
- **Diagnostic dédié** : analyse des raisons de réorientation
- **Suggestions de passerelles** : BTS ↔ Licence, DUT ↔ Master
- **Quiz spécialisé** (parcours « Réorientation » déjà dans le Module 3)
- **Mise en relation** avec des conseillers spécialisés
- **Suivi de dossier** de réorientation

### 22.3. Implémentation
- Package : `reorientation/`
- Logique métier : extension du Module 3

---

## 23. Module Réseau Social Interne

### 23.1. Contexte et objectifs
Créer un **espace d'échange** modéré entre élèves pour partager leurs expériences d'orientation.

### 23.2. Fonctionnalités
- **Forum thématique** : par filière, par métier, par région
- **Posts** : texte, images, liens
- **Commentaires** et likes
- **Modération** par les administrateurs
- **Anonymat** optionnel
- **Signalement** de contenus inappropriés

### 23.3. Implémentation
- Package : `reseau/`
- Entités : `Post`, `Comment`, `Reaction`

---

## 24. Module RIASEC (Test de Personnalité)

### 24.1. Contexte et objectifs
Le test **RIASEC** (Réaliste, Investigateur, Artistique, Social, Entreprenant, Conventionnel) est un référentiel psychométrique internationalement reconnu pour l'orientation professionnelle.

### 24.2. Fonctionnalités
- **30 questions** standardisées RIASEC
- **Calcul des scores** par dimension
- **Profil dominant** (ex : « SIA » = Social-Investigateur-Artistique)
- **Mapping** profil RIASEC ↔ fiches Métiers/Formations
- **Comparaison** des profils typiques (15 catalogues pré-calculés)

### 24.3. Implémentation
- Package : `riasec/`
- Entités : `RiasecQuestion`, `RiasecProfile`, `RiasecScore`
- Algorithme : scoring normalisé + matching vectoriel

---

## 25. Module Salle Virtuelle (Visio)

### 25.1. Contexte et objectifs
Fournir une **salle de visio-conférence intégrée** pour les RDV conseillers (cf. cahier §4.7 qui suggérait Jitsi).

### 25.2. Fonctionnalités
- **Génération automatique de lien Jitsi** par RDV
- **Salle d'attente virtuelle**
- **Chat textuel** intégré pendant la visio
- **Partage d'écran** (capacité native Jitsi)
- **Enregistrement** (optionnel, avec consentement)
- **Logs de présence** pour le compte-rendu

### 25.3. Implémentation
- Package : `sallevirtuelle/`
- Intégration : API Jitsi Meet (gratuite, open source)
- Sécurité : lien unique non-devinable, expiration temporelle

---

## 26. Module Simulateur de Parcours

### 26.1. Contexte et objectifs
Permettre à l'élève de **simuler différents parcours** d'orientation et visualiser les conséquences de ses choix.

### 26.2. Fonctionnalités
- **Simulation d'un parcours** : « Si je choisis Série C → Licence Maths → Master Data Science »
- **Affichage des étapes** : durée, prérequis, coûts, débouchés
- **Comparaison** de 2-3 parcours en parallèle
- **Indicateurs** : probabilité de réussite (basée sur les notes), durée, employabilité
- **Sauvegarde** des simulations favorites

### 26.3. Implémentation
- Package : `simulateur/`
- Logique : graphe de parcours + heuristiques de probabilité

---

## 27. Module Témoignages

### 27.1. Contexte et objectifs
Collecter et exposer des **témoignages** d'anciens élèves ou de professionnels pour humaniser l'orientation.

### 27.2. Fonctionnalités
- **Témoignages vidéo, audio, texte**
- **Catégorisation** : par filière, métier, situation (réorientation, reconversion)
- **Modération** avant publication
- **Liaison** avec les fiches Module 2
- **Soumission** par les utilisateurs (workflow de validation)
- **Mise en avant** sur les fiches concernées

### 27.3. Implémentation
- Package : `temoignage/`
- Entités : `Testimony`, `TestimonyMedia`

---

## 28. Module VAE (Validation des Acquis de l'Expérience)

### 28.1. Contexte et objectifs
Permettre aux **adultes en reconversion** de faire valider leurs acquis professionnels pour obtenir une certification.

### 28.2. Fonctionnalités
- **Questionnaire d'auto-évaluation** des compétences
- **Upload de preuves** : attestations, certificats, portfolios
- **Mise en relation** avec un conseiller VAE
- **Suivi du dossier** de validation
- **Ressources pédagogiques** sur la démarche VAE au Togo

### 28.3. Implémentation
- Package : `vae/`
- Cible : adultes en reconversion (rôle déjà défini Module 1)

---

## Récapitulatif des modules annexes

| # | Module | Package(s) | Priorité |
|---|--------|-----------|----------|
| 6 | Alumni | `alumni/`, `alums/` | Standard |
| 7 | Attestations | `attestations/` | Standard |
| 8 | Badges | `badge/` | Engagement |
| 9 | Cahier de bord | `cahierdebord/` | Premium |
| 10 | Calendrier | `calendrier/` | Standard |
| 11 | Carte des métiers | `cartemetiers/` | Premium |
| 12 | CV Generator | `cvgenerateur/` | Premium |
| 13 | Data Hub | `datahub/` | Admin |
| 14 | Défis | `defis/` | Engagement |
| 15 | Emploi | `emploi/` | Premium |
| 16 | Entretien | `entretien/` | Standard |
| 17 | Hors-ligne | `horsligne/` | Technique |
| 18 | Mentorat | `mentorat/` | Premium |
| 19 | Parrainage | `parrainage/` | Marketing |
| 20 | Portfolio | `portfolio/` | Premium |
| 21 | Prédiction ML | `prediction/` | Innovation |
| 22 | Réorientation | `reorientation/` | Standard |
| 23 | Réseau social | `reseau/` | Engagement |
| 24 | RIASEC | `riasec/` | Standard |
| 25 | Salle virtuelle | `sallevirtuelle/` | Standard |
| 26 | Simulateur | `simulateur/` | Premium |
| 27 | Témoignages | `temoignage/` | Standard |
| 28 | VAE | `vae/` | Premium |

**Total : 23 modules complémentaires** ajoutés au périmètre initial.

---

## Intégration avec les 5 modules du cahier initial

Ces modules complémentaires **s'articulent** autour des 5 modules principaux :

- **Avec Module 1 (Profils)** : tous les modules consomment l'identité utilisateur
- **Avec Module 2 (Bibliothèque)** : badges, défis, mentorat, simulateur, témoignages pointent vers les fiches
- **Avec Module 3 (Diagnostic)** : RIASEC, simulation, entretien étendent le diagnostic
- **Avec Module 4 (Accompagnement)** : mentorat, parrainage, VAE prolongent l'accompagnement
- **Avec Module 5 (Admin)** : tous les modules ont un back-office de gestion

---

## Conformité architecturale

Toutes les extensions respectent :
- ✅ Le pattern **Package by Feature**
- ✅ L'héritage JPA **JOINED** (lorsque polymorphisme)
- ✅ La sécurité 3 couches (JWT + `@PreAuthorize` + SPEL)
- ✅ La validation DTOs (`@Valid`)
- ✅ Le rate-limiting Redis
- ✅ La documentation Swagger
- ✅ Les tests unitaires et d'intégration

---

> **Note méthodologique pour le mémoire** : Cette annexe démontre la **capacité d'extension** de l'architecture initiale et la **valeur ajoutée** apportée pendant le stage. Elle constitue une contribution personnelle au-delà du cahier des charges.
