# CAHIER DES CHARGES FONCTIONNEL — Activ Education

## 1. Présentation du système

### 1.1. Périmètre

Activ Education est une plateforme numérique d'aide à l'orientation scolaire et professionnelle destinée aux jeunes Togolais. Le système couvre l'ensemble du parcours d'orientation : découverte des formations et métiers, diagnostic du profil, recommandations personnalisées, accompagnement par des conseillers, et suivi du parcours individuel.

### 1.2. Utilisateurs et profils

Cinq types d'utilisateurs interagissent avec le système.

| Profil | Description | Périmètre fonctionnel |
|--------|-------------|----------------------|
| Élève | Collégien, lycéen, étudiant, professionnel, autre | Parcours complet d'orientation, quiz, bibliothèque, messagerie, portfolio |
| Parent | Parent ou tuteur d'un ou plusieurs élèves | Suivi des enfants, messagerie avec conseillers, consultation des résultats |
| Conseiller | Professionnel de l'orientation | Accompagnement individuel, rendez-vous, messagerie, suivi des élèves |
| Administrateur | Gestionnaire de la plateforme | CRUD contenus, modération, statistiques, gestion utilisateurs |
| Super administrateur | Administrateur général | Toutes les fonctions admin + paramètres système, logs |

### 1.3. Interfaces

Le système est accessible via trois interfaces :

1. **Application mobile** (Android) : destinée aux élèves et parents, couvre l'ensemble des fonctionnalités grand public
2. **Interface web backoffice** : destinée aux conseillers et administrateurs, couvre la gestion des contenus et l'accompagnement
3. **API** : interface de programmation reliant les deux interfaces au système central

---

## 2. Modules fonctionnels

### 2.1. Module Authentification et comptes

#### 2.1.1. Inscription

| Identifiant | F-AUTH-01 |
|-------------|-----------|
| Titre | Création de compte élève |
| Acteurs | Élève non inscrit |
| Description | Un élève crée un compte en fournissant nom, prénom, email, mot de passe, type d'apprenant (Collégien, Lycéen, Étudiant, Professionnel, Autre) |
| Règles | L'email doit être unique dans le système. Le mot de passe doit comporter au moins 8 caractères. L'élève reçoit un email de confirmation. |
| Contrainte | Pour les moins de 15 ans, le consentement parental est requis avant activation complète du compte |

| Identifiant | F-AUTH-02 |
|-------------|-----------|
| Titre | Création de compte parent |
| Acteurs | Parent non inscrit |
| Description | Un parent crée un compte et peut optionnellement rattacher un ou plusieurs enfants (élèves) via leur email |
| Règles | Mêmes règles que F-AUTH-01. Le lien parent-enfant est vérifiable |

