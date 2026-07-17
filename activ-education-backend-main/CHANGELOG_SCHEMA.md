# CHANGELOG_SCHEMA — Module Prédiction (Phase 1)

> **Statut :** 🚧 En cours d'application
> **Date :** 2026-07-16
> **Contexte :** Module Prédiction & Recommandation IA — Phase 1 (modèle de données)
> **⚠️ Pas de Flyway/Liquibase** : ce fichier EST l'outil de migration.
> Tout changement de schéma non documenté ici est à proscrire.

---

## 1. Conversion `eleves.niveau` String → enum `NiveauScolaire`

### Avant
```sql
ALTER TABLE eleves
  ADD COLUMN niveau VARCHAR(100);  -- valeurs libres : "Terminale C", "Licence 2", etc.
```

### Après
```sql
-- ddl-auto=update Hibernate ne sait pas convertir un VARCHAR en ENUM
-- On garde VARCHAR + check constraint applicative + nouvelle colonne enum temporaire
ALTER TABLE eleves
  ADD COLUMN niveau_enum VARCHAR(20);

-- Migration des données existantes (mapping tolérant : cf. NiveauScolaire.parse)
UPDATE eleves
   SET niveau_enum = CASE
     WHEN UPPER(TRIM(niveau)) LIKE 'COLLEGE%'        THEN 'COLLEGE'
     WHEN UPPER(TRIM(niveau)) LIKE 'SECONDE%'        THEN 'LYCEE_2ND'
     WHEN UPPER(TRIM(niveau)) LIKE 'PREMIERE%'       THEN 'LYCEE_1ERE'
     WHEN UPPER(TRIM(niveau)) LIKE 'TERMINALE%'      THEN 'LYCEE_TLE'
     WHEN UPPER(TRIM(niveau)) LIKE 'L1%'             THEN 'BAC_1'
     WHEN UPPER(TRIM(niveau)) LIKE 'LICENCE 1%'      THEN 'BAC_1'
     WHEN UPPER(TRIM(niveau)) LIKE 'L2%'             THEN 'BAC_2'
     WHEN UPPER(TRIM(niveau)) LIKE 'LICENCE 2%'      THEN 'BAC_2'
     WHEN UPPER(TRIM(niveau)) LIKE 'L3%'             THEN 'BAC_3'
     WHEN UPPER(TRIM(niveau)) LIKE 'LICENCE 3%'      THEN 'BAC_3'
     WHEN UPPER(TRIM(niveau)) LIKE 'BAC+1%'          THEN 'BAC_1'
     WHEN UPPER(TRIM(niveau)) LIKE 'BAC+2%'          THEN 'BAC_2'
     WHEN UPPER(TRIM(niveau)) LIKE 'BAC+3%'          THEN 'BAC_3'
     ELSE NULL
   END
 WHERE niveau IS NOT NULL;

-- Une fois vérifié que la migration est OK :
ALTER TABLE eleves DROP COLUMN niveau;
ALTER TABLE eleves RENAME COLUMN niveau_enum TO niveau;
ALTER TABLE eleves
  ADD CONSTRAINT ck_eleves_niveau CHECK (
    niveau IS NULL OR niveau IN
      ('COLLEGE','LYCEE_2ND','LYCEE_1ERE','LYCEE_TLE','BAC_1','BAC_2','BAC_3')
  );
```

### Rétrocompatibilité
- La méthode statique `NiveauScolaire.parse(String)` accepte les anciens libellés ("Terminale C", "Licence 2 Informatique") et retourne `null` si elle ne sait pas trancher.
- Côté API : `EleveRequest.niveauEtude` change de type `String` → `NiveauScolaire`. Les clients mobile/backoffice devront envoyer une des 7 valeurs canoniques ou un libellé reconnu par `parse()`. Une rétrocompat de façade peut être ajoutée en `@JsonCreator` si nécessaire (à trancher en Phase 2 si trop de pannes côté front).

