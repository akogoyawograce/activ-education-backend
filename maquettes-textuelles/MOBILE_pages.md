# Maquette textuelle — Application Mobile (Flutter)

> **Contexte projet :** Application mobile (Flutter) de la plateforme **Activ EDUCATION** — orientation scolaire et professionnelle pour le Togo. Destinée aux **élèves** (collégiens, lycéens, étudiants, décrocheurs, en reconversion), **parents** et **conseillers**. Stack : Flutter / Dart, Material Design.
>
> **Architecture de navigation :** `main_scaffold` est un Scaffold avec une **BottomNavigationBar à 5 onglets** (Élève) ou 4 onglets (Parent/Conseiller) :
> - Élève : `Accueil` · `Explorer` · `Diagnostic` · `Messages` · `Profil`
> - Parent : `Accueil` · `Explorer` · `Messages` · `Profil`
> - Conseiller : `Accueil` · `Explorer` · `Messages` · `Profil`
> - Un `FloatingActionButton.small` rond en bas à droite → ouvre l'assistant **ORIA** (chat IA), visible sur tous les écrans principaux.
>
> **Conventions visuelles générales :**
> - **Couleurs et typographies :** *à définir par le maquettiste* (palette, polices, identité visuelle — non spécifiée ici, à harmoniser avec la charte graphique du projet).
> - Cartes : coins arrondis généreux, fond blanc sur fond de page très clair.
> - Bouton primaire : plein, coins arrondis, label en semi-gras.
> - Icônes : Material `Icons.*_rounded` en priorité.
> - Avatar par défaut : cercle avec initiales (première lettre du prénom + première lettre du nom) sur fond teinté.

---

## 1. Splash (`splash_screen.dart`)

**Type :** Écran de démarrage (plein écran, sans AppBar).

**Contenu :**
- **Centre écran :** [LOGO_ACTIV_EDUCATION — à insérer par le maquettiste], taille ~150 px de diamètre, avec ombre douce.
- **Sous le logo :** Texte "Activ Education" en grand format, gras.
- **Sous le titre :** Slogan "Ton orientation, ton avenir" en corps de texte secondaire.
- **Bas de l'écran :** Indicateur de chargement circulaire.

**Logique :** Affiche ce contenu pendant l'initialisation (vérification token, chargement profil). Redirige automatiquement vers Onboarding (1ère fois) ou Login / Home selon l'état d'authentification.

---

## 2. Onboarding — 3 pages (`onboarding_screen.dart`)

**Type :** Écran plein écran, swipeable horizontalement (PageView).

**Layout :**
- **Haut :** Bouton "Passer" en haut à droite (texte).
- **Centre :** Illustration custom (CustomPainter) occupant ~50 % de la hauteur + Titre principal en grand + Sous-texte.
- **Bas :** 3 dots indicators + Bouton primaire `Suivant` (puis `Commencer` sur la page 3), pleine largeur avec marges latérales.

**Page 1 — "Découvre ton orientation" :**
- Illustration : diagramme en arbre/pathfinder stylisé avec icônes d'école et de tendance.
- Titre : "Bienvenue sur Activ Education"
- Sous-texte : "La plateforme qui t'aide à trouver ta voie."

**Page 2 — "Quiz intelligent RIASEC" :**
- Illustration : maquette de quiz avec option à cocher et icône ampoule.
- Titre : "Un quiz pensé pour toi"
- Sous-texte : "En quelques questions, identifie les métiers et filières qui te correspondent."

**Page 3 — "Accompagnement humain" :**
- Illustration : carte conseiller avec avatar + icônes chat, visio, téléphone.
- Titre : "Des conseillers à ton écoute"
- Sous-texte : "Échange par messagerie, visio ou téléphone avec des conseillers d'orientation."

---

## 3. Authentification

### 3.1 Login (`auth/login_screen.dart`)

**Type :** Formulaire avec scroll.

**Layout (du haut vers le bas) :**
1. **Flèche retour** en haut à gauche (top: 16, padding 8).
2. **[LOGO_ACTIV_EDUCATION]** centré (largeur ~120 px).
3. **Titre** "Bon retour !" en grand format semi-gras.
4. **Sous-titre** "Connecte-toi pour continuer ton parcours".
5. **Champ Email** — `TextField` outline, `prefixIcon: Icons.email_outlined`, `hintText: "ex: prenom@email.com"`, validateur email.
6. **Champ Mot de passe** — `TextField` outline, `prefixIcon: Icons.lock_outline`, suffixe `IconButton` œil (visibilité), `hintText: "Mot de passe"`.
7. **Lien "Mot de passe oublié ?"** — `TextButton` aligné à droite, en couleur d'accent.
8. **Bouton primaire** "Se connecter" pleine largeur, désactivé si champs vides.
9. **Séparateur** "ou" centré avec lignes de chaque côté.
10. **Bouton secondaire outline** "Continuer avec Google" (placeholder, à finaliser).
11. **Lien bas** "Pas encore de compte ? **Inscris-toi**" centré.

**États :**
- Erreur → SnackBar d'erreur avec `Icons.error`.
- Loading → spinner dans le bouton.

### 3.2 Register — Étape 1 (`auth/register_screen.dart`)

**Type :** Formulaire, navigation via un bottom sheet pour le pays.

**Layout :**
1. **Flèche retour** + **Titre** "Créer un compte" + **Sous-titre** "Étape 1/2 — Tes informations".
2. **Sélecteur de pays** — Carte cliquable avec drapeau emoji + nom du pays + indicatif + chevron → ouvre un bottom sheet listant les pays (drapeau + nom + dial).
3. **Champ Nom** — `TextField` outline, label "Nom", `hintText: "Votre nom"`.
4. **Champ Prénom** — label "Prénom", `hintText: "Votre prénom"`.
5. **Champ Email** — label "Email", `hintText: "kofi@email.com"`, validateur.
6. **Champ Numéro** — avec préfixe pays (Togo +228) + champ `00 00 00 00`.
7. **Champ Mot de passe** — `hintText: "••••••••"`, suffixe œil.
8. **Champ Confirmer mot de passe**.
9. **Bouton primaire** "Continuer" → passe à l'étape 2.
10. **Lien bas** "Déjà un compte ? **Se connecter**".

