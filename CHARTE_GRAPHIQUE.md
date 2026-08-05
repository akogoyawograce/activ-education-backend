# Charte Graphique — Activ Education

> **Source Figma** : les valeurs ci-dessous sont implémentées dans le code.
> Des écarts existent entre mobile (Flutter), backoffice (Tailwind) et la maquette Figma — voir [Discrepances connues](#discrepances-connues).

---

## 1. Logo

- **Fichier** : `activ-education-fronted-main/activ_education/assets/images/logo.jpeg`
- **Adaptive icon** (Android) : fond blanc `#FFFFFF`

---

## 2. Palette de couleurs

### Mobile (Flutter) — `app_theme.dart`

| Token | Hex | Usage |
|-------|-----|-------|
| `primary` | `#1300C8` | Barres, liens, boutons outline, fonds bleus |
| `primaryDark` | `#0F00A0` | Hover / press |
| `primaryLight` | `#4A3DFF` | Variante claire |
| `accent` | `#FFA800` | CTA, boutons remplis (orange) |
| `accentLight` | `#FFD166` | Variante claire |
| `background` | `#FCF8FF` | Fond général (off-white) |
| `backgroundGrey` | `#F4F0FA` | Lavande grisé |
| `textDark` | `#1A1A2E` | Titres |
| `textMedium` | `#454556` | Corps de texte |
| `textLight` | `#B0B7C3` | Sous-titres, hints |
| `textWhite` | `#FFFFFF` | Texte sur fonds colorés |
| `success` | `#10B981` | Vert validation |
| `error` | `#EF4444` | Rouge erreur |
| `warning` | `#F59E0B` | Jaune attention |
| `cardBorder` | `#E5E7EB` | Bordures de cartes |
| `selectedCard` | `#1300C8` | État sélectionné |

### Backoffice (Tailwind v4) — `index.css`

| Token | Hex | Usage |
|-------|-----|-------|
| `primary` | `#3730E8` | Boutons, liens, accents |
| `primary-dark` | `#2a25b0` | Hover |
| `primary-light` | `#e8e7ff` | Fonds de carte |
| `secondary` | `#F59E0B` | Accent secondaire |
| `secondary-light` | `#fef3c7` | Fond light |
| `success` | `#10B981` | Vert |
| `danger` | `#EF4444` | Rouge |
| `text-main` | `#111827` | Texte principal |
| `text-secondary` | `#6B7280` | Texte secondaire |
| `background` | `#F9FAFB` | Fond général |
| `card` | `#FFFFFF` | Fond des cartes |
| `border` | `#E5E7EB` | Bordures |

### Correspondance StatCard (backoffice)

| Variant | Fond | Texte | Icône |
|---------|------|-------|-------|
| primary | `bg-primary-light` / `#e8e7ff` | `#3730E8` | `#3730E8` |
| secondary | `bg-secondary-light` / `#fef3c7` | `#F59E0B` | `#F59E0B` |
| success | `bg-success-light` / `#d1fae5` | `#10B981` | `#10B981` |
| danger | `bg-danger-light` / `#fee2e2` | `#EF4444` | `#EF4444` |

---

## 3. Typographie

### Polices

| Usage | Police | Poids disponibles |
|-------|--------|-------------------|
| Titres | **Poppins** | Regular(400), Medium(500), SemiBold(600), Bold(700), ExtraBold(800) |
| Corps | **Inter** | Regular(400), Medium(500), SemiBold(600), Bold(700), ExtraBold(800) |

### Échelle typographique (Mobile)

| Style | Police | Taille | Poids | Usage |
|-------|--------|--------|-------|-------|
| `displayLarge` | Poppins | 28 | W800 | Écran titre |
| `displayMedium` | Poppins | 24 | W800 | Écran sous-titre |
| `headingLarge` | Poppins | 20 | W700 | Titre de section |
| `headingMedium` | Poppins | 17 | W700 | Titre de carte |
| `headingSmall` | Poppins | 15 | W700 | Petit titre |
| `bodyLarge` | Inter | 15 | W400 | Paragraphe |
| `bodyMedium` | Inter | 14 | W400 | Texte courant |
| `label` | Inter | 13 | W600 | Label de champ |
| `buttonText` | Inter | 16 | W700 | Texte bouton |
| `caption` | Inter | 12 | W400 | Légal, aide |

### Backoffice

- `font-sans` → `'Inter', system-ui, sans-serif`
- Titres libres (pas d'échelle fixe en Tailwind)

---

## 4. Éléments UI

### Boutons

| Type | Mobile | Backoffice |
|------|--------|------------|
| **Primaire (rempli)** | Fond `#FFA800`, texte blanc, radius 14, hauteur 54px | Fond `#3730E8`, hover `#2a25b0` |
| **Outline** | Bordure `#1300C8` 1.5px, texte `#1300C8`, radius 14 | — |
| **Radius cartes** | — | `12px` (`--radius-card`) |

### Champs de saisie

| Propriété | Mobile | Backoffice |
|-----------|--------|------------|
| Radius | 12px | `rounded-lg` |
| Bordure défaut | `#E5E7EB` 1.5px | `border-border` |
| Bordure focus | `#1300C8` 2px | `ring-primary/30` |
| Padding | horizontal 16, vertical 16 | standard |

---

## 5. Ombres et arrière-plans

- **Backoffice login** : `bg-gradient-to-br from-primary/5 via-background to-primary/10`
- **Backoffice carte** : `shadow-lg border border-border`
- **Mobile** : Material 3, pas d'ombre explicite dans le thème

---

## 6. Discrepances connues

| Point | Mobile (Flutter) | Backoffice (Tailwind) | Mémoire / Figma |
|-------|-----------------|----------------------|-----------------|
| Primaire | `#1300C8` | `#3730E8` | `#4F46E5` (mémoire) |
| Secondaire | `#FFA800` | `#F59E0B` | `#10B981` (mémoire) |
| Fond | `#FCF8FF` | `#F9FAFB` | — |
| Texte titre | `#1A1A2E` | `#111827` | — |

> **Constat 2026-08-03** : les valeurs `#1300C8` (mobile) et `#3730E8` (backoffice) sont **toutes deux actives dans le code** (`app_theme.dart:5` et `index.css:4` respectivement). La valeur legacy `#3D35D9` mentionnée dans les anciens rapports (`resultat.md`, `rapport-2026-05-20.md`) n'est plus utilisée.
>
> **Décision à prendre** :
> - **Option A (recommandée)** : adopter `#1300C8` partout (mobile + backoffice) — aligne avec Figma `#4F46E5` à 1 pas près, et c'est la valeur la plus présente dans le code (mobile).
> - **Option B** : migrer mobile vers `#3730E8` (impact sur 58 écrans Flutter, refonte visuelle importante).
> - **Option C** : conserver les deux valeurs actuelles et reconnaître officiellement la divergence mobile/backoffice.
>
> Tant que la décision n'est pas tranchée : **ne pas modifier** les valeurs dans le code. Toute évolution passe par un RFC + validation visuelle.

> **TODOs actifs** :
> - Trancher l'option A/B/C ci-dessus
> - Uniformiser police en Inter/Poppins partout (anciennement Nunito — migration partielle)
> - Vérifier la valeur Figma exacte `#4F46E5` dans la maquette source (mémoire ou capture)

---

## 7. Fichiers sources

- Mobile theme : `activ_education/lib/theme/app_theme.dart`
- Mobile fonts : `activ_education/assets/fonts/`
- Mobile pubspec : `activ_education/pubspec.yaml` (lignes 62–84)
- Backoffice theme : `backoffice/src/index.css`
- Design doc : `seed/prompt-flutter.md`
- Mémoire projet : `memoire_activ_education.md`