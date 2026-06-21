package tg.edtch.activEducation.accompagnement.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour un rendez-vous.
 * Expose uniquement les {@code trackingId} (UUID) — jamais les clés primaires
 * internes (Long).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RendezVousResponse {

    /** Identifiant public du rendez-vous. */
    private UUID trackingId;

    private LocalDateTime dateHeurePrevue;

    /** Statut : PLANIFIE | TERMINE | ANNULE */
    private String statut;

    private String lienVisio;
    private String notes;

    /** trackingId public de l'élève — jamais son Long id. */
    private UUID eleveTrackingId;

    /** trackingId public du conseiller — jamais son Long id. */
    private UUID conseillerTrackingId;

    /** Date de création (audit). */
    private LocalDateTime createdAt;
}
