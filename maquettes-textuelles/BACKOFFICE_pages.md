# Maquette textuelle — Backoffice (React 19 + TS 6 + Tailwind v4)

> **Contexte projet :** Backoffice web d'Activ EDUCATION (Togo) — interface d'administration et de modération pour les **administrateurs** (ADMIN / SUPER_ADMIN) et les **conseillers d'orientation**. Stack : React 19, TypeScript 6 (strict + `erasableSyntaxOnly`), Vite, Tailwind v4, Zustand (state), TanStack Query v5 (data), React Router v7, Axios, Recharts (graphiques), Lucide React (icônes), `date-fns`.
>
> **URL API** : `VITE_API_BASE_URL` inclut le suffixe `/api/v1` (≠ mobile).
>
> **Authentification :** Login avec email + mot de passe → selon `niveauAcces` redirige vers :
> - `SUPER_ADMIN` → `/superadmin/dashboard`
> - `ADMIN` → `/admin/dashboard`
> - `CONSEILLER` → `/conseiller/dashboard`
> - 2FA : si requis, écran intermédiaire "Double authentification" avec code 6 chiffres.
>
> **Layout général (toutes pages internes) :**
> - **Sidebar gauche fixe** (~260 px) : logo Activ Education + navigation (groupée par section, indicateur de page active en bordure gauche + fond teinté).
> - **Header top** : breadcrumb / titre de page à gauche + notifications (cloche avec badge) + avatar utilisateur (menu déroulant : Mon profil, Déconnexion) à droite.
> - **Zone principale** : conteneur avec `max-width` + padding 24–32 px + grille responsive (1 col mobile, 2 cols tablette, 3–4 cols desktop).
> - **Cartes** : fond blanc, bordures subtiles, `rounded-[12px]`, padding 20–24 px.
> - **Boutons** : primaire (fond plein, texte blanc), secondaire (outline), danger.
> - **Champs de formulaire** : outline, label au-dessus, helper text en dessous, validation inline.
> - **Tables** : header en fond grisé, lignes hover, colonnes d'actions à droite (icônes Edit/Trash).
> - **Modals** : centrés, titre + corps + footer (boutons Annuler / Valider).
>
> **Rôles & accès :**
> - `CONSEILLER` : tableau de bord conseiller + FAQ + Messagerie + Rendez-vous + Profil + Utilisateurs (lecture) + Statistiques.
> - `ADMIN` : tout ce que conseiller + gestion des élèves / parents / conseillers / établissements / filières / métiers / séries / quiz / FAQ modération / notifications / seuils / matrices.
> - `SUPER_ADMIN` : tout ce qu'admin + gestion des administrateurs + logs d'audit + paramètres système.

---

## 1. Login (`pages/login/LoginPage.tsx`)

**Type :** Page publique (plein écran, sans sidebar).

**Layout :**
- **Centre écran** : carte 400 px max-width, fond blanc, `rounded-[12px]`, `shadow-lg`, padding 32 px.
- **Header carte :**
  - Cercle 64 px de diamètre, fond teinté, icône `GraduationCap` Lucide centrée.
  - Titre "Activ Education" (`text-2xl font-bold`).
  - Sous-titre "Backoffice — Connexion" (`text-sm text-text-secondary`).
- **Formulaire :**
  - Champ Email : `placeholder="ex: email@exemple.com"`, icône préfixe.
  - Champ Mot de passe : `placeholder="Mot de passe"`, suffixe œil (toggle visibilité).
  - Bandeau d'erreur si échec.
- **Bouton primaire pleine largeur** "Se connecter" (avec spinner pendant requête).
- **Lien** "Mot de passe oublié ?" en dessous.

**Variante 2FA (même page, state machine) :**
- Titre "Double authentification" + sous-titre "Entrez le code à 6 chiffres de votre application".
- Input OTP 6 chiffres unique (`placeholder="000000"`).
- 2 boutons : "Annuler" + "Vérifier".

---

## 2. Not Found (`pages/NotFoundPage.tsx`)

**Type :** Page publique plein écran.

**Layout :** Cercle icône + Titre "Page introuvable" + sous-texte "La page que vous cherchez n'existe pas." + Bouton "Retour à l'accueil".

---

## 3. Dashboards

### 3.1 Admin Dashboard (`pages/admin/AdminDashboard.tsx`)

