package tg.edtch.activEducation.diagnostic.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour un Quiz.
 * Expose uniquement le {@code trackingId} (UUID) — jamais la clé primaire
 * (Long).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {

    private UUID trackingId;
    private String titre;
    private String description;
    private Boolean estActif;

    /** Nombre de questions dans ce quiz. */
    private Integer nombreQuestions;

    private LocalDateTime createdAt;
}
