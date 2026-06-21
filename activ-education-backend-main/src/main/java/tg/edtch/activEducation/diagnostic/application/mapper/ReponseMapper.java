package tg.edtch.activEducation.diagnostic.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.diagnostic.application.dto.request.ReponseRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.ReponseResponse;
import tg.edtch.activEducation.diagnostic.domain.entite.Question;
import tg.edtch.activEducation.diagnostic.domain.entite.Reponse;

import java.util.UUID;

/**
 * Mapper dédié à l'entité {@link Reponse}.
 */
@Component
public class ReponseMapper {

    public Reponse toEntity(ReponseRequest request, Question question, Question prochaineQuestion) {
        if (request == null)
            return null;
        return Reponse.builder()
                .trackingId(UUID.randomUUID())
                .texteReponse(request.getTexteReponse())
                .categoriePoint(request.getCategoriePoint())
                .points(request.getPoints() != null ? request.getPoints() : 1)
                .question(question)
                .prochaineQuestion(prochaineQuestion)
                .build();
    }

    public ReponseResponse toResponse(Reponse reponse) {
        if (reponse == null)
            return null;
        return ReponseResponse.builder()
                .trackingId(reponse.getTrackingId())
                .texteReponse(reponse.getTexteReponse())
                .categoriePoint(reponse.getCategoriePoint())
                .points(reponse.getPoints())
                .questionTrackingId(reponse.getQuestion() != null ? reponse.getQuestion().getTrackingId() : null)
                .prochaineQuestionTrackingId(
                        reponse.getProchaineQuestion() != null ? reponse.getProchaineQuestion().getTrackingId() : null)
                .createdAt(reponse.getCreatedAt())
                .build();
    }

    public void updateFromRequest(ReponseRequest request, Reponse reponse, Question prochaineQuestion) {
        if (request.getTexteReponse() != null)
            reponse.setTexteReponse(request.getTexteReponse());
        if (request.getCategoriePoint() != null)
            reponse.setCategoriePoint(request.getCategoriePoint());
        if (request.getPoints() != null)
            reponse.setPoints(request.getPoints());

        reponse.setProchaineQuestion(prochaineQuestion);
    }
}
