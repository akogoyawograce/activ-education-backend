package tg.edtch.activEducation.prediction.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Création / mise à jour d'un {@link tg.edtch.activEducation.prediction.domain.entite.OrientationOutcome}.
 *
 * <p>Le serveur peut écraser les champs serveur-only
 * ({@code score_aspiration}, {@code score_realite}, {@code score_engagement},
 * {@code score_recommandation}) à partir des snapshots du profil au moment du
 * POST si l'appelant ne les fournit pas.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrientationOutcomeRequest {

    @NotNull
    private Long filiereId;

    /** Date du choix. Si non fournie, on prend la date du jour. */
    private LocalDate dateChoix;

    /** Libellé de la série du bac (si déjà connue au moment du choix). */
    private String serie;

    /** Snapshot RIASEC : map dimension → score (0..1). */
    private Map<String, BigDecimal> riasecSnapshot;

    /** Snapshot notes : {n2, n1, actuelle, tendance}. */
    private Map<String, BigDecimal> notesSnapshot;

    /**
     * Si fourni, persiste ce score de recommandation (cas où la recommandation
     * est calculée en amont, ex. via l'app mobile). Sinon, on laisse le
     * service recalculer.
     */
    private BigDecimal scoreRecommandation;
}
