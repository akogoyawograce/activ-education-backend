package tg.edtch.activEducation.profil.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour une notification.
 * Expose uniquement le {@code trackingId} (UUID) — jamais la clé primaire
 * interne (Long).
 * Le destinataire est référencé par son trackingId public.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    /** Identifiant public de la notification. */
    private UUID trackingId;

    private String titre;
    private String message;

    /** Indique si la notification a été lue. */
    private Boolean lue;

    /** trackingId public du destinataire — jamais son Long id. */
    private UUID utilisateurTrackingId;

    /** Date d'envoi de la notification. */
    private LocalDateTime createdAt;
}
