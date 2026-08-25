# Prompt pour Claude Code — Module Prédiction & Recommandation IA (Activ Education)

> À donner tel quel à Claude Code, avec en pièces jointes : `FONCTIONNALITES.md`,
> `generate_synthetic_data.py`, `train_model.py` (déjà produits), et l'accès au repo
> `activ-education-fronted-main/`.

---

## 1. Contexte du projet

Activ Education est une plateforme d'orientation scolaire et professionnelle pour le
Togo (Woélab/HubCity), avec trois clients consommant un même backend :

- **Mobile** : Flutter/Dart, `setState`, Dio + intercepteur JWT
- **Backoffice** : React 19 + TypeScript, Zustand, TanStack Query v5
- **Backend** : Spring Boot (Java 21), PostgreSQL 16 + pgvector, Redis, MinIO

Le backend expose déjà des modules fonctionnels matures : authentification/JWT,
bibliothèque de fiches (Série/Filière/Métier/Établissement) avec recherche
sémantique RAG (embeddings OpenAI 768 dim + pgvector), diagnostic RIASEC avec score
pondéré 60/40 (aspirations × réalité académique), messagerie/RDV, et un assistant
conversationnel ORIA (cascade Ollama → Groq → OpenAI).

**Ne pas reconstruire ces modules existants.** Le travail demandé ici est un module
**nouveau et complémentaire** : un moteur de recommandation/prédiction qui s'appuie
sur de vraies données de résultats (et non uniquement sur un score calculé au
moment du choix).

---

## 2. Objectif de la mission

Construire, de bout en bout, le module **Prédiction & Recommandation** suivant :

1. Diagnostics **différenciés par niveau scolaire** (collège / lycée / université),
   avec un test de personnalité RIASEC **universel** partagé par tous les niveaux.
2. Liste de filières **filtrée selon le niveau actuel** choisi par l'élève.
3. Ajout **optionnel** du bulletin/relevé de notes des **deux classes précédentes**
   ET **de l'année en cours** (même partielle — ex. seulement le 1er trimestre)
   pour calculer une **trajectoire académique** sur 3 points (progression/
   régression), pas seulement une moyenne instantanée.
4. Un moteur de recommandation combinant **trois signaux** :
   - **Aspiration** (scores RIASEC)
   - **Réalité académique** (notes + trajectoire sur 2 ans)
   - **Intérêt comportemental** (historique de consultation, favoris, recherches
     RAG — à partir des données déjà collectées par les modules existants)
5. Un pipeline de collecte du **résultat réel** (`orientation_outcome`) pour pouvoir,
   à terme, entraîner un vrai modèle supervisé au lieu d'une règle pondérée fixe.
6. Un prototype Python validé sur données synthétiques **avant** toute écriture
   côté backend Java, pour vérifier que l'approche tient avant d'investir du temps
   de développement.

---

## 3. Contraintes techniques à respecter impérativement

- **Java 21**, Spring Boot. Toute nouvelle entité étend `BaseEntity` (`Long` PK +
  UUID `trackingId` exposé dans les URLs REST — jamais l'ID interne).
- Le polymorphisme JPA existant utilise la stratégie **JOINED** avec
  `@SuperBuilder` (Lombok) pour `Fiche` et `Utilisateur`. Toute nouvelle hiérarchie
  de classes doit suivre le même pattern si elle en a besoin.
- **Pas de Flyway/Liquibase** — le projet utilise `ddl-auto=update`. Attention aux
  migrations destructives ; documenter tout changement de schéma dans un fichier
  `CHANGELOG_SCHEMA.md`.
- **JWT stateless**, CSRF désactivé, CORS multi-origines déjà configuré — ne pas
  toucher à la config sécurité existante sauf si strictement nécessaire.
- Cascade IA existante : **Ollama local → Groq → OpenAI**. Si le nouveau moteur de
  recommandation a besoin d'appeler un LLM (explications textuelles des
  recommandations, par ex.), réutiliser cette cascade, ne pas en créer une autre.
- Embeddings : `text-embedding-3-small` (OpenAI) → 768 dim → pgvector, cosinus
  (`<=>`). Réutiliser cette pipeline pour le signal "intérêt comportemental" basé
  sur les recherches RAG.
- Pas de WebSocket dans ce projet (polling 4s pour le chat) — rester cohérent,
  aucune fonctionnalité temps réel requise ici.

---

## 4. Plan de réalisation — à suivre dans cet ordre

### Phase 0 — Prototype Python (hors backend, validation de l'approche)