### 3.3 Register — Étape 2 Préférences (`auth/register_preferences_screen.dart`)

**Type :** Formulaire de préférences.

**Layout :**
1. **Titre** "Tes préférences" + **Sous-titre** "Aide-nous à personnaliser ton expérience".
2. **Section "Matières favorites"** — `Wrap` de chips sélectionnables (Maths, SVT, Physique, Français, Histoire-Géo, Anglais, Philosophie, Économie). Sélection multiple.
3. **Section "Tu apprends mieux…"** — `SegmentedButton` 3 options : `Par les textes` · `Par les vidéos` · `Les deux`.
4. **Bouton primaire** "Terminer l'inscription".
5. **Dialog de succès** après inscription : icône `check_circle_rounded` + "Inscription réussie !" + "Bienvenue sur Activ Education" + bouton "Continuer".

### 3.4 Register — Profile Setup (`auth/profile_setup_screen.dart`)

**Type :** Formulaire (peut être utilisé en mode édition ou onboarding post-inscription).

**Layout :**
1. **Flèche retour** + **Titre** "Dis-nous qui tu es" + **Sous-titre** "Personnalise ton expérience en 30 secondes."
2. **Label** "Je suis..." + grille 2x3 de cartes rôles (chaque carte = icône + label, avec état sélectionné bordure d'accent) :
   - `Collégien(ne)` · `Lycéen(ne)` · `Étudiant(e)`
   - `Parent` · `En reconversion` · `Jeune décrocheur`
3. **Label conditionnel** "Quelle classe ?" + `DropdownButton` selon le rôle (collégien : 6ème–3ème ; lycéen : Seconde/Terminale ; étudiant : L1–Doctorat).
4. **Label** "Ville de résidence" + `DropdownButton` chargé dynamiquement (chargement des villes Togolaises : Lomé, Kara, Sokodé, Kpalimé, Atakpamé, Tsévié, etc.) avec icône `location_on_outlined`.
5. **Bouton primaire** "Continuer".

### 3.5 OTP — vérification téléphone (`auth/otp_screen.dart`)

**Type :** Saisie de code à 4 chiffres.

**Layout :**
1. **Flèche retour** + **Titre** "Vérifie ton numéro".
2. **Sous-titre** "Un code à 4 chiffres a été envoyé au +228 XX XX XX XX."
3. **4 cases OTP** côte à côte (PinPut custom), auto-focus, navigation auto au suivant.
4. **Texte** "Pas reçu ? Renvoyer le code" (lien cliquable, désactivé 30s avec compte à rebours).
5. **Bouton primaire** "Valider" (désactivé tant que les 4 cases ne sont pas remplies).
6. **Dialog d'erreur** si code invalide : "Un nouveau code vous a été envoyé".

### 3.6 Mot de passe oublié (`auth/forgot_password_screen.dart`)

**Type :** Formulaire.

**Layout :**
1. **AppBar** avec flèche retour + titre "Mot de passe oublié".
2. **Titre** "Mot de passe oublié ?".
3. **Sous-titre** "Saisis ton email ou ton numéro de téléphone pour recevoir un code de réinitialisation."
4. **Toggle Email / Téléphone** (segmented) :
   - Si Email : champ email + icône `mail_outline_rounded`.
   - Si Téléphone : champ +228 + icône `phone_outlined`.
5. **Bouton primaire** "Envoyer le code".

### 3.7 Reset Password (`auth/reset_password_screen.dart`)

**Type :** Formulaire.

**Layout :**
1. **AppBar** avec flèche retour.
2. **Titre** "Nouveau mot de passe" + **sous-titre** "Crée un nouveau mot de passe sécurisé pour ton compte."
3. **Champ Nouveau mot de passe** avec suffixe œil + indicateur de force visuel.
4. **Helper text** "Au moins 8 caractères".
5. **Champ Confirmer mot de passe** + helper dynamique "Les mots de passe correspondent" (coche) ou erreur.
6. **Bouton primaire** "Réinitialiser" (actif si valide).
7. **Dialog de succès** : "Mot de passe réinitialisé avec succès" + auto-redirection login.

### 3.8 2FA Setup (`auth/totp_setup_screen.dart`)

**Type :** Écran informatif + scan QR.

**Layout :**
1. **AppBar** "Sécurité du compte".
2. **État non activé :** Icône `security` 64 px + Titre "Active la double authentification" + Sous-texte explicatif + Bouton "Activer la 2FA" (ouvre un bottom sheet avec QR code + secret).
3. **État activé :** Icône `shield` 80 px + Titre "Authentification à deux facteurs activée" + Sous-texte "Ta session est protégée par un code supplémentaire." + Bouton "Désactiver la 2FA" (avec dialog de confirmation).
4. **Dialog désactivation** : "Désactiver la 2FA ?" + "Cela réduit la sécurité de ton compte." + Annuler / Désactiver.

### 3.9 2FA Verify (`auth/totp_verify_screen.dart`)

**Type :** Saisie code 6 chiffres.

**Layout :**
1. **AppBar** "Vérification".
2. **Icône** `security` 80 px + Titre "Authentification à deux facteurs" + Sous-texte "Entre le code à 6 chiffres généré par ton application d'authentification."
3. **Champ OTP 6 chiffres** unique (`hintText: "000000"`, `maxLength: 6`).
4. **Bouton primaire** "Vérifier".
5. **SnackBar d'erreur** si "Code invalide. Réessaie."

---

## 4. Bottom Navigation (`main_scaffold.dart`)

**Type :** Conteneur principal (Scaffold avec `BottomNavigationBar`).

**Composants persistants :**
- **AppBar** par défaut (personnalisée par chaque onglet).
- **Body :** Widget switché selon l'onglet actif.
- **BottomNavigationBar** : 5 items (Élève) / 4 items (Parent/Conseiller), icônes `*_rounded` actives, `*_outlined` inactives, label en caption.
- **FloatingActionButton.small** rond en bas à droite, en couleur d'accent, icône `auto_awesome` → ouvre la page ORIA.

---

## 5. Dashboards (5 variantes selon rôle)

### 5.1 Dashboard Bachelier (`home/dashboard_bachelier.dart`)

**Type :** Dashboard personnel.

**Layout scroll (du haut vers le bas) :**
1. **Header** : "Bonjour {Prénom} !" + barre de recherche (`Icons.search_rounded`) + pastille notification.
2. **Carte profil complété** : circulaire "B" + pourcentage "Profil: X% complété" + lien "Compléter".
3. **Carte action requise** : "Que faire après le Bac ?" + sous-texte + bouton "Commencer le diagnostic".
4. **Carte moyenne générale** : icône `analytics_rounded` + valeur moyenne + "Dernières notes" + lien "Voir tout" → ouvre Notes.
5. **Section Actions rapides** : grille 4 cartes (Diagnostic, Bibliothèque, Conseillers, Profil).
6. **Section Recommandations IA** : card avec icône `auto_awesome` + titre + bouton "Voir mes recommandations".

### 5.2 Dashboard Conseiller (`home/dashboard_conseiller.dart`)

**Layout :**
1. **Header** : "Bonjour {Prénom} !" + statut "Conseiller orientation | En ligne" + bouton notif + aide.
2. **Carte questions en attente** : "X question(s) en attente de réponse" + bouton "Voir sur le site".
3. **Grille 4 stats** : `Tickets traités` · `RDV aujourd'hui` · `Satisfaction (★ 4.9)` · `Temps réponse (8h)`.
4. **Toggle Disponibilité** : `Icons.toggle_on_outlined` + "Disponibilité : Je suis disponible / indisponible" (un toggle, persisté via `PUT /conseillers/{id}`).
5. **Section Rendez-vous du jour** : liste cards (nom élève, heure, bouton "Rejoindre" `videocam_rounded` + bouton "Message" `chat_bubble_outline_rounded`).
6. **Bouton** "Ouvrir le back-office complet" (redirige vers le backoffice web).

### 5.3 Dashboard Décrocheur (`home/dashboard_decrocheur.dart`)

**Layout :**
1. **Header bienveillant** : "Bonjour {Prénom} !" + sous-texte "On est là pour toi, pas à pas." + statut RDV.
2. **Carte encouragement** : icône `favorite_rounded` + "On ne lâche rien !" + "tu n'es pas seul·e. Des conseillers sont là pour t'accompagner."
3. **3 cartes d'action** (grille) :
   - "Parler à un conseiller" (icône `support_agent_rounded`)
   - "Explorer les métiers" (icône `work_outline_rounded`)
   - "Prendre RDV" (icône `event_rounded`)
4. **Section "Ton parcours pas à pas"** : 3 step cards numérotées :
   1. "Fais le point" — Un quiz pour mieux cerner tes envies et tes atouts
   2. "Découvre des pistes" — Explore les métiers et formations qui te correspondent
   3. "Échange avec un conseiller" — Un accompagnement personnalisé
5. **Section "Ressources pour t'aider"** : 3 cards (Dispositifs de réinsertion, Formations qualifiantes, Jobs et stages) avec icône et flèche.

### 5.4 Dashboard Parent (`home/dashboard_parent.dart`)

**Layout :**
1. **Header** : titre "Espace Famille" + "Bonjour M. {Nom}".
2. **Sélecteur d'enfant** : liste horizontale de chips (un par enfant lié) + bouton "Lier un enfant" (ouvre un dialog : "Saisis l'identifiant (trackingId) de l'enfant").
3. **Carte enfant sélectionné** : avatar + "{Prénom} · {Niveau}" + "{Établissement}" + score diagnostic (%) + "1 recommandation disponible".
4. **Mini-stats** : "X docs" + "X RDV à venir".
5. **Boutons d'action** : `Suivi` (icône `trending_up_rounded`) et `Documents` (icône `description_rounded`).
6. **Carte notification** : "Nouveau message concernant {Prénom}" + icône `info_outline_rounded`.

### 5.5 Dashboard Reconversion (`home/dashboard_reconversion.dart`)

**Layout :**
1. **Header** : "Bonjour {Prénom} !" + sous-titre "Reconversion professionnelle".
2. **Carte citation inspirante** : « …aligner vos valeurs avec votre métier. » + bouton "Commencer ma réflexion".
3. **Section "Votre parcours recommandé"** : 3 step cards numérotées :
   1. "Parler à un conseiller" — action "Prendre RDV maintenant"
   2. "Quiz de reconversion" — action "Commencer le quiz"
   3. "Explorer les formations" — action "Découvrir"
4. **Section "Ressources utiles"** : cards de fiches (métiers, formations).
5. **Section "Financements et droits"** : icône `euro_rounded` + cards (Compte Personnel de Formation, Formation continue).

---

## 6. Explorer (Bibliothèque d'orientation)

### 6.1 Explorer Home (`explorer/explorer_screen.dart`)

**Type :** Page catalogue avec recherche + filtres.

**Layout :**
1. **AppBar** : titre "Explorer" + badge notification (!) + bouton tune (filtres).
2. **Barre de recherche** : `TextField` avec `prefixIcon: Icons.search_rounded`, `hintText: "Rechercher une filière, métier..."`, suggestion d'autocomplete.
3. **Onglets horizontaux** (Tabs scrollables) : `Tout` · `Séries` · `Filières` · `Métiers` · `Établissements`.
4. **Compteur de résultats** : "X résultat(s) pour 'recherche'".
5. **Section "Fiches d'orientation"** + lien "Voir tout" → ouvre `_AllCategoriesScreen` :
   - Card "Séries" avec icône `school_rounded`
   - Card "Filières"
   - Card "Métiers"
   - Card "Établissements"
6. **Section "FILIÈRE DU MOMENT"** : grande card mise en avant avec image de fond + texte "Très recherché ce mois" + icône `trending_up_rounded`.
7. **Liste verticale de cards de fiches** (les plus récentes / populaires).
8. **Empty state** : icône + "Aucun résultat trouvé" + "Essayez un autre mot-clé".

### 6.2 Category List (`explorer/category_list_screen.dart`)

**Type :** Liste filtrée d'un type de fiches.

**Layout :**
1. **AppBar** : titre dynamique (Séries/Filières/Métiers/Établissements) + bouton tune (filtres).
2. **Filtres actifs** affichés en chips horizontaux (avec X pour retirer).
3. **Liste de cards** (paramétrée par `CategoryType` : `series` / `filieres` / `metiers` / `etablissements`) avec :
   - Petit badge coloré en haut à droite (type)
   - Icône spécifique au type
   - Titre + sous-titre
   - Chevron `Icons.chevron_right_rounded` à droite
4. **Empty state** : "Aucun résultat" + sous-texte conditionnel ("avec ces filtres" ou "Revenez plus tard").

### 6.3 Fiche Detail (`explorer/fiche_detail_screen.dart`)

**Type :** Page de détail d'une fiche (série, filière, métier, établissement).

**Layout scroll :**
1. **AppBar transparente** : flèche retour + icône favori `bookmark_rounded` (toggle) + icône partage `share_outlined`.
2. **Hero image** pleine largeur avec overlay gradient sombre + Titre + Tags (type, niveau) en chips.
3. **Section "En bref"** : résumé court + 3–4 bullets.
4. **Section "Comprendre en vidéo"** (si dispo) : card avec thumbnail + bouton play `Icons.play_arrow_rounded`.
5. **Section "Photos"** : carrousel horizontal d'images.
6. **Section "Pour aller plus loin"** : _Section accordéon / expandable list :
   - "Programme détaillé" (matières principales)
   - "Profil idéal" (texte statique : "Élève curieux, rigoureux, passionné…")
   - "Débouchés"
   - "Témoignages"
   - "Conditions d'admission" (filière)
   - "Programme pédagogique" (filière)
   - "Débouchés professionnels" (métier)
   - "Missions principales" (métier)
   - "Compétences requises" (métier)
   - "Débouchés au Togo" (métier)
   - "Fourchette de salaire" (métier)
   - "Localisation" (établissement)
   - "Offre de formation" (établissement)
   - "Filières proposées" (établissement)
   - "Contacts" + "Site web" (établissement)
7. **Bouton "Recommandé par l'IA"** (caroussel de fiches similaires).
8. **SnackBar** "Connectez-vous pour ajouter aux favoris" si non authentifié.

### 6.4 Favoris (`explorer/favorites_screen.dart`)

**Type :** Liste des fiches favorites.

**Layout :**
1. **AppBar** "Mes favoris".
2. **Liste cards** avec :
   - Bouton suppression glissable (Dismissible, `Icons.delete_outline_rounded`)
   - Icône `bookmark_rounded`
   - Titre de la fiche + sous-titre + chevron
3. **SnackBar** "Retiré des favoris" au swipe.
4. **Empty state** : "Aucun favori" + "Ajoute des fiches en favoris pour les retrouver ici."

### 6.5 Carte Établissements (`explorer/etablissements_map_screen.dart`)

**Type :** Carte OpenStreetMap (FlutterMap).

**Layout :**
1. **AppBar** "Établissements".
2. **FlutterMap** plein écran :
   - Tile : `https://tile.openstreetmap.org/{z}/{x}/{y}.png`
   - Markers : un par établissement (initiale du nom dans un cercle)
   - Markers cluster au zoom faible
3. **Bottom sheet au tap marker** : titre + ville + adresse + site web (lien) + bouton "Voir la fiche" (redirige vers FicheDetail).

### 6.6 Filtres Catalogue (`explorer/catalogue_filter_sheet.dart`)

**Type :** Bottom sheet de filtres (modal).

**Layout :**
1. **Header** : titre "Filtrer" + bouton "Réinitialiser" (texte).
2. **Section "Trier par"** : 3 chips (Pertinence, Ordre alphabétique, Les plus consultés), sélection unique.
3. **Section "Domaine"** : chips (Sciences, Lettres, Commerce, Technique, Arts), multi.
4. **Section "Niveau"** : chips (3ème, Seconde, Première, Terminale, BAC+1, BAC+2, BAC+3, BAC+5), multi.
5. **Section "Ville"** : chips (Lomé, Kara, Sokodé, Kpalimé, Atakpamé, Tsévié), multi.
6. **Switch** "Établissements publics uniquement".
7. **Bouton pleine largeur** "Appliquer les filtres".

---

## 7. Diagnostic (Quiz + Résultats)

### 7.1 Quiz — Liste (`diagnostic/quiz_screen.dart`)

**Type :** Page d'entrée (sélection d'un quiz).

