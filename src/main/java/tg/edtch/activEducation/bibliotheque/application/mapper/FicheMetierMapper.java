package tg.edtch.activEducation.bibliotheque.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheMetierRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheMetierResponse;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheResponse;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheMetier;

import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FicheMetierMapper {

    public FicheMetier toEntity(FicheMetierRequest request, Set<FicheFiliere> filieres) {
        if (request == null)
            return null;
        return FicheMetier.builder()
                .trackingId(UUID.randomUUID())
                .titre(request.getTitre())
                .resume(request.getResume())
                .contenu(request.getContenu())
                .estPublie(request.getEstPublie() != null ? request.getEstPublie() : false)
                .secteur(request.getSecteur())
                .missions(request.getMissions())
                .competences(request.getCompetences())
                .formationsAcces(request.getFormationsAcces())
                .debouchesTogo(request.getDebouchesTogo())
                .fourchetteSalaire(request.getFourchetteSalaire())
                .filieresPreparantes(filieres)
                .build();
    }

    public FicheMetierResponse toResponse(FicheMetier entity) {
        if (entity == null)
            return null;
        return FicheMetierResponse.builder()
                .trackingId(entity.getTrackingId())
                .titre(entity.getTitre())
                .resume(entity.getResume())
                .contenu(entity.getContenu())
                .imageUrls(entity.getImageUrls() != null ? new java.util.HashSet<>(entity.getImageUrls())
                        : new java.util.HashSet<>())
                .videoUrls(entity.getVideoUrls() != null ? new java.util.HashSet<>(entity.getVideoUrls())
                        : new java.util.HashSet<>())
                .documentUrls(entity.getDocumentUrls() != null ? new java.util.HashSet<>(entity.getDocumentUrls())
                        : new java.util.HashSet<>())
                .estPublie(entity.getEstPublie())
                .nbConsultations(entity.getNbConsultations())
                .typeFiche("METIER")
                .secteur(entity.getSecteur())
                .missions(entity.getMissions())
                .competences(entity.getCompetences())
                .formationsAcces(entity.getFormationsAcces())
                .debouchesTogo(entity.getDebouchesTogo())
                .fourchetteSalaire(entity.getFourchetteSalaire())
                .filieresPreparantes(entity.getFilieresPreparantes().stream()
                        .map(f -> FicheResponse.builder()
                                .trackingId(f.getTrackingId())
                                .titre(f.getTitre())
                                .resume(f.getResume())
                                .typeFiche("FILIERE")
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }

    public void updateFromRequest(FicheMetierRequest request, FicheMetier entity, Set<FicheFiliere> filieres) {
        if (request == null)
            return;
        if (request.getTitre() != null)
            entity.setTitre(request.getTitre());
        if (request.getResume() != null)
            entity.setResume(request.getResume());
        if (request.getContenu() != null)
            entity.setContenu(request.getContenu());
        if (request.getEstPublie() != null)
            entity.setEstPublie(request.getEstPublie());
        if (request.getSecteur() != null)
            entity.setSecteur(request.getSecteur());
        if (request.getMissions() != null)
            entity.setMissions(request.getMissions());
        if (request.getCompetences() != null)
            entity.setCompetences(request.getCompetences());
        if (request.getFormationsAcces() != null)
            entity.setFormationsAcces(request.getFormationsAcces());
        if (request.getDebouchesTogo() != null)
            entity.setDebouchesTogo(request.getDebouchesTogo());
        if (request.getFourchetteSalaire() != null)
            entity.setFourchetteSalaire(request.getFourchetteSalaire());
        if (filieres != null)
            entity.setFilieresPreparantes(filieres);
    }
}