### Fichiers impactés
- `profil/domain/enums/NiveauScolaire.java` (nouveau)
- `profil/domain/entite/Eleve.java` (champ `niveau` typé enum)
- `profil/application/dto/request/EleveRequest.java` (champ `niveauEtude` typé enum)
- `profil/application/dto/response/EleveResponse.java` (idem)
- `profil/application/mapper/EleveMapper.java` (3 sites : `toEntity`, `toResponse`, `updateFromRequest`)
- `profil/domain/service/ReleveNotesService.java` (2 sites : `setNiveau`, `determinerNiveau()` doit retourner `NiveauScolaire`)
- `shared/ai/service/OriaService.java` (lecture : utiliser `.name()` pour la sérialisation texte)
- `shared/ai/service/RecommandationIAService.java` (idem)

---

## 2. Nouvelle table `niveaux_filieres` (mapping niveau ↔ filière éligible)

Une filière peut cibler plusieurs niveaux. Plutôt qu'un `niveau_requis` String sur
`FicheFiliere`, on crée une table de mapping.

```sql
CREATE TABLE niveaux_filieres (
  id                  BIGSERIAL PRIMARY KEY,
  tracking_id         UUID NOT NULL UNIQUE,
  fiche_filiere_id    BIGINT NOT NULL REFERENCES fiches_filiere(id) ON DELETE CASCADE,
  niveau              VARCHAR(20) NOT NULL,
  est_principal       BOOLEAN NOT NULL DEFAULT FALSE,
  created_at          TIMESTAMP,
  updated_at          TIMESTAMP,

  CONSTRAINT uk_filiere_niveau UNIQUE (fiche_filiere_id, niveau),
  CONSTRAINT ck_niveaux_filieres_niveau CHECK (
    niveau IN ('COLLEGE','LYCEE_2ND','LYCEE_1ERE','LYCEE_TLE','BAC_1','BAC_2','BAC_3')
  )
);

CREATE INDEX idx_niveaux_filieres_filiere ON niveaux_filieres(fiche_filiere_id);
CREATE INDEX idx_niveaux_filieres_niveau ON niveaux_filieres(niveau);
```

### Note
- Le champ `FicheFiliere.niveauRequis` (String, 100) est **conservé** pour la
  rétrocompat du filtrage existant (`listerParNiveau` du module
  `bibliotheque/`). Une migration de `niveauRequis` vers un enum est **hors
  scope** de la Phase 1 — risque trop élevé de casser le front.
- Le `niveau_requis` String reste utilisé comme "label principal" affiché dans
  la fiche ; `niveaux_filieres` est la source de vérité pour le filtrage
  algorithmique.

### Fichiers impactés
- `bibliotheque/domain/entite/NiveauFiliere.java` (nouveau)
- `bibliotheque/domain/dto/NiveauFiliereRequest.java` (nouveau)
- `bibliotheque/domain/dto/NiveauFiliereResponse.java` (nouveau)
- `bibliotheque/repository/NiveauFiliereRepository.java` (nouveau)

---

## 3. Nouvelle table `notes_historique` (bulletins 2 ans + année en cours)

Stocke les moyennes annuelles pour calculer la trajectoire académique
(régression linéaire sur 3 points).

```sql
CREATE TABLE notes_historique (
  id                  BIGSERIAL PRIMARY KEY,
  tracking_id         UUID NOT NULL UNIQUE,
  eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
  annee_scolaire      VARCHAR(9) NOT NULL,        -- ex. "2024-2025"
  classe              VARCHAR(50) NOT NULL,       -- ex. "Terminale C", "Licence 2"
  niveau              VARCHAR(20) NOT NULL,        -- enum NiveauScolaire
  matiere             VARCHAR(100),               -- NULL si moyenne générale
  moyenne             NUMERIC(5,2) NOT NULL,
  est_partielle       BOOLEAN NOT NULL DEFAULT FALSE,
  est_moyenne_generale BOOLEAN NOT NULL DEFAULT FALSE,
  source              VARCHAR(20) NOT NULL DEFAULT 'SAISIE_MANUELLE',
                      -- SAISIE_MANUELLE / OCR / IMPORT_CSV
  created_at          TIMESTAMP,
  updated_at          TIMESTAMP,

  CONSTRAINT ck_notes_historique_niveau CHECK (
    niveau IN ('COLLEGE','LYCEE_2ND','LYCEE_1ERE','LYCEE_TLE','BAC_1','BAC_2','BAC_3')
  ),
  CONSTRAINT ck_notes_historique_moyenne CHECK (moyenne BETWEEN 0 AND 20)
);

CREATE INDEX idx_notes_historique_eleve ON notes_historique(eleve_id);
CREATE INDEX idx_notes_historique_niveau ON notes_historique(niveau);
CREATE INDEX idx_notes_historique_annee  ON notes_historique(annee_scolaire);
```

