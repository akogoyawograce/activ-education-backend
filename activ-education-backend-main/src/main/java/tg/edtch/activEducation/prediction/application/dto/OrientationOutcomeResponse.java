package tg.edtch.activEducation.prediction.application.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Représentation externe d'un {@link tg.edtch.activEducation.prediction.domain.entite.OrientationOutcome}.
 *
 * <p>Renvoie les snapshots RIASEC et notes au format JSON brut (JsonNode) :
 * leur structure est libre (cf. champs JSONB en base) et le front s'attend à
 * les recevoir tels quels.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrientationOutcomeResponse {

    private UUID trackingId;

    private Long eleveId;

    private Long filiereId;

    private LocalDate dateChoix;

    private String serie;

    /**
     * Snapshot RIASEC sérialisé tel quel (JsonNode → JSON brut) pour ne pas
     * exposer les métadonnées internes Jackson (containerNode, bigDecimal, ...).
     */
    @JsonRawValue
    private JsonNode riasecSnapshot;

    /** Idem pour le snapshot des notes. */
    @JsonRawValue
    private JsonNode notesSnapshot;

    private BigDecimal scoreRecommandation;

    private BigDecimal scoreAspiration;

    private BigDecimal scoreRealite;

    private BigDecimal scoreEngagement;

    /** EN_COURS / ADMIS / RECALE / ABANDON / REORIENTE. */
    private String statut;

    private Integer satisfaction;

    private LocalDate dateMajStatut;

    private String commentaire;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
