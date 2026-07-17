package tg.edtch.activEducation.prediction.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Une ligne du dataset d'entraînement supervisé, exportable en CSV.
 *
 * <p>Conçu pour être consommé par le script Python de la Phase 5
 * ({@code train_model.py}). Volontairement plat (pas de JSONB imbriqué)
 * pour ne pas imposer un parser à pandas.</p>
 *
 * <p><strong>Anonymisation</strong> : pas d'identifiant direct. Le champ
 * {@code rowId} est un hash SHA-256 tronqué du {@code trackingId} original,
 * stable entre exports mais non réversible.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionDatasetRow {

    /** Identifiant anonyme stable. */
    private String rowId;

    /** Niveau canonique au moment du choix (LYCEE_TLE, BAC_1, ...). */
    private String niveau;

    /** Série du bac (si connue). */
    private String serie;

    /** Top 3 dimensions RIASEC au moment du choix (codes séparés par "|"). */
    private String riasecTop3;

    /** Score RIASEC global (somme / 3, 0..1). */
    private BigDecimal riasecScore;

    /** Moyenne de l'année N (si renseignée). */
    private BigDecimal noteActuelle;

    /** Moyenne de l'année N-1 (si renseignée). */
    private BigDecimal noteN1;

    /** Moyenne de l'année N-2 (si renseignée). */
    private BigDecimal noteN2;

    /** Tendance de la trajectoire : +1 / 0 / -1 (régression linéaire 3 points). */
    private Integer tendanceNotes;

    /** Score d'aspiration (RIASEC vs filière). */
    private BigDecimal scoreAspiration;

    /** Score de réalité (notes vs seuil d'admission). */
    private BigDecimal scoreRealite;

    /** Score d'engagement (3ᵉ signal, plafonné à 0.25 par construction). */
    private BigDecimal scoreEngagement;

    /** Score combiné. */
    private BigDecimal scoreRecommandation;

    /** Label cible : 1 = ADMIS, 0 = RECALE. */
    private Integer label;
}
