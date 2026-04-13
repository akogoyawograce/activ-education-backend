package tg.edtch.activEducation.diagnostic.domain.service;

import tg.edtch.activEducation.diagnostic.application.dto.request.ReponseRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.ReponseResponse;

import java.util.List;
import java.util.UUID;

/**
 * Contrat de service pour les options de réponse d'une Question.
 * Tous les identifiants exposés sont des {@code UUID trackingId}.
 */
public interface ReponseService {

    /**
     * Ajoute une option de réponse à une question (identifiée par son trackingId).
     */
    ReponseResponse ajouterReponse(UUID questionTrackingId, ReponseRequest request);

    ReponseResponse getReponse(UUID trackingId);

    /** Retourne toutes les options de réponse d'une question donnée. */
    List<ReponseResponse> getReponsesParQuestion(UUID questionTrackingId);

    ReponseResponse modifierReponse(UUID trackingId, ReponseRequest request);

    /** Hard-delete — la réponse fait partie de la structure d'un quiz. */
    void supprimerReponse(UUID trackingId);
}
