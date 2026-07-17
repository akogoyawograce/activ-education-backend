package tg.edtch.activEducation.vae.domain.entite;
import jakarta.persistence.*; import lombok.*; import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity; import java.util.UUID;
@Entity @Table(name = "dossiers_vae")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class DossierVAE extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(name = "eleve_tracking_id", nullable = false, length = 36) private String eleveTrackingId;
    @Column(name = "diplome_vise", length = 200) private String diplomeVise;
    @Column(name = "niveau_vise", length = 50) private String niveauVise;
    @Column(columnDefinition = "TEXT") private String experiences;
    @Column(length = 20) @Builder.Default private String statut = "BROUILLON";
    @Column(name = "conseiller_tracking_id", length = 36) private String conseillerTrackingId;
    @PrePersist protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
