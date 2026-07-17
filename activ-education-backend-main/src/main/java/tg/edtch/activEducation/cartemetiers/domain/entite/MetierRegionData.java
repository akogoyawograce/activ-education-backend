package tg.edtch.activEducation.cartemetiers.domain.entite;
import jakarta.persistence.*; import lombok.*; import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity; import java.util.UUID;
@Entity @Table(name = "metiers_region_data")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class MetierRegionData extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(name = "metier_tracking_id", length = 36) private String metierTrackingId;
    @Column(name = "metier_nom", length = 200) private String metierNom;
    @Column(length = 50) private String region;
    @Column(length = 100) private String ville;
    @Column(name = "nb_offres") private Integer nbOffres = 0;
    @Column(name = "salaire_moyen", length = 100) private String salaireMoyen;
    @Column(length = 20) private String tendance;
    @Column(length = 100) private String secteur;
    @PrePersist protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
