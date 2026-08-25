# 📚 Documentation Activ Education — Index

> Ce dossier centralise **toute la documentation** du projet Activ Education, organisée par thème.
> Source unique de vérité pour le stage à HubCity/Woélab (Institut Polytechnique DEFITECH).

---

## 🗂️ Arborescence

```
docs/
├── memoire/          # Documentation pour la rédaction du mémoire de Licence Pro
├── projet/           # Documentation technique et fonctionnelle du projet
├── rapports/         # Rapports de sessions, audits, diagnostics
└── README.md         # ← ce fichier (index)
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

## 📕 `docs/rapports/` — Rapports et comptes-rendus

*(à compléter avec les rapports de session, audits, diagnostics)*

| Fichier | Contenu |
|---------|---------|
| *(vide)* | Rapports de session, audits dette technique, bilans |

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
| **Total organisé** | **12** | **~137 pages** |

---

> ✅ **Nettoyage effectué** : tous les doublons ont été supprimés. La source de vérité est désormais **`docs/`**.
