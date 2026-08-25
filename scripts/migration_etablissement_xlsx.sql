-- Migration : ajoute les colonnes XLSX manquantes à fiches_etablissement
-- + crée les tables satellites pour les données structurées
-- À exécuter sur la DB activ_education.
--
-- Date : 23 août 2026
-- Source : Base_Etablissements_Superieurs_Togo.xlsx (24 feuilles)

BEGIN;

-- ── 1. Nouvelles colonnes sur fiches_etablissement ──────────────────────
ALTER TABLE fiches_etablissement
    -- Identité / branding (feuille Etablissements)
    ADD COLUMN IF NOT EXISTS sigle VARCHAR(50),
    ADD COLUMN IF NOT EXISTS logo_url TEXT,
    ADD COLUMN IF NOT EXISTS devise TEXT,
    ADD COLUMN IF NOT EXISTS slogan TEXT,
    ADD COLUMN IF NOT EXISTS statut_juridique TEXT,
    ADD COLUMN IF NOT EXISTS numero_agrement VARCHAR(100),
    ADD COLUMN IF NOT EXISTS date_agrement VARCHAR(50),
    ADD COLUMN IF NOT EXISTS autorite_tutelle VARCHAR(200),
    ADD COLUMN IF NOT EXISTS accreditations TEXT,
    ADD COLUMN IF NOT EXISTS reconnaissance_cames BOOLEAN,
    ADD COLUMN IF NOT EXISTS annee_creation INTEGER,
    ADD COLUMN IF NOT EXISTS historique TEXT,
    ADD COLUMN IF NOT EXISTS presentation TEXT,
    ADD COLUMN IF NOT EXISTS mission TEXT,
    ADD COLUMN IF NOT EXISTS vision TEXT,
    ADD COLUMN IF NOT EXISTS valeurs TEXT,
    -- Localisation détaillée
    ADD COLUMN IF NOT EXISTS region VARCHAR(100),
    ADD COLUMN IF NOT EXISTS prefecture VARCHAR(100),
    ADD COLUMN IF NOT EXISTS commune VARCHAR(100),
    ADD COLUMN IF NOT EXISTS quartier VARCHAR(200),
    ADD COLUMN IF NOT EXISTS lien_google_maps TEXT,
    ADD COLUMN IF NOT EXISTS plan_campus TEXT,
    -- Calendrier
    ADD COLUMN IF NOT EXISTS ouverture_inscriptions VARCHAR(200),
    ADD COLUMN IF NOT EXISTS cloture_inscriptions VARCHAR(200),
    ADD COLUMN IF NOT EXISTS dates_concours VARCHAR(200),
    ADD COLUMN IF NOT EXISTS date_rentree VARCHAR(100),
    ADD COLUMN IF NOT EXISTS vacances VARCHAR(200),
    ADD COLUMN IF NOT EXISTS examens VARCHAR(200),
    ADD COLUMN IF NOT EXISTS soutenances VARCHAR(200),
    -- Frais (XLSX, valeur libre — texte car devises/unités variables)
    ADD COLUMN IF NOT EXISTS frais_inscription VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reinscription VARCHAR(100),
    ADD COLUMN IF NOT EXISTS scolarite_annuelle VARCHAR(100),
    ADD COLUMN IF NOT EXISTS paiement_echelonne BOOLEAN,
    ADD COLUMN IF NOT EXISTS mensualites VARCHAR(100),
    ADD COLUMN IF NOT EXISTS frais_assurance VARCHAR(100),
    ADD COLUMN IF NOT EXISTS frais_uniforme VARCHAR(100),
    ADD COLUMN IF NOT EXISTS frais_carte_etudiant VARCHAR(100),
    ADD COLUMN IF NOT EXISTS frais_laboratoire VARCHAR(100),
    ADD COLUMN IF NOT EXISTS frais_examens VARCHAR(100),
    ADD COLUMN IF NOT EXISTS frais_bibliotheque VARCHAR(100),
    ADD COLUMN IF NOT EXISTS frais_autres VARCHAR(100),
    ADD COLUMN IF NOT EXISTS frais_devise VARCHAR(20),
    -- Infrastructures (XLSX : beaucoup de booléens + nombres)
    ADD COLUMN IF NOT EXISTS nb_batiments INTEGER,
    ADD COLUMN IF NOT EXISTS nb_amphitheatres INTEGER,
    ADD COLUMN IF NOT EXISTS nb_salles_classe INTEGER,
    ADD COLUMN IF NOT EXISTS bibliotheque BOOLEAN,
    ADD COLUMN IF NOT EXISTS bibliotheque_numerique BOOLEAN,
    ADD COLUMN IF NOT EXISTS nb_laboratoires INTEGER,
    ADD COLUMN IF NOT EXISTS salle_informatique BOOLEAN,
    ADD COLUMN IF NOT EXISTS fablab BOOLEAN,
    ADD COLUMN IF NOT EXISTS incubateur BOOLEAN,
    ADD COLUMN IF NOT EXISTS wifi BOOLEAN,
    ADD COLUMN IF NOT EXISTS internat BOOLEAN,
    ADD COLUMN IF NOT EXISTS restaurant BOOLEAN,
    ADD COLUMN IF NOT EXISTS cafeteria BOOLEAN,
    ADD COLUMN IF NOT EXISTS terrain_sport BOOLEAN,
    ADD COLUMN IF NOT EXISTS infirmerie BOOLEAN,
    ADD COLUMN IF NOT EXISTS parking BOOLEAN,
    ADD COLUMN IF NOT EXISTS accessibilite_pmr BOOLEAN,
    ADD COLUMN IF NOT EXISTS securite BOOLEAN,
    ADD COLUMN IF NOT EXISTS infrastructures_synthese TEXT,
    -- Vie étudiante
    ADD COLUMN IF NOT EXISTS clubs TEXT,
    ADD COLUMN IF NOT EXISTS associations TEXT,
    ADD COLUMN IF NOT EXISTS bureau_etudiants BOOLEAN,
    ADD COLUMN IF NOT EXISTS activites_culturelles TEXT,
    ADD COLUMN IF NOT EXISTS sports TEXT,
    ADD COLUMN IF NOT EXISTS hackathons BOOLEAN,
    ADD COLUMN IF NOT EXISTS competitions TEXT,
    ADD COLUMN IF NOT EXISTS conferences TEXT,
    ADD COLUMN IF NOT EXISTS evenements TEXT,
    -- Enseignants
    ADD COLUMN IF NOT EXISTS nb_enseignants INTEGER,
    ADD COLUMN IF NOT EXISTS enseignants_permanents INTEGER,
    ADD COLUMN IF NOT EXISTS enseignants_vacataires INTEGER,
    ADD COLUMN IF NOT EXISTS enseignants_titulaires INTEGER,
    ADD COLUMN IF NOT EXISTS enseignants_docteurs INTEGER,
    ADD COLUMN IF NOT EXISTS enseignants_experts_pro BOOLEAN,
    -- Statistiques
    ADD COLUMN IF NOT EXISTS nb_etudiants INTEGER,
    ADD COLUMN IF NOT EXISTS nb_diplomes INTEGER,
    ADD COLUMN IF NOT EXISTS taux_reussite VARCHAR(50),
    ADD COLUMN IF NOT EXISTS taux_abandon VARCHAR(50),
    ADD COLUMN IF NOT EXISTS ratio_enseignants_etudiants VARCHAR(50),
    ADD COLUMN IF NOT EXISTS taux_insertion_pro VARCHAR(50),
    ADD COLUMN IF NOT EXISTS satisfaction VARCHAR(50),
    -- Partenariats
    ADD COLUMN IF NOT EXISTS partenaires_synthese TEXT,
    ADD COLUMN IF NOT EXISTS universites_partenaires TEXT,
    ADD COLUMN IF NOT EXISTS entreprises_partenaires TEXT,
    ADD COLUMN IF NOT EXISTS ong_partenaires TEXT,
    ADD COLUMN IF NOT EXISTS ministeres_partenaires TEXT,
    ADD COLUMN IF NOT EXISTS org_internationales_partenaires TEXT,
    ADD COLUMN IF NOT EXISTS doubles_diplomes BOOLEAN,
    ADD COLUMN IF NOT EXISTS programmes_mobilite BOOLEAN,
    -- Recherche
    ADD COLUMN IF NOT EXISTS recherche_synthese TEXT,
    ADD COLUMN IF NOT EXISTS laboratoires TEXT,
    ADD COLUMN IF NOT EXISTS publications TEXT,
    ADD COLUMN IF NOT EXISTS projets_recherche TEXT,
    ADD COLUMN IF NOT EXISTS brevets TEXT,
    ADD COLUMN IF NOT EXISTS innovations TEXT,
    -- Bourses
    ADD COLUMN IF NOT EXISTS bourses_internes TEXT,
    ADD COLUMN IF NOT EXISTS bourses_gouvernementales TEXT,
    ADD COLUMN IF NOT EXISTS bourses_internationales TEXT,
    ADD COLUMN IF NOT EXISTS bourses_criteres TEXT,
    ADD COLUMN IF NOT EXISTS bourses_montants TEXT,
    -- Documents (URLs vers PDF)
    ADD COLUMN IF NOT EXISTS brochure_url TEXT,
    ADD COLUMN IF NOT EXISTS guide_etudiant_url TEXT,
    ADD COLUMN IF NOT EXISTS reglement_interieur_url TEXT,
    ADD COLUMN IF NOT EXISTS calendrier_academique_url TEXT,
    ADD COLUMN IF NOT EXISTS dossier_inscription_url TEXT,
    ADD COLUMN IF NOT EXISTS formulaire_admission_url TEXT,
    -- Avis
    ADD COLUMN IF NOT EXISTS note_moyenne_avis NUMERIC(3,2),
    ADD COLUMN IF NOT EXISTS nb_avis INTEGER,
    ADD COLUMN IF NOT EXISTS avis_positifs_resume TEXT,
    ADD COLUMN IF NOT EXISTS avis_critiques_resume TEXT;

