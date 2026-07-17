# Récapitulatif session — Chantiers A, B, C du module simulateur + predictions

**Date** : 2026-07-17
**Branche** : main
**Repo** : activ-education-backend-main (Spring Boot 4.0.5 + Java 21)

---

## Chantier A — Amélioration de `/comparer` (analyse comparative)

### Fichiers modifiés
- `simulateur/domain/dto/ScenarioResult.java` : ajout des classes internes `ComparaisonAnalyse` + `DeltaParScenario`, ajout du champ `comparaison` (Map + List + getters/setters)
- `simulateur/domain/service/SimulateurParcoursService.java` : `comparer(...)` enrichi → appelle `calculerComparaison(scenarios, resultats)` qui calcule :
  - meilleur / pire scénario (par score moyen)
  - delta max-min par filière commune (présente dans ≥ 2 scénarios)
  - synthèse en langage naturel
  - L'analyse est attachée au **premier** résultat uniquement (le front lit la liste et lit `comparaison` sur l'élément 0)

### Fichiers créés
- `test/.../simulateur/SimulateurParcoursServiceTest.java` (4 tests)
  - `comparerAvecFiliereCommune` — 2 scénarios avec 1 filière commune → analyse calculée
  - `comparerAucunCommun` — 0 filière commune → synthèse "Aucun point commun"
  - `explorerNonRegression` — `explorer()` reste vert
  - `comparerUnSeulScenario` — 1 scénario → pas d'analyse (early return)

## Chantier B — Scénarios types / templates préfabriqués

### Fichiers créés
- `simulateur/application/dto/ScenarioTemplate.java` — record avec `trackingId`, `titre`, `description`, `CategorieTemplate` (enum), `Supplier<ScenarioRequest>`
- `simulateur/application/service/ScenarioTemplateRegistry.java` — bean `@Component` avec 6 templates hardcodés (UUIDs stables `11111111-...`):
  1. "Et si je montais ma moyenne de maths de 2 points ?"
  2. "Et si j'allais à Lomé au lieu de Kara ?"
  3. "Et si je choisissais une filière courte (BTS/DUT) ?"
  4. "Et si je passais de la série C à la série D ?"
  5. "Et si j'ajoutais l'anglais LV2 ?"
  6. "Et si je visais une école privée ?"
- `simulateur/application/controller/ScenarioTemplateController.java` — 3 endpoints:
  - `GET /api/v1/simulateur/scenarios-types` → liste triée
  - `GET /api/v1/simulateur/scenarios-types/{id}` → détail (404 si inconnu)
  - `POST /api/v1/simulateur/scenarios-types/{id}/executer?eleveTrackingId=...` → exécute et renvoie un `ScenarioResult`
- `test/.../simulateur/ScenarioTemplateControllerTest.java` (5 tests)
  - `lister6Templates` — 6 templates triés par catégorie
  - `getParId` — 200 ou 404
  - `executer` — 200 + ScenarioResult
  - `executerIdInconnu` — 404
  - `supplierRetourneUneInstanceFraiche` — sanity check no shared state

## Chantier C — Upload bulletins PDF/image + déclencheur modèle 3 signaux

### Pièces existantes réutilisées (telles quelles)
- `OcrService.extraireNotes(MultipartFile)` (profil/domain/service/OcrService.java) — OCR PDFBox + OpenAI vision
- `DocumentService.uploadDocument(...)` — upload MinIO + persistance `Document`
- `NoteSaisiManuelService.ajouterNote(...)` — création d'une note par matière
- `Recommandation3SignauxService.recommander(UUID)` — moteur 3 signaux (Phase 3)
- `application.properties` — `spring.servlet.multipart.max-file-size=20MB` (déjà configuré)

