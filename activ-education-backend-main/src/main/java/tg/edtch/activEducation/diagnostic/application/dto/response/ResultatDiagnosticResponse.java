package tg.edtch.activEducation.diagnostic.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour un résultat de diagnostic.
 * Expose uniquement des {@code trackingId} (UUID) — jamais de clés primaires
 * (Long).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultatDiagnosticResponse {

    private UUID trackingId;
    private LocalDateTime datePassage;
    private Double scoreFinal;
    private String profilDecouvert;
    private String recommandation;

    /** trackingId public de l'élève. */
    private UUID eleveTrackingId;

    /** trackingId public du quiz passé. */
    private UUID quizTrackingId;

    private LocalDateTime createdAt;
}
