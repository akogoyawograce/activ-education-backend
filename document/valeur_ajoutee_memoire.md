# Valeur ajoutée — Projet de mémoire / soutenance

**Auteur** : Professionnel de l'orientation scolaire  
**Projet** : Activ Education (plateforme d'orientation scolaire et professionnelle au Togo)

---

## Idées retenues (totalement inédites — présentes dans aucun projet existant)

### 1. 🎙️ Assistant vocal d'orientation (Voice-First)
- Concept : L'élève parle à l'assistant via un bouton micro. STT (Whisper) → IA → TTS.
- Valeur : Inclusion (illettrisme, timidité, handicap visuel).
- Titre : "Assistant vocal d'orientation inclusive"

### 2. 👥 Réseau social d'orientation
- Concept : Fil d'actualité, partage de parcours, likes, commentaires. Profil public "portfolio orientation".
- Valeur : L'orientation est sociale — les pairs s'influencent plus qu'un algorithme.
- Titre : "Réseau social d'orientation scolaire"

### 3. 🎯 Simulateur de parcours "Et si... ?" (What-If)
- Concept : L'élève construit un scénario (série → notes → établissement) et visualise les débouchés.
- Valeur : Rendre concret l'impact des choix d'orientation.
- Titre : "Simulateur de parcours orientation"

### 4. 📊 Portfolio de compétences dynamique + matching métier
- Concept : L'élève renseigne cours, hobbies, bénévolat, stages, soft skills → carte de compétences → match avec les métiers.
- Valeur : Orientation par les forces, pas les notes. Révèle le potentiel.
- Titre : "Portfolio de compétences augmenté"

### 5. 🤖 Simulation d'entretien d'orientation (AI mock interview)
- Concept : L'IA joue le conseiller, pose des questions, évalue la clarté du projet.
- Valeur : Préparation aux vrais entretiens.
- Titre : "Simulateur d'entretien orientation par IA"

### 6. 🗺️ Carte thermique de l'orientation (DataHub Togo)
- Concept : Dashboard public temps réel : top métiers explorés, séries par région, heatmap Togo.
- Valeur : Outil Ministère, chercheurs, médias. Référence nationale.
- Titre : "Baromètre numérique de l'orientation au Togo"

### 7. 🔐 Passeport numérique de compétences (Open Badges)
- Concept : Badges vérifiables (Open Badge Standard) pour chaque étape franchie.
- Valeur : L'orientation devient valorisable sur le marché du travail.
- Titre : "Badges numériques d'orientation"

---

## Idée recommandée (combinaison gagnante)

> **🎯 Simulateur de parcours (n°3) + 📊 Portfolio de compétences (n°4) + 🗺️ Carte thermique (n°6)**

Ces trois combinés racontent une histoire complète :
*"Je diagnostique mon profil → je construis mon portfolio → je simule mon avenir → je visualise les tendances nationales"*

Rien de tout cela n'existe dans les projets existants.

---

## Plan d'implémentation

Chaque module sera implémenté dans l'ordre suivant :

| # | Module | Backend (Spring Boot) | Frontend (Flutter / React) |
|---|--------|----------------------|---------------------------|
| 1 | Portfolio de compétences | Entities, Service, Controller | Écran de saisie + visualisation radar |
| 2 | Simulateur de parcours | Moteur de simulation, Controller | Interface scénarios + comparaison |
| 3 | Carte thermique | Agrégation stats, Endpoint | Dashboard avec Recharts / graphs |
| 4 | Assistant vocal | STT/TTS + endpoint | Bouton micro + playback |
| 5 | Entretien IA | Prompt + évaluation | Interface chat simulateur |
| 6 | Réseau social | Fil d'actualité, CRUD posts | Timeline + commentaires |
| 7 | Passeport badges | Génération + vérification | Wallet badges |

---

*Document généré le 24 juin 2026*
