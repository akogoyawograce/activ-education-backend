package tg.edtch.activEducation.profil.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.profil.application.dto.request.NoteSaisiManuelRequest;
import tg.edtch.activEducation.profil.application.dto.response.NoteSaisiManuelResponse;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.entite.NoteSaisiManuel;

import java.util.UUID;

/**
 * Mapper dédié à l'entité {@link NoteSaisiManuel}.
 */
@Component
public class NoteSaisiManuelMapper {

    /**
     * Convertit un {@link NoteSaisiManuelRequest} en entité
     * {@link NoteSaisiManuel}.
     * L'élève propriétaire est passé explicitement — résolu par le Service via son
     * trackingId.
     * Un nouveau trackingId est généré automatiquement.
     */
    public NoteSaisiManuel toEntity(NoteSaisiManuelRequest request, Eleve eleve) {
        if (request == null)
            return null;
        return NoteSaisiManuel.builder()
                .trackingId(UUID.randomUUID())
                .matiere(request.getMatiere())
                .note(request.getNote())
                .coefficient(request.getCoefficient())
                .anneeScolaire(request.getAnneeScolaire())
                .semestreOuTrimestre(request.getSemestreOuTrimestre())
                .eleve(eleve)
                .build();
    }

    /**
     * Convertit une entité {@link NoteSaisiManuel} en
     * {@link NoteSaisiManuelResponse}.
     * Le Long id de la note et de l'élève ne sont jamais exposés.
     */
    public NoteSaisiManuelResponse toResponse(NoteSaisiManuel note) {
        if (note == null)
            return null;
        return NoteSaisiManuelResponse.builder()
                .trackingId(note.getTrackingId())
                .matiere(note.getMatiere())
                .note(note.getNote())
                .coefficient(note.getCoefficient())
                .anneeScolaire(note.getAnneeScolaire())
                .semestreOuTrimestre(note.getSemestreOuTrimestre())
                .eleveTrackingId(note.getEleve() != null ? note.getEleve().getTrackingId() : null)
                .createdAt(note.getCreatedAt())
                .build();
    }

    /**
     * Met à jour les champs modifiables d'une {@link NoteSaisiManuel} existante.
     * L'élève rattaché et le trackingId sont non modifiables.
     */
    public void updateFromRequest(NoteSaisiManuelRequest request, NoteSaisiManuel note) {
        if (request.getMatiere() != null)
            note.setMatiere(request.getMatiere());
        if (request.getNote() != null)
            note.setNote(request.getNote());
        if (request.getCoefficient() != null)
            note.setCoefficient(request.getCoefficient());
        if (request.getAnneeScolaire() != null)
            note.setAnneeScolaire(request.getAnneeScolaire());
        if (request.getSemestreOuTrimestre() != null)
            note.setSemestreOuTrimestre(request.getSemestreOuTrimestre());
    }
}