**Layout :**
1. **AppBar** avec flèche retour.
2. **Titre** "Choisis un quiz" + **Sous-titre** "Sélectionne le quiz que tu veux faire".
3. **Liste verticale de cards quiz** : icône + titre + description + nombre de questions "{X} questions" + chevron.
4. **Au tap :** lance l'écran Quiz Questions.

### 7.2 Quiz — Questions (`diagnostic/quiz_screen.dart` — mode session)

**Type :** Question par question (système branché RIASEC).

**Layout :**
1. **Header** : titre du quiz + "Question X".
2. **Indicateur de progression** : "{X} / {total}".
3. **Catégorie RIASEC** ("Tes habitudes") avec icône `lightbulb_outline_rounded`.
4. **Texte de la question** en headingMedium.
5. **Liste d'options** (cards cliquables avec radio button visuel).
6. **Bouton primaire** "Question suivante" (désactivé tant que pas de réponse) ou "Terminer le quiz" sur la dernière.
7. **Bouton retour** `Icons.arrow_back_rounded` (avec dialog de confirmation "Quitter le quiz ? — Ta progression sera perdue.").
8. **Bouton fermeture** `Icons.close_rounded` (top right).
9. **État erreur** : icône `error_outline_rounded` + message + bouton "Réessayer".

