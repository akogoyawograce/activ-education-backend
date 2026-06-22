package tg.edtch.activEducation.bibliotheque.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.bibliotheque.application.dto.request.EntreeFAQRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.EntreeFAQResponse;
import tg.edtch.activEducation.bibliotheque.domain.entite.EntreeFAQ;

@Component
public class EntreeFAQMapper {

    public EntreeFAQ toEntity(EntreeFAQRequest request) {
        if (request == null)
            return null;
        return EntreeFAQ.builder()
                .question(request.getQuestion())
                .reponse(request.getReponse())
                .categorie(request.getCategorie())
                .estPublie(request.getEstPublie() != null ? request.getEstPublie() : false)
                .build();
    }

    public EntreeFAQResponse toResponse(EntreeFAQ entity) {
        if (entity == null)
            return null;
        return EntreeFAQResponse.builder()
                .trackingId(entity.getTrackingId())
                .question(entity.getQuestion())
                .reponse(entity.getReponse())
                .categorie(entity.getCategorie())
                .estPublie(entity.getEstPublie())
                .nbVues(entity.getNbVues())
                .nbUtile(entity.getNbUtile())
                .nbPasUtile(entity.getNbPasUtile())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateFromRequest(EntreeFAQRequest request, EntreeFAQ entity) {
        if (request == null)
            return;
        if (request.getQuestion() != null)
            entity.setQuestion(request.getQuestion());
        if (request.getReponse() != null)
            entity.setReponse(request.getReponse());
        if (request.getCategorie() != null)
            entity.setCategorie(request.getCategorie());
        if (request.getEstPublie() != null)
            entity.setEstPublie(request.getEstPublie());
    }
}
