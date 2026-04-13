package tg.edtch.activEducation.accompagnement.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.accompagnement.application.dto.response.MessageResponse;
import tg.edtch.activEducation.accompagnement.domain.entite.Message;
import tg.edtch.activEducation.profil.domain.entite.Utilisateur;

import java.util.UUID;

/**
 * Mapper dédié à l'entité {@link Message}.
 */
@Component
public class MessageMapper {

    /**
     * Crée une entité {@link Message} à partir du contenu et des deux utilisateurs.
     * L'expéditeur et le destinataire sont résolus par le Service via leurs
     * trackingIds.
     */
    public Message toEntity(String contenu, Utilisateur expediteur, Utilisateur destinataire) {
        return Message.builder()
                .trackingId(UUID.randomUUID())
                .contenu(contenu)
                .expediteur(expediteur)
                .destinataire(destinataire)
                .lu(false)
                .build();
    }

    /**
     * Convertit une entité {@link Message} en {@link MessageResponse}.
     * Les Long ids des utilisateurs ne sont jamais exposés.
     */
    public MessageResponse toResponse(Message message) {
        if (message == null)
            return null;
        return MessageResponse.builder()
                .trackingId(message.getTrackingId())
                .contenu(message.getContenu())
                .dateEnvoi(message.getDateEnvoi())
                .lu(message.getLu())
                .expediteurTrackingId(message.getExpediteur() != null ? message.getExpediteur().getTrackingId() : null)
                .destinataireTrackingId(
                        message.getDestinataire() != null ? message.getDestinataire().getTrackingId() : null)
                .createdAt(message.getCreatedAt())
                .build();
    }
}
