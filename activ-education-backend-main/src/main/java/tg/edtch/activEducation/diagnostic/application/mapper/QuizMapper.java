package tg.edtch.activEducation.diagnostic.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.diagnostic.application.dto.request.QuizRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.QuizResponse;
import tg.edtch.activEducation.diagnostic.domain.entite.Quiz;

import java.util.UUID;

/**
 * Mapper dédié à l'entité {@link Quiz}.
 */
@Component
public class QuizMapper {

    public Quiz toEntity(QuizRequest request) {
        if (request == null)
            return null;
        boolean actif = (request.getEstActif() != null) ? request.getEstActif() : true;
        return Quiz.builder()
                .trackingId(UUID.randomUUID())
                .titre(request.getTitre())
                .description(request.getDescription())
                .estActif(actif)
                .build();
    }

    public QuizResponse toResponse(Quiz quiz) {
        if (quiz == null)
            return null;
        return QuizResponse.builder()
                .trackingId(quiz.getTrackingId())
                .titre(quiz.getTitre())
                .description(quiz.getDescription())
                .estActif(quiz.getEstActif())
                .nombreQuestions(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0)
                .createdAt(quiz.getCreatedAt())
                .build();
    }

    public void updateFromRequest(QuizRequest request, Quiz quiz) {
        if (request.getTitre() != null)
            quiz.setTitre(request.getTitre());
        if (request.getDescription() != null)
            quiz.setDescription(request.getDescription());
        if (request.getEstActif() != null)
            quiz.setEstActif(request.getEstActif());
    }
}
