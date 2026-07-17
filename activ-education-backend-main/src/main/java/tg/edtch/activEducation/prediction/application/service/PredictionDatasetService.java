package tg.edtch.activEducation.prediction.application.service;

import tg.edtch.activEducation.prediction.application.dto.PredictionDatasetRow;

import java.util.List;

/**
 * Construction du dataset d'entraînement supervisé (Phase 5).
 *
 * <p>Pour l'instant, agrège les {@code orientation_outcome} fermés (ADMIS /
 * RECALE) avec un identifiant anonyme (SHA-256 tronqué du trackingId).
 * Voir {@code CHANGELOG_SCHEMA.md} § 4 et {@code train_model.py} pour le
 * format exact attendu.</p>
 */
public interface PredictionDatasetService {

    /**
     * Construit la liste de lignes du dataset.
     * Le filtrage par label (ADMIS/RECALE) est appliqué ici.
     */
    List<PredictionDatasetRow> construireDataset();

    /** Sérialise en CSV (header + lignes RFC 4180). */
    String serialiserCsv(List<PredictionDatasetRow> rows);
}
