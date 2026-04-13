package tg.edtch.activEducation.accompagnement.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.accompagnement.application.dto.request.DisponibiliteRequest;
import tg.edtch.activEducation.accompagnement.application.dto.response.DisponibiliteResponse;
import tg.edtch.activEducation.accompagnement.domain.entite.Disponibilite;
import tg.edtch.activEducation.profil.domain.entite.Conseiller;

import java.util.UUID;

/**
 * Mapper dédié à l'entité {@link Disponibilite}.
 */
@Component
public class DisponibiliteMapper {

    private static final String[] JOURS = { "", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi",
            "Dimanche" };

    /**
     * Convertit un {@link DisponibiliteRequest} en entité {@link Disponibilite}.
     * Le conseiller est passé explicitement — résolu par le Service via son
     * trackingId.
     */
    public Disponibilite toEntity(DisponibiliteRequest request, Conseiller conseiller) {
        if (request == null)
            return null;
        return Disponibilite.builder()
                .trackingId(UUID.randomUUID())
                .jourSemaine(request.getJourSemaine())
                .heureDebut(request.getHeureDebut())
                .heureFin(request.getHeureFin())
                .conseiller(conseiller)
                .build();
    }

    /**
     * Convertit une entité {@link Disponibilite} en {@link DisponibiliteResponse}.
     * Le Long id du conseiller n'est jamais exposé.
     */
    public DisponibiliteResponse toResponse(Disponibilite dispo) {
        if (dispo == null)
            return null;
        String jourLabel = (dispo.getJourSemaine() != null && dispo.getJourSemaine() >= 1
                && dispo.getJourSemaine() <= 7)
                        ? JOURS[dispo.getJourSemaine()]
                        : "Inconnu";
        return DisponibiliteResponse.builder()
                .trackingId(dispo.getTrackingId())
                .jourSemaine(dispo.getJourSemaine())
                .jourLabel(jourLabel)
                .heureDebut(dispo.getHeureDebut())
                .heureFin(dispo.getHeureFin())
                .conseillerTrackingId(dispo.getConseiller() != null ? dispo.getConseiller().getTrackingId() : null)
                .createdAt(dispo.getCreatedAt())
                .build();
    }

    /** Met à jour les champs d'une disponibilité existante. */
    public void updateFromRequest(DisponibiliteRequest request, Disponibilite dispo) {
        if (request.getJourSemaine() != null)
            dispo.setJourSemaine(request.getJourSemaine());
        if (request.getHeureDebut() != null)
            dispo.setHeureDebut(request.getHeureDebut());
        if (request.getHeureFin() != null)
            dispo.setHeureFin(request.getHeureFin());
    }
}
