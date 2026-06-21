package tg.edtch.activEducation.bibliotheque.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheFiliereRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheFiliereResponse;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheResponse;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheSerie;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FicheFiliereMapper {

    public FicheFiliere toEntity(FicheFiliereRequest request, Set<FicheSerie> series) {
        if (request == null)
            return null;
        return FicheFiliere.builder()
                .trackingId(UUID.randomUUID())
                .titre(request.getTitre())
                .resume(request.getResume())
                .contenu(request.getContenu())
                .estPublie(request.getEstPublie() != null ? request.getEstPublie() : false)
                .duree(request.getDuree())
                .niveauRequis(request.getNiveauRequis())
                .conditionsAdmission(request.getConditionsAdmission())
                .programme(request.getProgramme())
                .debouchesMetiers(request.getDebouchesMetiers())
                .domaine(request.getDomaine())
                .seriesAssociees(series)
                .build();
    }

    public FicheFiliereResponse toResponse(FicheFiliere entity) {
        if (entity == null)
            return null;
        return FicheFiliereResponse.builder()
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

    public void updateFromRequest(FicheFiliereRequest request, FicheFiliere entity, Set<FicheSerie> series) {
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
        if (series != null)
            entity.setSeriesAssociees(series);
    }
}