### Différence avec `note_saisi_manuellement` (existante)
L'existant stocke des **notes par matière** pour la **note instantanée**. Le
nouveau `notes_historique` stocke des **moyennes annuelles** sur 3 ans pour la
**trajectoire**. Les deux coexistent ; un endpoint de migration pourra
synchroniser la note courante vers `notes_historique` au moment de l'analyse.

### Fichiers impactés
- `profil/domain/entite/NotesHistorique.java` (nouveau)
- `profil/application/dto/request/NotesHistoriqueRequest.java` (nouveau)
- `profil/application/dto/response/NotesHistoriqueResponse.java` (nouveau)
- `profil/repository/NotesHistoriqueRepository.java` (nouveau)
- `profil/application/controller/NotesHistoriqueController.java` (nouveau)

---

## 4. Nouvelle table `orientation_outcome` (suivi des choix)

Cible d'entraînement supervisé (Phase 5) : on enregistre le choix de filière
de l'élève, son état à N mois, et sa satisfaction.

```sql
CREATE TABLE orientation_outcome (
  id                       BIGSERIAL PRIMARY KEY,
  tracking_id              UUID NOT NULL UNIQUE,
  eleve_id                 BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
  filiere_id               BIGINT NOT NULL REFERENCES fiches_filiere(id),
  date_choix               DATE NOT NULL,
  riasec_snapshot          JSONB,                   -- {R:0.7, I:0.8, ...}
  notes_snapshot           JSONB,                   -- {n2, n1, actuelle, tendance}
  serie                    VARCHAR(10),             -- "A", "B", ...
  score_recommandation     NUMERIC(5,3),            -- score combiné Phase 3
  score_aspiration         NUMERIC(5,3),
  score_realite            NUMERIC(5,3),
  score_engagement         NUMERIC(5,3),
  statut                   VARCHAR(20) NOT NULL DEFAULT 'EN_COURS',
                           -- EN_COURS / ADMIS / RECALE / ABANDON / REORIENTE
  satisfaction             INTEGER,                 -- 1..5
  date_maj_statut          DATE,
  commentaire              TEXT,
  created_at               TIMESTAMP,
  updated_at               TIMESTAMP,

  CONSTRAINT ck_orientation_outcome_statut CHECK (
    statut IN ('EN_COURS','ADMIS','RECALE','ABANDON','REORIENTE')
  ),
  CONSTRAINT ck_orientation_outcome_satisfaction
    CHECK (satisfaction IS NULL OR satisfaction BETWEEN 1 AND 5)
);

CREATE INDEX idx_orientation_outcome_eleve   ON orientation_outcome(eleve_id);
CREATE INDEX idx_orientation_outcome_filiere ON orientation_outcome(filiere_id);
CREATE INDEX idx_orientation_outcome_statut  ON orientation_outcome(statut);
CREATE INDEX idx_orientation_outcome_date    ON orientation_outcome(date_choix);
```

### Notes
- Le brief ne mentionne pas `EN_COURS` / `ABANDON` / `RECALE` — on les ajoute
  car ils sont nécessaires pour suivre la vie d'un choix (un élève n'est pas
  immédiatement `ADMIS` ou `REORIENTE`).
- Les colonnes `score_*` sont remplies **rétroactivement** par la Phase 3
  pour les outcomes déjà créés (script de backfill à écrire).

