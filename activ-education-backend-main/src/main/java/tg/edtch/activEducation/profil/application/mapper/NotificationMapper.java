package tg.edtch.activEducation.profil.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.profil.application.dto.request.NotificationRequest;
import tg.edtch.activEducation.profil.application.dto.response.NotificationResponse;
import tg.edtch.activEducation.profil.domain.entite.Notification;
import tg.edtch.activEducation.profil.domain.entite.Utilisateur;

import java.util.UUID;

/**
 * Mapper dédié à l'entité {@link Notification}.
 */
@Component
public class NotificationMapper {

    /**
     * Convertit un {@link NotificationRequest} en entité {@link Notification}.
     * Le destinataire est passé explicitement — résolu par le Service via son
     * trackingId.
     */
    public Notification toEntity(NotificationRequest request, Utilisateur destinataire) {
        if (request == null)
            return null;
        return Notification.builder()
                .trackingId(UUID.randomUUID())
                .titre(request.getTitre())
                .message(request.getMessage())
                .lue(false)
                .utilisateur(destinataire)
                .build();
    }

    /**
     * Convertit une entité {@link Notification} en {@link NotificationResponse}.
     * Le Long id du destinataire n'est jamais exposé.
     */
    public NotificationResponse toResponse(Notification notification) {
        if (notification == null)
            return null;
        return NotificationResponse.builder()
                .trackingId(notification.getTrackingId())
                .titre(notification.getTitre())
                .message(notification.getMessage())
                .lue(notification.getLue())
                .utilisateurTrackingId(
                        notification.getUtilisateur() != null
                                ? notification.getUtilisateur().getTrackingId()
                                : null)
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
