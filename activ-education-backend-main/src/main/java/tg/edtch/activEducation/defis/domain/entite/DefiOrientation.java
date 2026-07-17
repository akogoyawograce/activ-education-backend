package tg.edtch.activEducation.defis.domain.entite;
import jakarta.persistence.*; import lombok.*; import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity; import java.util.UUID;
@Entity @Table(name = "defis_orientation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class DefiOrientation extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(nullable = false, unique = true, length = 50) private String code;
    @Column(nullable = false, length = 100) private String nom;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(length = 30) private String type;
    @Column(columnDefinition = "TEXT") private String condition;
    @Column(name = "xp_gagnes") private Integer xpGagnes = 50;
    @Column(length = 50) private String icone;
    @Column(length = 50) private String categorie;
    @PrePersist protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
