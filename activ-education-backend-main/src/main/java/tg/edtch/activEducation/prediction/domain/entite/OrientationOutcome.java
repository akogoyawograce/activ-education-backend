package tg.edtch.activEducation.prediction.domain.entite;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Suivi d'un choix d'orientation d'un élève — source de vérité pour
 * l'entraînement supervisé (Phase 5).
 *
 * <p>Pour un couple (élève, filière), on enregistre le choix initial, des
 * snapshots de son profil au moment du choix (RIASEC + notes), et le résultat
 * réel observé (statut + satisfaction).</p>
 *
 * <p>Le statut évolue au fil du temps : EN_COURS (défaut à la création) →
 * ADMIS / RECALE / ABANDON / REORIENTE.</p>
 *
 * <p>Voir {@code CHANGELOG_SCHEMA.md} § 4.</p>
 */
@Entity
@Table(name = "orientation_outcome",
       indexes = {
           @Index(name = "idx_orientation_outcome_eleve",   columnList = "eleve_id"),
           @Index(name = "idx_orientation_outcome_filiere", columnList = "filiere_id"),
           @Index(name = "idx_orientation_outcome_statut",  columnList = "statut"),
           @Index(name = "idx_orientation_outcome_date",    columnList = "date_choix")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrientationOutcome extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "eleve_id", nullable = false)
    private Long eleveId;

    @Column(name = "filiere_id", nullable = false)
    private Long filiereId;

    @Column(name = "date_choix", nullable = false)
    private LocalDate dateChoix;

    /** Snapshot du profil RIASEC au moment du choix (JSONB). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "riasec_snapshot", columnDefinition = "jsonb")
    private JsonNode riasecSnapshot;

    /** Snapshot des notes (n2, n1, actuelle, tendance) au moment du choix (JSONB). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notes_snapshot", columnDefinition = "jsonb")
    private JsonNode notesSnapshot;

    /** Série du baccalauréat (si déjà obtenue au moment du choix). */
    @Column(name = "serie", length = 10)
    private String serie;

    /** Score combiné issu du moteur de la Phase 3 (0 à 1). */
    @Column(name = "score_recommandation", precision = 5, scale = 3)
    private BigDecimal scoreRecommandation;

    /** Sous-scores (0 à 1) — utiles pour le debugging et l'analyse. */
    @Column(name = "score_aspiration", precision = 5, scale = 3)
    private BigDecimal scoreAspiration;

    @Column(name = "score_realite", precision = 5, scale = 3)
    private BigDecimal scoreRealite;

    @Column(name = "score_engagement", precision = 5, scale = 3)
    private BigDecimal scoreEngagement;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    @Builder.Default
    private StatutOrientation statut = StatutOrientation.EN_COURS;

    /** Satisfaction déclarée par l'élève (1..5), null tant que non renseignée. */
    @Column(name = "satisfaction")
    private Integer satisfaction;

    /** Date de dernière mise à jour du statut (≠ updatedAt qui est l'audit générique). */
    @Column(name = "date_maj_statut")
    private LocalDate dateMajStatut;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    public enum StatutOrientation {
        EN_COURS,
        ADMIS,
        RECALE,
        ABANDON,
        REORIENTE
    }

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null) {
            this.trackingId = UUID.randomUUID();
        }
    }
}
