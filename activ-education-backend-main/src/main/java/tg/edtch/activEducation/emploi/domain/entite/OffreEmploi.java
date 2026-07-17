package tg.edtch.activEducation.emploi.domain.entite;
import jakarta.persistence.*; import lombok.*; import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity; import java.util.UUID;
@Entity @Table(name = "offres_emploi")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class OffreEmploi extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(nullable = false, length = 200) private String titre;
    @Column(nullable = false, length = 200) private String entreprise;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(length = 20) private String type;
    @Column(length = 200) private String lieu;
    @Column(length = 50) private String region;
    @Column(length = 100) private String secteur;
    @Column(name = "metier_tracking_id", length = 36) private String metierTrackingId;
    @Column(length = 100) private String salaire;
    @Column(name = "date_limite") private java.time.LocalDate dateLimite;
    @Column(name = "est_publie") @Builder.Default private Boolean estPublie = false;
    @Column(name = "est_actif") @Builder.Default private Boolean estActif = true;
    @PrePersist protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
