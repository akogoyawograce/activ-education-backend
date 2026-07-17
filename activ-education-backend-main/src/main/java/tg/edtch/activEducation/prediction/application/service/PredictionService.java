package tg.edtch.activEducation.prediction.application.service;

import tg.edtch.activEducation.prediction.domain.dto.PredictionReussiteRequest;
import tg.edtch.activEducation.prediction.domain.dto.PredictionReussiteResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service applicatif pour la gestion des prédictions de réussite d'un élève.
 *
 * <p>Les prédictions sont l'<strong>historique</strong> des évaluations émises
 * par le moteur de la Phase 3 — chaque appel au moteur 3 signaux peut
 * éventuellement être persisté ici pour alimenter l'entraînement supervisé
 * (Phase 5).</p>
 *
 * <p>Sécurité : le {@code eleveTrackingId} est passé en argument et forcé
 * par le service (côté serveur). Le client ne peut pas créer une prédiction
 * au nom d'un autre élève via le body de la requête.</p>
 */
public interface PredictionService {

    /**
     * Liste les prédictions de réussite d'un élève, de la plus récente à
     * la plus ancienne.
     */
    List<PredictionReussiteResponse> listerParEleve(UUID eleveTrackingId);

    /**
     * Crée une nouvelle prédiction de réussite pour un élève donné. Le
     * {@code eleveTrackingId} du path est forcé côté serveur — toute
     * incohérence avec le body déclenche {@code IllegalArgumentException}.
     */
    PredictionReussiteResponse creer(UUID eleveTrackingId, PredictionReussiteRequest request);
}
