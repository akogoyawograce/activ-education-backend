package tg.edtch.activEducation.accompagnement.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour un message.
 * Expose uniquement les {@code trackingId} (UUID) — jamais les clés primaires
 * internes (Long).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    /** Identifiant public du message. */
    private UUID trackingId;

    private String contenu;
    private LocalDateTime dateEnvoi;

    /** Indique si le message a été lu par le destinataire. */
    private Boolean lu;

    /** trackingId public de l'expéditeur. */
    private UUID expediteurTrackingId;

    /** trackingId public du destinataire. */
    private UUID destinataireTrackingId;

    /** Date de création (audit). */
    private LocalDateTime createdAt;
}
