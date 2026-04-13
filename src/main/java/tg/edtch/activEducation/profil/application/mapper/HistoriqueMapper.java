package tg.edtch.activEducation.profil.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.profil.application.dto.request.HistoriqueRequest;
import tg.edtch.activEducation.profil.application.dto.response.HistoriqueResponse;
import tg.edtch.activEducation.profil.domain.entite.Historique;
import tg.edtch.activEducation.profil.domain.entite.Utilisateur;

import java.util.UUID;

/**
 * Mapper dédié à l'entité {@link Historique}.
 * L'historique est en lecture seule — pas de méthode updateFromRequest.
 */
@Component
public class HistoriqueMapper {

    /**
     * Crée une entité {@link Historique} depuis un {@link HistoriqueRequest}.
     * L'utilisateur concerné est passé explicitement — résolu par le Service via
     * trackingId.
     */
    public Historique toEntity(HistoriqueRequest request, Utilisateur utilisateur) {
        if (request == null)
            return null;
        return Historique.builder()
                .trackingId(UUID.randomUUID())
                .action(request.getAction())
                .details(request.getDetails())
                .utilisateur(utilisateur)
                .build();
    }

    /**
     * Convertit une entité {@link Historique} en {@link HistoriqueResponse}.
     * Les Long ids de l'entrée et de l'utilisateur ne sont jamais exposés.
     */
    public HistoriqueResponse toResponse(Historique historique) {
        if (historique == null)
            return null;
        return HistoriqueResponse.builder()
                .trackingId(historique.getTrackingId())
                .action(historique.getAction())
                .details(historique.getDetails())
                .utilisateurTrackingId(
                        historique.getUtilisateur() != null
                                ? historique.getUtilisateur().getTrackingId()
                                : null)
                .createdAt(historique.getCreatedAt())
                .build();
    }
}
