package tg.edtch.activEducation.diagnostic.domain.service;

import tg.edtch.activEducation.diagnostic.application.dto.request.QuestionRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.QuestionResponse;

import java.util.List;
import java.util.UUID;

/**
 * Contrat de service pour les Questions d'un Quiz.
 * Tous les identifiants exposés sont des {@code UUID trackingId}.
 */
public interface QuestionService {

    /** Ajoute une question à un quiz existant (identifié par son trackingId). */
    QuestionResponse ajouterQuestion(UUID quizTrackingId, QuestionRequest request);

    QuestionResponse getQuestion(UUID trackingId);

    /** Retourne toutes les questions d'un quiz, triées par ordre. */
    List<QuestionResponse> getQuestionsParQuiz(UUID quizTrackingId);

    QuestionResponse modifierQuestion(UUID trackingId, QuestionRequest request);

    /**
     * Hard-delete — une question supprimée entraîne la suppression de ses réponses
     * (cascade).
     */
    void supprimerQuestion(UUID trackingId);
}
