package tg.edtch.activEducation.diagnostic.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.diagnostic.application.dto.request.ScoreMatriceRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.ScoreMatriceResponse;

import java.util.UUID;

/**
 * Contrat de service pour la gestion des matrices de score de diagnostic.
 */
public interface ScoreMatriceService {

    ScoreMatriceResponse creerMatrice(ScoreMatriceRequest request);

    ScoreMatriceResponse getMatrice(UUID trackingId);

    Page<ScoreMatriceResponse> listerMatrices(Pageable pageable);

    ScoreMatriceResponse modifierMatrice(UUID trackingId, ScoreMatriceRequest request);

    void supprimerMatrice(UUID trackingId);
}
