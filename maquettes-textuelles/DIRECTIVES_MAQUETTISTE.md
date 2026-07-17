# 📐 Directives pour le maquettiste — Ordre de création + couleurs

> Document de cadrage destiné au maquettiste. Il précise **dans quel ordre créer les pages** (par lots logiques) et fournit **les couleurs et styles exacts** à utiliser sur le projet.

---

## 🎨 Palette de couleurs OFFICIELLE (extraite du code)

Le projet dispose déjà d'un design system codé. **Utilise ces couleurs**, ne les invente pas.

### Mobile (Flutter — `AppColors`)

| Token | Hex | Usage |
|---|---|---|
| `primary` | `#1300C8` | Couleur principale (boutons primaires, AppBar, liens actifs, onglets sélectionnés) |
| `primaryDark` | `#0F00A0` | Hover/pressed des boutons primaires |
| `primaryLight` | `#4A3DFF` | États actifs, focus, accents lumineux |
| `accent` | `#FFA800` | CTA secondaires, encouragements, badges (orange/jaune) |
| `accentLight` | `#FFD166` | Variante claire de l'accent |
| `background` | `#FCF8FF` | Fond de page (beige légèrement lilas) |
| `backgroundGrey` | `#F4F0FA` | Sections secondaires, chips |
| `backgroundBlue` | `#1300C8` | Cartes spéciales (même que primary) |
| `textDark` | `#1A1A2E` | Titres, texte principal |
| `textMedium` | `#454556` | Corps de texte, labels |
| `textLight` | `#B0B7C3` | Captions, placeholders, texte secondaire |
| `textWhite` | `#FFFFFF` | Texte sur fond coloré |
| `success` | `#10B981` | Validations, états OK, badges succès |
| `error` | `#EF4444` | Erreurs, suppressions, validations failed |
| `warning` | `#F59E0B` | Avertissements |
| `cardBorder` | `#E5E7EB` | Bordures de cartes |
| `selectedCard` | `#1300C8` | Cartes sélectionnées (filtre, rôle, etc.) |

### Backoffice (React — `tailwind.config`)

| Token | Hex | Usage |
|---|---|---|
| `primary` | `#3730E8` | Couleur principale (boutons primaires, liens actifs) |
| `primary-dark` | `#2a25b0` | Hover des boutons primaires |
| `primary-light` | `#e8e7ff` | Fonds teintés, états hover, badges |
| `secondary` | `#F59E0B` | Couleur d'accent (warning, certains CTA) |
| `secondary-light` | `#fef3c7` | Fond des alertes warning |
| `success` | `#10B981` | Validations |
| `success-light` | `#d1fae5` | Fond des alertes succès |
| `danger` | `#EF4444` | Erreurs, suppressions |
| `danger-light` | `#fee2e2` | Fond des bandeaux d'erreur |
| `text-main` | `#111827` | Texte principal, titres |
| `text-secondary` | `#6B7280` | Sous-titres, descriptions |
| `text-light` | `#9CA3AF` | Captions, metadata |
| `background` | `#F9FAFB` | Fond de page |
| `card` | `#FFFFFF` | Fond des cartes |
| `border` | `#E5E7EB` | Bordures |

> ⚠️ **Note** : les deux projets utilisent des nuances légèrement différentes de bleu/primary (le mobile est plus violet `#1300C8`, le backoffice est plus bleu royal `#3730E8`). **C'est volontaire** : ils ont chacun leur identité. Ne pas harmoniser.

---

## 🔤 Typographie

- **Mobile (Flutter) :**
  - Titres (`displayLarge`, `headingLarge`, etc.) : **Poppins** SemiBold/Bold
  - Corps (`bodyLarge`, `bodyMedium`, etc.) : **Inter** Regular/Medium
  - Boutons : Inter SemiBold
  - Couleurs : `textDark` pour titres, `textMedium` pour corps
- **Backoffice (React) :**
  - Police unique : **Inter** (système ou Google Fonts)
  - Tailles Tailwind par défaut (`text-sm`, `text-base`, `text-lg`, `text-2xl`, etc.)
  - Couleurs : `text-main` pour titres, `text-secondary` pour descriptions

---

## 📱 LOT MOBILE — Ordre de création

> **Logique** : on commence par les pages publiques/communes (sans connexion), puis l'authentification, puis le cœur de l'app élève, puis les variantes de rôle, puis les features secondaires.

### **LOT 1 — Fondations (5 pages)** — *À créer en premier*