### 7.3 Résultats Diagnostic (`diagnostic/resultats_screen.dart`)

**Type :** Affichage post-quiz.

**Layout scroll :**
1. **AppBar** avec bouton X (retour home).
2. **Hero header** : icône `auto_awesome` + "Résultats du diagnostic" + card "Recommandation personnalisée".
3. **Score circulaire** : "{X}%" + sous-titre.
4. **Section "Filières recommandées pour vous"** : 3 cards horizontales (nom + "Voir le détail" + flèche).
5. **Section "Historique de vos résultats"** : liste des anciens résultats (score + date + chip "Dernier").
6. **Bouton outlined** "Refaire un quiz" + bouton primary "Explorer toutes les filières".

### 7.4 Notes (`diagnostic/notes_screen.dart`)

**Type :** Gestion des notes scolaires.

**Layout :**
1. **AppBar** "Mes notes" (ou "Ajouter une note" en mode ajout) + bouton + `Icons.add_rounded`.
2. **Mode liste :**
   - **Card moyenne** en haut : icône `analytics_rounded` + "Moyenne générale" + valeur.
   - **Liste de notes** : matiere + note/20 + coefficient + chip trimestre.
   - **Swipe-to-delete** avec confirmation.
3. **Mode ajout (formulaire) :**
   - Champ "Matière" + `prefixIcon: Icons.book_rounded`
   - Champ "Note /20" + `prefixIcon: Icons.star_rounded`
   - Champ "Coefficient" + `prefixIcon: Icons.trending_up_rounded`
   - Champ "Trimestre (optionnel)" + `prefixIcon: Icons.calendar_today_rounded`
   - Bouton "Enregistrer la note"
