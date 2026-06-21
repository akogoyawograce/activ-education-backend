package tg.edtch.activEducation.diagnostic.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.diagnostic.application.dto.request.QuizRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.QuizResponse;

import java.util.UUID;

/**
 * Contrat de service pour la gestion des Quiz de diagnostic.
 * Tous les identifiants exposés sont des {@code UUID trackingId}.
 */
public interface QuizService {

    QuizResponse creerQuiz(QuizRequest request);

    QuizResponse getQuiz(UUID trackingId);

    Page<QuizResponse> listerActifs(Pageable pageable);

    QuizResponse modifierQuiz(UUID trackingId, QuizRequest request);

    /** Désactive un quiz (soft-delete : estActif = false). */
    void desactiverQuiz(UUID trackingId);
}
