# SPÉCIFICATIONS UI/UX — Maquettes Banai.cio

## 1. PLATEFORME BACKOFFICE (React 19 + Tailwind CSS v4)

### Palette de couleurs exacte (index.css)

Primaire : #3730E8 (indigo). Primaire foncé : #2a25b0. Primaire clair : #e8e7ff.
Secondaire : #F59E0B (ambre). Secondaire clair : #fef3c7.
Succès : #10B981 (émeraude). Succès clair : #d1fae5.
Danger : #EF4444 (rouge). Danger clair : #fee2e2.
Fond page : #F9FAFB (gris clair). Cartes : #FFFFFF.
Bordures : #E5E7EB. Texte principal : #111827 (gris foncé). Texte secondaire : #6B7280 (gris moyen).
Sidebar : 240px fixe. Rayon des cartes : 12px (rounded-[12px]).
Police : Inter, system-ui, sans-serif.

### Layout général

Barre latérale gauche (sidebar) : largeur 240px, fixed, hauteur 100vh, fond #3730E8, z-index 30. Zone de contenu principal : padding 24px 28px, overflow-y scroll.

### Sidebar

Haut de la sidebar : logo centré (hauteur 40px — l'utilisateur ajoutera le logo), titre "Activ Education" en blanc bold, sous-titre "Espace {rôle}" en blanc opacité 60, taille 12px.

Navigation : groupes séparés par des bordures border-t border-white/10. Les libellés de groupes sont en uppercase, 10px, blanc opacité 40.
Chaque item de navigation : icône Lucide 16px, label, chevron droit. Item actif = fond bg-white/15, texte blanc. Item inactif = texte blanc opacité 70, hover fond bg-white/10.

Bas de la sidebar : avatar avec initiales (carré 36px, fond blanc opacité 20), nom, rôle, bouton Déconnexion (icône LogOut).

### Composants communs

**DataTable** : élément `<table>` dans une carte bg-card rounded-[12px] border border-border. En-tête de tableau (thead) fond bg-gray-50/50, labels en text-xs font-semibold text-text-secondary uppercase. Corps (tbody) avec divide-y divide-border. Lignes de chargement (SkeletonRow) avec animate-pulse et 6 largeurs alternées. Pagination avec icônes ChevronLeft/ChevronRight et numéro de page. État vide : icône Inbox avec texte.

**Modal** : overlay fond noir opacité 30. Carte blanche max-w-lg ou max-w-2xl, rounded-[12px], padding 24px. Titre et bouton X (fermeture). Trois tailles disponibles : sm, md, lg. Fermeture par touche Escape et clic sur le fond.

**StatCard** : carte avec icône ronde (fond à 10% de la couleur), valeur en texte 2xl font-bold, titre en text-sm text-text-secondary, tendance optionnelle avec flèche (ArrowUp/ArrowDown) et label. Cinq couleurs disponibles : primary (indigo), secondary (ambre), success (vert), danger (rouge), blue (bleu).

**StatusBadge** : inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium. Mapping des statuts : PUBLIE, VALIDE, SUCCES → fond vert ; BROUILLON, NORMAL → fond gris ; EN_ATTENTE, REVISION → fond ambre ; ERREUR, URGENT → fond rouge ; AVERTISSEMENT → fond jaune.

**PageHeader** : carte bg-card rounded-[12px] border border-border padding 20px 24px. Titre en text-xl font-semibold, description en text-text-secondary text-sm, boutons d'action optionnels.

**Skeleton** : SkeletonRow (h-4 bg-gray-200 animate-pulse), SkeletonCard (pour les cartes), SkeletonList (liste de lignes).

**UserAvatar** : initiales (1 à 2 caractères) dans un cercle. Couleur basée sur un hash (8 couleurs possibles). Trois tailles : sm (32px), md (40px), lg (64px). Supporte URL d'image optionnelle. Point vert optionnel pour le statut en ligne.

**KpiCardGrid** : grille responsive grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4. Contient des StatCards.

**ProtectedRoute** : vérifie l'authentification (isAuthenticated) et le rôle utilisateur (CONSEILLER, ADMIN, SUPER_ADMIN). Redirige vers /login si non authentifié. Affiche "Accès refusé" en cas de rôle insuffisant.

**RichTextEditor** : éditeur WYSIWYG basé sur contentEditable avec barre d'outils (gras, italique, souligné, listes ordonnées et non ordonnées, titres H1-H3, liens). Composant existant mais non utilisé par aucune page actuelle.

---

## PAGES BACKOFFICE — 29 pages

---

### LoginPage (/login)

Fond de page : bg-gradient-to-br from-primary/5 via-background to-primary/10. Carte centrée max-w-md, bg-card, rounded-[12px], shadow, padding 32px.
Logo en haut (hauteur 56px, centré, mx-auto). Titre "Activ Education", sous-titre "Backoffice — Connexion".

Étape 1 — formulaire de connexion : champ email avec icône Mail, champ mot de passe avec icône Lock, bouton "Se connecter" avec icône LogIn. Messages d'erreur affichés dans un bloc bg-danger-light, texte text-danger, bordure border-danger/20.

Étape 2 — double authentification TOTP (si activée) : icône Shield dans un cercle fond primary/10. Texte "Double authentification". Champ de saisie centré, texte 2xl, tracking 8px entre les caractères, placeholder "000000", maxLength 6, inputMode numeric. Bouton "Vérifier". Lien "Retour" pour revenir à l'étape 1.

---

### Pages Conseiller (10 routes)

---

#### ConseillerDashboard (/conseiller/dashboard)

En-tête : "Bon retour, {prenom}" et "Voici un aperçu de votre activité".

Zone de statistiques : grille responsive de 4 StatCards (sm:grid-cols-2 lg:grid-cols-4). Cartes :
- Rendez-vous aujourd'hui (icône Calendar, couleur primary)
- Messages non lus (icône MessageSquare, couleur secondary)
- Taux de réponse (icône TrendingUp, couleur success)
- Conseillers disponibles (icône Users, couleur blue)

Zone split deux colonnes (lg:grid-cols-2) :
- Colonne gauche : carte "Rendez-vous du jour". Si vide, message "Aucun rendez-vous prévu aujourd'hui". Sinon, liste d'éléments avec heure (text-primary), identifiant élève, StatusBadge, bouton "Démarrer".
- Colonne droite : carte "Messages récents". Si vide, message "Aucun message récent". Sinon, liste d'éléments avec UserAvatar, expéditeur, contenu tronqué, timestamp relatif (formatDistanceToNow), point rouge pour les messages non lus.

État de chargement : SkeletonCard dans la grille et SkeletonList dans les colonnes.

---

#### MessagesPage (/conseiller/messages)

Layout split horizontal : liste des conversations à gauche (largeur 360px) et zone de chat à droite.

Panneau gauche : en-tête avec le nombre de conversations et de messages non lus. Barre de recherche. Liste des conversations avec UserAvatar, identifiant du contact tronqué, dernier message tronqué, timestamp, badge indiquant le nombre de messages non lus. Bouton "Nouveau message" en bas.

Panneau droit : en-tête avec le nom du contact. Historique des messages sous forme de bulles (messages reçus alignés à gauche, fond gris ; messages envoyés alignés à droite, fond primary/10). Timestamps affichés sous chaque bulle. Coches de lecture pour les messages envoyés. Barre de saisie en bas avec champ texte et bouton d'envoi (icône Send, touche Entrée pour envoyer). État vide affiché si aucune conversation sélectionnée.

Modal Nouveau Message : champs pour l'identifiant du destinataire et le texte du message.

Rafraîchissement automatique : conversations toutes les 5 secondes, compteur de non-lus toutes les 10 secondes.

---

#### RendezVousPage (/conseiller/rendez-vous)

PageHeader "Rendez-vous". DataTable avec colonnes : Date/Heure, Élève, Type (Visio/Téléphone/Présentiel avec icônes), Statut (StatusBadge), Actions (Détail, Terminer, Annuler).

Filtres horizontaux sous forme de pills : Tous, Planifié, Confirmé, Terminé, Annulé.

Modal Détail : informations du rendez-vous (date, statut, élève, type, notes, lien Visio). Boutons d'action : "Marquer terminé", "Annuler".

---

#### UtilisateursPage (/conseiller/utilisateurs)

PageHeader "Utilisateurs". Switcher d'onglets : Élèves (icône GraduationCap) et Parents (icône Users).

Barre de recherche.

Affichage en grille de 3 colonnes de cartes. Chaque carte contient : UserAvatar, nom, email, téléphone, information supplémentaire (niveau d'étude pour les élèves, nombre d'enfants pour les parents).

Modal Détail Élève : avatar, nom, trackingId, email, téléphone, niveauEtude, typeApprenant, établissement, filière.

Modal Détail Parent : avatar, nom, trackingId, email, téléphone, liste des enfants.

---

#### OriaPage (/conseiller/oria)

En-tête : avatar ORIA (icône Bot dans un cercle bleu), titre "ORIA", bouton "Nouvelle conversation" qui efface la session en cours.

Zone des messages : messages de l'utilisateur alignés à droite en bleu, messages de l'IA alignés à gauche avec avatar Bot. Spinner de chargement pendant les réponses.

Barre de saisie en bas : champ texte et bouton d'envoi. Touche Entrée déclenche l'envoi.

API utilisée : POST /oria/message, DELETE /oria/session/:id.

---

#### FAQPage (/conseiller/faq)

PageHeader "FAQ". Barre de recherche et catégories affichées en pills.

Accordéon : chaque élément affiche la question, la catégorie et un chevron. Au clic, la réponse se déploie.

---

#### ProfilPage (/conseiller/profil)

PageHeader "Mon Profil".

Carte profil à gauche : UserAvatar en large, nom complet, titre "Conseiller d'orientation", badge "Actif".

Section d'informations à droite organisée en cartes distinctes :
- "Informations générales" : email (icône Mail), téléphone (icône Phone), membre depuis (icône Calendar)
- "Spécialités" : tags colorés
- "Qualifications" : liste à puces
- "Biographie" : texte
- "Années d'expérience"

---

#### StatistiquesPage (/conseiller/statistiques)

PageHeader "Statistiques".

Grille de 4 StatCards : Total élèves (icône Users), Total rendez-vous (icône Briefcase), Taux de complétion (icône ClipboardList), Conseillers actifs (icône TrendingUp, couleur blue).

Graphique en barres (BarChart Recharts) "Rendez-vous mensuels" : deux séries (Total et Terminés) par mois.

Carte "Activité récente" : liste des 5 derniers rendez-vous avec StatusBadge.

---

#### NotificationsPage (/conseiller/notifications)

Même composant que NotificationsPage de l'administrateur (décrit ci-dessous).

---

### Pages Admin (19 routes dont 17 composants uniques)

---

#### AdminDashboard (/admin/dashboard)

PageHeader "Tableau de bord" / "Vue d'ensemble".

KpiCardGrid avec 5 StatCards : Total Élèves (icône GraduationCap), Total Parents (icône Users), Conseillers (icône Briefcase), Quiz (icône FileQuestion), Fiches (icône BookOpen).

Graphique linéaire (LineChart Recharts) "Activité (7 derniers jours)" avec deux courbes : Inscriptions en #3730E8 et Quiz complétés en #10B981.

Graphique en donut (PieChart Recharts, innerRadius=55, outerRadius=80) "Répartition des profils" : Écolier en violet, Collégien en indigo, Lycéen en ambre, Étudiant en vert, Professionnel en rouge, Autre en gris.

Tableau manuel "Dernières fiches modifiées" : colonnes Titre, Type, Modifiée le, Statut (StatusBadge Publié/Brouillon).

---

#### ElevesPage (/admin/eleves)

PageHeader "Étudiants". Barre de recherche. Boutons d'action : "Ajouter" (icône UserPlus), "Exporter CSV" (icône Download), "Importer CSV" (icône Upload, input fichier caché).

DataTable avec colonnes : Avatar (UserAvatar), Nom, Prénom, Email, Téléphone, Type (typeApprenant), Niveau, Établissement, Actions (icône Pencil pour modifier).

Modal Détail (taille lg) : informations affichées en grille, boutons Modifier et Supprimer.

Modal Création (taille lg) : champs nom, prénom, email, téléphone, motDePasse, niveauEtude, typeApprenant (select avec options AUTRE, COLLEGIEN, LYCEEN, PROFESSIONNEL, ECOLIER, ETUDIANT), etablissementActuel, filière.

Modal Édition (taille lg) : mêmes champs que la création, motDePasse en optionnel.

---

#### ParentsPage (/admin/parents)

DataTable : Avatar, Nom, Prénom, Email, Téléphone, Enfants (nombre), Actions (icône Pencil, icône Trash2).

Modal Détail : informations du parent et liste des enfants avec boutons pour lier ou délier un enfant.

Modal Création et Édition : nom, prénom, email, téléphone, motDePasse.

Modal Confirmation Suppression (taille sm) : icône AlertTriangle, message de confirmation.

---

#### ConseillersPage (/admin/conseillers) — PAS de DataTable

PageHeader "Gestion des conseillers". Barre de recherche.

Grille de 3 colonnes de cartes. Chaque carte contient : UserAvatar avec dot de statut en ligne, nom, email, spécialités en tags, qualifications en tags, années d'expérience en tag. Boutons d'action : Modifier (icône Pencil), Désactiver.

Modal Création et Édition (taille lg) : nom, prénom, email, téléphone, motDePasse, spécialités, biographie, qualifications, années d'expérience. En cas de succès, affichage du trackingId avec bouton de copie.

Modal Confirmation Désactivation (taille sm).

---

#### FilieresPage (/admin/filieres)

DataTable : Titre, Domaine, Établissements (nombre), Statut (StatusBadge), Actions (Publier/Dépublier, icône Pencil).

Barre de recherche et filtre par domaine (select).

Modal Création et Édition (taille lg) : titre, résumé, domaine, durée, niveauRequis, conditionsAdmission, programme, débouchésMetiers, estPublie (checkbox).

---

#### MetiersPage (/admin/metiers)

DataTable : Métier (titre), Secteur, Statut (StatusBadge), Actions (Publier/Dépublier, icône Pencil).

Barre de recherche et filtre par secteur (select, 13 options).

Modal Création et Édition (taille lg) : titre, résumé, secteur, missions, compétences, formationsAccès, débouchésTogo, fourchetteSalaire, estPublie (checkbox).

---

#### SeriesPage (/admin/series)

DataTable : Série (titre), Niveau, Statut (StatusBadge), Actions (Publier/Dépublier, icône Pencil).

Barre de recherche et filtre par niveau (select).

Modal Création et Édition (taille lg) : titre, résumé, niveau (select avec options Bac, Licence, Master, Doctorat, Secondaire), matièresPrincipales, coefficients, débouchés, estPublie (checkbox).

---

#### EtablissementsPage (/admin/etablissements)

DataTable : Établissement (titre), Ville, Niveau, Type, Statut (StatusBadge), Médias (icônes), Actions (Publier/Dépublier, icône Pencil).

Barre de recherche et filtres par ville, niveau et type (selects).

Upload de fichiers (images et vidéos) avec prévisualisation.

Modal Création et Édition (taille lg) : titre, résumé, ville (select 6 options), niveau (select 7 options), typeEtablissement (select 7 options), adresse, contacts, siteWeb, contenu, offreFormation, estPublic (checkbox), estPublie (checkbox).

---

#### QuizPage (/admin/quiz)

DataTable : Titre, Description (line-clamp 2), Questions (nombre), Statut (StatusBadge), Actions (Éditer avec navigation vers l'éditeur, icône Trash2 pour supprimer).

Modal Création : titre et description du quiz.

Modal Confirmation Suppression (taille sm) : icône AlertTriangle.

---

#### QuizEditorPage (/admin/quiz/:id/edit)

Barre supérieure : bouton retour avec icône ArrowLeft (navigation vers /admin/quiz), titre du quiz, nombre de questions.

Layout deux colonnes (grid-cols-[320px_1fr]) :
- Colonne gauche (320px) : titre "Questions", bouton "+" Ajouter. Liste numérotée des questions avec icône GripVertical (indicateur de glisser-déposer). Question sélectionnée avec fond primary/10.
- Colonne droite : formulaire QuestionForm pour la question sélectionnée. Si aucune question sélectionnée, texte "Sélectionnez une question" avec icône ArrowLeft.

QuestionForm (composant QuizEditorPageForm.tsx) : champs texteQuestion (textarea), typeQuestion (select avec options RIASEC, CONNAISSANCE, INTERET, PERSONNALITE), domaine (select avec 9 domaines), difficulté (boutons 1 à 5), niveauCible (select avec options Tous, Écolier, Collégien, Lycéen, Étudiant, Professionnel), tags (input texte). Section Options : chaque option avec champ texte, select RIASEC category (options R, I, A, S, E, C, NON_RENSEIGNE), bouton de suppression. Bouton "Ajouter une option".

---

#### FAQModerationPage (/admin/faq) — PAS de DataTable

PageHeader "Modération FAQ" et bouton "Nouvelle FAQ".

Switcher d'onglets : En attente, Publiée, Refusée.

Grille de 2 colonnes de cartes. Chaque carte contient : question, réponse (line-clamp 2), StatusBadge, catégorie (pill), nombre de vues, votes (pouces 👍/👎), boutons d'action (Publier, Dépublier, Refuser, Supprimer).

Pagination manuelle.

Modal Création : question, réponse, catégorie (select), estPublie (checkbox).

---

#### SeuilsPage (/admin/seuils) — PAS de DataTable

PageHeader "Seuils d'admission". Champ de recherche par filière (filiereTrackingId).

Tableau HTML manuel : colonnes Filière, Matière, Seuil minimum, Actions (Modifier icône Pencil, Supprimer icône Trash2).

Modal Édition (taille sm) : nom de la filière en lecture seule, matière en lecture seule, noteMinimum (input 0 à 20, pas de 0.5).

Modal Création (taille sm) : filiereTrackingId, matiereRequise, noteMinimum.

---

#### ScoreMatricesPage (/admin/matrices) — PAS de DataTable

PageHeader "Matrices de scores" et bouton "+". Tableau HTML manuel : colonnes Titre, Goûts personnels, Académique, Marché du travail, Total estimé, Actions (Modifier icône Pencil, Supprimer icône Trash2).

Modal Création et Édition (taille sm) : titreMatrice, scoreGoutsPersonnel (nombre), scoreAcademique (nombre), scoreMarcheTravail (nombre).

---

#### AdminStatistiquesPage (/admin/statistiques)

PageHeader "Statistiques". KpiCardGrid avec 4 StatCards : Total Élèves, Conseillers, Quiz, Quiz complétés.

Graphique linéaire (LineChart Recharts) "Inscriptions et quiz complétés (30 jours)".

Graphique en barres (BarChart Recharts) "Rendez-vous par mois" (12 mois).

Graphique en donut (PieChart Recharts) "Quiz par domaine".

Carte "Résumé plateforme" : 4 statistiques en boîtes.

---

#### AdminProfilPage (/admin/profil) — PAS de DataTable

Titre "Mon Profil". Carte profil : avatar avec initiales (cercle fond primary/light, texte primary), nom complet (userName), badge niveau d'accès (icône Shield ou ShieldAlert).

Informations affichées en lignes avec icônes : Nom/Prénom (icône User), Email (icône Mail), Téléphone (icône Phone), Niveau d'accès (icône Shield), Inscrit le (icône Calendar).

---

#### NotificationsPage (/admin/notifications) — PAS de DataTable

PageHeader : nombre total de notifications et nombre de non-lues.

Filtre toggle : Toutes ou Non lues. Boutons "Tout lire" et "Envoyer".

Liste de cartes : chaque carte contient une icône colorée (selon le titre), le titre, le message, un timestamp relatif, des boutons d'action "Marquer lue" et "Supprimer". Les notifications non lues ont une bordure bleue et un point bleu.

Modal Envoi (taille sm) : userId, titre, message.

---

### Pages Super Admin (3 supplémentaires)

---

#### SuperAdminDashboard (/superadmin/dashboard)

Titre "Tableau de bord Super Admin". KpiCardGrid avec 4 StatCards : Total Élèves, Conseillers, Quiz, Fiches.

Section "Gestion des Administrateurs" : DataTable avec colonnes Nom, Prénom, Email, Niveau d'accès (StatusBadge), Statut (Actif avec point vert, Inactif avec point gris), Actions (icône Pencil, icône Trash2). Bouton "Nouvel administrateur".

Modal Création et Édition (taille lg) : nom, prénom, email, téléphone, motDePasse, niveauAcces (select avec options ADMIN, SUPER_ADMIN, MODERATEUR, GESTIONNAIRE_CONSEILLER).

Modal Confirmation Suppression (taille sm).

---

#### ParametresPage (/superadmin/parametres) — Interface par onglets

Titre "Paramètres". Navigation gauche verticale avec 4 onglets :

1. **Gestion des rôles** (icône Settings) : tableau des permissions par rôle (SUPER_ADMIN, MODERATEUR, GESTIONNAIRE_CONSEILLER) et par section (dashboard, eleves, quiz, faq, conseillers, parametres) avec cases à cocher.

2. **Mode Maintenance** (icône Wrench) : toggle Activer/Désactiver, champ textarea pour le message, bouton Enregistrer. API : /admin/maintenance.

3. **Poids des quiz** (icône Sliders) : liste des paramètres clé/valeur avec édition inline et bouton Enregistrer.

4. **Intégrité Système** (icône Activity) : cartes de statut pour Serveur, Base de données, Stockage. Bannière "100% SÉCURISÉ".

Carte d'aide "Besoin d'aide ?" avec guide accessible via modal.

---

#### LogsPage (/superadmin/logs)

PageHeader "Journaux d'Audit".

Filtres : date (de/à), email (recherche), action (select avec options CONNEXION, MODIFICATION, SUPPRESSION, CREATION, CONSULTATION, EXPORT, TENTATIVE_ECHEC).

DataTable : Horodatage, Utilisateur (email), Action, Ressource, Niveau (StatusBadge avec couleurs INFO, AVERTISSEMENT, ERREUR), IP.

Trois cartes d'actions en bas : "Rapport PDF" (icône print), "Export CSV" (icône download), "Nettoyage" (non fonctionnel).

Footer : nombre d'entrées et version du serveur.

---

### Résumé composants par page

Page | DataTable | Modal | StatCard/KpiCardGrid | Spécificité
LoginPage | non | non | non | Formulaire en 2 étapes (login + TOTP)
AdminDashboard | non | non | KpiCardGrid 5 items | Graphiques Recharts Line + Pie
AdminStatistiquesPage | non | non | KpiCardGrid 4 items | Graphiques Recharts Line + Bar + Pie
AdminProfilPage | non | non | non | Carte profil avec initiales et infos
ElevesPage | oui | oui (3 modaux) | non | Import/Export CSV
ParentsPage | oui | oui (3 modaux) | non | Lier/délier enfants
ConseillersPage (admin) | non | oui (2 modaux) | non | Grille 3 colonnes de cartes
FilieresPage | oui | oui | non | Filtre domaine
MetiersPage | oui | oui | non | Filtre secteur (13 options)
SeriesPage | oui | oui | non | Filtre niveau
EtablissementsPage | oui | oui | non | Upload fichiers avec prévisualisation
QuizPage | oui | oui (2 modaux) | non | Navigation vers QuizEditorPage
QuizEditorPage | non | non | non | Layout 2 colonnes + QuestionForm
FAQModerationPage | non | oui | non | Grille 2 colonnes + 3 onglets
SeuilsPage | non | oui (2 modaux) | non | Tableau HTML manuel
ScoreMatricesPage | non | oui | non | Tableau HTML manuel
NotificationsPage | non | oui | non | Liste de cartes
ConseillerDashboard | non | non | StatCard x4 | Listes RDV + Messages
MessagesPage | non | oui | non | Split panel (liste + chat)
RendezVousPage | oui | oui | non | Filtres statut en pills
UtilisateursPage | non | oui (2 modaux) | non | Grille 3 colonnes + onglets Élèves/Parents
OriaPage | non | non | non | Interface chat complète
FAQPage (conseiller) | non | non | non | Accordéon
ProfilPage (conseiller) | non | non | non | Cartes par sections
StatistiquesPage (conseiller) | non | non | StatCard x4 | BarChart Recharts
SuperAdminDashboard | oui | oui (2 modaux) | KpiCardGrid 4 items | CRUD administrateurs
ParametresPage | non | non | non | 4 onglets verticaux
LogsPage | oui | non | non | Filtres + 3 cartes d'actions

---

## 2. PLATEFORME MOBILE FLUTTER

### Palette de couleurs Flutter (app_theme.dart)

Primaire : #1300C8 (indigo foncé). Primaire clair : #4A3DFF.
Accent : #FFA800 (ambré/orange). Accent clair : #FFD166.
Fond page : #FCF8FF (blanc violacé). Fond gris : #F4F0FA (gris violacé).
Texte foncé : #1A1A2E. Texte moyen : #454556. Texte clair : #B0B7C3.
Succès : #10B981. Erreur : #EF4444. Warning : #F59E0B.
Bordures carte : #E5E7EB.

Les couleurs Flutter sont différentes du backoffice : primaire = #1300C8, accent = #FFA800 (orange), fond = #FCF8FF.

Polices : Inter pour le corps, Poppins pour les titres.

Bouton primaire (PrimaryButton) : fond #FFA800, texte blanc, hauteur 54px, border-radius 14, largeur totale (full width).

Bouton outline (OutlineButton) : bordure #1300C8 1.5px, texte #1300C8, hauteur 54px, border-radius 14.

Input : border-radius 12, bordure #E5E7EB 1.5px, bordure au focus #1300C2 2px.

### Structure générale

Chaque écran est composé d'une StatusBar en haut, d'un contenu principal dans SingleChildScrollView avec padding de 20px, et d'une BottomNavigationBar (AppBottomNav) en bas. La BottomNav est un Container blanc avec SafeArea et une Row de tabs en Expanded. Chaque _NavItem contient une icône (filled quand actif, outline quand inactif) et un label en 10px. Actif = fond primary/10 + texte primary. Inactif = texte clair (textLight).

Nombre de tabs :
- Élève et Conseiller : 5 tabs (Accueil, Explorer, Diagnostic, Messages, Profil)
- Parent : 4 tabs (Accueil, Explorer, Messages, Profil)

Un bouton flottant (FAB) permet d'accéder à l'assistant ORIA (route /oria).

---

## ÉCRANS FLUTTER — 31 screen widgets, 35 routes

---

### AUTH (9 routes, 9 screens)

**SplashScreen** (route /) : fond #FCF8FF, logo centré. Vérification du JWT (expiration). Si valide, navigation vers /home. Si expiré, navigation vers /onboarding.

**OnboardingScreen** (/onboarding) : PageView avec 3 à 4 slides. Chaque slide contient une illustration, un titre en Poppins 24px et une description en Inter 15px. DotIndicator (points gris et primary). Bouton "Commencer" (PrimaryButton orange) qui navigue vers /register.

**RegisterScreen** (/register) : choix du rôle avec 3 grandes cartes (icône + texte) : "Je suis un élève", "Je suis un parent", "Je suis un conseiller". Formulaire dynamique selon le rôle avec champs nom, prénom, email, mot de passe.

**RegisterPreferencesScreen** (/register-preferences) : question "Quel type d'apprenant es-tu ?" avec 6 cartes sélectionnables. Bouton "Continuer".

**ProfileSetupScreen** (/profile-setup) : barre de progression. Champs affichés selon le type d'apprenant (établissement, niveau, série, centre d'intérêt). Bouton "Enregistrer".

**LoginScreen** (/login) : logo, champs email et mot de passe, bouton "Se connecter" en orange. Lien "Mot de passe oublié ?" vers /forgot-password. Lien "Créer un compte" vers /register. Si 2FA requis, navigation vers /totp-verify avec challengeToken.

**ForgotPasswordScreen** (/forgot-password) : champ email et bouton "Envoyer le code" qui navigue vers /otp.

**OtpScreen** (/otp) : 6 champs de saisie pour le code OTP, timer de compte à rebours, bouton "Renvoyer", bouton "Vérifier".

**ResetPasswordScreen** (/reset-password) : nouveau mot de passe, confirmation, bouton "Réinitialiser".

---

### 2FA/TOTP (2 routes, 2 screens)

**TotpSetupScreen** (/totp-setup) : QR code à scanner, instruction "Entrez le code 6 chiffres", bouton "Activer la 2FA".

**TotpVerifyScreen** (/totp-verify) : reçoit challengeToken et email en arguments. 6 champs de saisie, bouton "Vérifier", lien "Retour".

---

### Navigation principale (2 routes, 1 widget)

**MainScaffold** (/home et /dashboard) : BottomNav adaptative au rôle et au type d'apprenant. Dashboard sélectionné automatiquement : CONSEILLER vers DashboardConseiller, PARENT vers DashboardParent, PROFESSIONNEL vers DashboardReconversion, AUTRE vers DashboardDecrocheur, autres vers DashboardBachelier.

---

### Dashboard (5 widgets, inclus dans MainScaffold)

**DashboardBachelier** (tab Accueil pour COLLEGIEN, LYCEEN, ETUDIANT, ECOLIER) : fond #F4F0FA, RefreshIndicator, SingleChildScrollView, padding 20.

Sections dans l'ordre :
1. Header : "Bonjour {prenom} !" en 22px bold, badge niveau (fond accent #FFA800, texte blanc, 10px), icône search, icône notifications.
2. Carte Diagnostic/Action : fond blanc, arrondi 16px, icône, texte, flèche.
3. Carte Moyenne : cercle avec la note, liste des 5 dernières notes.
4. Actions Rapides : 4 icônes rondes (Quiz, Explorer, ORIA, Messages).
5. Modules Outils : 2 lignes (Portfolio, Simulateur, Entretien, DataHub, Badges, Témoignages, Réseau).
6. Recommandation IA : fond carte, texte, icône.
7. Conseillers CTA : "Trouver un conseiller".
8. RDV à venir : liste ou message vide.
9. Messages récents : extraits.
10. Explorer CTA.
11. Aide et FAQ.
État de chargement : SkeletonDashboard.

**DashboardReconversion** (pour PROFESSIONNEL) : fond #FCF8FF, padding 20. Sections : Header, Carte Citation, Timeline en 5 étapes (bilan, formation, financement, projet, emploi), Ressources, Financements CPF/VAE, Bilan Compétences, Conseillers CTA, Section Expert.

**DashboardDecrocheur** (pour AUTRE) : fond #FCF8FF, padding 20. Sections : Header, Carte Bienveillance en ambre, Actions Rapides (ORIA, Options, RDV, Ressources), Parcours Pas à Pas, Ressources d'aide, Témoignages.

**DashboardParent** (pour PARENT) : fond #FCF8FF, padding 20. Header avec onglets enfants horizontaux. Vue de l'enfant sélectionné (résultats quiz, messages, documents, RDV). Bouton "Ajouter un enfant".

**DashboardConseiller** (pour CONSEILLER) : fond #FCF8FF, padding 20. Header, switch disponibilité, cartes statistiques (questions en attente, messages, RDV), liste RDV du jour, liste Messages récents.

---

### Routes nommées (26 routes)

**RecommandationIAScreen** (/recommandation-ia) : titre, liste des fiches recommandées. Message spécifique si le profil est vide (orange avec appel à l'action).

**EnfantSuiviScreen** (/enfant-suivi) : reçoit enfantTrackingId en argument. Affiche nom, niveau, radar de compétences, notes, bouton diagnostic.

**NotificationsScreen** (/notifications) : liste chronologique des notifications. Point rouge pour les éléments non lus. Bouton "Tout marquer comme lu".

**FaqScreen** (/faq) : catégories en chips, liste des questions avec expansion, votes Utile et Pas utile.

**SupportScreen** (/support) : contact, FAQ, bouton Créer un ticket.

**ExplorerScreen** (/explorer) : barre de recherche, tabs horizontaux (Tout, Séries, Filières, Métiers, Établissements), liste de cards par onglet, bouton filtre.

**FavoritesScreen** (/favorites) : liste des fiches favorites tous types confondus.

**FicheDetailScreen** (/fiche-detail) : reçoit une fiche (Map) en argument. Image, titre, résumé, contenu, champs spécifiques selon le type, fiches liées, bouton favoris, bouton contacter conseiller, bouton quiz.

**GlobalSearchScreen** (/search) : barre de recherche, résultats groupés par catégorie.

**EtablissementsMapScreen** (/etablissements-map) : carte flutter_map avec marqueurs et clusters, infobulle au clic, accès au détail.

**QuizScreen** (/quiz) : reçoit quizTrackingId en argument. Carte de démarrage, questions affichées une par une avec barre de progression, 4 à 6 boutons de réponse.

**ResultatsScreen** (/resultats) : reçoit score, profil, quizId en arguments. Radar RIASEC, profil dominant, suggestions de fiches, boutons d'action.

**NotesScreen** (/notes) : liste des notes, moyenne, formulaire d'ajout, bouton OCR.

**MessagesListScreen** (/messages) : liste des conversations, recherche, bouton "Nouveau message", bouton "Mes tickets".

**ChatScreen** (/chat) : reçoit expediteurId et expediteurNom en arguments. En-tête avec le nom, bulles de messages, input d'envoi.

**RdvScreen** (/rdv) : reçoit conseillerId et conseillerNom en arguments. DatePicker, sélection de créneaux, confirmation.

**RdvListScreen** (/rdv-list) : onglets "À venir" et "Passés", liste des rendez-vous.

**ProfileScreen** (/profile) : photo modifiable (try-catch sur image_picker pour éviter crash already_active), informations, menu avec options : modifier, mot de passe, notifications, 2FA, consentement, documents, historique, déconnexion en rouge.

**OriaScreen** (/oria) : session persistée via FlutterSecureStorage. Historique des messages (bulles assistant fond gris à gauche, bulles utilisateur fond primary/10 à droite). Input texte, microphone (VoiceService STT), toggle auto-speak, chips de suggestions, points de chargement, bouton "Nouvelle conversation".

**SimulateurParcoursScreen** (/simulateur) : étapes séquentielles avec sélecteurs.

**PortfolioScreen** (/portfolio) : reçoit eleveTrackingId en argument. Radar de compétences, liste, ajout, recommandations métier.

**DataHubScreen** (/datahub) : carte thermique du Togo, statistiques des top métiers, séries par région.

**EntretienScreen** (/entretien) : reçoit eleveTrackingId en argument. Chat simulation IA, questions/réponses, score final.

**ReseauScreen** (/reseau) : reçoit utilisateurId et nomUtilisateur en arguments. Fil de publications, publier, commentaires, profil public.

**BadgeScreen** (/badges) : reçoit eleveTrackingId en argument. Grille de badges (obtenus et verrouillés), bouton info avec AlertDialog listant les critères.

**TemoignageScreen** (/temoignages) : témoignages par métier, témoignages vedettes, bouton info avec AlertDialog explicatif.

---

### Routes erreur (2 routes + catch-all)

**NotFoundScreen** (/404) : message d'erreur, bouton "Retour à l'accueil".

**NetworkErrorScreen** (/network-error) : message d'erreur réseau, bouton "Réessayer".

**Catch-all** (onUnknownRoute) : redirige vers NotFoundScreen avec message "Route {name} introuvable".

---

### Routes définies mais non utilisées

- /diagnostic (AppRoutes.diagnostic) : route définie dans AppRoutes mais aucune navigation dans main.dart.
- /portfolio-analyse (AppRoutes.portfolioAnalyse) : route définie dans AppRoutes mais aucune navigation dans main.dart.

---

### Composants Flutter récurrents

**AppBottomNav** : Container blanc, SafeArea, Row avec Expanded sur chaque _NavItem. Icône filled quand actif, outline quand inactif. Label 10px. Actif : fond primary/10, texte primary. Inactif : texte textLight.

**PrimaryButton** : fond #FFA800, texte blanc, hauteur 54px, border-radius 14, full width. Utilisé pour les actions principales (connexion, inscription, validation).

**OutlineButton** : bordure #1300C8 1.5px, texte #1300C8, hauteur 54px, border-radius 14. Utilisé pour les actions secondaires.

**InputField** : border-radius 12, bordure #E5E7EB 1.5px, bordure de focus #1300C8 2px. Placeholder en texte clair.

**SkeletonDashboard** : écran de chargement avec rectangles animés (shimmer) reproduisant la structure du dashboard.

**UserAvatar Flutter** : cercle avec initiales, taille configurable, couleur basée sur le hash du nom.

**StatusBadge Flutter** : badge coloré avec texte, mapping de statuts similaire au backoffice.

**PageHeader Flutter** : titre en Poppins 20px bold + sous-titre en Inter 14px textLight.

**EmptyState Flutter** : icône + message + bouton d'action optionnel.
