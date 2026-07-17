package tg.edtch.activEducation.profil.application.mapper;

import tg.edtch.activEducation.profil.application.dto.request.NotesHistoriqueRequest;
import tg.edtch.activEducation.profil.application.dto.response.NotesHistoriqueResponse;
import tg.edtch.activEducation.profil.domain.entite.NotesHistorique;
import tg.edtch.activEducation.profil.domain.enums.NiveauScolaire;

/**
 * Mapping entity ↔ DTO pour {@link NotesHistorique}.
 *
 * <p>Le champ {@code eleve} (relation {@code @ManyToOne}) n'est PAS mappé
 * ici : il est résolu côté service à partir d'un {@code eleveId}. Le mapper
 * reste agnostique de la persistance.</p>
 */
public final class NotesHistoriqueMapper {

    private NotesHistoriqueMapper() { }

    public static NotesHistorique toEntity(NotesHistoriqueRequest request) {
        NiveauScolaire niveau = request.getNiveau() == null
                ? null
                : NiveauScolaire.parse(request.getNiveau());
        return NotesHistorique.builder()
                .anneeScolaire(request.getAnneeScolaire())
                .classe(request.getClasse())
                .niveau(niveau)
                .matiere(request.getMatiere())
                .moyenne(request.getMoyenne())
                .estPartielle(Boolean.TRUE.equals(request.getEstPartielle()))
                .estMoyenneGenerale(Boolean.TRUE.equals(request.getEstMoyenneGenerale()))
                .source(NotesHistorique.SourceDonnee.SAISIE_MANUELLE)
                .build();
    }

    public static NotesHistoriqueResponse toResponse(NotesHistorique entity) {
        NiveauScolaire niveau = entity.getNiveau();
        return NotesHistoriqueResponse.builder()
                .trackingId(entity.getTrackingId())
                .eleveId(entity.getEleve() != null ? entity.getEleve().getId() : null)
                .anneeScolaire(entity.getAnneeScolaire())
                .classe(entity.getClasse())
                .niveau(niveau != null ? niveau.name() : null)
                .niveauLabel(niveau != null ? niveau.getLabel() : null)
                .matiere(entity.getMatiere())
                .moyenne(entity.getMoyenne())
                .estPartielle(entity.getEstPartielle())
                .estMoyenneGenerale(entity.getEstMoyenneGenerale())
                .source(entity.getSource() != null ? entity.getSource().name() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
