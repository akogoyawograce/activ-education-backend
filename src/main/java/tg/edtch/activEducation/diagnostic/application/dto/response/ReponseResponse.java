package tg.edtch.activEducation.diagnostic.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour une option de réponse de quiz.
 * Expose uniquement le {@code trackingId} (UUID) — jamais la clé primaire
 * (Long).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReponseResponse {

    private UUID trackingId;
    private String texteReponse;

    /** Catégorie RIASEC ou domaine associé (peut être null). */
    private String categoriePoint;

    private Integer points;

    /** trackingId public de la question parente — jamais son Long id. */
    private UUID questionTrackingId;

    /** trackingId de la prochaine question (si branchement). */
    private UUID prochaineQuestionTrackingId;

    private LocalDateTime createdAt;
}