### Fichiers impactés
- `prediction/domain/entite/OrientationOutcome.java` (nouveau)
- `prediction/application/dto/request/OrientationOutcomeRequest.java` (nouveau)
- `prediction/application/dto/response/OrientationOutcomeResponse.java` (nouveau)
- `prediction/repository/OrientationOutcomeRepository.java` (nouveau)

---

## 5. Nouvelle table `engagement_signal` (vue matérialisée des signaux comportementaux)

Agrégat par (élève, fiche) des consultations / favoris / recherches RAG. C'est
le **3e signal** du moteur de recommandation (Phase 3).

```sql
CREATE TABLE engagement_signal (
  id                            BIGSERIAL PRIMARY KEY,
  tracking_id                   UUID NOT NULL UNIQUE,
  eleve_id                      BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
  fiche_id                      BIGINT NOT NULL,    -- polymorphe vers n'importe quelle fiche
  fiche_type                    VARCHAR(30) NOT NULL,  -- SERIE / FILIERE / METIER / ETABLISSEMENT
  nb_consultations              INTEGER NOT NULL DEFAULT 0,
  en_favori                     BOOLEAN NOT NULL DEFAULT FALSE,
  temps_lecture_moyen_secondes  INTEGER,            -- agrégé sur les 30 derniers jours
  derniere_consultation         TIMESTAMP,
  score_similarite_recherche    NUMERIC(5,3),       -- 0..1
  derniere_actualisation        TIMESTAMP NOT NULL DEFAULT NOW(),
  created_at                    TIMESTAMP,
  updated_at                    TIMESTAMP,

  CONSTRAINT uk_engagement_eleve_fiche UNIQUE (eleve_id, fiche_id, fiche_type),
  CONSTRAINT ck_engagement_fiche_type CHECK (
    fiche_type IN ('SERIE','FILIERE','METIER','ETABLISSEMENT')
  ),
  CONSTRAINT ck_engagement_score_sim CHECK (
    score_similarite_recherche IS NULL
    OR score_similarite_recherche BETWEEN 0 AND 1
  )
);

CREATE INDEX idx_engagement_eleve     ON engagement_signal(eleve_id);
CREATE INDEX idx_engagement_fiche     ON engagement_signal(fiche_id, fiche_type);
CREATE INDEX idx_engagement_derniere  ON engagement_signal(derniere_consultation);
```

### Politique de rafraîchissement
- **Pas de mise à jour temps réel** (coût trop élevé pour un signal auxiliaire).
- Une **tâche planifiée** (`@Scheduled`, à câbler en Phase 3 ou plus tard)
  recalcule les agrégats depuis la table `historique_utilisateur` (consultations
  de fiches) et la table `favoris`.
- Pour la Phase 3, on accepte que les agrégats soient **partiellement**
  rafraîchi en stream (`PUT /consultation` → maj incrémentale) et le reste par
  batch quotidien.

### Fichiers impactés
- `prediction/domain/entite/EngagementSignal.java` (nouveau)
- `prediction/repository/EngagementSignalRepository.java` (nouveau)

---

## 6. `quiz.niveau_cible` (déjà couvert par `questions.niveau_cible`)

L'entité `Question` a déjà un champ `niveau_cible` (cf. `Question.java` ligne 49
+ `QuizEditorPage.tsx` côté backoffice). On **ne duplique pas** ce champ sur
`Quiz` : la couverture par-question suffit. Aucune migration de schéma.

### Action
- Documenter dans `RESULTATS_PROTOTYPE.md` et la javadoc de `Question` que
  `niveau_cible` est l'attribut de ciblage. Un quiz est "multi-niveaux" si
  ses questions couvrent plusieurs valeurs.

---

## 7. Résumé du delta

| Type | Table | Δ colonnes | Risque |
|---|---|---|---|
| Conversion type | `eleves.niveau` | VARCHAR(100) → VARCHAR(20) + CHECK | **Moyen** — données existantes à migrer, voir § 1 |
| Nouvelle table | `niveaux_filieres` | +7 col. | Faible |
| Nouvelle table | `notes_historique` | +13 col. | Faible |
| Nouvelle table | `orientation_outcome` | +16 col. | Faible |
| Nouvelle table | `engagement_signal` | +13 col. | Faible |