Objectif : prouver que le modèle apprend quelque chose de cohérent avant d'écrire
la moindre ligne Java.

- Reprendre et étendre `generate_synthetic_data.py` pour simuler, en plus de
  l'existant (RIASEC, moyenne générale, filière choisie, statut, satisfaction) :
  - `niveau_actuel` (COLLEGE, LYCEE_2ND, LYCEE_1ERE, LYCEE_TLE, BAC_1, BAC_2, BAC_3)
  - `notes_n2`, `notes_n1` (moyennes des deux années précédentes) et
    `notes_actuelle` (moyenne de l'année en cours, potentiellement partielle —
    simuler un flag `annee_partielle` avec moins de matières renseignées) →
    `tendance_notes = pente(notes_n2, notes_n1, notes_actuelle)` (régression
    linéaire simple sur les 3 points plutôt qu'une simple différence à 2 points)
  - un score d'intérêt comportemental simulé par filière : `nb_consultations`,
    `en_favori` (bool), `score_similarite_recherche` (0 à 1)
- Étendre `train_model.py` pour inclure ces nouvelles variables et comparer les
  performances (ROC-AUC, feature importances) avec et sans le signal
  comportemental.
- Produire un court rapport (`RESULTATS_PROTOTYPE.md`) : quelles variables
  comptent le plus, est-ce que le signal comportemental apporte un vrai gain ou
  seulement du bruit, et quel est le risque de "bulle de filtre" observé.

**Critère d'acceptation** : le modèle prototype tourne, le rapport est lisible et
compare clairement les configurations de variables.

### Phase 1 — Modèle de données (Spring Boot / PostgreSQL)

Ajouter, en respectant les conventions du §3 :

- **Enum `NiveauScolaire`** : `COLLEGE`, `LYCEE_2ND`, `LYCEE_1ERE`, `LYCEE_TLE`,
  `BAC_1`, `BAC_2`, `BAC_3`, etc. — champ sur l'entité `Eleve`.
- **Champ `niveau_requis`** sur `Filiere` (ou table de mapping niveau ↔ filières
  éligibles, si une filière peut cibler plusieurs niveaux).
- **Table `notes_historique`** : `eleve_id`, `annee_scolaire`, `classe`, `matiere`,
  `moyenne`, `est_partielle` (bool — pour l'année en cours si seul un trimestre est
  disponible). Couvre les 2 classes précédentes **et** l'année en cours ; permet de
  calculer la trajectoire sur 3 points au lieu de 2.
- **Table `diagnostic_template`** : ajouter un champ `niveau_cible` pour que
  `QuizEditorPage` (backoffice) puisse créer un quiz différent par niveau, sans
  dupliquer le moteur de quiz existant. Le quiz RIASEC lui-même reste unique et
  sans `niveau_cible` (universel).
- **Table `orientation_outcome`** :
  `id`, `tracking_id`, `eleve_id`, `filiere_id`, `date_choix`,
  `riasec_snapshot` (JSON), `notes_snapshot` (JSON), `serie`, `score_recommandation`,
  `statut` (`EN_COURS`, `ADMIS`, `RECALE`, `ABANDON`, `REORIENTE`),
  `satisfaction` (1-5, optionnel), `date_maj_statut`.
- **Table ou vue `engagement_signal`** : agrégat par (élève, fiche) de
  `nb_consultations`, `en_favori`, `temps_lecture_moyen`, `derniere_consultation`,
  `score_similarite_recherche` (calculé à partir des requêtes RAG passées et de
  l'embedding de la fiche). Peut être une vue matérialisée recalculée
  périodiquement plutôt qu'une table mise à jour en temps réel.

Documenter tout ça dans `CHANGELOG_SCHEMA.md` avant d'exécuter (rappel : pas de
migration versionnée, donc vigilance particulière ici).

### Phase 2 — Endpoints API

```
GET  /api/v1/niveaux                                        → liste des niveaux disponibles
GET  /api/v1/filieres?niveau={niveau}                       → filières filtrées par niveau
GET  /api/v1/eleves/{trackingId}/notes-historique            → historique notes (2 ans)
POST /api/v1/eleves/{trackingId}/notes-historique             → ajout bulletin d'une classe précédente
POST /api/v1/eleves/{trackingId}/orientation-outcome           → créer/mettre à jour un suivi de choix
GET  /api/v1/eleves/{trackingId}/orientation-outcome           → historique des choix de l'élève
GET  /api/v1/admin/prediction/dataset                          → export anonymisé (admin, pour entraînement)
GET  /api/v1/eleves/{trackingId}/recommandation-ia             → recommandation combinant les 3 signaux
```

Le dernier endpoint remplace/complète `GET /api/v1/eleves/{trackingId}/recommandation-ia`
existant (actuellement basé uniquement sur les embeddings) — vérifier s'il faut
créer une v2 (`/api/v2/...`) pour ne pas casser l'existant côté mobile pendant la
transition.

### Phase 3 — Moteur de recommandation (3 signaux)

- Implémenter un service Java (`RecommandationService`) qui calcule, pour chaque
  élève et chaque filière candidate :
  - `score_aspiration` (cosinus RIASEC élève ↔ profil filière, logique existante)
  - `score_realite` (notes actuelles + trajectoire 2 ans vs seuil d'admission)
  - `score_engagement` (à partir de la table/vue `engagement_signal`)
  - `score_final = pondération(score_aspiration, score_realite, score_engagement)`
- Pondération **configurable** (fichier de config ou table `ParametreController`
  existante), avec un plafond sur `score_engagement` (max ~20-25% du score final)
  pour éviter l'effet bulle de filtre identifié en Phase 0.
- Toujours inclure dans le top résultats au moins 1-2 filières "découverte" (fort
  `score_aspiration`/`score_realite` mais faible `score_engagement`).
- Cette phase reste une **règle pondérée explicite**, pas un modèle ML — le vrai
  entraînement viendra en Phase 5, une fois assez de données `orientation_outcome`
  réelles collectées.

### Phase 4 — Intégration diagnostic multi-niveaux (mobile + backoffice)

- Mobile (Flutter) : à l'étape "choix du niveau actuel", appeler
  `GET /api/v1/filieres?niveau={niveau}` et adapter l'écran de quiz pour charger le
  `diagnostic_template` correspondant au niveau. Ajouter un écran optionnel
  "ajouter mes bulletins" couvrant 3 entrées possibles : les 2 classes
  précédentes et l'année en cours (avec possibilité de renseigner un bulletin
  partiel si l'année n'est pas terminée), avant d'afficher la recommandation
  finale.
- Backoffice (`QuizEditorPage`) : ajouter un sélecteur de `niveau_cible` à la
  création/édition d'un quiz, pour que les conseillers/admins puissent gérer un
  quiz par niveau.

### Phase 5 — Pipeline d'entraînement (une fois les données réelles disponibles)

- Script Python (`train_model.py`, déjà prototypé en Phase 0) branché sur l'export
  `GET /api/v1/admin/prediction/dataset` au lieu du CSV synthétique.
- Déclenchement manuel dans un premier temps (pas d'automatisation CI tant que le
  volume de données est faible) : ré-entraîner et comparer au modèle en
  production, ne déployer que si les métriques s'améliorent.
- Microservice d'inférence simple (FastAPI ou Flask) appelé par le backend Spring
  Boot, ou modèle exporté et chargé directement en Java si le volume de calcul le
  permet — à trancher selon les contraintes d'hébergement du projet.

---

## 5. Livrables attendus

- [ ] `generate_synthetic_data.py` et `train_model.py` étendus (Phase 0)
- [ ] `RESULTATS_PROTOTYPE.md` (Phase 0)
- [ ] `CHANGELOG_SCHEMA.md` + entités/migrations Spring Boot (Phase 1)
- [ ] Endpoints REST documentés dans Swagger (Phase 2)
- [ ] `RecommandationService` + tests unitaires (Phase 3)
- [ ] Écrans mobile + backoffice mis à jour (Phase 4)
- [ ] Pipeline d'entraînement prêt à être relancé sur données réelles (Phase 5)

## 6. Critères de validation globaux

- Aucune régression sur les modules existants (ORIA, OCR, RAG bibliothèque,
  diagnostic RIASEC actuel doivent continuer à fonctionner pendant la transition).
- Le score `score_engagement` ne dépasse jamais le plafond fixé dans la
  pondération.
- Les tests unitaires couvrent au minimum : filtrage des filières par niveau,
  calcul de la trajectoire de notes, calcul du score combiné, et la création
  d'un `orientation_outcome`.
- Toute nouvelle table respecte `BaseEntity` (Long PK + UUID `trackingId`).

## 7. Ordre de priorité si le temps manque

Si tout ne peut pas être fait : **Phase 0 > Phase 1 > Phase 2 > Phase 3**. Les
phases 4 et 5 peuvent attendre une itération suivante — l'essentiel est d'avoir
le backend prêt à collecter les vraies données le plus tôt possible, car c'est le
facteur limitant de tout le projet.
