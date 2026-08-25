# Récapitulatif de session — 6 août 2026

**Projet** : Activ EDUCATION
**Auteur** : Session de travail avec agent IA Claude
**Date** : 6 août 2026

---

## 1. Vue d'ensemble

| Métrique | Valeur |
|---|---|
| Bugs corrigés | 2 |
| Nouveaux tests ajoutés | 14 (10 + 4) |
| Tests pré-existants réécrits | 6 (OpenAIEmbeddingServiceImplTest) |
| Total tests verts | **112/112** |
| Régressions | 0 |
| Smoke test live | OK |

**Bugs traités** :
1. **Bug entretien** — JSON d'erreur brut affiché dans la bulle du recruteur
2. **Bug widget "Ma recommandation"** — message générique indistinct entre « profil vide » et « LLM en panne »

---

## 2. Bug #1 — JSON d'erreur dans la bulle du recruteur

### Symptôme

Le frontend affichait, dans la bulle de la question du recruteur, le JSON brut de l'erreur :

```json
{"score":10,"feedback":"..."}
```

au lieu d'un message lisible et d'un mécanisme de retry.

### Cause racine

`appelerOpenAI()` (l'ancien nom) renvoyait ce JSON comme fallback indistinctement pour les deux usages :
- la **génération de question** (champ `question` du DTO)
- l'**évaluation** de la réponse de l'élève (champs `score` / `feedback`)

Aucun mécanisme de distinction « erreur vs contenu valide » n'existait côté DTO.

### Fix backend

**`entretien/domain/dto/EntretienResponse.java`** (+46 lignes)
- Ajout des champs :
  - `Boolean erreur`
  - `String messageErreur`
- Annotation `@JsonInclude(JsonInclude.Include.NON_NULL)` pour omettre les champs `null` à la sérialisation
- Constructeur static factory `erreur(String message)` pour produire une réponse d'erreur explicite

**`entretien/domain/service/EntretienService.java`** (+311 lignes, refactor majeur)
- Wrapper générique `ResultatAppelLLM<T>` encapsulant soit la valeur désérialisée, soit une erreur
- Retry 1× sur JSON malformé (`JsonProcessingException`) avant de basculer en erreur
- Découpage en méthodes distinctes :
  - `appelerLLMTexteAvecRetry()` (réponse texte libre, ex. question)
  - `appelerLLMJsonAvecRetry()` (réponse JSON structurée, ex. évaluation)
  - `estErreurTransport()` (détection 4xx/5xx)
- Renforcement des prompts système :
  - Prompt question : interdiction explicite de préambule et de JSON
  - Prompt évaluation : instruction `FORMAT STRICT JSON` + exemple concret

### Fix frontend

**`lib/models/entretien_models.dart`**
- `fromJson` étendu pour lire `erreur` et `messageErreur`

**`lib/screens/entretien/entretien_screen.dart`**
- Affichage d'un **bandeau rouge** en cas d'erreur
- Bouton **« Réessayer »** qui rejoue `_derniereAction` (question ou évaluation) sans perdre l'état de l'entretien

### Tests ajoutés — `entretien/domain/service/EntretienServiceTest.java` (6 tests)

| # | Nom | Vérifie |
|---|---|---|
| 1 | `malformedJsonThenRetry_renvoieErreurTrue` | 2 appels HTTP, `erreur=true`, `repository.save` jamais appelé |
| 2 | `erreurHttp_renvoieErreurTrueSansRetry` | 1 appel HTTP, pas de retry sur 5xx |
| 3 | `evaluationJsonInvalide_stockePlaceholder` | Graceful handling du JSON d'évaluation invalide |
| 4 | `generationReussie_questionValide` | 1 appel, `question = mocked value` |
| 5 | `codeFenceJson_acceptable` | Strip des fences ```json ... ``` |
| 6 | `evaluationJsonValide_stockeeCorrectement` | JSON valide persisté en base |

### Vérification
- `mvn test` → **104/104 verts**
- Smoke test live : OK

---

## 3. Bug #2 — Widget « Ma recommandation » message générique

### Symptôme

L'élève de test voyait sur la home le widget afficher inconditionnellement :

> Désolé, je n'ai pas pu générer une recommandation pour le moment

… que son profil soit incomplet **ou** que le LLM soit en panne. Aucune distinction possible côté UI.

### Diagnostic phase 1

- Le widget home appelle `_api.getRecommandationIA(uid)` → `GET /api/v1/eleves/{trackingId}/recommandation-ia` (**v1**, LLM)
- Le bouton « Tester la v2 » appelle `GET /api/v1/eleves/{trackingId}/recommandation-ia/v2` (**v2**, moteur algorithmique 3 signaux)
- `RecommandationIAService` lignes 122-130 : `try/catch` swallow l'exception → message générique
- Cause racine en amont : `OpenAIEmbeddingServiceImpl.generateAnswer()` n'avait **PAS** de fallback Ollama, contrairement à `generateEmbedding()` (qui en avait un depuis le 3 août)
- Avec `OPENAI_API_KEY=REVOKED_REPLACE_ME` dans `.env` → 401 OpenAI → exception → catchée silencieusement

### Décision

Garder **v1** (LLM) et corriger la **cause racine** (manque de fallback). Validé avec l'utilisateur via `AskUserQuestion`.

### Fix backend 2A — `OpenAIEmbeddingServiceImpl.java` (+148 lignes)

- Injection de 2 nouvelles propriétés :
  - `@Value("${ollama.chat.url}")`
  - `@Value("${ollama.chat.model}")`
- Extraction de la méthode `callOpenAIChat()` (utilisée par v1 et autres consommateurs)
- Nouvelle méthode `callOllamaChat()` :
  - `POST /api/generate` avec `stream=false`
  - Lecture du champ `response` du JSON Ollama
- Réécriture de `generateAnswer()` :
  1. Tente OpenAI si la clé est valide
  2. Sinon / en cas d'erreur → fallback Ollama
  3. **429 (quota)** remonte une exception dédiée — Ollama ne « fixera » pas un quota OpenAI

### Fix backend 2B — DTO typé

**Nouveau fichier** : `shared/ai/domain/dto/RecommandationIAResponse.java`

```java
public record RecommandationIAResponse(String recommandation, Statut type) {
    public enum Statut { OK, PROFIL_INCOMPLET, ERREUR_LLM }
}
```

**`RecommandationIAService.java`** (+51 lignes) :
- 3 constructeurs statiques : `ok()`, `profilIncomplet()`, `erreurLlm()`
- Profil vide → **aucun appel LLM** (économie + UX claire)
- Profil complet + LLM OK → `OK` avec texte
- Profil complet + LLM throw → `ERREUR_LLM` (sans propager l'exception)

**`RecommandationIAController.java`** (+15 lignes) :
- Signature passée de `Map<String, String>` → `RecommandationIAResponse`
- Sérialisation auto : `{recommandation, type}`

### Fix frontend 2C

**Nouveau fichier** : `lib/models/recommandation_ia_models.dart`
- Enum `RecommandationIAStatut { ok, profilIncomplet, erreurLlm }`
- Classe `RecommandationIAResponse` avec helpers :
  - `bool get isOk`
  - `bool get isErreurTechnique`

**`lib/models/models.dart`** — export du nouveau modèle

**`lib/services/api_service.dart`** :
- `getRecommandationIA` retourne désormais l'objet typé
- Ne retourne plus `null` en cas d'erreur HTTP (toujours un objet)

**`lib/screens/home/dashboard_bachelier.dart`** :
- Widget adaptatif :
  - **Rouge** si `erreurLlm`
  - **Titre** change selon le statut
  - **Tap** = `_loadDashboardData()` (recharge complète)
  - **Bouton « Tester la v2 »** masqué si `erreurLlm`

### Fix doc 2D

**`JOURNAL_BORD_IA.md`** — §6 datée du 6 août 2026 (+336 lignes)
- Diagnostic complet
- Corrections backend/frontend
- Vérifications end-to-end

### Tests ajoutés — 14 tests

**`shared/ai/service/impl/OpenAIEmbeddingServiceImplTest.java`** (10 tests, dont 6 réécrits)

| # | Nom | Vérifie |
|---|---|---|
| 1 | `generateAnswer_cleRevoked_fallbackOllama` | Clé `REVOKED_REPLACE_ME` → fallback Ollama direct |
| 2 | `generateAnswer_openAI401_fallbackOllamaAutomatique` | 401 OpenAI → bascule automatique vers Ollama |
| 3 | `generateAnswer_cleValide_openAIDirect` | Clé valide → OpenAI direct, Ollama jamais appelé |
| 4 | `generateAnswer_quota429_exceptionDediee` | 429 → exception dédiée (pas de fallback Ollama) |
| 5-10 | (autres cas) | Robustesse, timeouts, etc. |

**`shared/ai/service/RecommandationIAServiceTest.java`** (4 tests)

| # | Nom | Vérifie |
|---|---|---|
| 1 | `profilVide_statutProfilIncomplet_aucunAppelLlm` | `PROFIL_INCOMPLET` + 0 appel LLM |
| 2 | `profilComplet_llmOk_statutOkAvecTexte` | `OK` + texte LLM |
| 3 | `profilComplet_llmThrow_statutErreurLlmSansException` | `ERREUR_LLM` + pas d'exception remontée |
| 4 | `troisStatutsMutuellementExclusifs` | Cohérence de l'enum |

### Vérification end-to-end live

Logs observés :

```
Clé OpenAI invalide/absente → fallback chat Ollama (modèle: qwen2:0.5b)
Réponse Ollama Chat générée : X caractères
```

Réponse HTTP :

```json
{"recommandation":"…","type":"OK"}
```

(auparavant : `Map<String,String>` non typé, impossible de distinguer le statut)

`mvn test` → **112/112 verts** (104 anciens + 14 nouveaux − 6 réécrits dans OpenAIEmbeddingServiceImplTest).

---

## 4. Fichiers modifiés — récapitulatif exhaustif

### Backend (modifiés)

| Fichier | Δ lignes | Nature |
|---|---|---|
| `entretien/domain/dto/EntretienResponse.java` | +46 | Ajout champs `erreur`/`messageErreur`, `@JsonInclude`, factory |
| `entretien/domain/service/EntretienService.java` | +311 | Refactor majeur (retry, ResultatAppelLLM, prompts) |
| `shared/ai/controller/RecommandationIAController.java` | +15 | Signature typée |
| `shared/ai/service/RecommandationIAService.java` | +51 | 3 statuts + constructeurs statiques |
| `shared/ai/service/impl/OpenAIEmbeddingServiceImpl.java` | +148 | Fallback Ollama dans `generateAnswer` |
| `JOURNAL_BORD_IA.md` | +336 | §6 nouvelle entrée (diagnostic + corrections) |

### Backend (nouveaux)

| Fichier | Nature |
|---|---|
| `shared/ai/domain/dto/RecommandationIAResponse.java` | Record + enum Statut |
| `test/java/.../entretien/domain/service/EntretienServiceTest.java` | 6 tests Mockito |
| `test/java/.../shared/ai/service/RecommandationIAServiceTest.java` | 4 tests Mockito |
| `test/java/.../shared/ai/service/impl/OpenAIEmbeddingServiceImplTest.java` | 10 tests (6 réécrits + 4 nouveaux) |

### Frontend (modifiés)

| Fichier | Nature |
|---|---|
| `lib/models/entretien_models.dart` | `fromJson` étendu (erreur + messageErreur) |
| `lib/models/models.dart` | Export `recommandation_ia_models` |
| `lib/services/api_service.dart` | `getRecommandationIA` typé, plus de retour `null` |
| `lib/screens/entretien/entretien_screen.dart` | Bandeau rouge + bouton « Réessayer » intelligent |
| `lib/screens/home/dashboard_bachelier.dart` | Widget adaptatif (rouge / titre / tap / v2 masquée) |

### Frontend (nouveaux)

| Fichier | Nature |
|---|---|
| `lib/models/recommandation_ia_models.dart` | Enum + classe + helpers |

---

## 5. Leçons apprises

1. **Toujours typer les réponses backend** — Un simple `Map<String,String>` force le frontend à deviner la sémantique. Un `record` avec `enum` rend les états explicites, testables et auto-documentés.

2. **Cohérence des fallbacks LLM** — `generateEmbedding` avait un fallback Ollama depuis le 3 août, mais `generateAnswer` ne l'avait pas. Toute méthode qui appelle un LLM externe doit suivre le même pattern : clé valide → provider principal ; sinon / erreur → fallback local.

3. **Smoke test live critique** — `mvn test` peut passer sans qu'Ollama soit réellement lancé (les mocks le court-circuitent). Un `curl` avec un vrai JWT sur le serveur démarré est indispensable pour confirmer le fix bout-en-bout.

4. **Tests Mockito avec JSON** — Utiliser `objectMapper.writeValueAsString()` pour échapper les guillemets dans les mocks. Sinon le parsing `throws` et le test échoue pour la mauvaise raison, masquant le vrai bug.

5. **Ne jamais commiter de clés API** — Les clés OpenAI et les PATs GitHub sont révoqués. `.env.local` doit contenir les vraies clés en local ; `.env` versionné doit porter des placeholders `REVOKED_REPLACE_ME`.

6. **Diagnostiquer avant de refondre** — La v2 (moteur algorithmique 3 signaux) était tentante comme solution de contournement, mais corriger la cause racine (fallback manquant) était plus rapide, plus pédagogique et plus pérenne.

---

## 6. Tâches restantes

- **Tâche #18 (pending)** : Pousser les 13+ commits accumulés. **Bloqué** : auth SSH/PAT requis sur la machine.
- **Secrets à régénérer** :
  - `OPENAI_API_KEY` (actuellement `REVOKED_REPLACE_ME` dans `.env`)
  - `GROQ_API_KEY` (idem)
  - Tant que ces clés ne sont pas régénérées et placées dans `.env.local`, le widget « Ma recommandation » continuera à utiliser Ollama local (`qwen2:0.5b`), ce qui fonctionne mais est plus lent et moins pertinent que GPT-4o-mini.
- **Tâches post-stage** : rotation de tous les secrets documentés dans `DEPLOY.md` avant tout déploiement en production.

---

*Fin du récapitulatif — 6 août 2026*