| # | Page | Couleurs principales | Notes |
|---|---|---|---|
| 1 | **Splash** | `primary` (logo, spinner), `background` (fond), `textWhite` (texte sur logo si besoin) | Centré, plein écran, sans AppBar |
| 2 | **Onboarding** (3 pages) | `primary` (dots, illustrations), `textDark` (titres), `textMedium` (sous-texte) | Illustrations custom placeholder |
| 3 | **Login** | `primary` (bouton, liens), `accent` (mot de passe oublié), `error` (snackbar) | Logo centré, fond `background` |
| 4 | **Register — Étape 1** | `primary` (bouton Continuer), `cardBorder` (champs) | Sélecteur pays en bottom sheet |
| 5 | **Register — Profile Setup** | `primary` (carte sélectionnée, bouton), `accent` (badges rôles) | Grille 2×3 de rôles |

### **LOT 2 — Authentification avancée (5 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 6 | **Register — Étape 2 Préférences** | `primary` (chips sélectionnés, bouton) |
| 7 | **OTP** (vérif téléphone) | `primary` (cases remplies, bouton), `error` (snackbar) |
| 8 | **Forgot Password** | `primary` (bouton), `error` (toast) |
| 9 | **Reset Password** | `primary` (bouton), indicateur de force (rouge→orange→vert) |
| 10 | **2FA Setup** + **2FA Verify** | `success` (état activé, icône shield), `primary` (boutons normaux) |

### **LOT 3 — Coeur élève (5 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 11 | **Main Scaffold + BottomNav** (5 onglets) | `primary` (onglet actif, FAB), `textLight` (inactif) |
| 12 | **Dashboard Bachelier** | `primary` (carte action requise), `accent` (highlight), stats `success`/`warning` |
| 13 | **Explorer Home** | `primary` (tabs, recherche), `accent` (badge notification) |
| 14 | **Category List** | `primary` (titre), chips couleurs par type de fiche |
| 15 | **Fiche Detail** | `primary` (favori, partage), gradient sombre sur hero |

### **LOT 4 — Explorer + Détails (5 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 16 | **Favoris** | `primary` (icône bookmark), `error` (swipe delete) |
| 17 | **Carte Établissements** (map) | `primary` (markers), `cardBorder` (bottom sheet) |
| 18 | **Filtres Catalogue** (bottom sheet) | `primary` (chips actifs), `success` (switch actif) |
| 19 | **Diagnostic — Quiz liste** | `primary` (cards), `accent` (badges type) |
| 20 | **Diagnostic — Quiz questions** | `primary` (option sélectionnée, bouton suivant), `accent` (catégorie RIASEC) |

### **LOT 5 — Diagnostic (5 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 21 | **Résultats Diagnostic** | `success` (score %), `primary` (filière du moment), `accent` (badge IA) |
| 22 | **Notes** | `accent` (étoiles), `primary` (bouton save), `error` (swipe delete) |
| 23 | **OCR Bulletin** | `primary` (icône scan, bouton), `success` (notes extraites) |
| 24 | **Diagnostic Enfant** (vue parent) | `primary` (badges séries A/C/D/E/F/G, couleurs distinctes) |
| 25 | **Recommandation IA** | `accent` (icône auto_awesome), `primary` (bouton Actualiser) |

### **LOT 6 — Variantes de dashboards (4 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 26 | **Dashboard Conseiller** | `primary` (header), `success` (RDV), `accent` (toggle dispo) |
| 27 | **Dashboard Décrocheur** | `accent` (carte encouragement orange), `primary` (step cards) |
| 28 | **Dashboard Parent** | `primary` (header famille), `accent` (notification), `success` (score enfant) |
| 29 | **Dashboard Reconversion** | `accent` (citation), `success` (carte financements), `primary` (parcours) |

### **LOT 7 — Profil, suivi, social (8 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 30 | **Profile** | `primary` (bouton Enregistrer), `error` (Déconnexion) |
| 31 | **Enfant Suivi** (parent) | `primary` (sections), `success`/`warning`/`error` (notes par score) |
| 32 | **Historique** | `primary` (icônes), `textLight` (dates) |
| 33 | **FAQ** | `primary` (recherche, chips catégorie actifs) |
| 34 | **Notifications** | Couleurs par type : `primary` (RDV), `accent` (RECOMMANDATION), `success` (MESSAGE), `warning` (DIAGNOSTIC) |
| 35 | **Support** | `primary` (cards cliquables) |
| 36 | **ORIA Chat** | `primary` (bulles user, avatar), `backgroundGrey` (bulles assistant) |
| 37 | **Réseau — Fil** | `primary` (avatar, boutons), `accent` (like) |

