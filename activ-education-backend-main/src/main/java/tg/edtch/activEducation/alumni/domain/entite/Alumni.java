package tg.edtch.activEducation.alumni.domain.entite;
import jakarta.persistence.*; import lombok.*; import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity; import java.util.UUID;
@Entity @Table(name = "alumnis")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Alumni extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(name = "ancien_eleve_tracking_id", nullable = false, length = 36) private String ancienEleveTrackingId;
    @Column(nullable = false, length = 100) private String nom;
    @Column(length = 100) private String email;
    @Column(length = 20) private String telephone;
    @Column(length = 20) private String promotion;
    @Column(name = "filiere_suivie", length = 200) private String filiereSuivie;
    @Column(name = "metier_actuel", length = 200) private String metierActuel;
    @Column(length = 200) private String entreprise;
    @Column(length = 100) private String secteur;
    @Column(columnDefinition = "TEXT") private String bio;
    @Column(name = "est_mentor") @Builder.Default private Boolean estMentor = false;
    @PrePersist protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
