package tg.edtch.activEducation.calendrier.domain.entite;
import jakarta.persistence.*; import lombok.*; import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity; import java.util.UUID;
@Entity @Table(name = "evenements_orientation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class EvenementOrientation extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(nullable = false, length = 200) private String titre;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "date_debut", nullable = false) private java.time.LocalDateTime dateDebut;
    @Column(name = "date_fin") private java.time.LocalDateTime dateFin;
    @Column(name = "type_evenement", length = 30) private String typeEvenement;
    @Column(name = "url_officielle", length = 500) private String urlOfficielle;
    @Column(length = 50) private String region;
    @Column(name = "est_national") @Builder.Default private Boolean estNational = false;
    @Column(name = "est_publie") @Builder.Default private Boolean estPublie = true;
    @PrePersist protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