### **LOT 8 — Features secondaires (6 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 38 | **Documents** | `primary` (FAB), `error` (delete) |
| 39 | **Témoignages** | `primary` (badge "À la une"), `accent` (étoile témoignage) |
| 40 | **Portfolio** | `accent` (étoiles niveau 1-5), `primary` (FAB add) |
| 41 | **Badges** | `accent` (badges débloqués), `textLight` (badges verrouillés) |
| 42 | **Conseillers — Annuaire** | `primary` (avatar), `accent` (spécialités chips) |
| 43 | **DataHub — Carte thermique** | `accent` (cercles proportionnels), `primary` (titre) |

### **LOT 9 — Outils & recherche (4 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 44 | **Entretien Simulé** | `primary` (card recruteur), `success` (envoyer réponse) |
| 45 | **Simulateur — Formulaire** | `primary` (bouton Lancer), `accent` (sections) |
| 46 | **Simulateur — Résultat** | `success` (score compatibilité), `primary` (sections) |
| 47 | **Recherche Globale** | `primary` (recherche), `textLight` (empty state) |

### **LOT 10 — Messages (5 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 48 | **Liste des conversations** | `primary` (FAB edit), `accent` (badge non lu), `error` (swipe delete) |
| 49 | **Conversation / Chat** | `primary` (bulles user), `backgroundGrey` (bulles interlocuteur) |
| 50 | **Liste des RDV** | `primary` (bouton rejoindre visio), `success` (RDV confirmé), `error` (annulé) |
| 51 | **Nouveau RDV** | `primary` (bouton planifier) |
| 52 | **Tickets** | `primary` (chips statut), `accent` (catégorie GENERAL) |

### **LOT 11 — Erreurs (3 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 53 | **Empty Content** | `textLight` (icône), `primary` (bouton action) |
| 54 | **Network Error** | `error` (icône wifi-off), `primary` (bouton Réessayer) |
| 55 | **Not Found (404)** | `primary` (bouton retour), `textLight` (404 géant) |

**Total mobile : 55 pages** (certaines ont des variantes, comme 2FA setup/verify qui sont 2 écrans dans le même lot).

---

## 🖥️ LOT BACKOFFICE — Ordre de création

> **Logique** : on commence par le login, puis on remonte la hiérarchie : SuperAdmin → Admin → Conseiller (les pages conseiller sont souvent les plus simples).

### **LOT 1 — Authentification (2 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 1 | **Login** (avec variante 2FA) | `primary` (bouton, lien), `danger-light` (bandeau erreur) |
| 2 | **Not Found (404)** | `primary` (bouton retour), `primary-light` (cercle icône) |

### **LOT 2 — SuperAdmin (3 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 3 | **SuperAdmin Dashboard** | `primary` (bouton Nouvel admin), `danger-light` (dialog suppression) |
| 4 | **Logs / Audit** | 3 cards d'actions rapides en `primary-light`, badges status (info/warn/error) colorés |
| 5 | **Paramètres système** | `success` (intégrité 100%), `warning` (badge démo ambre), `danger` (mode maintenance) |

### **LOT 3 — Admin Dashboard + Profil (2 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 6 | **Admin Dashboard** | KPI cards en `primary`/`secondary`/`success`/`danger` (1 couleur par KPI), charts Recharts |
| 7 | **Admin — Mon Profil** | `primary` (bouton modifier), `primary-light` (avatar bg) |

### **LOT 4 — Admin — Gestion utilisateurs (3 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 8 | **Élèves** | `primary` (bouton actions), `danger` (delete), chips par statut |
| 9 | **Parents** | Idem + section "Enfants liés" avec chips `primary-light` |
| 10 | **Conseillers** | Cards en grille, spécialités en chips colorés |

### **LOT 5 — Admin — Bibliothèque (4 pages)** — *même pattern*

| # | Page | Couleurs principales |
|---|---|---|
| 11 | **Établissements** | `primary` (boutons), `success` (Public), `warning` (Privé) |
| 12 | **Filières** | `primary`, chips par `Domaine` |
| 13 | **Métiers** | `primary`, chips par `Secteur` |
| 14 | **Séries** | `primary`, chips par `Niveau` |

### **LOT 6 — Admin — Diagnostic (4 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 15 | **Quiz — Liste** | `primary` (bouton nouveau), `success` (Publié), `text-light` (Brouillon) |
| 16 | **Quiz Editor** | `primary` (bouton ajouter option, sauvegarder), `success` (option correcte) |
| 17 | **Seuils d'admission** | `primary` (boutons actions) |
| 18 | **Matrices de scores** | `primary`, 3 inputs pondérés (somme = 100) |

