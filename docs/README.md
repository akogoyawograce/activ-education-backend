# 📚 Documentation Activ Education — Index

> Ce dossier centralise **toute la documentation** du projet Activ Education, organisée par thème.
> Source unique de vérité pour le stage à HubCity/Woélab (Institut Polytechnique DEFITECH).

---

## 🗂️ Arborescence

```
docs/
├── README.md         # ← ce fichier (index)
├── memoire/          # 8 documents pour la rédaction du mémoire
├── projet/           # 4 documents techniques du projet
├── rapports/         # 4 rapports de sessions et résultats
└── prompts-ia/       # 4 prompts et instructions pour l'IA (Claude, Copilot)
```

---

## 📘 `docs/memoire/` — Documents pour le mémoire

> 8 documents **numérotés** à utiliser dans l'ordre logique de rédaction.

| # | Fichier | Rôle | Pages |
|---|---------|------|-------|
| 01 | `01-cahier-charges-fonctionnel.md` | **Cahier des charges fonctionnel** (périmètre contractualisé) | ~15 |
| 02 | `02-cahier-charges-technique.md` | **Cahier des charges technique** (architecture cible) | ~13 |
| 03 | `03-specifications-techniques.md` | Spécifications techniques par module (JWT, JPA, sécurité) | ~17 |
| 04 | `04-problematique-recherche.md` | Problématique, sous-problèmes, hypothèses, indicateurs | ~3 |
| 05 | `05-valeur-ajoutee-memoire.md` | 7 idées innovantes à valoriser dans le mémoire | ~3 |
| 06 | `06-maquettes-ui-ux.md` | Maquettes Banai.cio + palette couleurs + specs UI | ~21 |
| 07 | `07-description-pour-theme.md` | Description synthétique du projet (pour générer un thème) | ~5 |
| 08 | `08-annexe-modules-complementaires.md` | **23 modules ajoutés** au-delà du cahier des charges | ~10 |
| 09 | `09-theme-memoire-activ-education.md` | **Thème retenu + variantes** pour le mémoire | ~2 |

### 📌 Ordre de lecture recommandé

1. **`04-problematique-recherche.md`** → pose le contexte de recherche
2. **`01-cahier-charges-fonctionnel.md`** → définit le périmètre contractualisé (5 modules)
3. **`02-cahier-charges-technique.md`** + **`03-specifications-techniques.md`** → détails d'implémentation
4. **`08-annexe-modules-complementaires.md`** → extensions (23 modules ajoutés)
5. **`05-valeur-ajoutee-memoire.md`** → argumentaire des innovations à valoriser
6. **`06-maquettes-ui-ux.md`** → design et UX de la plateforme
7. **`07-description-pour-theme.md`** → pour générer le thème du mémoire

---

## 📗 `docs/projet/` — Documentation du projet