-- ── 2. Tables satellites ──────────────────────────────────────────────

-- Réseaux sociaux (1 établissement → N réseaux)
CREATE TABLE IF NOT EXISTS etablissement_reseaux_sociaux (
    etablissement_id BIGINT NOT NULL REFERENCES fiches_etablissement(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL CHECK (type IN ('facebook', 'linkedin', 'instagram', 'tiktok', 'youtube', 'telegram', 'whatsapp')),
    url TEXT NOT NULL,
    PRIMARY KEY (etablissement_id, type)
);
CREATE INDEX IF NOT EXISTS idx_etab_rs_etab ON etablissement_reseaux_sociaux(etablissement_id);

-- Admissions détaillées (1 ↔ 1)
CREATE TABLE IF NOT EXISTS etablissement_admissions (
    etablissement_id BIGINT PRIMARY KEY REFERENCES fiches_etablissement(id) ON DELETE CASCADE,
    niveau_requis TEXT,
    serie_bac_acceptee TEXT,
    concours BOOLEAN,
    etude_dossier BOOLEAN,
    entretien BOOLEAN,
    test BOOLEAN,
    age_minimum INTEGER,
    nombre_places INTEGER,
    dates_inscription VARCHAR(200),
    dates_concours VARCHAR(200)
);

-- Responsables (1 ↔ 1)
CREATE TABLE IF NOT EXISTS etablissement_responsables (
    etablissement_id BIGINT PRIMARY KEY REFERENCES fiches_etablissement(id) ON DELETE CASCADE,
    directeur VARCHAR(200),
    recteur_doyen VARCHAR(200),
    president VARCHAR(200),
    fondateur VARCHAR(200),
    responsable_admissions VARCHAR(200),
    responsable_communication VARCHAR(200),
    contacts_administratifs TEXT
);

-- Stages & insertion pro (1 ↔ 1)
CREATE TABLE IF NOT EXISTS etablissement_stages (
    etablissement_id BIGINT PRIMARY KEY REFERENCES fiches_etablissement(id) ON DELETE CASCADE,
    stage_obligatoire BOOLEAN,
    duree_stage VARCHAR(100),
    entreprises_partenaires TEXT,
    alternance BOOLEAN,
    bureau_carriere BOOLEAN,
    offres_emploi TEXT,
    reseau_anciens TEXT
);

-- Diplômes délivrés (1 ↔ N)
CREATE TABLE IF NOT EXISTS etablissement_diplomes (
    etablissement_id BIGINT NOT NULL REFERENCES fiches_etablissement(id) ON DELETE CASCADE,
    diplome VARCHAR(50) NOT NULL,  -- ex. 'Licence', 'Master', 'BTS', 'CAP', 'BT', 'DUT', 'Doctorat'
    PRIMARY KEY (etablissement_id, diplome)
);
CREATE INDEX IF NOT EXISTS idx_etab_dip_etab ON etablissement_diplomes(etablissement_id);

COMMIT;

-- Vérification rapide
SELECT 'colonnes ajoutees' AS info, COUNT(*) AS valeur
FROM information_schema.columns
WHERE table_name='fiches_etablissement';
SELECT 'tables satellites creees' AS info, COUNT(*) AS valeur
FROM information_schema.tables
WHERE table_name LIKE 'etablissement_%'
  AND table_name NOT IN ('etablissement_details_xlsx', 'etablissement_filiere');
