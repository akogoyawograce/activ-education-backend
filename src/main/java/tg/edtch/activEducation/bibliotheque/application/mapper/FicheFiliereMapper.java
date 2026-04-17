package tg.edtch.activEducation.bibliotheque.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheFiliereRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheFiliereResponse;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheResponse;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheMetier;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FicheFiliereMapper {

    public FicheFiliere toEntity(FicheFiliereRequest request, Set<FicheMetier> metiers) {
        if (request == null)
            return null;
        return FicheFiliere.builder()
                .trackingId(UUID.randomUUID())
                .titre(request.getTitre())
                .resume(request.getResume())
                .contenu(request.getContenu())
                .videoUrl(request.getVideoUrl())
                .imageUrl(request.getImageUrl())
                .estPublie(request.getEstPublie() != null ? request.getEstPublie() : false)
                .duree(request.getDuree())
                .niveauRequis(request.getNiveauRequis())
                .conditionsAdmission(request.getConditionsAdmission())
                .programme(request.getProgramme())
                .debouchesMetiers(request.getDebouchesMetiers())
                .domaine(request.getDomaine())
                .metiersPrepares(metiers)
                .build();
    }

    public FicheFiliereResponse toResponse(FicheFiliere entity) {
        if (entity == null)
            return null;
        return FicheFiliereResponse.builder()
                .trackingId(entity.getTrackingId())
                .titre(entity.getTitre())
                .resume(entity.getResume())
                .imageUrl(entity.getImageUrl())
                .videoUrl(entity.getVideoUrl())
                .estPublie(entity.getEstPublie())
                .nbConsultations(entity.getNbConsultations())
                .typeFiche("FILIERE")
                .duree(entity.getDuree())
                .niveauRequis(entity.getNiveauRequis())
                .conditionsAdmission(entity.getConditionsAdmission())
                .programme(entity.getProgramme())
                .debouchesMetiers(entity.getDebouchesMetiers())
                .domaine(entity.getDomaine())
                .metiersPrepares(entity.getMetiersPrepares().stream()
                        .map(m -> FicheResponse.builder()
                                .trackingId(m.getTrackingId())
                                .titre(m.getTitre())
                                .resume(m.getResume())
                                .typeFiche("METIER")
                                .build())
                        .collect(Collectors.toSet()))
                .etablissements(entity.getEtablissements().stream()
                        .map(e -> FicheResponse.builder()
                                .trackingId(e.getTrackingId())
                                .titre(e.getTitre())
                                .resume(e.getResume())
                                .typeFiche("ETABLISSEMENT")
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }

    public void updateFromRequest(FicheFiliereRequest request, FicheFiliere entity, Set<FicheMetier> metiers) {
        if (request == null)
            return;
        if (request.getTitre() != null)
            entity.setTitre(request.getTitre());
        if (request.getResume() != null)
            entity.setResume(request.getResume());
        if (request.getContenu() != null)
            entity.setContenu(request.getContenu());
        if (request.getVideoUrl() != null)
            entity.setVideoUrl(request.getVideoUrl());
        if (request.getImageUrl() != null)
            entity.setImageUrl(request.getImageUrl());
        if (request.getEstPublie() != null)
            entity.setEstPublie(request.getEstPublie());
        if (request.getDuree() != null)
            entity.setDuree(request.getDuree());
        if (request.getNiveauRequis() != null)
            entity.setNiveauRequis(request.getNiveauRequis());
        if (request.getConditionsAdmission() != null)
            entity.setConditionsAdmission(request.getConditionsAdmission());
        if (request.getProgramme() != null)
            entity.setProgramme(request.getProgramme());
        if (request.getDebouchesMetiers() != null)
            entity.setDebouchesMetiers(request.getDebouchesMetiers());
        if (request.getDomaine() != null)
            entity.setDomaine(request.getDomaine());
        if (metiers != null)
            entity.setMetiersPrepares(metiers);
    }
}