### **LOT 7 — Admin — Modération & Stats (3 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 19 | **FAQ Modération** | `success` (Valider), `danger` (Rejeter), `primary` (Modifier) |
| 20 | **Notifications** | Icônes colorées par type (MESSAGE `primary` / DIAG `primary-light` / RDV `success` / RECO `warning`) |
| 21 | **Statistiques Admin** | KPI cards, charts Recharts (couleurs `primary`/`secondary`/`success`/`danger`) |

### **LOT 8 — Conseiller — Pages principales (5 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 22 | **Conseiller Dashboard** | KPI cards, `primary` (bouton Rejoindre) |
| 23 | **Profil Conseiller** | `primary` (avatar), `primary-light` (chips spécialités) |
| 24 | **Rendez-vous** | Chips par type (VISIO `primary` / TÉLÉPHONE `secondary` / PRÉSENTIEL `success`), statuts colorés |
| 25 | **Messagerie** | Bulles user `primary`, bulles contact `background`, temps relatifs |
| 26 | **ORIA (backoffice)** | Bulles user `primary`, bulles assistant `background` avec avatar ORIA |

### **LOT 9 — Conseiller — Annexes (3 pages)**

| # | Page | Couleurs principales |
|---|---|---|
| 27 | **FAQ Conseiller** | `primary` (chips catégorie), `success`/`danger` (toggle publier) |
| 28 | **Statistiques Conseiller** | KPI + charts Recharts |
| 29 | **Utilisateurs (lecture)** | Cards élèves/parents, badges statut |

**Total backoffice : 29 pages**.

---

## 🚀 Résumé — Ordre global

### À fournir en PREMIER (fondations critiques)

1. **Mobile LOT 1** (5 pages) + **Backoffice LOT 1** (2 pages) = 7 pages
   - Permet de valider le style, les couleurs, la typographie, la sidebar
2. **Backoffice LOT 2-3** (SuperAdmin + Admin Dashboard + Profil) = 5 pages
   - Couvre la navigation principale admin
3. **Mobile LOT 3** (Coeur élève : MainScaffold + Bachelier + Explorer) = 5 pages
   - Couvre la nav principale élève

### Puis (cœur applicatif)

4. **Mobile LOT 4-5** (Explorer détails + Diagnostic) = 10 pages
5. **Backoffice LOT 4-7** (Gestion utilisateurs + Bibliothèque + Diagnostic + Modération) = 14 pages
6. **Mobile LOT 6-7** (Variantes rôles + Profil/Suivi/Social) = 12 pages

### Enfin (secondaire)

7. **Mobile LOT 8-11** = 18 pages
8. **Backoffice LOT 8-9** = 8 pages

---

## 💡 Conseils pratiques pour le maquettiste

1. **Commence par la palette et la typographie** — Définis 4-5 styles Figma principaux (bouton primaire, bouton outline, input, card, badge) avant toute page.
2. **Crée un fichier de composants réutilisables** (Figma Components) : `Button`, `Input`, `Card`, `Modal`, `Chip`, `TabBar`, `Sidebar` — réutilise-les partout.
3. **Pour chaque lot, attends la validation avant de passer au suivant** — cela évite de tout refaire si un style est contesté.
4. **Mobile et Backoffice peuvent se faire en parallèle** (2 maquettistes ou 2 sessions Figma distinctes) car leurs designs systems sont différents.
5. **Les pages d'erreur (LOT 11 mobile, LOT 1 backoffice) sont en dernier** — elles n'ont pas de visuel riche à valider.
6. **Pense responsive mobile dès le départ** : 360 px (petit téléphone), 414 px (iPhone), 600 px (tablette) — même si l'app ne cible que Android/iOS.
7. **N'oublie pas les états** : pour chaque page clé, prévois les variantes empty / loading / erreur.

---

## 📦 Livrables attendus (à confirmer avec le maquettiste)

- **Format** : Figma (de préférence), Adobe XD, ou images PNG/JPG haute résolution
- **Résolution** : @2x minimum pour mobile
- **Couleurs** : au format HEX (déjà fournies dans ce document)
- **Polices** : Inter + Poppins (Google Fonts, gratuites)
- **Nomenclature fichiers** : `mobile-XX-nom-page.png` et `backoffice-XX-nom-page.png` pour s'aligner sur l'ordre des lots