| Identifiant | F-AUTH-03 |
|-------------|-----------|
| Titre | Création de compte conseiller |
| Acteurs | Administrateur |
| Description | Un administrateur crée un compte conseiller avec ses informations professionnelles (spécialités, qualifications, années d'expérience) |
| Règles | Seul un administrateur peut créer un compte conseiller |

#### 2.1.2. Connexion et sécurité

| Identifiant | F-AUTH-04 |
|-------------|-----------|
| Titre | Connexion |
| Acteurs | Tous les utilisateurs |
| Description | L'utilisateur se connecte avec son email et son mot de passe. Le système délivre un jeton d'accès temporaire et un jeton de rafraîchissement |
| Règles | Après 5 tentatives échouées en 15 minutes, le compte est temporairement bloqué. Le jeton d'accès expire après 15 minutes. Le jeton de rafraîchissement expire après 7 jours |

| Identifiant | F-AUTH-05 |
|-------------|-----------|
| Titre | Double authentification (2FA) |
| Acteurs | Conseillers, Administrateurs |
| Description | Après la connexion, un code à 6 chiffres est demandé. Le code est généré par une application d'authentification (Google Authenticator, Authy) |
| Règles | Configurable par l'utilisateur. Obligatoire pour les administrateurs |

| Identifiant | F-AUTH-06 |
|-------------|-----------|
| Titre | Mot de passe oublié |
| Acteurs | Tous les utilisateurs |
| Description | L'utilisateur demande une réinitialisation de mot de passe. Un code à usage unique est envoyé par email. Après validation du code, l'utilisateur peut définir un nouveau mot de passe |

#### 2.1.3. Profil

| Identifiant | F-PROF-01 |
|-------------|-----------|
| Titre | Consultation et modification du profil |
| Acteurs | Tous les utilisateurs |
| Description | Chaque utilisateur peut consulter et modifier ses informations personnelles (nom, prénom, téléphone, photo de profil) |
| Règles | L'email n'est pas modifiable après création. La photo de profil est stockée et optimisée pour le web |

| Identifiant | F-PROF-02 |
|-------------|-----------|
| Titre | Complétion du profil élève |
| Acteurs | Élève |
| Description | L'élève renseigne son établissement, sa classe, sa série, ses options, ses centres d'intérêt, son métier souhaité |
| Règles | Plus le profil est complet, plus les recommandations sont précises. Un indicateur de complétion guide l'utilisateur |

| Identifiant | F-PROF-03 |
|-------------|-----------|
| Titre | Saisie des notes |
| Acteurs | Élève |
| Description | L'élève saisit manuellement ses notes par matière avec coefficients |
| Règles | Les notes sont validées (plage 0-20). L'historique des notes est conservé |

| Identifiant | F-PROF-04 |
|-------------|-----------|
| Titre | Upload de bulletins et relevés |
| Acteurs | Élève |
| Description | L'élève importe un bulletin (PDF ou photo). Le système extrait automatiquement les notes par OCR |
| Règles | Formats acceptés : PDF, JPEG, PNG. Extraction automatique des matières, notes, coefficients |

### 2.2. Module Bibliothèque des formations

#### 2.2.1. Fiches

Quatre types de fiches sont gérés : Séries scolaires, Filières de formation, Métiers, Établissements.

| Identifiant | F-BIB-01 |
|-------------|-----------|
| Titre | Consultation des fiches |
| Acteurs | Tous les utilisateurs (lecture publique), Administrateur (création/modification/suppression) |
| Description | Chaque fiche contient un titre, un résumé, un contenu détaillé, des images, des vidéos et des documents associés |
| Contenu attendu | Séries : matières principales, coefficients. Filières : durée, niveau requis, programme, débouchés. Métiers : secteur, missions, compétences, formation, salaire. Établissements : adresse, ville, type, contacts, site web, offre de formation |

| Identifiant | F-BIB-02 |
|-------------|-----------|
| Titre | Navigation interconnectée |
| Acteurs | Tous les utilisateurs |
| Description | Les fiches sont reliées entre elles : une série est liée aux filières accessibles, une filière aux métiers préparés et aux établissements qui la proposent, un métier aux formations y menant |
| Règles | La navigation se fait par liens cliquables entre fiches. Un graphe de visualisation montre les connexions |

| Identifiant | F-BIB-03 |
|-------------|-----------|
| Titre | Recherche |
| Acteurs | Tous les utilisateurs |
| Description | L'utilisateur recherche des fiches par mots-clés. La recherche porte sur le titre, le résumé et le contenu. Des filtres par catégorie, ville, type sont disponibles |
| Règles | La recherche doit être rapide (< 2 secondes). Les résultats sont ordonnés par pertinence. La recherche fonctionne avec des termes approximatifs |

| Identifiant | F-BIB-04 |
|-------------|-----------|
| Titre | Favoris |
| Acteurs | Élève, Parent |
| Description | L'utilisateur peut marquer des fiches comme favorites pour les retrouver facilement. Il peut ajouter une note personnelle à chaque favori |
| Règles | Les favoris sont synchronisés entre sessions. L'utilisateur peut organiser ses favoris |

#### 2.2.2. FAQ

| Identifiant | F-BIB-05 |
|-------------|-----------|
| Titre | Consultation de la FAQ |
| Acteurs | Tous les utilisateurs |
| Description | Une foire aux questions présente les questions fréquentes classées par catégories (Orientation, Inscription, Bourse, Filière, Métier, Général) |
| Règles | Les questions sont affichées par catégorie. L'utilisateur peut voter "Utile" ou "Pas utile" sur chaque réponse |

| Identifiant | F-BIB-06 |
|-------------|-----------|
| Titre | Gestion de la FAQ |
| Acteurs | Administrateur |
| Description | L'administrateur crée, modifie, publie ou supprime des entrées FAQ. Une modération (validation avant publication) est possible |
| Règles | Les entrées non publiées ne sont visibles que par les administrateurs. L'ordre d'affichage est configurable |

### 2.3. Module Diagnostic et quiz

#### 2.3.1. Quiz RIASEC

| Identifiant | F-DIAG-01 |
|-------------|-----------|
| Titre | Passation du quiz RIASEC |
| Acteurs | Élève |
| Description | L'élève répond à une série de questions pour déterminer son profil RIASEC (Réaliste, Investigateur, Artistique, Social, Entreprenant, Conventionnel) |
| Règles | Minimum 30 questions. Chaque réponse est associée à un domaine RIASEC. Les domaines sous-représentés dans les réponses précédentes sont priorisés. Le quiz peut être interrompu et repris |

| Identifiant | F-DIAG-02 |
|-------------|-----------|
| Titre | Résultats du quiz |
| Acteurs | Élève, Parent |
| Description | Le système affiche le profil RIASEC de l'élève sous forme de code à 3 lettres (ex: RIA, SEA) avec un diagramme radar des scores |
| Règles | L'historique des quiz est conservé pour visualiser l'évolution. Les métiers correspondant au profil sont suggérés |

#### 2.3.2. Gestion des quiz

| Identifiant | F-DIAG-03 |
|-------------|-----------|
| Titre | Création et édition de quiz |
| Acteurs | Administrateur |
| Description | L'administrateur crée des quiz avec des questions, des réponses, des scores RIASEC par réponse, des domaines et des niveaux de difficulté |
| Règles | Les questions peuvent être de type RIASEC, Connaissance, Intérêt, Personnalité. Les scores sont configurables. Les quiz peuvent être activés/désactivés |

#### 2.3.3. Analyse des résultats scolaires

| Identifiant | F-DIAG-04 |
|-------------|-----------|
| Titre | Analyse des résultats scolaires |
| Acteurs | Élève |
| Description | Le système analyse les notes de l'élève (saisies manuellement ou extraites par OCR) et les compare aux seuils d'admission des filières |
| Règles | Chaque filière a des seuils d'admission configurables. Le système indique les filières accessibles et celles nécessitant des efforts |

### 2.4. Module Recommandation

| Identifiant | F-REC-01 |
|-------------|-----------|
| Titre | Recommandation personnalisée |
| Acteurs | Élève |
| Description | Le système croise l'ensemble des données de l'élève (profil RIASEC, notes, centres d'intérêt, niveau d'études) pour suggérer des filières et métiers adaptés |
| Règles | Les recommandations sont évolutives : elles s'affinent au fur et à mesure que le profil se complète. Au moins 3 suggestions sont proposées avec un score de compatibilité |

| Identifiant | F-REC-02 |
|-------------|-----------|
| Titre | Matching filières |
| Acteurs | Élève |
| Description | Pour chaque filière, le système affiche le niveau de compatibilité avec le profil de l'élève |
| Règles | La compatibilité est calculée à partir du RIASEC, des notes et des préférences. Les seuils d'admission sont vérifiés |

### 2.5. Module Assistant IA (ORIA)

| Identifiant | F-ORIA-01 |
|-------------|-----------|
| Titre | Chat avec l'assistant IA |
| Acteurs | Élève, Parent, Conseiller |
| Description | L'utilisateur dialogue avec un assistant conversationnel spécialisé en orientation scolaire. L'assistant répond aux questions sur les formations, les métiers, les procédures d'inscription |
| Règles | L'assistant utilise une base de connaissances locale. Si une question dépasse ses connaissances, il oriente vers un conseiller humain. L'historique de la conversation est conservé par session |

| Identifiant | F-ORIA-02 |
|-------------|-----------|
| Titre | Assistance vocale |
| Acteurs | Élève |
| Description | L'utilisateur peut poser des questions oralement et recevoir des réponses vocales |
| Règles | La reconnaissance vocale fonctionne en français. L'utilisateur peut alterner entre texte et voix |

### 2.6. Module Accompagnement

#### 2.6.1. Messagerie

| Identifiant | F-ACC-01 |
|-------------|-----------|
| Titre | Messagerie interne |
| Acteurs | Élève, Parent, Conseiller |
| Description | Les utilisateurs échangent des messages privés. Un élève peut contacter son conseiller référent. Un conseiller peut contacter les élèves qui lui sont assignés |
| Règles | Les messages sont conservés. L'expéditeur voit si le message a été lu. La messagerie est asynchrone |

#### 2.6.2. Rendez-vous

| Identifiant | F-ACC-02 |
|-------------|-----------|
| Titre | Gestion des disponibilités |
| Acteurs | Conseiller |
| Description | Le conseiller définit ses créneaux de disponibilité (jour, heure début, heure fin) |
| Règles | Les créneaux sont récurrents ou ponctuels. Le conseiller peut bloquer des créneaux |

| Identifiant | F-ACC-03 |
|-------------|-----------|
| Titre | Prise de rendez-vous |
| Acteurs | Élève, Parent |
| Description | L'utilisateur consulte les créneaux disponibles d'un conseiller et réserve un rendez-vous |
| Règles | Un rendez-vous dure 30 minutes par défaut. L'utilisateur reçoit une confirmation. Un rappel est envoyé avant le rendez-vous |

| Identifiant | F-ACC-04 |
|-------------|-----------|
| Titre | Visioconférence |
| Acteurs | Élève, Conseiller |
| Description | Un lien de visioconférence est généré automatiquement pour chaque rendez-vous |
| Règles | La visioconférence ne nécessite pas d'inscription. Le lien est accessible depuis le rendez-vous |

#### 2.6.3. Tickets de support

| Identifiant | F-ACC-05 |
|-------------|-----------|
| Titre | Système de tickets |
| Acteurs | Élève, Conseiller, Administrateur |
| Description | Un utilisateur crée un ticket pour signaler un problème ou poser une question. Le ticket est assigné à un conseiller ou administrateur |
| Cycle de vie | OUVERT → ASSIGNE → EN_COURS → RESOLU → FERME |
| Règles | L'assignation peut être automatique (round-robin). L'utilisateur est notifié à chaque changement de statut |

### 2.7. Module Parcours et suivi

#### 2.7.1. Portfolio

| Identifiant | F-SUI-01 |
|-------------|-----------|
| Titre | Portfolio de compétences |
| Acteurs | Élève |
| Description | L'élève renseigne ses compétences, cours suivis, hobbies, expériences de bénévolat. Le système analyse ses forces et suggère des métiers correspondants |
| Règles | Les compétences sont classées par catégories. Un diagramme radar visualise la répartition |

#### 2.7.2. Simulateur de parcours

| Identifiant | F-SUI-02 |
|-------------|-----------|
| Titre | Simulateur "Et si... ?" |
| Acteurs | Élève |
| Description | L'élève construit un scénario fictif (série → notes → établissement) et visualise les débouchés possibles |
| Règles | L'utilisateur peut comparer plusieurs scénarios. Les résultats montrent les filières accessibles, les métiers et les établissements |

#### 2.7.3. Badges et défis

| Identifiant | F-SUI-03 |
|-------------|-----------|
| Titre | Système de badges |
| Acteurs | Élève |
| Description | L'élève obtient des badges en accomplissant des actions : complétion du profil, passation d'un quiz, exploration de fiches, etc. |
| Règles | Chaque badge a des critères d'obtention clairs. Les badges sont visibles sur le profil public |

| Identifiant | F-SUI-04 |
|-------------|-----------|
| Titre | Défis d'orientation |
| Acteurs | Élève |
| Description | Des défis sont proposés pour encourager l'exploration : "Consulte 5 fiches métiers", "Passe le quiz RIASEC", etc. |
| Règles | Chaque défi rapporte des points d'expérience. Les défis sont débloqués progressivement |

#### 2.7.4. Cahier de bord

| Identifiant | F-SUI-05 |
|-------------|-----------|
| Titre | Journal d'orientation |
| Acteurs | Élève |
| Description | L'élève tient un journal personnel de son parcours d'orientation : réflexions, découvertes, questions |
| Règles | Les entrées peuvent être publiques ou privées. L'élève peut noter son humeur. Les entrées sont datées |

### 2.8. Module Administration

#### 2.8.1. Gestion des contenus (Backoffice)

| Identifiant | F-ADM-01 |
|-------------|-----------|
| Titre | CRUD fiches bibliothèque |
| Acteurs | Administrateur |
| Description | L'administrateur crée, modifie, publie ou supprime les fiches (séries, filières, métiers, établissements). Un éditeur de texte enrichi (WYSIWYG) est disponible |
| Règles | Les modifications sont journalisées. Les fiches peuvent être mises en brouillon avant publication |

| Identifiant | F-ADM-02 |
|-------------|-----------|
| Titre | Gestion des utilisateurs |
| Acteurs | Administrateur |
| Description | L'administrateur consulte, active/désactive les comptes élèves, parents, conseillers |
| Règles | La désactivation est un soft-delete (le compte est conservé mais inaccessible) |

| Identifiant | F-ADM-03 |
|-------------|-----------|
| Titre | Éditeur de quiz |
| Acteurs | Administrateur |
| Description | L'administrateur crée et modifie les quiz : questions, réponses, scores RIASEC, niveaux de difficulté |
| Règles | Les modifications sont enregistrées progressivement. L'ordre des questions est réorganisable |

| Identifiant | F-ADM-04 |
|-------------|-----------|
| Titre | Gestion des seuils et matrices |
| Acteurs | Administrateur |
| Description | L'administrateur configure les seuils d'admission des filières et les matrices de scores pour le matching RIASEC-filières |
| Règles | Les modifications prennent effet immédiatement. Un historique des modifications est conservé |

| Identifiant | F-ADM-05 |
|-------------|-----------|
| Titre | Modération FAQ |
| Acteurs | Administrateur |
| Description | L'administrateur valide, refuse ou supprime les questions proposées pour la FAQ. Il peut créer des entrées directement |
| Règles | Les entrées en attente sont signalées. La modération peut être déléguée à un conseiller |

#### 2.8.2. Statistiques

| Identifiant | F-ADM-06 |
|-------------|-----------|
| Titre | Tableau de bord statistiques |
| Acteurs | Administrateur, Conseiller |
| Description | Le système affiche des indicateurs : nombre d'élèves, de conseillers, de quiz complétés, de rendez-vous, d'inscriptions |
| Règles | Les données sont présentées sous forme de graphiques (courbes, barres, camemberts). Périodes configurables : 7, 30, 90 jours |

#### 2.8.3. Paramètres système

| Identifiant | F-ADM-07 |
|-------------|-----------|
| Titre | Paramètres applicatifs |
| Acteurs | Super administrateur |
| Description | Le super administrateur configure les paramètres généraux : poids des quiz, seuils, mode maintenance, messages système |
| Règles | Les modifications s'appliquent sans redémarrage. Le mode maintenance bloque l'accès aux utilisateurs normaux |

---

## 3. Règles de gestion transverses

### 3.1. Gestion des accès

| Règle | Description |
|-------|-------------|
| RG-01 | Un utilisateur non connecté peut consulter les fiches bibliothèque en lecture seule |
| RG-02 | Un utilisateur non connecté ne peut pas passer de quiz ni accéder aux fonctionnalités personnelles |
| RG-03 | Un élève ne peut consulter que son propre profil et ses propres données |
| RG-04 | Un parent ne peut consulter que les profils de ses enfants rattachés |
| RG-05 | Un conseiller ne peut consulter que les élèves qui lui sont assignés |
| RG-06 | Un administrateur peut consulter et gérer tous les utilisateurs et contenus |
| RG-07 | Les actions de création, modification et suppression sont journalisées |

### 3.2. Validation des données

| Règle | Description |
|-------|-------------|
| RG-08 | L'email doit être valide et unique dans le système |
| RG-09 | Le mot de passe doit contenir au moins 8 caractères |
| RG-10 | Les notes saisies doivent être comprises entre 0 et 20 |
| RG-11 | Les dates doivent être cohérentes (pas de date future pour une date de naissance) |

### 3.3. Sécurité

| Règle | Description |
|-------|-------------|
| RG-12 | Les mots de passe sont hachés avec un algorithme robuste (BCrypt, coût 12) |
| RG-13 | Les jetons d'accès expirent après 15 minutes |
| RG-14 | Les jetons de rafraîchissement expirent après 7 jours |
| RG-15 | Les jetons révoqués sont blacklistés |
| RG-16 | Après 5 tentatives de connexion échouées en 15 minutes, le compte est temporairement bloqué |
| RG-17 | Toutes les communications passent par HTTPS |

---

## 4. Contraintes techniques

| Contrainte | Description |
|------------|-------------|
| CT-01 | L'application mobile fonctionne sur Android 8 ou supérieur |
| CT-02 | L'interface web backoffice est responsive (ordinateur, tablette, mobile) |
| CT-03 | L'API utilise une architecture REST avec échanges JSON |
| CT-04 | La base de données est relationnelle |
| CT-05 | Les fichiers (images, documents) sont stockés dans un service de stockage d'objets |
| CT-06 | Les temps de réponse API sont inférieurs à 500 ms pour les requêtes courantes |
| CT-07 | La plateforme supporte au moins 10 000 utilisateurs simultanés |
| CT-08 | Les codes sources sont ouverts et documentés |

---

## 5. Glossaire

| Terme | Définition |
|-------|------------|
| RIASEC | Modèle de personnalité professionnelle en 6 types : Réaliste, Investigateur, Artistique, Social, Entreprenant, Conventionnel |
| OCR | Reconnaissance optique de caractères (extraction de texte depuis une image ou un PDF) |
| CRUD | Create, Read, Update, Delete — opérations de base sur les données |
| WYSIWYG | What You See Is What You Get — éditeur de texte visuel |
| Soft-delete | Suppression logique : les données sont marquées comme supprimées mais conservées en base |
| Jeton d'accès | Jeton temporaire permettant d'accéder aux ressources protégées |
| Jeton de rafraîchissement | Jeton à longue durée permettant d'obtenir un nouveau jeton d'accès |
| 2FA | Authentification à deux facteurs |
| Base de connaissances | Ensemble structuré d'informations utilisé par l'assistant IA |
| Matching | Algorithme de correspondance entre un profil et des offres de formation/métiers |
