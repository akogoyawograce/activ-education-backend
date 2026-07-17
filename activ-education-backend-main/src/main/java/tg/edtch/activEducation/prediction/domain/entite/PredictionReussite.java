package tg.edtch.activEducation.prediction.domain.entite;
import jakarta.persistence.*; import lombok.*; import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity; import java.util.UUID;
@Entity @Table(name = "predictions_reussite")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class PredictionReussite extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(name = "eleve_tracking_id", nullable = false, length = 36) private String eleveTrackingId;
    @Column(name = "filiere_tracking_id", length = 36) private String filiereTrackingId;
    @Column(name = "filiere_nom", length = 200) private String filiereNom;
    @Column(name = "score_prediction") private Double scorePrediction;
    @Column(name = "facteurs_cles", columnDefinition = "TEXT") private String facteursCles;
    @Column(name = "date_prediction") @Builder.Default private java.time.LocalDateTime datePrediction = java.time.LocalDateTime.now();
    @PrePersist protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
