package tg.edtch.activEducation.attestations.domain.entite;
import jakarta.persistence.*; import lombok.*; import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity; import java.util.UUID;
@Entity @Table(name = "attestations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Attestation extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(name = "eleve_tracking_id", nullable = false, length = 36) private String eleveTrackingId;
    @Column(name = "type_attestation", length = 30) private String typeAttestation;
    @Column(nullable = false, length = 200) private String titre;
    @Column(name = "contenu_json", columnDefinition = "TEXT") private String contenuJson;
    @Column(name = "code_verification", unique = true, length = 20) private String codeVerification;
    @Column(name = "url_pdf", length = 500) private String urlPdf;
    @Column(name = "date_emission") @Builder.Default private java.time.LocalDateTime dateEmission = java.time.LocalDateTime.now();
    @PrePersist protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