4. **Empty state** : "Aucune note saisie" + "Commencez par ajouter vos premières notes" + bouton action.

### 7.5 OCR Bulletin (`diagnostic/ocr_bulletin_screen.dart`)

**Type :** Scan d'image de bulletin.

**Layout :**
1. **AppBar** "Analyse de bulletin".
2. **État initial :** icône `document_scanner` 64 px + texte "Prends en photo ou importe ton bulletin" + bouton "Analyser le bulletin".
3. **Picker image** (`image_picker`, géré via `file_picker`).
4. **Loading overlay** pendant l'appel `/api/v1/eleves/{id}/ocr` (utilise OpenAI si image).
5. **Résultat :** `ListView` de notes extraites (titre = matière, trailing = "{X}/20", subtitle = coefficient).
6. **Erreur** affichée en haut + bouton "Réessayer".

### 7.6 Diagnostic Enfant (`home/diagnostic_enfant_screen.dart`)

**Type :** Vue parent — diagnostic d'un enfant.

**Layout :**
1. **AppBar** "Diagnostic".
2. **Cards de séries recommandées** (Sciences Mathématiques C, Sciences Expérimentales D, Lettres A, Sciences Techniques E, Sciences Technologiques F) avec :
   - Code série (badge coloré)
   - Libellé long
   - Couleur d'accent
3. **État erreur** : icône `error_outline_rounded` + "Une erreur est survenue" + bouton "Réessayer".

### 7.7 Recommandation IA (`home/recommandation_ia_screen.dart`)

**Type :** Recommandation personnalisée générée par IA.

**Layout :**
1. **AppBar** "Mon orientation personnalisée".
2. **Header** : icône `auto_awesome` (ou `edit_note` si profil incomplet) + "Recommandation personnalisée" + sous-titre "Basée sur ton profil, tes notes et tes quiz".
3. **Section "Conseils du conseiller IA"** : bloc de texte long généré (markdown rendu).
4. **Bouton** "Actualiser".
5. **État erreur** : icône + bouton "Réessayer".

---

## 8. Suivi & Profil

### 8.1 Profile (`profile/profile_screen.dart`)

**Type :** Profil utilisateur (édition).

**Layout :**
1. **Header** : photo (ou initiales) + nom complet + email + bouton édition.
2. **Section "Mon parcours"** :
   - Dropdown "Niveau" (propositions conditionnelles selon le statut : collégien, lycéen, étudiant)
   - Dropdown "Filière" (séries A/C/D/E/F/G si lycéen)
   - Dropdown "Métier souhaité"
