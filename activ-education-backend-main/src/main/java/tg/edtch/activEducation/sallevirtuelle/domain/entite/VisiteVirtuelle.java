package tg.edtch.activEducation.sallevirtuelle.domain.entite;
import jakarta.persistence.*; import lombok.*; import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity; import java.util.UUID;
@Entity @Table(name = "visites_virtuelles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class VisiteVirtuelle extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(nullable = false, unique = true, length = 50) private String code;
    @Column(nullable = false, length = 200) private String nom;
    @Column(name = "url_video", length = 500) private String urlVideo;
    @Column(name = "embed_code", columnDefinition = "TEXT") private String embedCode;
    @Column(name = "metier_tracking_id", length = 36) private String metierTrackingId;
    @Column(name = "filiere_tracking_id", length = 36) private String filiereTrackingId;
    @Column(name = "etablissement_tracking_id", length = 36) private String etablissementTrackingId;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "duree_secondes") private Integer dureeSecondes;
    @Column(name = "est_publie") @Builder.Default private Boolean estPublie = false;
    @PrePersist protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
