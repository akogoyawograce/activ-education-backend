package tg.edtch.activEducation.accompagnement.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.accompagnement.application.dto.request.RendezVousRequest;
import tg.edtch.activEducation.accompagnement.application.dto.response.RendezVousResponse;
import tg.edtch.activEducation.accompagnement.domain.entite.RendezVous;
import tg.edtch.activEducation.profil.domain.entite.Conseiller;
import tg.edtch.activEducation.profil.domain.entite.Eleve;

import java.util.UUID;

/**
 * Mapper dédié à l'entité {@link RendezVous}.
 */
@Component
public class RendezVousMapper {

    /**
     * Crée une entité {@link RendezVous} depuis un {@link RendezVousRequest}.
     * L'élève et le conseiller sont passés explicitement — résolus par le Service.
     */
    public RendezVous toEntity(RendezVousRequest request, Eleve eleve, Conseiller conseiller) {
        if (request == null)
            return null;
        return RendezVous.builder()
                .trackingId(UUID.randomUUID())
                .dateHeurePrevue(request.getDateHeurePrevue())
                .lienVisio(request.getLienVisio())
                .notes(request.getNotes())
                .eleve(eleve)
                .conseiller(conseiller)
                .statut(RendezVous.StatutRendezVous.PLANIFIE)
                .build();
    }

    /**
     * Convertit une entité {@link RendezVous} en {@link RendezVousResponse}.
     * Les Long ids de l'élève et du conseiller ne sont jamais exposés.
     */
    public RendezVousResponse toResponse(RendezVous rdv) {
        if (rdv == null)
            return null;
        return RendezVousResponse.builder()
                .trackingId(rdv.getTrackingId())
                .dateHeurePrevue(rdv.getDateHeurePrevue())
                .statut(rdv.getStatut() != null ? rdv.getStatut().name() : null)
                .lienVisio(rdv.getLienVisio())
                .notes(rdv.getNotes())
                .eleveTrackingId(rdv.getEleve() != null ? rdv.getEleve().getTrackingId() : null)
                .conseillerTrackingId(rdv.getConseiller() != null ? rdv.getConseiller().getTrackingId() : null)
                .createdAt(rdv.getCreatedAt())
                .build();
    }

    /**
     * Met à jour les champs modifiables (date, lien visio, notes). Le statut est
     * géré séparément.
     */
    public void updateFromRequest(RendezVousRequest request, RendezVous rdv) {
        if (request.getDateHeurePrevue() != null)
            rdv.setDateHeurePrevue(request.getDateHeurePrevue());
        if (request.getLienVisio() != null)
            rdv.setLienVisio(request.getLienVisio());
        if (request.getNotes() != null)
            rdv.setNotes(request.getNotes());
    }
}