**Layout :**
1. **PageHeader** : titre "Tableau de bord" + sous-titre "Vue d'ensemble de la plateforme".
2. **Grille de 4 KPI cards** (1 col mobile, 2 tablette, 4 desktop) :
   - Élèves actifs (icône `Users`, valeur en `text-2xl font-bold`).
   - Conseillers (icône `UserCheck`).
   - Quiz complétés (icône `ClipboardCheck`).
   - Fiches bibliothèque (icône `BookOpen`).
3. **Grille 2 colonnes** :
   - **Card large (2/3)** : "Activité (7 derniers jours)" + graphique Recharts `LineChart` (visites, inscriptions, quiz).
   - **Card (1/3)** : "Répartition des profils" + graphique Recharts `PieChart` (Élèves / Parents / Conseillers) + légende.
4. **Card large** : "Dernières fiches modifiées" + `Table` 5 dernières (titre + type + date + auteur).

### 3.2 Conseiller Dashboard (`pages/conseiller/ConseillerDashboard.tsx`)

**Layout :**
1. **Header** : "Bon retour, {Prénom} !" + sous-titre "Voici un aperçu de votre activité".
2. **Grille 4 KPI cards** :
   - "Rendez-vous aujourd'hui" (icône `Calendar`)
   - "Messages non lus" (icône `MessageCircle`)
   - "Taux de réponse" (icône `TrendingUp`)
   - "Conseillers disponibles" (icône `UserCheck`)
3. **Grille 2 colonnes :**
   - **Card "Rendez-vous du jour"** : liste verticale des RDV (avatar élève + nom + heure + bouton "Rejoindre" primaire). Si vide : "Aucun rendez-vous".
   - **Card "Messages récents"** : liste 5 derniers messages (nom + aperçu contenu + temps relatif via `date-fns`).
4. **Empty states** gérés avec messages explicites.

### 3.3 SuperAdmin Dashboard (`pages/superadmin/SuperAdminDashboard.tsx`)

**Layout :**
1. **Header** : "Tableau de bord Super Admin" + sous-titre "Vue d'ensemble de la plateforme".
2. **Card principale "Gestion des Administrateurs"** :
   - **Header card** : titre + sous-titre "Créez et gérez les comptes administrateurs de la plateforme" + bouton primaire "Nouvel administrateur".
   - **Liste de cards admins** (avatar + nom + email + `niveauAcces` chip coloré + 2 boutons icônes "Modifier" / "Supprimer").
   - **Pagination** (précédent / suivant avec flèches SVG).
