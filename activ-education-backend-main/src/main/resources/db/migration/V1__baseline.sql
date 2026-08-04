-- V1__baseline.sql
--
-- Baseline pour Flyway : ancre le schéma existant dans la table flyway_schema_history.
-- Avec baseline-on-migrate=true, ce fichier n'exécute rien (la DB existe déjà).
-- Les vraies migrations commenceront à partir de V2__xxx.sql.
--
-- Cette approche est recommandée par Flyway pour adopter l'outil sur une
-- base existante sans avoir à rétro-concevoir tout le schéma.

SELECT 1;