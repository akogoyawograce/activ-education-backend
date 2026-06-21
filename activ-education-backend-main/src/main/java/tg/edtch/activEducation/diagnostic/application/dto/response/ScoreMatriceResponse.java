package tg.edtch.activEducation.diagnostic.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour une ScoreMatrice.
 * Expose uniquement le {@code trackingId} (UUID) — jamais la clé primaire
 * (Long).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreMatriceResponse {

    private UUID trackingId;
    private String titreMatrice;
    private Double scoreGoutsPersonnel;
    private Double scoreAcademique;
    private Double scoreMarcheTravail;

    /**
     * Score total estimé (somme des 3 dimensions ou valeur explicitement stockée).
     */
    private Double scoreTotalEstime;

    private LocalDateTime createdAt;
}
