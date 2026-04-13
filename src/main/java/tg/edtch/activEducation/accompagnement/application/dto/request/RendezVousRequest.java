package tg.edtch.activEducation.accompagnement.application.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de requête pour la planification ou la modification d'un rendez-vous.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RendezVousRequest {

    /** trackingId public de l'élève concerné. */
    @NotNull(message = "L'élève est obligatoire")
    private UUID eleveTrackingId;

    /** trackingId public du conseiller concerné. */
    @NotNull(message = "Le conseiller est obligatoire")
    private UUID conseillerTrackingId;

    @NotNull(message = "La date et l'heure du rendez-vous sont obligatoires")
    @Future(message = "La date du rendez-vous doit être dans le futur")
    private LocalDateTime dateHeurePrevue;

    /** Lien de visioconférence (optionnel). */
    private String lienVisio;

    /** Notes ou instructions préalables (optionnel). */
    private String notes;
}