3. **Bottom sheet "Mon établissement"** : champ texte "Établissement".
4. **Bottom sheet "Mes centres d'intérêt"** : chips multi-sélection (matières préférées en CSV) + champ libre.
5. **Bottom sheet "Métier souhaité"** : champ texte.
6. **Bouton** "Enregistrer" (désactivé si non rempli).
7. **Section badges** + accès à `BadgeScreen`.
8. **Section portfolio** + accès à `PortfolioScreen`.
9. **Section sécurité** → 2FA Setup.
10. **Bouton déconnexion** (bottom, en couleur d'erreur).

### 8.2 Enfant Suivi (`home/enfant_suivi_screen.dart`)

**Type :** Vue parent — détail d'un enfant suivi.

**Layout :**
1. **AppBar** "{Prénom} {Nom}" ou "Suivi enfant".
2. **Header enfant** : avatar + nom + "{niveau} — {établissement}" + type apprenant.
3. **Section "Notes"** (icône `grading_rounded`) :
   - Card moyenne : "Moyenne : {X.XX}" + "{X} matière(s)"
   - Liste de notes : matière + note + coefficient
4. **Section "Quiz & Diagnostics"** (icône `quiz_rounded`) : liste des résultats.
5. **Section "Rendez-vous"** (icône `calendar_month_rounded`) : liste à venir.

### 8.3 Historique (`historique/historique_screen.dart`)

**Type :** Journal d'activité.

**Layout :**
1. **AppBar** "Journal d'activité".
2. **Liste** de cards `ListTile` :
   - Icône dynamique selon action (`Icons.history_edu`, `Icons.visibility`, etc.)
   - Titre : libellé de l'action
   - Subtitle : détails (si non vide)
   - Trailing : date "{J}/{M}/{A}"
3. **Empty state** : "Aucune activité".

### 8.4 FAQ (`home/faq_screen.dart`)

**Type :** Foire aux questions.

**Layout :**
1. **AppBar** "Foire Aux Questions" + flèche retour.
2. **Barre de recherche** : `hintText: "Comment pouvons-nous vous aider ?"`, `prefixIcon: Icons.search_rounded`.
3. **Chips de catégories horizontales** : `Tous` + catégories (Orientation, Plateforme, Conseillers, etc.), sélection unique.
4. **Liste de cards FAQ** : titre de la question + chevron + au tap expansion affiche la réponse + "X vues".
5. **Empty state** géré par `EmptyContentScreen`.

### 8.5 Notifications (`home/notifications_screen.dart`)

**Type :** Centre de notifications.

**Layout :**
1. **AppBar** "Notifications" + bouton "Tout lire" (top right).
2. **Onglets** : `Toutes` · `Non lues` · `Messages`.
3. **Liste de notifications** : icône colorée par type (MESSAGE / DIAGNOSTIC-QUIZ / RDV / RECOMMANDATION) + titre + sous-texte + temps relatif ("Il y a 5m", "Il y a 2h", "Il y a 3j" ou date).
4. **Swipe-to-delete** + bouton suppression.
5. **Empty states** par onglet.

### 8.6 Support (`home/support_screen.dart`)

**Type :** Hub d'aide.

**Layout :**
1. **AppBar** "Support".
2. **3 cards cliquables** (icônes + titre + sous-titre + chevron) :
   - "Foire Aux Questions" — Consultez les questions fréquentes
   - "Contacter un conseiller" — Envoyez un message à notre équipe
   - "Prendre rendez-vous" — Planifiez un entretien personnalisé
3. **Section "Mes derniers messages"** : aperçu + lien.
4. **Empty state** : "Aucun message pour l'instant".

---

## 9. Assistant ORIA — Chat IA (`chat/oria_screen.dart`)

**Type :** Chat conversationnel avec IA.

**Layout :**
1. **AppBar** : Avatar circulaire avec "O" + titre "ORIA" + sous-titre "Assistant IA" + bouton toggle voix (auto-speak ON/OFF).
2. **Zone de messages** (scrollable, bas = message le plus récent) :
   - Bulles user (droite)
   - Bulles assistant (gauche, avec avatar ORIA)
   - Bulle de bienvenue initiale : "Bonjour ! Je suis ORIA, ton conseiller IA. Je peux t'aider à explorer les formations, les établissements, ou tout sujet lié à ton parcours éducatif ! 🎓"
3. **Indicateur de saisie** (3 dots animés) pendant réponse IA.
4. **Barre de saisie (bas)** : `TextField` avec `prefixIcon: Icons.mic_rounded` (speech_to_text) + bouton send `Icons.send_rounded`.
5. **SnackBar erreur** : "Désolé, je n'ai pas pu répondre. Vérifie ta connexion et réessaie." + erreurs vocales.
6. **Backend** : POST `/api/v1/oria/message` avec polling (pas de WebSocket côté mobile).

---

## 10. Réseau Social & Communauté

### 10.1 Réseau — Fil (`reseau/reseau_screen.dart`)

**Type :** Mini réseau social.

**Layout :**
1. **AppBar** "Réseau".
2. **Tabs** : `Fil` · `Tendances`.
3. **Carte de publication (top)** : avatar + nom + champ texte "Partagez votre expérience..." + bouton send.
4. **Liste de posts** : avatar + nom + date + contenu texte + actions (Like, Commentaire, Partager).
5. **Swipe-to-delete** sur ses propres posts.
6. **Empty state** : icône `group` 64 px + "Aucun post pour le moment".

---

## 11. Documents (`documents/documents_screen.dart`)

**Type :** Gestion de documents scolaires.

**Layout :**
1. **AppBar** "Mes documents" + bouton +.
2. **Bottom sheet "Ajouter un document"** :
   - Sélecteur de fichier (extensions : pdf, doc, docx, xls, xlsx, ppt, pptx, txt, jpg, jpeg, png)
   - Dropdown "Type de document" (Bulletins, CV, Lettres, Autres)
   - Champ "Description (optionnelle)"
   - Champ date "Date du document" (yyyy-MM-dd)
   - Boutons "Annuler" / "Ajouter"
3. **Liste de documents** : nom + type + date + icône + menu (télécharger, supprimer).
4. **Dialog suppression** : "Supprimer 'nom.pdf' ?".

---

## 12. Témoignages (`temoignage/temoignage_screen.dart`)

**Type :** Liste de témoignages d'anciens élèves.

**Layout :**
1. **AppBar** "Témoignages" + bouton info "Comment ça marche" (ouvre un dialog explicatif).
2. **Liste de cards témoignages** : photo + nom + parcours + témoignage texte.
3. **Section "À la une"** mise en avant.
4. **Dialog "Comment ça fonctionne"** :
   - "Les témoignages sont des retours d'expérience d'anciens élèves comme toi."
   - "Découvre des parcours d'étudiants et professionnels"
   - "Inspire-toi de leur expérience pour t'orienter"
   - "Partage ton propre témoignage depuis ton profil"
   - "Les témoignages 'À la une' sont mis en avant par notre équipe"

---

## 13. Portfolio (`portfolio/portfolio_screen.dart`)

**Type :** CV/portfolio compétences.

**Layout :**
1. **AppBar** "Mon Portfolio" + bouton "Analyser mon profil" (analytics) + bouton "Ajouter une compétence".
2. **Section "Compétences"** : grille de cards par catégorie (Tech, Langues, Soft Skills, etc.) avec icône + nom + niveau (étoiles 1–5).
3. **Stats header** : "X compétences" + "Niveau moyen : Y.Y/5".
4. **Bouton "Ajouter ma première compétence"** (empty state).
5. **Dialog d'ajout** : champ nom + dropdown catégorie + slider niveau.

---

## 14. Badges (`badge/badge_screen.dart`)

**Type :** Passeport de badges gamification.

**Layout :**
1. **AppBar** "Passeport de badges" + bouton info (dialog "Comment ça fonctionne" : "Les badges récompensent ton parcours !").
2. **Grille de badges** (icônes rondes) : nom + description + état (débloqué/verrouillé) + date d'obtention.
3. **Bouton "Débloquer"** si non obtenu.
4. **SnackBar** "X nouveau(x) badge(s) débloqué(s) !".

---

## 15. Conseillers — Annuaire (`conseillers/conseillers_screen.dart`)

**Type :** Liste des conseillers d'orientation.

**Layout :**
1. **AppBar** "Annuaire des conseillers".
2. **Barre de recherche** : "Rechercher un conseiller..." + bouton clear.
3. **Liste de cards conseillers** : avatar (initiales) + nom + spécialité + "Envoyer un message" + bouton RDV.
4. **Empty state** : icône `person_search_rounded` 64 px + "Aucun conseiller trouvé".

---

## 16. DataHub — Carte thermique (`datahub/datahub_screen.dart`)

**Type :** Visualisation géographique des établissements.

**Layout :**
1. **AppBar** "Carte thermique" + toggle vue (liste/carte) + bouton refresh.
2. **Vue carte (par défaut)** : FlutterMap avec cercles proportionnels (nombre d'établissements par région).
3. **Vue liste** : cards par région avec stats.
4. **Section "Régions"** : liste avec nom + nombre d'établissements.
5. **Section "Répartition par type"** : camembert/diagramme (public/privé).
6. **Empty state** : icône `map` 64 px + "Aucune donnée régionale disponible".

---

## 17. Entretien Simulé (`entretien/entretien_screen.dart`)

**Type :** Simulation d'entretien d'embauche par IA.

**Layout :**
1. **AppBar** "Entretien simulé".
2. **État initial :** icône `record_voice_over` 72 px + Titre "Simulation d'entretien" + sous-texte "Entraînez-vous avec un recruteur IA".
3. **Formulaire de démarrage :**
   - Champ "Métier visé *" avec `hintText: "Ex: Développeur web, Infirmier, Comptable..."`.
   - Bouton "Commencer l'entretien" (avec icône `play_arrow`).
4. **Mode entretien actif :**
   - Header : titre métier + "Question {X}/{total}"
   - **Card question** : icône `person_outline` + label "Recruteur" + texte question
   - **Champ réponse** (multi-ligne) : `hintText: "Votre réponse..."`
   - **Bouton** "Envoyer la réponse" (avec loader si envoi en cours).

---

## 18. Simulateur de Parcours

### 18.1 Simulateur — Formulaire (`simulateur/simulateur_parcours_screen.dart`)

**Type :** Outil "Et si je choisissais telle série ?".

**Layout :**
1. **AppBar** "Simulateur de parcours".
2. **Titre** "Construis ton scénario" + sous-titre "Découvre les filières, métiers et écoles qui correspondent à tes choix."
3. **Section "Titre du scénario"** : `hintText: "Ex: Si je choisis Série C"`.
4. **Section "Série scolaire"** : dropdown des séries (chargé dynamiquement).
5. **Section "Niveau actuel"** : dropdown (Seconde → BAC+5).
6. **Section "Métier souhaité (optionnel)"** : `hintText: "Ex: Médecin, Architecte..."`.
7. **Bouton** "Lancer la simulation" → POST `/api/v1/simulateur/explorer`.

### 18.2 Simulateur — Résultat (`simulateur/simulateur_resultat_screen.dart`)

**Layout :**
1. **AppBar** "{titre du scénario}".
2. **Hero** : nom série + 3 stats (Filières / Métiers / Établissements).
3. **Score moyen de compatibilité** + durée.
4. **Section "Filières accessibles"** (icône `school`) : cards (titre + résumé + chip domaine).
5. **Section "Métiers possibles"** (icône `work`) : cards (titre + résumé).
6. **Section "Établissements"** (icône `location_city`) : cards (titre + ville + chip Public/Privé).

---

## 19. Recherche Globale (`search/global_search_screen.dart`)

**Type :** Recherche unifiée.

**Layout :**
1. **AppBar** "Recherche".
2. **Barre de recherche** pleine largeur + bouton clear (X) + `prefixIcon: Icons.search_rounded`.
3. **État initial** : icône `search_rounded` 64 px + "Que cherchez-vous ?" + "Filières, métiers, séries, établissements...".
4. **Résultats** : cards (icône colorée par type) + titre + résumé.
5. **Empty state** : icône `search_off_rounded` + "Aucun résultat" + "Essayez d'autres mots-clés".

---

## 20. Messages

### 20.1 Liste des conversations (`messages/messages_list_screen.dart`)

**Type :** Inbox de messagerie.

**Layout :**
1. **AppBar** "Messages" + bouton "X nouveau" (compteur non lus) + FAB `Icons.edit_rounded` (nouvelle conversation).
2. **Liste de conversations** (cards `ListTile`) :
   - Avatar (initiales expéditeur)
   - Nom expéditeur + aperçu dernier message + temps
   - Badge non lu (point d'accent)
   - Swipe-to-delete (`Icons.delete`)
3. **Empty state** : "Aucun message" + "Envoyez votre premier message\nà un conseiller" + bouton "Nouveau message".

### 20.2 Conversation / Chat (`messages/chat_screen.dart`)

**Type :** Chat bilatéral (WebSocket ou polling 15s).

**Layout :**
1. **AppBar** : avatar + nom interlocuteur + statut "En ligne" (WS) ou "Polling 15s" (fallback) + icône `chat_bubble_outline_rounded`.
2. **Zone de messages** (scrollable) :
   - Bulles user (droite)
   - Bulles interlocuteur (gauche)
3. **État initial** : "Commencez la conversation".
4. **Barre de saisie (bas)** : champ texte + bouton send.

### 20.3 Liste des RDV (`messages/rdv_list_screen.dart`)

**Type :** Liste des rendez-vous.

**Layout :**
1. **AppBar** "Mes rendez-vous" + bouton + (FAB dans le body) "Nouveau".
2. **Tabs** : "À venir" / "Passés" (ou upcoming toggle).
3. **Liste de cards RDV** : nom interlocuteur + date/heure + statut (Planifié/Annulé) + actions (Rejoindre visio, Annuler, Valider).
4. **Dialog annulation** : "Annuler ce rendez-vous ?" + date + Garder / Annuler.
5. **Dialog validation** : "Valider ce rendez-vous ?" + Annuler / Valider.

### 20.4 Nouveau RDV (`messages/rdv_screen.dart`)

**Type :** Formulaire de prise de RDV.

**Layout :**
1. **AppBar** "Nouveau rendez-vous" + flèche retour.
2. **Champs :** conseiller (sélection) + date (DatePicker) + heure (TimePicker) + type (Visio/Téléphone/Présentiel) + notes.
3. **Bouton** "Planifier le rendez-vous".
4. **SnackBar** "Rendez-vous planifié avec succès" / "Erreur".

### 20.5 Tickets (`messages/ticket_screen.dart`)

**Type :** Support tickets.

**Layout :**
1. **AppBar** "Mes tickets" + bouton +.
2. **Bottom sheet nouveau ticket :**
   - Champ "Sujet du ticket"
   - Champ "Description"
   - Bouton "Créer un ticket"
3. **Liste de tickets** : sujet + catégorie + statut (OUVERT / EN_COURS / FERME).
4. **Empty state** : "Aucun ticket".

---

## 21. Erreurs

### 21.1 Empty Content (`errors/empty_content_screen.dart`)

**Type :** Écran vide réutilisable.

**Layout :** Icône (paramétrable) + Titre + Sous-texte + Bouton action (paramétrable).

### 21.2 Network Error (`errors/network_error_screen.dart`)

**Type :** Erreur de connexion.

**Layout :** Icône wifi-off + Titre "Connexion perdue" + sous-texte "Impossible de se connecter au serveur. Vérifiez votre connexion internet et réessayez." + Bouton "Réessayer" (avec `Icons.refresh_rounded`).

### 21.3 Not Found (`errors/not_found_screen.dart`)

**Type :** 404.

**Layout :** "404" géant + "Page introuvable" + sous-texte + Bouton "Retour à l'accueil".

---

## 22. 2FA — déjà couvert en §3.8 et §3.9

---

## Notes pour le maquettiste — À COMPLÉTER PAR VOS SOINS

Les éléments suivants sont **à fournir** (non spécifiés dans le code, laissés à votre appréciation) :

### 🎨 Palette de couleurs
- **Couleur primaire** (identité Activ Education) — *à définir*
- **Couleur d'accent** (CTA secondaires, encouragements) — *à définir*
- **Couleur de succès** — *à définir*
- **Couleur d'erreur** — *à définir*
- **Couleur de fond de page** — *à définir*
- **3 niveaux typographiques** (texte foncé / moyen / clair) — *à définir*

### 🔤 Typographie
- Police principale (corps, captions, helpers) — *à définir*
- Police des titres (display, headings) — *à définir*

### 🖼️ Assets visuels
- **[LOGO_ACTIV_EDUCATION]** — à insérer sur Splash, Login
- **Illustrations onboarding** (3 illustrations custom pour les 3 pages d'onboarding)
- **Illustration ORIA** (avatar violet/animé de l'assistant IA)
- **Placeholder avatars** (cercle avec initiales — couleur de fond à définir)
- **Icône empty state documents/réseaux** — *à fournir si besoin*
- **Vignettes établissements** (miniatures pour la carte thermique et la map)

### 📐 Conventions
- Densité : généreuse aération, coins arrondis 12–16 px, ombres douces
- Microcopy : français, tutoiement, ton bienveillant (surtout décrocheur)
- États systématiques à prévoir : empty, loading, erreur réseau, non authentifié
- Composants Material dominants : `Card`, `ListTile`, `Chip`, `BottomSheet`, `Dialog`, `SnackBar`, `FloatingActionButton`, `TabBar`, `TextField` (outline), `DropdownButton`