3. **Modals :**
   - **"Nouvel administrateur"** : formulaire (Prénom, Nom, Email, Mot de passe, Niveau d'accès — dropdown SUPER_ADMIN/ADMIN, Téléphone).
   - **"Modifier l'administrateur"** : mêmes champs, mot de passe optionnel (`placeholder="Laissez vide pour un mot de passe par défaut"`).
   - **"Confirmer la suppression"** : dialog danger avec icône `AlertTriangle` + message + Annuler/Supprimer.

---

## 4. Admin — Gestion des utilisateurs

### 4.1 Élèves (`pages/admin/ElevesPage.tsx`)

**Layout :**
1. **PageHeader** "Gestion des étudiants" + bouton "Importer CSV" (ouvre un file picker) + bouton primaire "Nouvel élève".
2. **Barre de recherche** : `placeholder="Rechercher par nom ou email..."` + bouton clear.
3. **Table paginée** (colonnes) :
   - Avatar + Nom complet
   - Email
   - Téléphone
   - Niveau d'étude (chip)
   - Statut
   - Date d'inscription
   - Actions (boutons icônes : Voir détails, Modifier, Supprimer)
4. **Pagination** (10 par page).
5. **Modal "Détails de l'élève"** (slide-over ou modal) :
   - Header : avatar + nom + email
   - Boutons : "Modifier" + "Supprimer"
   - Sections : Informations personnelles, Parcours, Quiz, Rendez-vous, Recommandations.
6. **Modal "Nouvel élève"** : champs (Nom, Prénom, Email, Mot de passe `placeholder="Min. 8 caractères"`, Téléphone, Niveau d'étude `placeholder="Ex: Terminale, Licence 3"`, Date de naissance).
7. **Modal "Modifier"** : pré-rempli.
8. **Dialog suppression** confirmation.

### 4.2 Parents (`pages/admin/ParentsPage.tsx`)

**Layout :**
1. **PageHeader** "Gestion des parents" + bouton primaire "Nouveau parent".
2. **Barre de recherche** `placeholder="Rechercher par nom ou email..."`.
3. **Table** : Avatar+Nom · Email · Téléphone · Nb enfants · Actions.
4. **Modal "Détails du parent"** :
   - Header : nom + email + boutons Modifier/Supprimer
   - **Section "Enfants liés"** : liste avec bouton "Retirer l'enfant" par enfant (X) + bouton "Copier" trackingId + message "Copié !".
   - **Section "Ajouter un enfant"** : champ `placeholder="Tracking ID de l'élève"` + bouton + + message d'erreur si invalide.
5. **Modals "Nouveau parent" / "Modifier"** : mêmes champs que pour élève + champ enfants (multi-select via trackingIds).
6. **Dialog suppression**.

### 4.3 Conseillers (`pages/admin/ConseillersPage.tsx`)

**Layout :**
1. **PageHeader** "Gestion des conseillers" + bouton primaire "Nouveau conseiller".
2. **Barre de recherche** `placeholder="Rechercher un conseiller..."`.
3. **Grille de cards conseillers** (3 par ligne desktop) :
   - Header card : avatar + nom + email
   - Description (line-clamp-2)
   - Spécialités (chips)
   - Boutons "Voir détails" / "Modifier" / "Supprimer"
4. **Pagination** avec flèches SVG.
5. **Modal "Détails"** (lecture seule).
6. **Modals "Nouveau conseiller" / "Modifier"** : Nom, Prénom, Email, Mot de passe, Téléphone, Spécialités (chips multi), Qualifications `placeholder="Ex: Master en psychologie, Certifié"`, Biographie.
7. **Dialog suppression**.

### 4.4 Admin — Mon Profil (`pages/admin/AdminProfilPage.tsx`)

**Layout :**
1. **PageHeader** "Mon Profil" + sous-titre "Informations de votre compte administrateur".
2. **Card profil** :
   - Header card : grand avatar + nom complet + bouton "Modifier" (mode édition inline ou modal).
   - **InfoRow** (icône + label + valeur) :
     - `User` Nom
     - `Mail` Email
     - `Phone` Téléphone (ou "Non renseigné")
     - `Shield` Niveau d'accès (`SUPER_ADMIN` / `ADMIN` / `MODERATEUR`)
     - `Calendar` Inscrit le (date localisée fr-FR)
3. **Skeleton** pendant chargement (`SkeletonList rows={5}`).

---

## 5. Admin — Bibliothèque (Catalogue d'orientation)

> **Pattern commun à toutes les pages Bibliothèque :** PageHeader + bouton "Nouvel X" + barre recherche + Table/Card list + Modals (Détails / Création / Édition / Suppression).

### 5.1 Établissements (`pages/admin/EtablissementsPage.tsx`)

**Layout :**
1. **PageHeader** "Gestion des établissements" + bouton "Nouvel établissement".
2. **Barre de recherche** `placeholder="Rechercher un établissement..."` + chips filtres (Ville, Type, Niveau).
3. **Cards/Table paginée** (10 par page) : Titre · Ville · Type · Niveau.
4. **Modal "Détails"** :
   - Header : titre + résumé
   - Sections : Localisation, Contacts, Offre de formation, Filières proposées, Médias (galerie)
   - **Section "Ajouter des médias"** : file picker + bouton "Ajouter"
   - Boutons "Modifier" / "Supprimer"
5. **Modal "Nouvel établissement"** : Titre, Résumé, Type (Public/Privé), Ville, Adresse, Téléphone/Email `placeholder="Téléphone / Email"`, Site web `placeholder="https://example.tg"`, Offre de formation, Filières (multi), Contacts, Photo (upload).

### 5.2 Filières (`pages/admin/FilieresPage.tsx`)

**Layout :**
1. **PageHeader** "Gestion des filières" + bouton "Nouvelle filière".
2. **Recherche** `placeholder="Rechercher une filière..."`.
3. **Cards/Table** : Titre · Domaine · Durée.
4. **Modal "Détails"** : titre + résumé + sections (Programme, Compétences, Débouchés, Établissements).
5. **Modal "Nouvelle filière / Modifier"** : Titre, Résumé, Domaine (dropdown depuis set dynamique), Durée `placeholder="Ex: 3 ans"`, Programme, Débouchés.

### 5.3 Métiers (`pages/admin/MetiersPage.tsx`)

**Layout :**
1. **PageHeader** "Gestion des métiers" + bouton "Nouveau métier".
2. **Recherche** `placeholder="Rechercher un métier..."` + chip filtre Secteur.
3. **Cards/Table** : Titre · Secteur · Salaire.
4. **Modal "Détails"** : titre + résumé + sections.
5. **Modal "Nouveau / Modifier"** : Titre, Résumé, Secteur, Missions, Compétences, Salaire `placeholder="Ex: 200 000 - 500 000 FCFA"`, Débouchés Togo.

### 5.4 Séries (`pages/admin/SeriesPage.tsx`)

**Layout :**
1. **PageHeader** "Gestion des séries" + bouton "Nouvelle série".
2. **Recherche** `placeholder="Rechercher une série..."` + chip filtre Niveau.
3. **Cards/Table** : Titre · Niveau.
4. **Modal "Détails"** : titre + résumé + sections (Matières, Débouchés).
5. **Modal "Nouvelle / Modifier"** : Titre, Résumé, Niveau, Matières principales, Débouchés.

---

## 6. Admin — Diagnostic & Algorithme

### 6.1 Quiz — Liste (`pages/admin/QuizPage.tsx`)

**Layout :**
1. **PageHeader** "Gestion des quiz" + bouton "Nouveau quiz".
2. **Table** : Titre · Nombre de questions · Statut (Publié/Brouillon) · Actions.
3. **Modal "Nouveau quiz"** : Titre `placeholder="Ex: Quiz d'orientation"`, Description, Type (RIASEC/Carrière/Aptitudes), Nombre de questions.
4. **Dialog suppression**.

### 6.2 Quiz Editor (`pages/admin/QuizEditorPage.tsx` + `QuizEditorPageForm.tsx`)

**Layout :**
1. **Header** : titre du quiz + sous-titre "Édition des questions" + bouton "Enregistrer" + bouton "Publier".
2. **Layout 2 colonnes :**
   - **Sidebar gauche (1/3)** : "Questions" + bouton "Ajouter une question" + liste verticale des questions (numéro + libellé + icône drag + actions). Empty state : "Aucune question — Ajoutez votre première question pour commencer".
   - **Zone principale (2/3)** : `QuizEditorPageForm` :
     - Titre "Question"
     - Champ "Énoncé" `placeholder="Saisissez votre question..."`
     - Champ "Tags" `placeholder="science, biologie, ADN"` (CSV)
     - Section "Options de réponse" : `placeholder="Option..."` × 4 + bouton "Ajouter une option" + radio pour marquer la bonne réponse
     - Empty state : "Aucune option de réponse — Ajoutez au moins 2 options"
     - Bouton "Sauvegarder la question"
3. **État non chargé** : spinner + "Chargement des questions...".

### 6.3 Seuils d'admission (`pages/admin/SeuilsPage.tsx`)

**Layout :**
1. **PageHeader** "Seuils d'admission" + bouton "Nouveau seuil".
2. **Filtre** `placeholder="Filtrer par trackingId de filière…"` + bouton clear.
3. **Table** colonnes : Filière · Série · Note minimale · Actions.
4. **Empty state** : "Aucun seuil d'admission configuré".
5. **Modal "Nouveau seuil" / "Modifier le seuil"** : Filière (dropdown), Série (dropdown conditionnel), Note minimale (number), Description.

### 6.4 Matrices de scores (`pages/admin/ScoreMatricesPage.tsx`)

**Layout :**
1. **PageHeader** "Matrices de scores" + bouton "Nouvelle matrice".
2. **Table** colonnes : Titre · Goûts personnels (0–100) · Académique (0–100) · Marché du travail (0–100) · Total estimé · Actions.
3. **Empty state** : "Aucune matrice de score configurée".
4. **Modal "Nouvelle matrice" / "Modifier la matrice"** (taille `sm`) :
   - Titre (input, validation `trim().length > 0`)
   - 3 sliders/number inputs : Goûts personnels, Académique, Marché du travail (somme = 100)
   - Bouton "Enregistrer" (désactivé si titre vide ou en cours de mutation).

---

## 7. Admin — FAQ & Modération

### 7.1 FAQ Modération (`pages/admin/FAQModerationPage.tsx`)

**Layout :**
1. **PageHeader** "Modération FAQ" + bouton "Nouvelle FAQ" + bouton "Tout valider" (batch).
2. **Tabs** : `En attente` · `Publiée` · `Rejetée` · `Toutes`.
3. **Sous-titre contextuel** par tab (ex: "FAQ en attente de modération").
4. **Cards FAQ** (3 par ligne) :
   - Question (titre, line-clamp-2)
   - Réponse (line-clamp-3)
   - **Boutons d'action par card** : ✓ Valider · ✗ Rejeter · ✏️ Modifier
   - Bouton "Voir détails" (modal)
5. **Pagination** avec flèches.
6. **Modal "Nouvelle FAQ"** : Question, Réponse, Catégorie, Tags.

### 7.2 Notifications (`pages/admin/NotificationsPage.tsx`)

**Layout :**
1. **PageHeader** "Notifications{ X non lues}" + bouton "Envoyer une notification" + bouton "Tout marquer comme lu".
2. **Tabs** (optionnel) : Toutes · Non lues · Envoyées.
3. **Liste de notifications** : icône colorée par type (MESSAGE / DIAGNOSTIC-QUIZ / RDV / RECOMMANDATION / DEFAULT) + titre + sous-texte (line-clamp-2) + temps relatif ("À l'instant" / "Il y a 5m" / "Il y a 2h" / "Il y a 3j" / "12 mar") + boutons (Marquer lu, Supprimer).
4. **Empty state** : "Aucune notification" + sous-texte.
5. **Modal "Envoyer une notification"** :
   - Champ Destinataire `placeholder="Tracking ID du destinataire"`
   - Champ Titre `placeholder="Titre de la notification"`
   - Champ Contenu `placeholder="Contenu du message"` (textarea).
   - Dropdown Type (INFO / SUCCESS / WARNING / ERROR).
   - Bouton "Envoyer".

---

## 8. Admin — Statistiques (`pages/admin/AdminStatistiquesPage.tsx`)

**Layout :**
1. **PageHeader** "Statistiques" + sélecteur de période (7j / 30j / 90j / 1an).
2. **Grille 4 KPI** : Élèves · Conseillers · Quiz · Quiz complétés.
3. **Grille 2 colonnes :**
   - **Card large "Inscriptions (30 derniers jours)"** : `AreaChart` Recharts.
   - **Card "Répartition par rôle"** : `PieChart`.
4. **Card "Évolution mensuelle"** : `LineChart` (Recharts).
5. **Card "Activité récente"** : tableau résumé.

---

## 9. Conseiller — Pages spécifiques

### 9.1 FAQ (`pages/conseiller/FAQPage.tsx`)

**Layout :**
1. **PageHeader** "FAQ" + description "Consultez les questions fréquemment posées".
2. **Barre de recherche** `placeholder="Rechercher une question..."` + chips catégories (TOUS + catégories) + bouton "Nouvelle FAQ" (visible si autorisé).
3. **Accordéon FAQ** : card par question, click pour expand (toggle), affiche la réponse.
4. **Empty state** : "Aucune question trouvée".

### 9.2 Messagerie (`pages/conseiller/MessagesPage.tsx`)

**Type :** Messagerie split-view (comme WhatsApp Web).

**Layout 2 colonnes (responsive : 1 col mobile, 2 cols desktop) :**
- **Sidebar gauche (360 px) :**
  - Barre de recherche `placeholder="Rechercher..."`
  - Liste de contacts (avatar + nom + aperçu + temps relatif via `date-fns` format).
  - Empty state : "Aucune conversation" + bouton "Nouvelle conversation".
- **Zone principale (droite) :**
  - Si conversation sélectionnée : header (avatar + nom + trackingId) + zone messages scrollable (bulles user/contact, dates, bouton "Supprimer" sur chaque message) + footer (input + bouton send `placeholder="Écrivez votre message..."`).
  - Si non : état vide avec titre "Messagerie" + sous-texte + bouton "Démarrer une conversation".
- **Modals** : confirmation suppression message.

### 9.3 ORIA (`pages/conseiller/OriaPage.tsx`)

**Type :** Chat avec l'IA ORIA (côté backoffice conseiller).

**Layout :**
1. **Header** : "ORIA — Assistant IA d'orientation".
2. **Bouton "Nouvelle conversation"** (top right).
3. **Zone messages** : bulles user (droite) + bulles assistant (gauche, avec avatar ORIA).
4. **Barre de saisie (bas)** : `placeholder="Pose ta question à ORIA..."` + bouton send (avec loader si `sending`).
5. **Bouton désactivé** si input vide ou envoi en cours.

### 9.4 Profil Conseiller (`pages/conseiller/ProfilPage.tsx`)

**Layout :**
1. **PageHeader** "Mon Profil".
2. **Card identité** : grand avatar + nom complet + sous-titre "Conseiller d'orientation".
3. **Card "Informations générales"** (3 colonnes ou stack) : Email, Téléphone, Membre depuis.
4. **Card "Spécialités"** : chips (si renseigné) + "Aucune spécialité renseignée".
5. **Card "Qualifications"** : liste ou texte.
6. **Card "Biographie"** : paragraphe (corps de texte).
7. **Card "Années d'expérience"** : valeur + label.
8. **Empty states** sur chaque card si non renseigné.

### 9.5 Rendez-vous Conseiller (`pages/conseiller/RendezVousPage.tsx`)

**Layout :**
1. **PageHeader** "Rendez-vous" + description "Gérez vos rendez-vous" + bouton primaire "Nouveau RDV".
2. **Tabs / filtres** : À venir · Aujourd'hui · Passés.
3. **Liste de cards RDV** (avatar élève + nom + date + heure + type [VISIO / TÉLÉPHONE / PRÉSENTIEL] + statut) :
   - Boutons actions : "Voir détails" (icône œil), "Marquer terminé" (icône check), "Annuler" (icône X).
4. **Modal "Détails du rendez-vous"** (lecture seule) :
   - Sections : Date & Heure · Statut (chip coloré) · Élève (trackingId) · Type (chip) · Notes · Lien Visio (avec boutons "Copier" + "Rejoindre").
5. **Modal "Nouveau RDV"** : champs (Élève, Date, Heure, Type, Notes, Lien Visio).
6. **Dialog suppression**.

### 9.6 Statistiques Conseiller (`pages/conseiller/StatistiquesPage.tsx`)

**Layout :**
1. **PageHeader** "Statistiques" + description "Suivez votre activité et vos performances" + sélecteur de période.
2. **Grille 4 KPI** : Total élèves · Total rendez-vous · Taux de complétion · Conseillers actifs.
3. **Card "Rendez-vous mensuels"** : `BarChart` Recharts.
4. **Card "Activité récente"** : liste (avatar + nom + action + temps).
5. **Empty state** : "Aucune donnée disponible" / "Aucune activité récente".

### 9.7 Utilisateurs (`pages/conseiller/UtilisateursPage.tsx`)

**Layout :**
1. **PageHeader** "Utilisateurs" + 2 boutons : "Importer élèves" / "Importer parents" (file pickers CSV).
2. **Barre de recherche** `placeholder="Rechercher par nom, prénom ou email..."`.
3. **Layout 2 colonnes :**
   - **Colonne "Élèves"** : liste cards (avatar + nom + email + bouton "Voir détails").
   - **Colonne "Parents"** : liste cards (avatar + nom + email + bouton "Voir détails").
4. **Empty states** par colonne.
5. **Modal "Détails de l'élève"** (lecture seule) :
   - Header : nom + email
   - Sections : Téléphone · Niveau d'étude · Type d'apprenant · Établissement · Filière.
6. **Modal "Détails du parent"** (lecture seule) : mêmes sections.

---

## 10. Super Admin — Pages exclusives

### 10.1 Logs / Audit (`pages/superadmin/LogsPage.tsx`)

**Layout :**
1. **PageHeader** "Journaux d'Audit" + description "Suivi des activités et événements système".
2. **Barre de recherche** `placeholder="Rechercher..."` + filtres (Niveau, Date, Utilisateur).
3. **Section "Actions rapides"** (3 cards) :
   - **Card "Rapport PDF Hebdomadaire"** + bouton "Générer".
   - **Card "Export CSV (Raw Data)"** + bouton "Exporter".
   - **Card "Nettoyage Automatique"** + sous-texte "Supprime les logs de plus de 90 jours" + bouton "Nettoyer".
4. **Table logs** colonnes : Date/Heure · Utilisateur · Action · Niveau (badge coloré INFO/WARN/ERROR via `StatusBadge`) · Détails · IP.
5. **Pagination** (50 par page).
6. **Filtre date range** (DatePicker).

### 10.2 Paramètres système (`pages/superadmin/ParametresPage.tsx`)

**Layout :**
1. **PageHeader** "Paramètres" + description "Configuration globale de la plateforme".
2. **Tabs / sections :**
   - **"Permissions des rôles"** (badge "Données de démonstration" ambre) : table (Rôle × Permission), grille de cases à cocher. **Note : données mockées actuellement.**
   - **"Mode Maintenance"** :
     - Card "État actuel" : toggle ON/OFF + bouton "Activer" / "Désactiver".
     - Card "Message personnalisé" : textarea `placeholder="Entrez le message à afficher aux utilisateurs..."` + helper text + bouton "Enregistrer".
   - **"Intégrité Système"** (badge "Données de démonstration") :
     - Card "100% SÉCURISÉ — Aucune vulnérabilité détectée".
     - Liste de services (label + value) : PostgreSQL, Redis, MinIO, etc.
     - Card "Tests d'intrusion" + description.
   - **"Poids des quiz et recommandations"** (paramètres `RECOMMENDATION`) :
     - Liste de paramètres avec description, valeur actuelle, bouton "Modifier".
3. **Card "Besoin d'aide ?"** en bas de page.

---

## 11. Composants UI transverses (référence)

> Toutes les pages utilisent ces composants partagés (cf. `src/components/`) :

- **`PageHeader`** : titre + description + slot `actions` (boutons à droite).
- **`Modal`** : props `open`, `onClose`, `title`, `size` (`sm` / `md` / `lg`).
- **`DataTable`** : columns, data, pagination, actions par ligne.
- **`StatusBadge`** : `status` → couleur (success / warning / danger / info / neutral).
- **`EmptyState`** : icon + title + subtitle + action button.
- **`SkeletonList`** : rows de skeleton pour loading.
- **`InfoRow`** : icon + label + value (utilisé dans les profils).

---

## Notes pour le maquettiste — À COMPLÉTER PAR VOS SOINS

Les éléments suivants sont **à fournir** (non spécifiés dans le code, laissés à votre appréciation) :

### 🎨 Palette de couleurs
- **`primary`** + déclinaisons `primary-light` (fond 10 %), `primary-dark` (hover) — *à définir*
- **`secondary`**, **`accent`** — *à définir*
- **`success`**, **`danger`**, **`warning`**, **`info`** — *à définir*
- **`text-main`** (presque noir), **`text-secondary`** (gris foncé), **`text-light`** (gris clair) — *à définir*
- **`border`** (gris très clair pour bordures) — *à définir*
- **`card`** (blanc), **`background`** (gris très clair / beige) — *à définir*
- **`danger-light`** (rouge pâle pour bandeaux d'erreur) — *à définir*

### 🔤 Typographie
- Police principale (Tailwind par défaut, Inter ou sans-serif système) — *à définir*

### 🖼️ Assets visuels
- **[LOGO_ACTIV_EDUCATION]** — à insérer dans la sidebar (top) et sur la page Login
- **Favicon** — *à fournir*
- **Placeholder avatar utilisateur** (cercle avec initiales) — *à styliser*
- **Vignettes illustrations** pour les états vides (empty states) — *à fournir si besoin*

### 📐 Conventions de design
- Responsive : `grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4` pour KPI, `grid-cols-1 lg:grid-cols-2` pour le reste
- **Icônes** : librairie `lucide-react` (exemples : `GraduationCap`, `Users`, `UserCheck`, `Calendar`, `MessageCircle`, `TrendingUp`, `BookOpen`, `Mail`, `Phone`, `Shield`, `AlertTriangle`, `User`, `ClipboardCheck`)
- **Microcopy** : français, vouvoiement (administratif), ton professionnel
- **Densité** : moins aéré que le mobile (interface pro), mais aération correcte (24–32 px padding)
- **Tables** : prévoir skeleton rows, état vide, hover lignes, pagination bas
- **Modals** : taille `sm` pour les formulaires simples, `md`/`lg` pour les détails riches
- **Toast / SnackBar** : pour confirmations (succès en vert, erreur en rouge)
- **Charts** : Recharts (LineChart, BarChart, AreaChart, PieChart) avec couleurs de la palette
- **Sidebar active state** : bordure gauche 3 px couleur primaire + fond teinté + texte couleur primaire
- **Pas de mode sombre** (info projet)
