package tg.edtch.activEducation.diagnostic.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.diagnostic.application.dto.request.QuestionRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.QuestionResponse;
import tg.edtch.activEducation.diagnostic.domain.entite.Question;
import tg.edtch.activEducation.diagnostic.domain.entite.Quiz;

import java.util.UUID;

/**
 * Mapper dédié à l'entité {@link Question}.
 */
@Component
public class QuestionMapper {

    public Question toEntity(QuestionRequest request, Quiz quiz) {
        if (request == null)
            return null;
        return Question.builder()
                .trackingId(UUID.randomUUID())
                .texteQuestion(request.getTexteQuestion())
                .ordre(request.getOrdre())
                .niveauCible(request.getNiveauCible())
                .domaine(request.getDomaine())
                .difficulte(request.getDifficulte())
                .tags(request.getTags())
                .typeQuestion(request.getTypeQuestion())
                .quiz(quiz)
                .build();
    }

    public QuestionResponse toResponse(Question question) {
        if (question == null)
            return null;
        return QuestionResponse.builder()
                .trackingId(question.getTrackingId())
                .texteQuestion(question.getTexteQuestion())
                .ordre(question.getOrdre())
                .niveauCible(question.getNiveauCible())
                .domaine(question.getDomaine())
                .difficulte(question.getDifficulte())
                .tags(question.getTags())
                .typeQuestion(question.getTypeQuestion())
                .quizTrackingId(question.getQuiz() != null ? question.getQuiz().getTrackingId() : null)
                .nombreReponses(question.getReponses() != null ? question.getReponses().size() : 0)
                .createdAt(question.getCreatedAt())
                .build();
    }

    public void updateFromRequest(QuestionRequest request, Question question) {
        if (request.getTexteQuestion() != null)
            question.setTexteQuestion(request.getTexteQuestion());
        if (request.getOrdre() != null)
            question.setOrdre(request.getOrdre());
        if (request.getNiveauCible() != null)
            question.setNiveauCible(request.getNiveauCible());
        if (request.getDomaine() != null)
            question.setDomaine(request.getDomaine());
        if (request.getDifficulte() != null)
            question.setDifficulte(request.getDifficulte());
        if (request.getTags() != null)
            question.setTags(request.getTags());
        if (request.getTypeQuestion() != null)
            question.setTypeQuestion(request.getTypeQuestion());
    }
}