### Fichiers créés
- `profil/domain/enums/Periode.java` — enum DEBUT/MILIEU/FIN (avec `label` pour l'UI)
- `profil/domain/enums/TypePeriode.java` — enum TRIMESTRE/SEMESTRE
- `profil/application/dto/request/BulletinUploadRequest.java` — DTO input (file + anneeScolaire + periode + typePeriode + numeroPeriode), validé `@Pattern` pour l'année (`YYYY-YYYY`)
- `profil/application/dto/response/BulletinUploadResponse.java` — DTO output consolidé (document + notes extraites + notes créées + recommandation + message)
- `profil/domain/service/BulletinUploadOrchestrator.java` — interface (`orchestrer`, `orchestrerBatch`)
- `profil/domain/service/serviceImple/BulletinUploadOrchestratorImpl.java` — implémentation : `OCR → Document → NotesSaisiManuel × N → Recommandation 3 signaux` (transaction unique). `buildSemestreLabel(req)` dérive "Trimestre 2" / "Semestre 1" depuis (TypePeriode, numeroPeriode)
- `profil/application/controller/BulletinUploadController.java` — 2 endpoints:
  - `POST /api/v1/eleves/{trackingId}/bulletins` — mono upload (5 `@RequestParam`)
  - `POST /api/v1/eleves/{trackingId}/bulletins/batch` — batch 1..3 fichiers (tableaux parallèles)
- `test/.../profil/domain/service/serviceImple/BulletinUploadOrchestratorImplTest.java` (7 tests)
  - `orchestrerCheminNominal` — 3 notes extraites → 3 notes créées + reco
  - `orchestrerEleveIntrouvable` — 404, court-circuit
  - `orchestrerAucuneNoteExtraite` — OCR vide, reco quand même
  - `orchestrerBatch2Bulletins` — 2 bulletins → 2 résultats
  - `buildSemestreLabel` — sanity check mapping
  - `orchestrerBatchVide` — IllegalArgumentException
  - `orchestrerBatchTrop` — IllegalArgumentException (> 3)
- `test/.../profil/application/controller/BulletinUploadControllerTest.java` (4 tests)
  - `uploadMono` — 200 + BulletinUploadResponse
  - `uploadBatch3Fichiers` — 200 + 3 réponses
  - `uploadBatch4FichiersTrop` — IllegalArgumentException
  - `uploadBatchTaillesDifferentes` — IllegalArgumentException

### Fichiers modifiés
- `shared/security/config/SecurityConfig.java` — 1 ligne explicite (lisibilité) :
  ```java
  .requestMatchers(HttpMethod.POST,
      "/api/v1/eleves/*/bulletins",
      "/api/v1/eleves/*/bulletins/batch")
      .authenticated()
  ```
  (Sécurité fine via `@PreAuthorize("@security.isOwner(#eleveTrackingId) or hasRole('ADMIN')")` sur le controller)

## Bilan global

| Module | Tests avant | Tests après | Δ |
|---|---|---|---|
| simulateur | 0 | 9 | +9 |
| profil | 6 | 17 | +11 |
| Autres (prediction, shared...) | ~54 | ~54 | 0 |
| **TOTAL** | **60** | **71** | **+11** |

- **Compile** : 531 fichiers, BUILD SUCCESS
- **Tests globaux** : 71/71, 0 échec, 0 erreur, 1 skipped
- **Régression** : 0

## Hors scope (à voir en session dédiée)

- OCR multi-page PDF (bulletins togolais font souvent 2 pages — `OcrService` actuel ne fait que la 1ʳᵉ)
- Validation manuelle post-OCR (l'élève relit/confirme les notes détectées avant sauvegarde)
- Notifications push au conseiller quand son élève upload un bulletin
- Endpoint de listing des bulletins uploadés (reprendre `getDocuments` déjà existant dans `DocumentService`)

## Pattern / conventions appliquées

- **Sécurité fine** : `@PreAuthorize("@security.isOwner(#eleveTrackingId) or hasRole('ADMIN')")` au niveau méthode
- **UUID dans les URLs** : tous les endpoints utilisent `UUID trackingId` (jamais `Long id`)
- **Package by Feature** : nouveau code dans `profil/domain/enums/`, `profil/application/dto/`, `profil/domain/service/`, `profil/application/controller/`
- **Mockito strict** : tests avec `@ExtendWith(MockitoExtension.class)`, pas de stubs inutilisés
- **Logs structurés** : `log.info("Orchestration bulletin : eleve={} annee={} periode={}/{}/T{}", ...)` pour traçabilité
- **Transactions** : orchestrateur en `@Transactional`, méthodes du moteur 3 signaux en `@Transactional(readOnly = true)`
- **DTOs Lombok** : `@Data @Builder @NoArgsConstructor @AllArgsConstructor` partout
- **Validation Bean Validation** : `@NotBlank`, `@NotNull`, `@Min`, `@Max`, `@Pattern` sur les DTOs
- **Tests unitaires** : pattern "instanciation directe du controller avec mock" (pas de MockMvc sauf si vraiment nécessaire)
