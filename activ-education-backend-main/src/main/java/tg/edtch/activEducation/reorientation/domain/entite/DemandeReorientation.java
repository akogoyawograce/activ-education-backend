package tg.edtch.activEducation.reorientation.domain.entite;
import jakarta.persistence.*; import lombok.*; import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity; import java.util.UUID;
@Entity @Table(name = "demandes_reorientation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class DemandeReorientation extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(name = "eleve_tracking_id", nullable = false, length = 36) private String eleveTrackingId;
    @Column(name = "filiere_actuelle", length = 200) private String filiereActuelle;
    @Column(name = "nouvelle_filiere", length = 200) private String nouvelleFiliere;
    @Column(name = "metier_vise", length = 200) private String metierVise;
    @Column(columnDefinition = "TEXT") private String raison;
    @Column(length = 20) @Builder.Default private String statut = "EN_ATTENTE";
    @Column(name = "conseiller_tracking_id", length = 36) private String conseillerTrackingId;
    @Column(name = "conseiller_commentaire", columnDefinition = "TEXT") private String conseillerCommentaire;
    @PrePersist protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
