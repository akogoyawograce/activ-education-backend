package tg.edtch.activEducation.bibliotheque.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheSerieRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheResponse;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheSerieResponse;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheSerie;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FicheSerieMapper {

    public FicheSerie toEntity(FicheSerieRequest request, Set<FicheFiliere> filieres) {
        if (request == null)
            return null;
        return FicheSerie.builder()
                .trackingId(UUID.randomUUID())
                .titre(request.getTitre())
                .resume(request.getResume())
                .contenu(request.getContenu())
                .videoUrl(request.getVideoUrl())
                .imageUrl(request.getImageUrl())
                .estPublie(request.getEstPublie() != null ? request.getEstPublie() : false)
                .niveau(request.getNiveau())
                .matieresPrincipales(request.getMatieresPrincipales())
                .debouches(request.getDebouches())
                .coefficients(request.getCoefficients())
                .filieresAssociees(filieres)
                .build();
    }

    public FicheSerieResponse toResponse(FicheSerie entity) {
        if (entity == null)
            return null;
        return FicheSerieResponse.builder()
                .trackingId(entity.getTrackingId())
                .titre(entity.getTitre())
                .resume(entity.getResume())
                .imageUrl(entity.getImageUrl())
                .videoUrl(entity.getVideoUrl())
                .estPublie(entity.getEstPublie())
                .nbConsultations(entity.getNbConsultations())
                .typeFiche("SERIE")
                .niveau(entity.getNiveau())
                .matieresPrincipales(entity.getMatieresPrincipales())
                .debouches(entity.getDebouches())
                .coefficients(entity.getCoefficients())
                .filieresAssociees(entity.getFilieresAssociees().stream()
                        .map(f -> FicheResponse.builder()
                                .trackingId(f.getTrackingId())
                                .titre(f.getTitre())
                                .resume(f.getResume())
                                .typeFiche("FILIERE")
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }

    public void updateFromRequest(FicheSerieRequest request, FicheSerie entity, Set<FicheFiliere> filieres) {
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
        if (request.getNiveau() != null)
            entity.setNiveau(request.getNiveau());
        if (request.getMatieresPrincipales() != null)
            entity.setMatieresPrincipales(request.getMatieresPrincipales());
        if (request.getDebouches() != null)
            entity.setDebouches(request.getDebouches());
        if (request.getCoefficients() != null)
            entity.setCoefficients(request.getCoefficients());
        if (filieres != null)
            entity.setFilieresAssociees(filieres);
    }
}