| # | Fichier | Rôle |
|---|---------|------|
| 01 | `01-description-projet.md` | Description technique complète (architecture, stack, fonctionnalités) |
| 02 | `02-charte-graphique.md` | Identité visuelle, couleurs, typographies |
| 03 | `03-reference-projet.md` | Document de référence global (synthèse pour l'IA / nouveaux venus) |
| 04 | `04-fonctionnalites.md` | Inventaire complet des fonctionnalités (mobile, backoffice, backend) |

---

## 📕 `docs/rapports/` — Rapports de session et résultats

| # | Fichier | Contenu |
|---|---------|---------|
| 01 | `01-resultats-prototype.md` | Résultats du prototype |
| 02 | `02-session-ses-104b.md` | Notes de session 104b (juillet 2026) |
| 03 | `03-rapport-session-2026-07-17.md` | Rapport de session 17 juillet 2026 |
| 04 | `04-recap-session-2026-08-06.md` | Récap session 6 août 2026 |

---

## 🤖 `docs/prompts-ia/` — Prompts et instructions pour l'IA

| # | Fichier | Rôle |
|---|---------|------|
| 01 | `01-prompt-claude-module-prediction.md` | Prompt pour le module prédiction (Phase 5) |
| 02 | `02-prompt-claude-memoire.md` | Prompt de rédaction du mémoire |
| 03 | `03-instructions-claude.md` | Personnalisation Claude (mémoire stage) |
| 04 | `04-collecte-donnees-entrainement.md` | Données d'entraînement (consignes) |

---

## 🗑️ Nettoyage effectué (2026-08-25)

> ✅ **Tous les doublons ont été supprimés**. Le dossier `document/` a été retiré. Seuls les fichiers de la racine qui sont **encore valides** (instructions IA, prompts, mémoire en cours, rapports) sont conservés.

### Fichiers supprimés (doublons)
- `DESCRIPTION_PROJET.md`, `DESCRIPTION_PROJET_POUR_THEME.md`, `CAHIER_DES_CHARGES_ANNEXE.md`
- `CHARTE_GRAPHIQUE.md`, `ACTIV_EDUCATION_REFERENCE.md`
- `document/*` (13 fichiers) — tout le dossier a été supprimé

### Fichiers racine conservés (non doublons)
| Fichier | Raison |
|---------|--------|
| `CLAUDE.md`, `AGENTS.md` | Instructions pour l'IA (Claude/Copilot) |
| `instructions_claude.md` | Personnalisation du mémoire |
| `prompt_claude_memoire.md` | Prompt de rédaction du mémoire |
| `memoire_activ_education.md` | Mémoire en cours de rédaction |
| `RAPPORT_SESSION_2026-07-17.md`, `RECAP_SESSION_2026-08-06.md` | Rapports de session |
| `RESULTATS_PROTOTYPE.md`, `Rapport de collecte...` | Rapports de recherche |
| `Plan structuré de constitution du dataset .md` | Plan dataset |
| `collecte_donnees_entrainement.md` | Données d'entraînement |
| `PROMPT_CLAUDE_CODE_MODULE_PREDICTION (1).md` | Prompt module prédiction |
| `session-ses_104b.md` | Notes de session |

---

## 🔗 Liens rapides

### Pour le mémoire (DEFITECH)
- 📄 Canevas : `Caneva Redation mémoire Génie Logiciel.pdf` (à la racine)
- 📄 Mémoire en cours : `memoire_activ_education.md` (à la racine)
- 📄 Prompt mémoire : `prompt_claude_memoire.md` (à la racine)

### Documentation projet (HubCity/Woélab)
- 📄 Cahier de charge original (côté HubCity) : `activ-education-fronted-main/seed/cahier_de_charge.md`
- 📄 État du projet : `activ-education-fronted-main/seed/etat-projet.md`

### Pour l'IA (Claude/Copilot)
- 🤖 Instructions : `CLAUDE.md`, `AGENTS.md`, `instructions_claude.md`
- 🤖 Prompt module prédiction : `PROMPT_CLAUDE_CODE_MODULE_PREDICTION (1).md`

---

## 📊 Statistiques de la documentation

| Catégorie | Documents | Lignes |
|-----------|-----------|--------|
| Mémoire | 8 | ~87 pages |
| Projet | 4 | ~50 pages |
| Rapports | 4 | ~40 pages |
| Prompts IA | 4 | ~15 pages |
| **Total organisé** | **20** | **~192 pages** |

---

## ✅ Nettoyage effectué (2026-08-25)

**Phase 1** : Création de `docs/` avec 12 documents numérotés + index
**Phase 2** : Suppression des doublons racine + `document/`
**Phase 3** : Réorganisation finale de la racine — 12 fichiers .md → **3 fichiers essentiels** :
- `AGENTS.md`, `CLAUDE.md` (instructions IA lues au démarrage)
- `memoire_activ_education.md` (mémoire en rédaction)

**Déplacements** :
- `RAPPORT_SESSION_*`, `RECAP_SESSION_*`, `RESULTATS_PROTOTYPE.md`, `session-ses_104b.md` → `docs/rapports/`
- `prompt_claude_memoire.md`, `instructions_claude.md`, `PROMPT_CLAUDE_CODE_*`, `collecte_donnees_entrainement.md` → `docs/prompts-ia/`
- 2 doublons racine (`Plan structuré...`, `Rapport de collecte...`) → supprimés (versions identiques dans `donnée/`)

---

> 🎯 **Source de vérité** : `docs/`. Les 3 fichiers à la racine sont les seuls justifiés (instructions IA + mémoire en cours).
