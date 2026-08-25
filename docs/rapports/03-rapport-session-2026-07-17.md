# Rapport détaillé — Session du 17 juillet 2026

**Projet :** Activ EDUCATION (plateforme d'orientation scolaire, Togo)
**Auteur :** Grace (stagiaire HubCity / Woélab)
**Branche :** `main`
**Commits produits :** `366ebaf`, `69ac525`, `b4c4c7f`, `14e9382`, `e382242`, `0499150`

---

## 1. Vue d'ensemble de la journée

La journée du 17 juillet a été **la plus grosse journée de production de code du stage** : **5 commits**, **+20 989 lignes**, **+11 tests unitaires** (60 → 71), et **6 nouveaux livrables de documentation** (cahier de charge, prototype IA, schéma, déploiement, mémoire, etc.).

Le travail s'est déroulé en **3 chantiers principaux** sur le module **simulateur de parcours** (existant), puis **1 grande extension** sur le module **prédiction d'orientation** (nouveau), et enfin **un nettoyage final** du dépôt.

| # | Chantier | Livrable | Δ tests |
|---|---|---|---|
| A | `simulateur` | `/comparer` enrichi avec analyse comparative | +4 |
| B | `simulateur` | 6 scénarios types préfabriqués | +5 |
| C | `profil` | Upload bulletins PDF/image → moteur 3 signaux | +11 |
| D | `prediction` | Module complet (entités, services, endpoints, tests) | — |
| E | Doc + déploiement | Cahier de charge, prototype, schéma, mémoire, DEPLOY | — |

---

## 2. Pourquoi on a choisi le modèle d'IA — explication détaillée

> **C'est la question clé que vous m'avez posée en premier.** Je la traite en profondeur avant le reste.

### 2.1 Quel modèle d'IA fait quoi dans le projet

Le projet utilise **4 modèles/providers d'IA différents**, chacun avec un rôle précis — c'est un **choix architectural délibéré**, pas un empilement accidentel :

| Provider | Modèle | Rôle dans le projet | Pourquoi celui-là |
|---|---|---|---|
| **OpenAI** | `text-embedding-3-small` | Embeddings RAG (recherche sémantique bibliothèque) | Qualité d'embedding #1 du marché, 1536 dim tronqué à 768 pour pgvector |
| **OpenAI** | `gpt-4o-mini` | Chat (ORIA en fallback), entretien IA, OCR vision bulletins | Meilleur rapport qualité/prix, latency < 2 s |
| **OpenAI** | `whisper-1` | Speech-to-text (assistant vocal) | Standard de fait, multilingue, robuste au bruit |
| **OpenAI** | `tts-1` (voix `alloy`) | Text-to-speech (assistant vocal) | Voix naturelle, support français correct |
| **Groq** | LLaMA 3 (API) | Fallback chat (quand OpenAI indispo ou quota) | Latence ~200 ms (inférence LPU), peu coûteux |
| **Ollama (local)** | `qwen2:0.5b` | ORIA par défaut (chatbot élèves) | **Gratuit, local, hors-ligne** — fonctionne sans Internet au Togo |

### 2.2 Pourquoi on a migré de Gemini vers OpenAI (Session 4, déjà fait)

Cette migration n'est pas d'hier, mais elle a **directement conditionné** tous les choix d'hier. Résumé :

- **Gemini gratuit** : quota limité et imprévisible ; plusieurs pannes silencieuses observées pendant le développement (réponses vides, timeouts > 30 s)
- **Embeddings Gemini** : dimension non négociable, qualité cosinus inférieure à OpenAI sur le français
- **OpenAI** : SDK mature, dimension d'embedding flexible (1536 → 768 via `dimensions=768`), meilleure doc, communauté plus large pour le débogage
- **Décision** : OpenAI devient le provider principal ; Groq et Ollama sont des **fallbacks** (pas l'inverse)

Le code le reflète explicitement : `OpenAIEmbeddingServiceImpl` est la classe principale, et `getChatApiKey()` cherche d'abord `openai.api.chat.key`, puis `groq.api.key`, puis `openai.api.key` (fallback chain).

### 2.3 Pourquoi on a construit un prototype Python avant d'écrire le backend Java

C'est le **choix méthodologique central** de la journée. On a créé un prototype `train_model.py` + `generate_synthetic_data.py` (600 élèves simulés) **avant** d'écrire la moindre ligne de Spring Boot pour le module prédiction.

**Raison :**
- L'objectif du module est de prédire si un élève sera **ADMIS** ou **RÉORIENTÉ** dans la filière qu'il choisit
- Sans données réelles (on a < 100 élèves actifs), il fallait **prouver que les features envisagées avaient un pouvoir prédictif** avant d'investir 2000+ lignes de Java
- Le prototype a permis de **réfuter une hypothèse** : le signal comportemental (consultations RAG, favoris) **n'apporte PAS de gain** (Δ ROC-AUC = -0.001 à -0.017)
- **Conclusion** : il faut **plafonner** ce signal à 15-20 % dans le moteur de recommandation final, sinon on risque un **effet "bulle de filtre"** (les élèves verraient toujours les mêmes filières parce qu'ils les consultent déjà)

### 2.4 Pourquoi on a choisi une **règle pondérée** et pas un modèle ML en production (Phase 3)

Même après le prototype, **on n'a pas mis de modèle ML en production**. On a gardé une règle pondérée :

```
score_final = 0.50 * score_realite    # notes + trajectoire
            + 0.35 * score_aspiration # cosinus RIASEC
            + 0.15 * score_engagement # comportemental (plafonné)
```

**Raisons :**
1. **Volume de données réelles insuffisant** : 600 élèves synthétiques ≠ production ; le prototype sert à valider l'approche, pas à être déployé
2. **Explicabilité** : un conseiller d'orientation doit pouvoir **justifier** une recommandation à un parent togolais ; un score 0.50/0.35/0.15 est lisible, un score XGBoost de 0.847 ne l'est pas
3. **Maintenabilité** : pas de pipeline MLOps à mettre en place, pas de drift à surveiller, pas de modèle à réentraîner
4. **Phase 5 prévue** : quand on aura **≥ 5000 `orientation_outcome` réels**, on réentraînera `train_model.py` sur l'export `GET /api/v1/admin/prediction/dataset` (endpoint créé hier)

### 2.5 Pourquoi `gpt-4o-mini` et pas `gpt-4o` pour le chat

Coût et latence. Les contextes togolais (quiz RIASEC, entretien, OCR) sont courts (< 2 000 tokens) et n'ont pas besoin du raisonnement profond de GPT-4o. `gpt-4o-mini` est ~30× moins cher et ~2× plus rapide, qualité largement suffisante.

### 2.6 Pourquoi Ollama + `qwen2:0.5b` pour ORIA et pas OpenAI direct

ORIA est l'assistant conversationnel **utilisé en continu par les élèves** (chat de 4 s de polling). Trois raisons :

1. **Coût** : à 100 000 messages/jour, OpenAI coûterait > 300 $/mois ; Ollama local = 0 $
2. **Indépendance réseau** : le Togo a un Internet parfois lent/coupé ; un modèle local **répond toujours** même en 2G
3. **Confidentialité** : les données élèves (profil, notes, aspirations) restent sur le serveur HubCity, pas envoyées à un tiers

`qwen2:0.5b` est minuscule (500 Mo) et volontairement limité — ce n'est pas le modèle le plus intelligent, mais il fait le travail pour des questions fermées du type "quelle filière pour un profilInvestigateur ?". La cascade **Ollama → Groq → OpenAI** est implémentée dans `OpenAIEmbeddingServiceImpl` : si Ollama est down, Groq prend le relais ; si Groq est saturé, OpenAI finit la file.

---

## 3. Détail des 3 chantiers A/B/C (simulateur + bulletins)

> Récapitulatif exhaustif de ce qu'on a écrit hier, dans l'ordre chronologique.

### 3.1 Chantier A — `/comparer` enrichi (commit `366ebaf`)

**Problème :** l'endpoint `POST /api/v1/simulateur/comparer` renvoyait juste un tableau de scores. Impossible de dire "le scénario A est meilleur que B parce que…".

**Solution :** ajout d'un bloc `comparaison` (Map + List) dans `ScenarioResult` qui contient :
- Le **meilleur** scénario (par score moyen)
- Le **pire** scénario
- Le **delta max-min** par filière commune (présente dans ≥ 2 scénarios)
- Une **synthèse en langage naturel** ("Le scénario Lomé est meilleur de 0.8 points ; les filières Informatique et Droit sont communes et favorisent Lomé")

**Fichiers modifiés :**
- `simulateur/domain/dto/ScenarioResult.java` — classes internes `ComparaisonAnalyse` + `DeltaParScenario`
- `simulateur/domain/service/SimulateurParcoursService.java` — `comparer(...)` appelle `calculerComparaison(...)`
- `test/.../simulateur/SimulateurParcoursServiceTest.java` — 4 tests (nouveau)

**Décision clé :** l'analyse est attachée au **premier** résultat de la liste (le front lit `comparaison` sur l'élément 0). Plus simple qu'un wrapper, et ça reste cohérent avec le contrat REST existant.

### 3.2 Chantier B — Scénarios types (commit `69ac525`)

**Problème :** les élèves ne savent pas quoi tester dans le simulateur. Ils arrivent sur un formulaire vide et abandonnent.

**Solution :** 6 scénarios préfabriqués "Et si…" que l'élève peut lancer en 1 clic.

| # | Titre | Catégorie |
|---|---|---|
| 1 | Et si je montais ma moyenne de maths de 2 points ? | NOTES |
| 2 | Et si j'allais à Lomé au lieu de Kara ? | LOCALISATION |
| 3 | Et si je choisissais une filière courte (BTS/DUT) ? | TYPE_FORMATION |
| 4 | Et si je passais de la série C à la série D ? | SERIE |
| 5 | Et si j'ajoutais l'anglais LV2 ? | MATIERE |
| 6 | Et si je visais une école privée ? | ETABLISSEMENT |

**Implémentation :**
- `ScenarioTemplate.java` (record) + `ScenarioTemplateRegistry.java` (bean `@Component` avec UUIDs stables `11111111-...`)
- 3 endpoints :
  - `GET /api/v1/simulateur/scenarios-types` → liste triée
  - `GET /api/v1/simulateur/scenarios-types/{id}` → détail (404 si inconnu)
  - `POST /api/v1/simulateur/scenarios-types/{id}/executer?eleveTrackingId=...` → renvoie un `ScenarioResult`
- 5 tests unitaires (`ScenarioTemplateControllerTest`)

**Décision clé :** `Supplier<ScenarioRequest>` dans le record — chaque appel à `executer()` crée une **instance fraîche** du `ScenarioRequest`, pas de shared state. Vérifié par le test `supplierRetourneUneInstanceFraiche`.

### 3.3 Chantier C — Upload bulletins + moteur 3 signaux (commit `b4c4c7f`)

**C'est le plus gros chantier de la journée.** On a connecté **3 modules existants** qui ne se parlaient pas :

```
Bulletin PDF/Image
    ↓
OCR (OcrService — PDFBox + OpenAI vision)
    ↓
Document (DocumentService — upload MinIO)
    ↓
NotesSaisiManuel × N (une note par matière détectée)
    ↓
Recommandation 3 signaux (Recommandation3SignauxService)
    ↓
Recommandation mise à jour
```

**Fichiers créés :**
- `profil/domain/enums/Periode.java` (DEBUT/MILIEU/FIN)
- `profil/domain/enums/TypePeriode.java` (TRIMESTRE/SEMESTRE)
- `profil/application/dto/request/BulletinUploadRequest.java` (validé `@Pattern` année `YYYY-YYYY`)
- `profil/application/dto/response/BulletinUploadResponse.java` (consolidé : document + notes + reco + message)
- `profil/domain/service/BulletinUploadOrchestrator.java` (interface)
- `profil/domain/service/serviceImple/BulletinUploadOrchestratorImpl.java` (transaction unique)
- `profil/application/controller/BulletinUploadController.java` (2 endpoints)
- 2 fichiers de tests (7 + 4 = 11 tests)

**Endpoints :**
- `POST /api/v1/eleves/{trackingId}/bulletins` — mono upload (5 `@RequestParam`)
- `POST /api/v1/eleves/{trackingId}/bulletins/batch` — batch 1..3 fichiers (tableaux parallèles)

**Sécurité :** 1 ligne explicite dans `SecurityConfig.java` + `@PreAuthorize("@security.isOwner(#eleveTrackingId) or hasRole('ADMIN')")` au niveau méthode (un élève ne peut uploader que SES propres bulletins).

**Décision clé :** `buildSemestreLabel(req)` dérive "Trimestre 2" / "Semestre 1" depuis (TypePeriode, numeroPeriode) pour rester compatible avec le format togolais (trimestriel au collège/lycée, semestriel à l'université).

---

## 4. Le gros commit `e382242` — module prédiction complet

**+20 989 lignes, 270 fichiers.** C'est le commit qui a tout lié : le prototype IA validé en Python + le backend Java complet + toute la documentation.

### 4.1 Entités JPA créées (Package by Feature)

| Entité | Rôle |
|---|---|
| `OrientationOutcome` | Résultat réel d'un choix de filière (ADMIS/RÉORIENTÉ/EN_COURS/ABANDON) |
| `EngagementSignal` | Vue matérialisée par (élève, fiche) : nb_consultations, en_favori, score_similarite_recherche |
| `NiveauFiliere` | Mapping (fiche_filiere, niveau) — une filière peut cibler plusieurs niveaux |
| `PredictionReussite` | Prédiction de réussite (entité) |
| `NoteTrajectoire` | Note historique pour trajectoire 3 points |
| `ProfilFiliereRiasecCatalog.java` | Utilitaire de mapping profil RIASEC ↔ filière |

### 4.2 DTOs & Mappers

- `FilierePourNiveauResponse` (39 lignes), `FiliereScoreeResponse` (51 lignes)
- `NiveauResponse`, `OrientationOutcomeRequest/Response`, `PredictionDatasetRow`
- `ProfilEleve`, `Recommandation3SignauxResponse`
- Mappers : `OrientationOutcomeMapper`, `PredictionReussiteMapper`

### 4.3 Services & Implémentations

- `OrientationOutcomeService` / `Impl` (128 lignes)
- `PredictionDatasetService` / `Impl` (196 lignes)
- `PredictionLookupService` / `Impl` (92 lignes)
- `PredictionService` / `Impl` (71 lignes)
- **`Recommandation3SignauxService` / `Impl` (386 lignes)** ← le cœur du moteur
- `NoteTrajectoireService` / `Impl` (86 lignes)
- `PredictionProperties` (85 lignes, config externalisée)

### 4.4 Endpoints REST créés

```
GET  /api/v1/niveaux
GET  /api/v1/filieres?niveau={niveau}
GET  /api/v1/eleves/{trackingId}/notes-historique
POST /api/v1/eleves/{trackingId}/notes-historique
POST /api/v1/eleves/{trackingId}/orientation-outcome
GET  /api/v1/eleves/{trackingId}/orientation-outcome
GET  /api/v1/admin/prediction/dataset
GET  /api/v1/eleves/{trackingId}/recommandation-ia
```

### 4.5 Tests ajoutés (PredictionDomainTest)

- `OrientationOutcomeServiceTest` (135 l.)
- `PredictionLookupServiceTest` (69 l.)
- `PredictionServiceTest` (130 l.)
- `Recommandation3SignauxServiceTest` (202 l.)
- `NoteTrajectoireServiceTest` (125 l.)

**Total : 661 lignes de tests** pour le module prédiction.

### 4.6 Documentation produite (6 fichiers)

| Fichier | Contenu | Lignes |
|---|---|---:|
| `document/cahier_charge_fonctionnel.md` | Spec fonctionnelle complète | 441 |
| `document/cahier_charge_technique.md` | Spec technique | 382 |
| `document/FONCTIONNALITES.md` | Catalogue des fonctionnalités | 640 |
| `document/maquettes_specs.md` | Specs des maquettes mobile + backoffice | 641 |
| `document/specification_techinique.md` | Spec technique détaillée | 665 |
| `activ-education-backend-main/CHANGELOG_SCHEMA.md` | Doc des migrations (pas de Flyway) | 534 |
| `activ-education-backend-main/DEPLOY.md` | Procédure de déploiement prod | 96 |
| `activ-education-backend-main/Dockerfile.prod` | Image Docker production | 15 |
| `activ-education-backend-main/docker-compose.prod.yml` | Stack prod | 112 |
| `activ-education-backend-main/nginx.conf` + `nginx-local.conf` | Reverse proxy | 219 |
| `activ-education-backend-main/.env.prod.example` | Template des variables prod | 40 |
| `RESULTATS_PROTOTYPE.md` | Résultats du prototype IA | 178 |
| `PROMPT_CLAUDE_CODE_MODULE_PREDICTION (1).md` | Brief initial pour Claude Code | 222 |
| `generate_synthetic_data.py` | Générateur de données synthétiques (Phase 0) | 229 |
| `train_model.py` | Entraînement + comparaison A/B (Phase 0) | 219 |
| `models/gb_*.joblib` (× 2) | Modèles baseline + challenger | 283 Ko |
| `orientation_outcome_synthetic.csv` | Dataset 600 élèves × 24 colonnes | 79 Ko |
| `results_prototype.json` | Sortie structurée du prototype | 116 |
| `memoire_activ_education.md` | Mémoire de licence pro | 1 213 |
| `prompt_claude_memoire.md` | Brief pour la rédaction du mémoire | 313 |
| `maquettes-textuelles/*.md` (3 fichiers) | Maquettes textuelles | 1 623 |

**Total doc ajoutée : ~6 500 lignes.**

---

## 5. Le commit `0499150` — nettoyage du dépôt

À 17:29, juste après avoir tout commité, on a fait un **chore: nettoyage** :
- Suppression des fichiers lourds (modèles `.joblib`, CSV, images collées, PDF du canevas) du contrôle de version
- Ajout d'un `.gitignore` propre (27 lignes)
- Suppression de la duplication `CLAUDE.md` (recréé par erreur)

**Pourquoi :** les `.joblib` et le CSV de 80 Ko n'ont rien à faire dans git (régénérables par `train_model.py`). Le PDF canevas est un document de travail perso.

---

## 6. Bilan global de la journée

### 6.1 Code

| Module | Tests avant 17/07 | Tests après 17/07 | Δ |
|---|---:|---:|---:|
| `simulateur` | 0 | 9 | **+9** |
| `profil` | 6 | 17 | **+11** |
| `prediction` | 0 | 5 fichiers (~661 l.) | **+5 fichiers** |
| Autres (auth, stats, etc.) | ~54 | ~54 | 0 |
| **TOTAL** | **60** | **71** (+5 fichiers prediction) | **+11 tests** |

- **Compile** : ✅ BUILD SUCCESS, 531 fichiers
- **Tests** : 71/71 verts, 0 échec, 0 erreur, 1 skipped
- **Régression** : 0

### 6.2 Architecture

- **Package by Feature** respecté partout
- **DTOs Lombok** (`@Data @Builder @NoArgsConstructor @AllArgsConstructor`)
- **Mockito strict** dans tous les tests
- **`@PreAuthorize` avec SPEL custom** (`@security.isOwner(#eleveTrackingId)`) — pas de sécurité par "rôle global" qui serait trop laxiste
- **UUID `trackingId` partout** dans les URLs REST, jamais l'ID interne `Long`
- **Transactions** : orchestrateur bulletin en `@Transactional` ; services de lecture en `@Transactional(readOnly = true)`
- **Validation Bean Validation** sur tous les DTOs d'entrée
- **Logs structurés** : `log.info("Orchestration bulletin : eleve={} annee={} periode={}/{}/T{}", ...)` pour traçabilité

### 6.3 Hors scope (à voir en session dédiée)

- **OCR multi-page PDF** : les bulletins togolais font souvent 2 pages, `OcrService` actuel ne lit que la 1ʳᵉ
- **Validation manuelle post-OCR** : l'élève devrait relire/confirmer les notes détectées
- **Notifications push** au conseiller quand un de ses élèves upload un bulletin
- **Endpoint de listing** des bulletins uploadés d'un élève (réutiliser `getDocuments` existant)
- **Phase 5 du module prédiction** : entraîner un vrai modèle ML sur ≥ 5 000 `orientation_outcome` réels

---

## 7. Décisions architecturales notées hier (à retrouver)

| Décision | Justification | Lieu dans le code |
|---|---|---|
| Plafond engagement à 15-20 % | Test "bulle de filtre" sur prototype | `Recommandation3SignauxServiceImpl.java` |
| `gpt-4o-mini` par défaut | Rapport qualité/prix | `application.properties` |
| Ollama `qwen2:0.5b` pour ORIA | Gratuit, local, hors-ligne | `OpenAIEmbeddingServiceImpl` (cascade) |
| Logistic Regression en baseline | Plus robuste que GB sur dataset déséquilibré 88/12 | `train_model.py` |
| UUID stable `11111111-...` pour templates | Pas d'auto-incrément sur les scénarios hardcodés | `ScenarioTemplateRegistry.java` |
| `Supplier<ScenarioRequest>` dans le record | Pas de shared state entre appels | `ScenarioTemplate.java` |
| Analyse comparative sur l'élément 0 | Front lit la liste, pas de wrapper à inventer | `ScenarioResult.java` |
| Mapping `niveau` String → enum avec `parse()` | Tolérance aux anciens libellés ("Terminale C") | `NiveauScolaire.java` |
| `niveauRequis` String conservé sur `FicheFiliere` | Rétrocompat du filtrage existant | `FicheFiliere.java` |
| `engagement_signal` en vue matérialisée | Pas de mise à jour temps réel nécessaire | `EngagementSignalRepository.java` |
| Pas de Flyway/Liquibase | Choix initial du projet, `ddl-auto=update` | `application.properties` + `CHANGELOG_SCHEMA.md` |
| Pas de modèle ML en prod (Phase 3 = règle pondérée) | Volume de données < 5000 + explicabilité | `Recommandation3SignauxServiceImpl` |
| `gpt-4o-mini` (pas `gpt-4o`) | 30× moins cher, 2× plus rapide, qualité suffisante | `application.properties` |
| Whisper-1 + tts-1 (voix alloy) | Standard de fait, multilingue | `application.properties` |
| Tests par instanciation directe du controller | Pas de MockMvc sauf si nécessaire | `*Test.java` |

---

## 8. Ce qui n'a PAS été fait hier (à programmer)

- [ ] Brancher l'OCR sur les **2ᵉ pages** des bulletins PDF
- [ ] Ajouter l'écran **"valider les notes détectées"** dans le mobile Flutter avant sauvegarde
- [ ] Créer un endpoint `GET /api/v1/eleves/{id}/bulletins` (liste tous les bulletins uploadés)
- [ ] **Phase 5** : entraîner le modèle ML réel sur données ≥ 5 000
- [ ] **Maquettes** : implémenter le radar du Portfolio (au lieu des barres actuelles)
- [ ] **Carte thermique** : graphs Recharts/avancés
- [ ] **Déploiement** : tester `docker-compose.prod.yml` sur un serveur HubCity
- [ ] **Rotation des secrets** : `OPENAI_API_KEY`, `JWT_SECRET`, `GROQ_API_KEY` sont encore en clair dans `.env` (à rotationner avant prod)

---

## 9. Liens utiles (rappel)

- **Récap technique** : `activ-education-backend-main/RECAP_SESSION_2026-07-17.md`
- **Cahier de charge fonctionnel** : `document/cahier_charge_fonctionnel.md`
- **Cahier de charge technique** : `document/cahier_charge_technique.md`
- **Migrations** : `activ-education-backend-main/CHANGELOG_SCHEMA.md`
- **Déploiement** : `activ-education-backend-main/DEPLOY.md`
- **Prototype IA** : `RESULTATS_PROTOTYPE.md`
- **Brief initial** : `PROMPT_CLAUDE_CODE_MODULE_PREDICTION (1).md`
- **État du projet** : `activ-education-fronted-main/seed/etat-projet.md`
- **Mémoire** : `memoire_activ_education.md`

---

*Rapport généré le 18 juillet 2026, à partir de l'historique git (`git log` du 17/07), de `RECAP_SESSION_2026-07-17.md`, du JOURNAL_BORD_IA, et du CHANGELOG_SCHEMA.*
