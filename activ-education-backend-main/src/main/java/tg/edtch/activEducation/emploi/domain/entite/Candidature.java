package tg.edtch.activEducation.emploi.domain.entite;
import jakarta.persistence.*; import lombok.*; import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity; import java.util.UUID;
@Entity @Table(name = "candidatures")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Candidature extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(name = "offre_tracking_id", nullable = false, length = 36) private String offreTrackingId;
    @Column(name = "eleve_tracking_id", nullable = false, length = 36) private String eleveTrackingId;
    @Column(columnDefinition = "TEXT") private String message;
    @Column(length = 20) @Builder.Default private String statut = "EN_ATTENTE";
    @Column(name = "date_candidature") @Builder.Default private java.time.LocalDateTime dateCandidature = java.time.LocalDateTime.now();
    @PrePersist protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
