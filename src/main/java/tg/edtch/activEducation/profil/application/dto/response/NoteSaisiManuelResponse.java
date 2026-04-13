package tg.edtch.activEducation.profil.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour une note saisie manuellement.
 * Expose uniquement le {@code trackingId} (UUID) — jamais la clé primaire
 * interne (Long).
 * L'élève est référencé par son trackingId public.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteSaisiManuelResponse {

    /** Identifiant public de la note — seul identifiant exposé à l'extérieur. */
    private UUID trackingId;

    private String matiere;
    private Double note;
    private Integer coefficient;
    private String anneeScolaire;
    private String semestreOuTrimestre;

    /** trackingId public de l'élève propriétaire — jamais son Long id. */
    private UUID eleveTrackingId;

    /** Date de saisie de la note (audit). */
    private LocalDateTime createdAt;
}
