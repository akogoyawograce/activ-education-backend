package tg.edtch.activEducation.diagnostic.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour une Question.
 * Expose uniquement le {@code trackingId} (UUID) — jamais la clé primaire
 * (Long).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {

    private UUID trackingId;
    private String texteQuestion;
    private Integer ordre;
    private String niveauCible;

    /** trackingId public du quiz parent. */
    private UUID quizTrackingId;

    /** Nombre d'options de réponse disponibles. */
    private Integer nombreReponses;

    private String domaine;
    private Integer difficulte;
    private String tags;
    private String typeQuestion;

    private LocalDateTime createdAt;
}
