package tg.edtch.activEducation.prediction.application.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import tg.edtch.activEducation.prediction.application.dto.OrientationOutcomeRequest;
import tg.edtch.activEducation.prediction.application.dto.OrientationOutcomeResponse;
import tg.edtch.activEducation.prediction.domain.entite.OrientationOutcome;

import java.time.LocalDate;
import java.util.Map;

/**
 * Mapping entity ↔ DTO pour {@link OrientationOutcome}.
 *
 * <p>Le mapper reçoit un {@link ObjectMapper} injecté (Spring Boot en
 * fournit un par défaut) pour convertir les Map (côté request) en JsonNode
 * (côté entity / DB JSONB). C'est la seule logique non-triviale : tout le
 * reste est du field-to-field.</p>
 */
@RequiredArgsConstructor
public class OrientationOutcomeMapper {

    private final ObjectMapper objectMapper;

    public OrientationOutcome toEntity(OrientationOutcomeRequest request,
                                       Long eleveId,
                                       LocalDateSupplier today) {
        return OrientationOutcome.builder()
                .eleveId(eleveId)
                .filiereId(request.getFiliereId())
                .dateChoix(request.getDateChoix() != null ? request.getDateChoix() : today.now())
                .serie(request.getSerie())
                .riasecSnapshot(toJsonNode(request.getRiasecSnapshot()))
                .notesSnapshot(toJsonNode(request.getNotesSnapshot()))
                .scoreRecommandation(request.getScoreRecommandation())
                .statut(OrientationOutcome.StatutOrientation.EN_COURS)
                .build();
    }

    public OrientationOutcomeResponse toResponse(OrientationOutcome entity) {
        return OrientationOutcomeResponse.builder()
                .trackingId(entity.getTrackingId())
                .eleveId(entity.getEleveId())
                .filiereId(entity.getFiliereId())
                .dateChoix(entity.getDateChoix())
                .serie(entity.getSerie())
                .riasecSnapshot(entity.getRiasecSnapshot())
                .notesSnapshot(entity.getNotesSnapshot())
                .scoreRecommandation(entity.getScoreRecommandation())
                .scoreAspiration(entity.getScoreAspiration())
                .scoreRealite(entity.getScoreRealite())
                .scoreEngagement(entity.getScoreEngagement())
                .statut(entity.getStatut() != null ? entity.getStatut().name() : null)
                .satisfaction(entity.getSatisfaction())
                .dateMajStatut(entity.getDateMajStatut())
                .commentaire(entity.getCommentaire())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private JsonNode toJsonNode(Map<String, ?> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return objectMapper.valueToTree(map);
    }

    /** Petit indirection pour permettre de mocker la date dans les tests. */
    @FunctionalInterface
    public interface LocalDateSupplier {
        LocalDate now();
    }
}
