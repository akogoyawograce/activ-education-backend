package tg.edtch.activEducation.defis.domain.entite;
import jakarta.persistence.*; import lombok.*; import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity; import java.util.UUID;
@Entity @Table(name = "defis_releves")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class DefiReleve extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(name = "eleve_tracking_id", nullable = false, length = 36) private String eleveTrackingId;
    @Column(name = "defi_code", nullable = false, length = 50) private String defiCode;
    @Column(name = "date_releve") @Builder.Default private java.time.LocalDateTime dateReleve = java.time.LocalDateTime.now();
    @Column(length = 20) @Builder.Default private String statut = "EN_COURS";
    private Integer progression = 0;
    @PrePersist protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