**Hibernate `ddl-auto=update` créera les nouvelles tables** automatiquement.
Pour la conversion de `eleves.niveau` (§ 1), il faut exécuter manuellement le
bloc SQL en deux temps :
1. Ajouter `niveau_enum` via Hibernate (ou manuellement avant `mvn spring-boot:run`)
2. Exécuter l'UPDATE + DROP/RENAME
3. Relancer Spring Boot

Une approche alternative — **plus simple et moins risquée** — consiste à
**garder `Eleve.niveau` comme String en base** mais mapper vers l'enum
côté Java via un `AttributeConverter` JPA. C'est l'option que je retiens
par défaut (cf. `NiveauScolaireConverter.java`) :

```java
@Converter(autoApply = true)
public class NiveauScolaireConverter implements AttributeConverter<NiveauScolaire, String> {
    @Override public String convertToDatabaseColumn(NiveauScolaire n) {
        return n == null ? null : n.name();
    }
    @Override public NiveauScolaire convertToEntityAttribute(String s) {
        return NiveauScolaire.parse(s);
    }
}
```

→ **Aucun DDL à exécuter manuellement.** Le champ reste VARCHAR(20) (réduit
de 100) en base, mais le mapping Java est désormais strict. Migration de
données : on s'appuie sur `parse()` pour tolérer les anciens libellés.

## 8. Checklist d'application

- [x] § 1 — `NiveauScolaire.java` créé
- [x] § 1 — `NiveauScolaireConverter.java` créé
- [x] § 1 — `Eleve.niveau` typé enum + converter
- [x] § 1 — `EleveRequest.niveauEtude` typé enum
- [x] § 1 — `EleveResponse.niveauEtude` typé enum
- [x] § 1 — `EleveMapper` adapté
- [x] § 1 — `ReleveNotesService.determinerNiveau()` retourne `NiveauScolaire`
- [x] § 1 — `OriaService` / `RecommandationIAService` utilisent `.name()`
- [x] § 2 — `NiveauFiliere` (entity, repo, DTO)
- [x] § 3 — `NotesHistorique` (entity, repo, DTO, controller)
- [x] § 4 — `OrientationOutcome` (entity, repo, DTO)
- [x] § 5 — `EngagementSignal` (entity, repo)
- [x] Build OK : `./mvnw clean install -DskipTests`
- [x] Démarrage OK : `curl http://localhost:8080/actuator/health` → UP

---

# CHANGELOG_SCHEMA — Module Prédiction (Phase 2 — Endpoints REST)

> **Statut :** ✅ Livré
> **Date :** 2026-07-16
> **Contexte :** Module Prédiction & Recommandation IA — Phase 2 (API REST)

## 6. Endpoints REST ajoutés

| Endpoint | Méthode | Auth | Rôle min |
|---|---|---|---|
| `/api/v1/niveaux` | GET | JWT | `ELEVE` |
| `/api/v1/filieres?niveau=` | GET | JWT | `ELEVE` |
| `/api/v1/eleves/{id}/notes-historique` | GET/POST | JWT | `ELEVE` (owner) |
| `/api/v1/eleves/{id}/notes-historique/moyennes-generales` | GET | JWT | `ELEVE` |
| `/api/v1/eleves/{id}/orientation-outcome` | GET/POST | JWT | `ELEVE` (owner) |
| `/api/v1/eleves/{id}/orientation-outcome/{id}` | PATCH | JWT | `CONSEILLER`/`ADMIN` |
| `/api/v1/admin/prediction/dataset` | GET | JWT | `ADMIN` |

> Tous les endpoints nouveaux tombent dans `anyRequest().authenticated()` de
> `SecurityConfig.java` — pas de modification de la config sécurité requise.

---

# CHANGELOG_SCHEMA — Module Prédiction (Phase 3 — Moteur 3 signaux)

> **Statut :** ✅ Livré
> **Date :** 2026-07-16
> **Contexte :** Module Prédiction & Recommandation IA — Phase 3 (moteur algorithmique)

