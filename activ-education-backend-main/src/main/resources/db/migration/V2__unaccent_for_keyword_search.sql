-- V2 — Active l'extension PostgreSQL `unaccent` pour la recherche par mot-clé
--
-- Contexte (cf. JOURNAL_BORD_IA.md 6 août 2026 §2) :
-- La recherche RAG mot-clé d'ORIA (`rechercherParMotCle`) ne trouvait pas les fiches
-- dont l'info pertinente est dans `fiches_etablissement.ville` (ex: 'Lomé') parce que :
--   1. La requête JPQL ne touchait que titre/resume/contenu (colonnes de la table parente)
--   2. Même avec JOIN sur fiches_etablissement, LOWER('Lomé') LIKE '%lome%' échouait à
--      cause de l'accent sur le 'é' (LOWER = 'lomé', pas 'lome')
--
-- Correction appliquée dans le repository (FicheRepository.java) :
--   - Requête native avec LEFT JOIN vers fiches_etablissement + fiches_filiere
--   - Fonction unaccent() sur chaque colonne et sur le terme de recherche
--
-- Cette migration rend l'extension disponible. Idempotente.

CREATE EXTENSION IF NOT EXISTS unaccent;
