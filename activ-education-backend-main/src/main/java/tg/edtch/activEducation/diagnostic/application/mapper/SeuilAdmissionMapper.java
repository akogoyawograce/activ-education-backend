package tg.edtch.activEducation.diagnostic.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.diagnostic.application.dto.request.SeuilAdmissionRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.SeuilAdmissionResponse;
import tg.edtch.activEducation.diagnostic.domain.entite.SeuilAdmission;

import java.util.UUID;

/**
 * Mapper dédié à l'entité {@link SeuilAdmission}.
 */
@Component
public class SeuilAdmissionMapper {

    /**
     * Crée une entité {@link SeuilAdmission}.
     * La filière est optionnelle — passée null si non fournie.
     */
    public SeuilAdmission toEntity(SeuilAdmissionRequest request, FicheFiliere filiere) {
        if (request == null)
            return null;
        return SeuilAdmission.builder()
                .trackingId(UUID.randomUUID())
                .matiereRequise(request.getMatiereRequise())
                .noteMinimum(request.getNoteMinimum())
                .conditionsTextuelles(request.getConditionsTextuelles())
                .filiere(filiere)
                .build();
    }

    /**
     * Convertit une entité {@link SeuilAdmission} en
     * {@link SeuilAdmissionResponse}.
     * Expose le trackingId de la filière (hérité de Fiche) et son titre.
     */
    public SeuilAdmissionResponse toResponse(SeuilAdmission seuil) {
        if (seuil == null)
            return null;
        UUID filiereTrackingId = null;
        String filiereTitre = null;
        if (seuil.getFiliere() != null) {
            filiereTrackingId = seuil.getFiliere().getTrackingId();
            filiereTitre = seuil.getFiliere().getTitre();
        }
        return SeuilAdmissionResponse.builder()
                .trackingId(seuil.getTrackingId())
                .matiereRequise(seuil.getMatiereRequise())
                .noteMinimum(seuil.getNoteMinimum())
                .conditionsTextuelles(seuil.getConditionsTextuelles())
                .filiereTrackingId(filiereTrackingId)
                .filiereTitre(filiereTitre)
                .createdAt(seuil.getCreatedAt())
                .build();
    }

    public void updateFromRequest(SeuilAdmissionRequest request, SeuilAdmission seuil, FicheFiliere filiere) {
        if (request.getMatiereRequise() != null)
            seuil.setMatiereRequise(request.getMatiereRequise());
        if (request.getNoteMinimum() != null)
            seuil.setNoteMinimum(request.getNoteMinimum());
        if (request.getConditionsTextuelles() != null)
            seuil.setConditionsTextuelles(request.getConditionsTextuelles());
        // La filière peut être remplacée ou retirée (null)
        seuil.setFiliere(filiere);
    }
}
