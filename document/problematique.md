# Problématique — Activ Education

## Contexte

Au Togo, comme dans la plupart des pays d'Afrique subsaharienne, l'orientation scolaire et professionnelle reste un luxe accessible à une minorité. Les établissements secondaires et universitaires manquent de conseillers d'orientation qualifiés — on compte en moyenne **1 conseiller pour 5 000 élèves** dans le public, contre un ratio recommandé de 1 pour 300. Les rares services d'orientation sont concentrés dans les grandes villes (Lomé, Kara), laissant les zones rurales sans aucun accompagnement.

Parallèlement, la démocratisation de l'accès au téléphone mobile — **plus de 80 % des jeunes Togolais possèdent un smartphone** — ouvre une opportunité sans précédent : atteindre massivement les élèves là où ils se trouvent, via une application gratuite, intuitive et adaptée au contexte local.

## Problème général

**Comment offrir un accompagnement à l'orientation scolaire et professionnelle personnalisé, accessible et pertinent à chaque jeune Togolais, quel que soit son lieu de résidence, son niveau d'études ou sa situation socio-économique ?**

## Sous-problèmes

### 1. Absence de données structurées sur l'offre de formation

Il n'existe pas au Togo de base de données centralisée, exhaustive et à jour des filières de formation, des établissements et des débouchés métiers. Les informations sont dispersées entre les ministères, les établissements et les bouche-à-oreille. Comment structurer, maintenir et rendre explorable cette connaissance de manière collaborative ?

### 2. Inadéquation entre les profils et les parcours proposés

Les algorithmes de recommandation classiques (filière suggérée selon la série) ignorent la personnalité, les centres d'intérêt et les compétences réelles de l'élève. Comment concevoir un système de diagnostic multidimensionnel (quiz RIASEC, notes académiques, centres d'intérêt) qui propose des parcours pertinents au-delà des stéréotypes scolaires ?

### 3. Barrière de la langue et de la littératie numérique

Une part significative des jeunes Togolais maîtrise mal le français écrit ou n'a jamais utilisé d'application autre que les réseaux sociaux. Comment rendre l'orientation accessible à ces publics via des interfaces vocales, visuelles et des mécanismes de gamification ?

### 4. Passage à l'échelle avec des ressources limitées

Comment concevoir une architecture technique capable de desservir des centaines de milliers d'utilisateurs avec une infrastructure minimale, une connectivité intermittente (zones rurales) et un coût de fonctionnement proche de zéro ?

### 5. Évolution et adaptation continue

Le système éducatif togolais évolue (réformes des curricula, nouvelles filières, création d'universités). Comment concevoir une plateforme dont les contenus et les règles métier peuvent être mis à jour sans intervention technique, par des administrateurs non développeurs ?

## Questions de recherche

1. **QR1** : Un modèle de scoring multidimensionnel (RIASEC + notes académiques + préférences) peut-il améliorer significativement la pertinence des recommandations d'orientation par rapport aux approches traditionnelles basées uniquement sur la série scolaire ?

2. **QR2** : L'intégration d'un assistant IA conversationnel avec RAG (Retrieval-Augmented Generation) adapté au contexte local permet-elle de compenser le manque de conseillers humains dans les zones sous-desservies ?

3. **QR3** : Une architecture full-stack basée sur des technologies open source (Spring Boot, Flutter, PostgreSQL, MinIO) peut-elle supporter un passage à l'échelle vers des centaines de milliers d'utilisateurs avec un budget d'infrastructure minimal ?

4. **QR4** : Un assistant vocal multilingue (français + langues locales) peut-il réduire la fracture numérique dans l'accès à l'orientation scolaire ?

5. **QR5** : Une approche de versioning et de gestion de contenu sans code (WYSIWYG, paramètres applicatifs configurables) permet-elle une maintenance durable de la plateforme par des acteurs locaux non techniques ?

## Hypothèses

- **H1** : Les élèves utilisant l'application Activ Education ont un taux de satisfaction et de pertinence perçue des recommandations supérieur à celui des méthodes traditionnelles d'orientation.
- **H2** : L'assistant ORIA répond correctement à au moins 70 % des questions d'orientation courantes sans escalade humaine.
- **H3** : L'architecture retenue (Spring Boot + PostgreSQL + MinIO) supporte 10 000 utilisateurs simultanés avec un temps de réponse inférieur à 2 secondes.
- **H4** : Le taux d'adoption en zones rurales est significativement plus élevé chez les utilisateurs de l'assistant vocal que chez ceux utilisant uniquement l'interface texte.

## Périmètre de la solution

La plateforme Activ Education couvre :
- **L'exploration** : bibliothèque de 280+ établissements et 117 fiches de formation, avec moteur de recherche sémantique
- **Le diagnostic** : quiz adaptatif RIASEC, OCR bulletins de notes, validation de niveau BEPC/BAC
- **La recommandation** : algorithme personnalisé + assistant IA ORIA avec RAG
- **L'accompagnement** : messagerie, tickets, rendez-vous visio, rappels SMS
- **La valorisation** : portfolio de compétences, badges, CV, attestations
- **La communauté** : réseau social, témoignages, alumni, mentorat
- **L'administration** : backoffice complet avec éditeur WYSIWYG, statistiques, logs d'audit

## Acteurs concernés

| Acteur | Rôle dans la problématique |
|--------|---------------------------|
| Élèves (collégiens, lycéens, étudiants) | Bénéficiaires directs du service d'orientation |
| Parents | Accompagnateurs, donneurs de consentement |
| Conseillers d'orientation | Accompagnateurs humains, modérateurs |
| Administrateurs (Ministère, proviseurs) | Gestionnaires des contenus et des données |
| Décideurs politiques | Destinataires des données agrégées (DataHub) |
| Chercheurs en éducation | Évaluateurs de l'impact |

## Indicateurs de succès

- **Utilisation** : 50 000 élèves actifs mensuels après 12 mois
- **Satisfaction** : note moyenne > 4/5 sur les recommandations
- **Autonomie** : > 60 % des questions d'orientation résolues par ORIA sans intervention humaine
- **Impact** : augmentation du taux de poursuite d'études dans les filières recommandées
- **Accessibilité** : > 30 % d'utilisateurs en zones rurales
