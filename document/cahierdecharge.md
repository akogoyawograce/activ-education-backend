# CAHIER DES CHARGES — Activ Education

## Introduction

Le présent cahier des charges définit les spécifications fonctionnelles et techniques de la plateforme **Activ Education**, une solution numérique d'aide à l'orientation scolaire et professionnelle destinée aux jeunes Togolais. Ce document sert de référence pour le développement, la validation et le déploiement du projet.

## Contexte

### Contexte général

Au Togo, l'orientation scolaire et professionnelle reste un service inaccessible pour la majorité des jeunes. Le ratio conseiller d'orientation/élèves est de 1 pour 5 000 dans le public, contre un ratio recommandé de 1 pour 300 par les standards internationaux. Les services existants sont concentrés dans les grandes villes (Lomé, Kara), laissant les zones rurales sans accompagnement.

Parallèlement, plus de 80 % des jeunes Togolais possèdent un smartphone, créant une opportunité unique de délivrer des services d'orientation à grande échelle via une application mobile.

### Contexte précis

Plusieurs lacunes ont été identifiées :

1. **Absence de données centralisées** : il n'existe pas de base de données exhaustive et à jour des filières de formation, des établissements et des débouchés métiers au Togo
2. **Recommandations inadaptées** : les approches traditionnelles se limitent à la série scolaire sans prendre en compte la personnalité, les centres d'intérêt ou les compétences réelles
3. **Fracture numérique** : une partie significative des jeunes maîtrise mal le français écrit ou les interfaces complexes
4. **Évolutivité** : le système éducatif togolais est en constante évolution (réformes, nouvelles filières, création d'universités)
5. **Coût** : les solutions existantes sont souvent payantes ou financées par des partenariats internationaux non pérennes

## Problématique

Comment offrir un accompagnement à l'orientation scolaire et professionnelle personnalisé, accessible gratuitement et pertinent culturellement à chaque jeune Togolais, indépendamment de son lieu de résidence, de son niveau d'études ou de sa situation socio-économique ?

Cette problématique se décline en cinq sous-questions :
1. Comment structurer et maintenir une base de connaissances complète du système éducatif togolais ?
2. Comment concevoir un diagnostic multidimensionnel pertinent au-delà de la simple série scolaire ?
3. Comment rendre l'orientation accessible aux publics peu familiers du numérique ?
4. Comment assurer la pérennité et l'évolutivité avec des ressources limitées ?
5. Comment permettre la mise à jour des contenus sans compétences techniques ?

## Objectifs

### Objectif général

Développer une plateforme numérique d'orientation scolaire et professionnelle gratuite, accessible sur mobile et sur le web, adaptée au contexte togolais et capable d'accompagner chaque jeune dans la construction de son projet d'avenir.

### Objectifs spécifiques

1. **Structurer l'information** : créer et maintenir une base de données centralisée des séries scolaires, filières de formation, établissements et métiers au Togo
2. **Diagnostiquer le profil** : concevoir un système d'évaluation multidimensionnel combinant test de personnalité (RIASEC), résultats académiques et centres d'intérêt
3. **Recommander des parcours** : proposer des correspondances personnalisées entre le profil de l'utilisateur et les offres de formation/métiers
4. **Assister par l'IA** : intégrer un assistant conversationnel spécialisé dans l'orientation scolaire au Togo
5. **Accompagner le parcours** : fournir des outils de suivi (rendez-vous, messagerie, portfolio, simulateur)
6. **Garantir l'accessibilité** : concevoir une interface intuitive avec support vocal pour réduire la fracture numérique
7. **Assurer la maintenabilité** : permettre aux administrateurs non techniques de mettre à jour les contenus

## Étude de l'existant

### État des lieux

L'orientation scolaire au Togo repose actuellement sur :

| Moyen | Limites |
|-------|---------|
| Conseillers d'orientation dans les établissements | Ratio insuffisant (1:5000), concentrés en ville |
| Brochures et guides papier | Obsolètes rapidement, diffusion limitée |
| Sites web ministériels | Informations partielles, non centralisées |
| Bouche-à-oreille | Source principale mais peu fiable |
| Plateformes internationales (Orientation.com, etc.) | Non adaptées au système éducatif togolais |

### Analyse des besoins

Les utilisateurs potentiels (élèves, parents, conseillers, administrateurs) expriment le besoin de :
- Une information fiable et actualisée sur les formations et métiers
- Un accompagnement personnalisé gratuit
- Une interface simple, fonctionnant sur des smartphones d'entrée de gamme
- Un accès hors ligne possible
- Des contenus dans un langage accessible

## Critique de l'existant

Les solutions actuelles présentent des insuffisances majeures :

1. **Couverture géographique insuffisante** : seules les grandes villes disposent de services d'orientation
2. **Absence de personnalisation** : les recommandations ne tiennent pas compte du profil individuel
3. **Information fragmentée** : aucune source unique et fiable n'existe
4. **Non-adaptation au contexte local** : les plateformes internationales ignorent les spécificités du système éducatif togolais
5. **Coût d'accès** : les services spécialisés sont payants
6. **Obsolescence** : les supports papier et les sites statiques ne suivent pas les réformes

## Proposition de solution

### Architecture proposée

La plateforme Activ Education sera composée de trois applications complémentaires :

1. **Application mobile** (Android grand public) : interface principale pour les élèves et parents
2. **Interface web d'administration** (Backoffice) : gestion des contenus et des utilisateurs
3. **API centrale** : cœur métier accessible par les deux interfaces

### Fonctionnalités prévues

#### Module Authentification et profils
- Inscription et connexion sécurisées
- Profils multiples : Élève, Parent, Conseiller, Administrateur
- Authentification à deux facteurs pour les comptes sensibles
- Gestion des mots de passe oubliés
- Consentement parental pour les mineurs

#### Module Bibliothèque des formations
- Fiches détaillées pour les séries scolaires, filières, métiers et établissements
- Navigation interconnectée (série → filière → métier → établissement)
- Moteur de recherche avec filtres
- Système de favoris et historique
- Accès public en consultation

#### Module Diagnostic d'orientation
- Test de personnalité RIASEC (Réaliste, Investigateur, Artistique, Social, Entreprenant, Conventionnel)
- Saisie et analyse des résultats scolaires
- Questionnaire sur les centres d'intérêt
- Scoring multidimensionnel
- Génération de quiz par intelligence artificielle

#### Module Recommandation personnalisée
- Analyse croisée du profil (test + notes + préférences)
 Suggestion de filières et métiers adaptés
- Matching avec les seuils d'admission des formations
- Recommandations évolutives dans le temps

#### Module Assistant IA (ORIA)
- Assistant conversationnel spécialisé en orientation
- Réponses basées sur une base de connaissances locale
- Support du chat textuel
- Assistant vocal (reconnaissance et synthèse vocale)
- Suggestions contextuelles

#### Module Accompagnement
- Messagerie interne élève-conseiller
- Prise de rendez-vous en ligne
- Système de tickets de support
- Notifications et rappels

#### Module Parcours et suivi
- Portfolio de compétences
- Simulateur de parcours "Et si... ?"
- Cahier de bord personnel
- Badges et gamification

#### Module Administration (Backoffice)
- Gestion des fiches (CRUD) : séries, filières, métiers, établissements
- Éditeur de contenu WYSIWYG
- Gestion des utilisateurs
- Modération des questions/réponses (FAQ)
- Statistiques et indicateurs
- Paramètres applicatifs
- Journal d'audit

### Modules complémentaires (phases ultérieures)

- Réseau social d'orientation
- Témoignages d'anciens élèves
- Offres d'emploi et candidatures
- Validation des acquis d'expérience (VAE)
- Visites virtuelles d'établissements
- Carte interactive des métiers par région
- Prédiction de réussite académique
- Parrainage entre élèves

### Contraintes techniques

- Application mobile fonctionnant sur Android 8+ (entrée de gamme)
- Interface web responsive (ordinateur, tablette, mobile)
- API sécurisée (authentification par token, chiffrement)
- Base de données relationnelle avec capacités de recherche vectorielle
- Stockage de fichiers (images, documents, vidéos)
- Hébergement sur infrastructure locale ou cloud à faible coût
- Codes sources ouverts

### Sécurité

- Chiffrement des mots de passe (algorithme de hachage robuste)
- Jetons d'authentification à durée limitée
- Protection contre les attaques par force brute (limitation de requêtes)
- Communication chiffrée (HTTPS)
- Contrôle d'accès par rôles
- Journalisation des actions sensibles

## Conclusion

Le projet Activ Education répond à un besoin socialement prioritaire au Togo : permettre à chaque jeune, où qu'il se trouve et quel que soit son niveau, de bénéficier d'un accompagnement gratuit et personnalisé dans son orientation scolaire et professionnelle.

La solution proposée combine une application mobile grand public, une interface d'administration web et une API centrale, couvrant l'ensemble du parcours d'orientation : découverte des métiers et formations, diagnostic du profil, recommandations personnalisées, accompagnement par des conseillers et assistant IA, suivi du parcours.

Le présent cahier des charges servira de référence pour les phases de conception, développement, test et déploiement de la plateforme.
