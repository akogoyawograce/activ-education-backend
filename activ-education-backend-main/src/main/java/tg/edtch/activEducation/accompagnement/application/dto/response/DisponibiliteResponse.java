package tg.edtch.activEducation.accompagnement.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO de réponse pour une disponibilité de conseiller.
 * Expose uniquement le {@code trackingId} (UUID) — jamais la clé primaire
 * interne (Long).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisponibiliteResponse {

    /** Identifiant public de ce créneau de disponibilité. */
    private UUID trackingId;

    /** Jour ISO : 1 = Lundi, ..., 7 = Dimanche. */
    private Integer jourSemaine;

    /** Libellé lisible du jour : ex. "Lundi". */
    private String jourLabel;

    private LocalTime heureDebut;
    private LocalTime heureFin;

    /** trackingId public du conseiller — jamais son Long id. */
    private UUID conseillerTrackingId;

    /** Date de création (audit). */
    private LocalDateTime createdAt;
}
