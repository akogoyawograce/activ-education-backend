package tg.edtch.activEducation.bibliotheque.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FavoriResponse;
import tg.edtch.activEducation.bibliotheque.domain.entite.Favori;

@Component
public class FavoriMapper {

    public FavoriResponse toResponse(Favori entity) {
        if (entity == null)
            return null;
        return FavoriResponse.builder()
                .trackingId(entity.getTrackingId())
                .utilisateurTrackingId(entity.getUtilisateur().getTrackingId())
                .ficheTrackingId(entity.getFiche().getTrackingId())
                .ficheTitre(entity.getFiche().getTitre())
                .notePersonnelle(entity.getNotePersonnelle())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
