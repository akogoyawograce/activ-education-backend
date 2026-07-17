package tg.edtch.activEducation.parrainage.domain.entite;
import jakarta.persistence.*; import lombok.*; import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity; import java.util.UUID;
@Entity @Table(name = "parrainages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Parrainage extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(name = "parrain_tracking_id", nullable = false, length = 36) private String parrainTrackingId;
    @Column(name = "filleul_tracking_id", nullable = false, length = 36) private String filleulTrackingId;
    @Column(name = "date_debut") @Builder.Default private java.time.LocalDateTime dateDebut = java.time.LocalDateTime.now();
    @Column(length = 20) @Builder.Default private String statut = "ACTIF";
    @Column(name = "nb_echanges") @Builder.Default private Integer nbEchanges = 0;
    @PrePersist protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
