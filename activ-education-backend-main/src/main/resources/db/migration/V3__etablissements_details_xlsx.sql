-- V3__etablissements_details_xlsx.sql
--
-- Sprint xlsx — 6 août 2026 : ajoute la table `etablissement_details_xlsx`
-- qui stocke les données structurées de Base_Etablissements_Superieurs_Togo.xlsx
-- (24 feuilles) en JSON libre, liées 1-1 à fiches_etablissement.
--
-- Cf. entité tg.edtch.activEducation.bibliotheque.domain.entite.EtablissementDetailsXlsx.
--
-- Pourquoi du JSON et pas 7 entités JPA ?
--   - Le MVP vise à éviter la migration de schéma coûteuse (CLAUDE.md §pièges
--     critiques : ddl-auto=update complique toute ALTER TABLE).
--   - 575 lignes de Formations + 208 lignes de Sources : sérialiser en JSON
--     évite de multiplier les relations.
--   - Si le besoin de requêtes typées émerge (ex: "toutes les écoles dont
--     le téléphone est +228 22"), on migrera vers des entités dédiées dans
--     un sprint ultérieur.

CREATE TABLE IF NOT EXISTS etablissement_details_xlsx (
    id                       BIGSERIAL PRIMARY KEY,
    created_at               TIMESTAMP,
    updated_at               TIMESTAMP,
    created_by               VARCHAR(100),
    updated_by               VARCHAR(100),
    -- Identifiant source xlsx (ex : "TG-UL-001"). Nullable : la fiche peut
    -- être créée sans être importée depuis le xlsx.
    id_source_xlsx           VARCHAR(50),
    -- FK partagée avec fiches_etablissement.id (pattern JOINED du projet).
    -- Unique : 1-1 strict.
    fiche_id                 BIGINT NOT NULL UNIQUE,
    -- JSON libre sérialisé par ObjectMapper via
    -- EtablissementDetailsXlsxServiceImpl.upsertDetails().
    donnees_etendues_json    TEXT,
    -- Date d'extraction depuis le xlsx pour traçabilité.
    date_extraction          TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_etab_details_fiche
    ON etablissement_details_xlsx(fiche_id);

-- Pas de FK vers fiches_etablissement(id) car Hibernate insert en deux
-- temps (fiche parente d'abord, détails ensuite). La cohérence est assurée
-- par l'unicité du `fiche_id` et les contraintes applicatives du service.
