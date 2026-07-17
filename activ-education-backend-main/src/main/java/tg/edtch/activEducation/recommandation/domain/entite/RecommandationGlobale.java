package tg.edtch.activEducation.recommandation.domain.entite;
import jakarta.persistence.*; import lombok.*; import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity; import java.util.UUID;
@Entity @Table(name = "recommandations_globales")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class RecommandationGlobale extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(name = "eleve_tracking_id", nullable = false, length = 36) private String eleveTrackingId;
    @Column(name = "metier_tracking_id", length = 36) private String metierTrackingId;
    @Column(name = "metier_nom", length = 200) private String metierNom;
    @Column(name = "score_global") private Double scoreGlobal;
    @Column(columnDefinition = "TEXT") private String sources;
    @Column(name = "date_recommandation") @Builder.Default private java.time.LocalDateTime dateRecommandation = java.time.LocalDateTime.now();
    @PrePersist protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
