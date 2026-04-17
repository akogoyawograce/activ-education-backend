package tg.edtch.activEducation.diagnostic.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour un SeuilAdmission.
 * Expose uniquement le {@code trackingId} (UUID) — jamais la clé primaire
 * (Long).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeuilAdmissionResponse {

    private UUID trackingId;
    private String matiereRequise;
    private Double noteMinimum;
    private String conditionsTextuelles;

    /** trackingId public de la filière associée (null si non rattaché). */
    private UUID filiereTrackingId;

    /** Titre de la filière (pour lisibilité dans la réponse). */
    private String filiereTitre;

    private LocalDateTime createdAt;
}
