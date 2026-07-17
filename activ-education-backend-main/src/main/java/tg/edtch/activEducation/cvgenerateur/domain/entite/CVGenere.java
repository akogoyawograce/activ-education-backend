package tg.edtch.activEducation.cvgenerateur.domain.entite;
import jakarta.persistence.*; import lombok.*; import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity; import java.util.UUID;
@Entity @Table(name = "cvs_generes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class CVGenere extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(name = "eleve_tracking_id", nullable = false, length = 36) private String eleveTrackingId;
    @Column(nullable = false, length = 200) private String titre;
    @Column(columnDefinition = "TEXT") private String contenuJson;
    @Column(name = "template", length = 50) @Builder.Default private String template = "classique";
    @Column(name = "url_pdf", length = 500) private String urlPdf;
    @Column(length = 20) @Builder.Default private String statut = "BROUILLON";
    @PrePersist protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
