package tg.edtch.activEducation.prediction.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Une filière scorée par le moteur 3 signaux.
 *
 * <p>Représente une ligne du top retourné par
 * {@code GET /api/v1/eleves/{trackingId}/recommandation-ia/v2}.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiliereScoreeResponse {

    private UUID trackingId;

    /** Titre de la fiche filière. */
    private String titre;

    /** Domaine (Sciences, Lettres, Gestion, ...). */
    private String domaine;

    /** Durée de la formation. */
    private String duree;

    /** Score de similarité RIASEC (0..1). */
    private BigDecimal scoreAspiration;

    /** Score de réalité (notes vs seuil d'admission, 0..1). */
    private BigDecimal scoreRealite;

    /** Score d'engagement comportemental (0..1, plafonné par le moteur). */
    private BigDecimal scoreEngagement;

    /** Score combiné = Σ(poids · sous-score). */
    private BigDecimal scoreFinal;

    /** true si cette filière a été ajoutée comme "découverte" (engagement faible). */
    private Boolean estDecouverte;

    /** Raison textuelle de l'inclusion (utile pour le mobile et le debug). */
    private String raisonClassement;
}
