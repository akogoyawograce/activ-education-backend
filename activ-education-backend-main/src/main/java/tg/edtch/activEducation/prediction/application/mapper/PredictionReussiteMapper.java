package tg.edtch.activEducation.prediction.application.mapper;

import tg.edtch.activEducation.prediction.domain.dto.PredictionReussiteRequest;
import tg.edtch.activEducation.prediction.domain.dto.PredictionReussiteResponse;
import tg.edtch.activEducation.prediction.domain.entite.PredictionReussite;

import java.util.UUID;

/**
 * Mapping entity ↔ DTO pour {@link PredictionReussite}.
 *
 * <p>Logique volontairement minimale : on est sur un snapshot historique
 * (un point dans le temps). Pas de date "calculée" à mocker côté test,
 * la date de prédiction est settée par l'entité via {@code @Builder.Default}
 * (now() à la création).</p>
 *
 * <p>Stateless : peut être instancié librement ou injecté comme
 * {@code @Component} si on veut le mutualiser.</p>
 */
public class PredictionReussiteMapper {

    public PredictionReussite toEntity(String eleveTrackingId, PredictionReussiteRequest request) {
        if (request == null) {
            return null;
        }
        return PredictionReussite.builder()
                .trackingId(UUID.randomUUID())
                .eleveTrackingId(eleveTrackingId)
                .filiereTrackingId(request.filiereTrackingId())
                .filiereNom(request.filiereNom())
                .scorePrediction(request.scorePrediction())
                .facteursCles(request.facteursCles())
                .build();
    }

    public PredictionReussiteResponse toResponse(PredictionReussite entity) {
        if (entity == null) {
            return null;
        }
        return new PredictionReussiteResponse(
                entity.getTrackingId() != null ? entity.getTrackingId().toString() : null,
                entity.getEleveTrackingId(),
                entity.getFiliereTrackingId(),
                entity.getFiliereNom(),
                entity.getScorePrediction(),
                entity.getFacteursCles(),
                entity.getDatePrediction() != null ? entity.getDatePrediction().toString() : null
        );
    }
}
