package tg.edtch.activEducation.alumni.domain.entite;
import jakarta.persistence.*; import lombok.*; import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity; import java.util.UUID;
@Entity @Table(name = "mentorats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Mentorat extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(name = "mentor_tracking_id", nullable = false, length = 36) private String mentorTrackingId;
    @Column(name = "mentore_tracking_id", nullable = false, length = 36) private String mentoreTrackingId;
    @Column(name = "date_debut") @Builder.Default private java.time.LocalDateTime dateDebut = java.time.LocalDateTime.now();
    @Column(length = 20) @Builder.Default private String statut = "ACTIF";
    @Column(name = "nb_seances") @Builder.Default private Integer nbSeances = 0;
    @PrePersist protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