## 7. Moteur de recommandation 3 signaux

### 7.1 Algorithme

```
score_final = 0.50·score_realite + 0.35·score_aspiration + 0.15·score_engagement
```

avec plafond strict `poids_engagement ≤ 0.20` (cf. `PredictionProperties.poidsEngagementMax`).

### 7.2 Sous-scores

| Sous-score | Formule | Plage | Source |
|---|---|---|---|
| `score_aspiration` | cosinus(riasec6_élève, riasec6_filière) | 0..1 | `TestRIASECResultat` × `ProfilFiliereRiasecCatalog` |
| `score_realite` | note_extrapolée / seuil_admission + bonus_tendance | 0..1 | `NotesHistorique` (3 dernières moyennes) |
| `score_engagement` | `1 - exp(-(0.1·consultations + 0.5·enFavori + 0.3·simRAG))` | 0..1 | `EngagementSignal` |

### 7.3 Configuration externalisée

```properties
app.prediction.poids-aspiration=0.35
app.prediction.poids-realite=0.50
app.prediction.poids-engagement=0.15
app.prediction.poids-engagement-max=0.20   # garde-fou "anti bulle de filtre"
app.prediction.top-n=10
app.prediction.decouvertes-min=2           # nb de "découvertes" garanties dans le top
app.prediction.seuil-admission-defaut=12.0
```

### 7.4 Découvertes

Le moteur force l'inclusion de N "découvertes" dans le top : filières à
**fort** score_aspiration/realite mais **faible** score_engagement
(`engagement ≤ 0.1` ET `aspiration ≥ 0.6`). Cela évite la bulle de filtre
en exposant des options pertinentes mais pas encore consultées.

### 7.5 Endpoint

`GET /api/v1/eleves/{trackingId}/recommandation-ia/v2` — cohabite avec le
v1 (LLM OpenAI/Groq/Ollama). Aucune régression sur l'existant.

### 7.6 Tests

```
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
- NoteTrajectoireServiceTest            6/6 ✅
- PredictionLookupServiceTest            4/4 ✅
- Recommandation3SignauxServiceTest     3/3 ✅
- OrientationOutcomeServiceTest          4/4 ✅
```

### 7.7 Limites connues

- **Catalogue RIASEC en dur** : 15 profils typiques (Informatique,
  Médecine, Droit, Gestion, ...). Les autres filières tombent sur un
  profil neutre (0.5/0.5/0.5/0.5/0.5/0.5). À raffiner en Phase 5 avec
  les données réelles de `orientation_outcome`.
- **Seuil d'admission par défaut = 12/20** : appliqué quand la fiche
  filière ne précise pas son propre seuil. À terme, ajouter une colonne
  `seuil_admission` sur `FicheFiliere`.
- **Trajectoire 3 points = 1 an de projection** : suffisant pour le
  secondaire (3 années lycée), insuffisant pour le supérieur (L1→L3).

---

# CHANGELOG_SCHEMA — Module Prédiction (Phase 4 — Intégration clients)

> **Statut :** ✅ Livré
> **Date :** 2026-07-16
> **Contexte :** Module Prédiction & Recommandation IA — Phase 4 (mobile Flutter + backoffice React)

## 8. Câblage côté mobile (Flutter)

Trois nouveaux écrans + un service + des modèles Dart consomment les
endpoints livrés en Phase 2/3 :

| Écran | Route Flutter | Endpoint backend principal |
|---|---|---|
| `SelectionNiveauScreen` | `/selection-niveau` | `GET /api/v1/niveaux` + `PUT /api/v1/eleves/{id}` |
| `BulletinsHistoriqueScreen` | `/bulletins-historique` | `POST /api/v1/eleves/{id}/notes-historique` |
| `Recommandation3SignauxScreen` | `/recommandation-3-signaux` | `GET /api/v1/eleves/{id}/recommandation-ia/v2` |

### 8.1 Parcours utilisateur

