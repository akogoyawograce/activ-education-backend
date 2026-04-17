package tg.edtch.activEducation.bibliotheque.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheEtablissementRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheEtablissementResponse;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheResponse;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheEtablissement;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FicheEtablissementMapper {

    public FicheEtablissement toEntity(FicheEtablissementRequest request, Set<FicheFiliere> filieres) {
        if (request == null)
            return null;
        return FicheEtablissement.builder()
                .trackingId(UUID.randomUUID())
                .titre(request.getTitre())
                .resume(request.getResume())
                .contenu(request.getContenu())
                .videoUrl(request.getVideoUrl())
                .imageUrl(request.getImageUrl())
                .estPublie(request.getEstPublie() != null ? request.getEstPublie() : false)
                .adresse(request.getAdresse())
                .ville(request.getVille())
                .region(request.getRegion())
                .typeEtablissement(FicheEtablissement.TypeEtablissement.valueOf(
                        request.getTypeEtablissement() != null ? request.getTypeEtablissement() : "UNIVERSITE"))
                .contacts(request.getContacts())
                .siteWeb(request.getSiteWeb())
                .offreFormation(request.getOffreFormation())
                .estPublic(request.getEstPublic() != null ? request.getEstPublic() : true)
                .filieresProposees(filieres)
                .build();
    }

    public FicheEtablissementResponse toResponse(FicheEtablissement entity) {
        if (entity == null)
            return null;
        return FicheEtablissementResponse.builder()
                .trackingId(entity.getTrackingId())
                .titre(entity.getTitre())
                .resume(entity.getResume())
                .imageUrl(entity.getImageUrl())
                .videoUrl(entity.getVideoUrl())
                .estPublie(entity.getEstPublie())
                .nbConsultations(entity.getNbConsultations())
                .typeFiche("ETABLISSEMENT")
                .adresse(entity.getAdresse())
                .ville(entity.getVille())
                .region(entity.getRegion())
                .typeEtablissement(entity.getTypeEtablissement().name())
                .contacts(entity.getContacts())
                .siteWeb(entity.getSiteWeb())
                .offreFormation(entity.getOffreFormation())
                .estPublic(entity.getEstPublic())
                .filieresProposees(entity.getFilieresProposees().stream()
                        .map(f -> FicheResponse.builder()
                                .trackingId(f.getTrackingId())
                                .titre(f.getTitre())
                                .resume(f.getResume())
                                .typeFiche("FILIERE")
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }

    public void updateFromRequest(FicheEtablissementRequest request, FicheEtablissement entity,
            Set<FicheFiliere> filieres) {
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
        if (request.getAdresse() != null)
            entity.setAdresse(request.getAdresse());
        if (request.getVille() != null)
            entity.setVille(request.getVille());
        if (request.getRegion() != null)
            entity.setRegion(request.getRegion());
        if (request.getTypeEtablissement() != null)
            entity.setTypeEtablissement(FicheEtablissement.TypeEtablissement.valueOf(request.getTypeEtablissement()));
        if (request.getContacts() != null)
            entity.setContacts(request.getContacts());
        if (request.getSiteWeb() != null)
            entity.setSiteWeb(request.getSiteWeb());
        if (request.getOffreFormation() != null)
            entity.setOffreFormation(request.getOffreFormation());
        if (request.getEstPublic() != null)
            entity.setEstPublic(request.getEstPublic());
        if (filieres != null)
            entity.setFilieresProposees(filieres);
    }
}
