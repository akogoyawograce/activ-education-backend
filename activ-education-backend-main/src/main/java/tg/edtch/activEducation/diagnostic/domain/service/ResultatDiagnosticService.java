package tg.edtch.activEducation.diagnostic.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.diagnostic.application.dto.request.ResultatDiagnosticRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.ResultatDiagnosticResponse;

import java.util.Optional;
import java.util.UUID;

/**
 * Contrat de service pour les résultats de diagnostic.
 * Un résultat est append-only — pas de modification après enregistrement.
 * Tous les identifiants exposés sont des {@code UUID trackingId}.
 */
public interface ResultatDiagnosticService {

    /** Enregistre le résultat d'un diagnostic passé par un élève. */
    ResultatDiagnosticResponse enregistrerResultat(ResultatDiagnosticRequest request);

    ResultatDiagnosticResponse getResultat(UUID trackingId);

    /** Retourne les résultats paginés d'un élève, triés par date décroissante. */
    Page<ResultatDiagnosticResponse> getResultatsEleve(UUID eleveTrackingId, Pageable pageable);

    /**
     * Retourne le dernier résultat d'un élève pour un quiz donné.
     * Utile pour afficher le dernier profil détecté.
     */
    Optional<ResultatDiagnosticResponse> getDernierResultat(UUID eleveTrackingId, UUID quizTrackingId);

    Optional<ResultatDiagnosticResponse> getDernierResultat(UUID eleveTrackingId);

    /** Supprime un résultat (admin seulement — hard-delete). */
    void supprimerResultat(UUID trackingId);
}