```
[Dashboard / RecommandationIA v1]
  └─> [Tester la recommandation v2 (3 signaux)]
        ├─ si niveauEtude null → SelectionNiveauScreen (grille 7 cartes)
        │                            └─> PUT niveau → BulletinsHistoriqueScreen
        └─ sinon                 → BulletinsHistoriqueScreen (3 années pré-remplies)
                                     └─> POST 3..15 lignes → Recommandation3SignauxScreen
                                                                  └─> Top 10 + 3 sous-scores
                                                                      + badge "Découverte"
```

### 8.2 Fichiers ajoutés

- `activ_education/lib/services/prediction_service.dart` (6 méthodes HTTP)
- `activ_education/lib/models/prediction_models.dart` (NiveauModel, FilierePourNiveauModel, NoteHistoriqueRequest/Response, FiliereScoreeModel, Recommandation3SignauxModel, ProfilEleveModel)
- `activ_education/lib/screens/orientation/selection_niveau_screen.dart`
- `activ_education/lib/screens/orientation/bulletins_historique_screen.dart`
- `activ_education/lib/screens/orientation/recommandation_3_signaux_screen.dart`

### 8.3 Fichiers modifiés

- `lib/services/api_service.dart` — ajout du sub-service `prediction = PredictionService()`
- `lib/models/models.dart` — export de `prediction_models.dart`
- `lib/theme/app_routes.dart` — 3 nouvelles routes
- `lib/main.dart` — 3 builders de page
- `lib/screens/home/recommandation_ia_screen.dart` — CTA v2 + méthode `_lancerParcoursV2()`
- `lib/screens/home/dashboard_bachelier.dart` — pill "Tester la v2" dans la tuile IA

## 9. Alignement backoffice (React/TypeScript)

### 9.1 Dropdown `niveau_cible` aligné sur l'enum `NiveauScolaire`

Le backoffice `QuizEditorPageForm.tsx` exposait 6 valeurs legacy
(`Ecolier`, `Collégien`, `Lycéen`, `Étudiant`, `Professionnel`,
`Tous niveaux`) qui ne matchaient pas l'enum `NiveauScolaire` côté backend
(COLLEGE, LYCEE_2ND, LYCEE_1ERE, LYCEE_TLE, BAC_1, BAC_2, BAC_3).

**Avant** :
```ts
const NIVEAUX = [
  { value: '', label: 'Tous niveaux' },
  { value: 'Ecolier', label: 'Écolier' },
  { value: 'Collégien', label: 'Collégien' },
  { value: 'Lycéen', label: 'Lycéen' },
  { value: 'Étudiant', label: 'Étudiant' },
  { value: 'Professionnel', label: 'Professionnel' },
]
```

**Après** :
```ts
const NIVEAUX = [
  { value: '', label: 'Tous niveaux' },
  { value: 'COLLEGE', label: 'Collège' },
  { value: 'LYCEE_2ND', label: 'Seconde' },
  { value: 'LYCEE_1ERE', label: 'Première' },
  { value: 'LYCEE_TLE', label: 'Terminale' },
  { value: 'BAC_1', label: 'Licence 1' },
  { value: 'BAC_2', label: 'Licence 2' },
  { value: 'BAC_3', label: 'Licence 3' },
]
```

### 9.2 Effet de bord à surveiller

- Les `Question.niveauCible` déjà en base avec une **valeur legacy**
  (`Ecolier` / `Collégien` / `Lycéen` / etc.) deviendront **invisibles**
  dans le dropdown (l'option n'existera plus). La valeur en base n'est
  pas effacée — elle sera de nouveau sélectionnable si l'admin revient
  à une saisie manuelle (raw input).
- Recommandation : faire un audit SQL après MEP :
  ```sql
  SELECT niveau_cible, COUNT(*)
    FROM questions
   WHERE niveau_cible NOT IN ('COLLEGE','LYCEE_2ND','LYCEE_1ERE',
                              'LYCEE_TLE','BAC_1','BAC_2','BAC_3')
     AND niveau_cible IS NOT NULL
   GROUP BY niveau_cible;
  ```
  → Si des questions publiées ont une valeur legacy, prévoir une tâche
  de migration manuelle en Phase 5 (script SQL `UPDATE`).


