package tg.edtch.activEducation.bibliotheque.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheEtablissementRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheEtablissementResponse;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheResponse;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheEtablissement;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;

import java.util.Set;
import java.util.UUID;
import java.util.Locale;
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
                .estPublie(request.getEstPublie() != null ? request.getEstPublie() : false)
                .adresse(request.getAdresse())
                .ville(request.getVille())
                .typeEtablissement(parseTypeEtablissement(
                        request.getTypeEtablissement() != null ? request.getTypeEtablissement() : "UNIVERSITE"))
                .niveau(request.getNiveau())
                .contacts(request.getContacts())
                .siteWeb(request.getSiteWeb())
                .offreFormation(request.getOffreFormation())
                .estPublic(request.getEstPublic() != null ? request.getEstPublic() : true)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
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
                .contenu(entity.getContenu())
                .imageUrls(entity.getImageUrls() != null ? new java.util.HashSet<>(entity.getImageUrls())
                        : new java.util.HashSet<>())
                .videoUrls(entity.getVideoUrls() != null ? new java.util.HashSet<>(entity.getVideoUrls())
                        : new java.util.HashSet<>())
                .documentUrls(entity.getDocumentUrls() != null ? new java.util.HashSet<>(entity.getDocumentUrls())
                        : new java.util.HashSet<>())
                .estPublie(entity.getEstPublie())
                .nbConsultations(entity.getNbConsultations())
                .typeFiche("ETABLISSEMENT")
                .adresse(entity.getAdresse())
                .ville(entity.getVille())

                .typeEtablissement(entity.getTypeEtablissement().name())
                .niveau(entity.getNiveau())
                .contacts(entity.getContacts())
                .siteWeb(entity.getSiteWeb())
                .offreFormation(entity.getOffreFormation())
                .estPublic(entity.getEstPublic())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
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
        if (request.getEstPublie() != null)
            entity.setEstPublie(request.getEstPublie());
        if (request.getAdresse() != null)
            entity.setAdresse(request.getAdresse());
        if (request.getVille() != null)
            entity.setVille(request.getVille());

        if (request.getTypeEtablissement() != null)
            entity.setTypeEtablissement(parseTypeEtablissement(request.getTypeEtablissement()));
        if (request.getNiveau() != null)
            entity.setNiveau(request.getNiveau());
        if (request.getContacts() != null)
            entity.setContacts(request.getContacts());
        if (request.getSiteWeb() != null)
            entity.setSiteWeb(request.getSiteWeb());
        if (request.getOffreFormation() != null)
            entity.setOffreFormation(request.getOffreFormation());
        if (request.getEstPublic() != null)
            entity.setEstPublic(request.getEstPublic());
        if (request.getLatitude() != null)
            entity.setLatitude(request.getLatitude());
        if (request.getLongitude() != null)
            entity.setLongitude(request.getLongitude());
        if (filieres != null)
            entity.setFilieresProposees(filieres);
    }

    private FicheEtablissement.TypeEtablissement parseTypeEtablissement(String rawType) {
        String normalized = rawType == null ? "" : rawType.trim()
                .replace("-", "_")
                .replace(" ", "_")
                .toUpperCase(Locale.ROOT);
        return FicheEtablissement.TypeEtablissement.valueOf(normalized);
    }
}
