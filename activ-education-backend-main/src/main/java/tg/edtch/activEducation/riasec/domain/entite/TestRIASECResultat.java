package tg.edtch.activEducation.riasec.domain.entite;
import jakarta.persistence.*; import lombok.*; import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity; import java.util.UUID;
@Entity @Table(name = "tests_riasec_resultats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class TestRIASECResultat extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(name = "eleve_tracking_id", nullable = false, length = 36) private String eleveTrackingId;
    @Column(name = "code_profil", length = 5) private String codeProfil;
    @Column(columnDefinition = "TEXT") private String titres;
    @Column(name = "score_realiste") private Integer scoreRealiste = 0;
    @Column(name = "score_investigateur") private Integer scoreInvestigateur = 0;
    @Column(name = "score_artistique") private Integer scoreArtistique = 0;
    @Column(name = "score_social") private Integer scoreSocial = 0;
    @Column(name = "score_entreprenant") private Integer scoreEntreprenant = 0;
    @Column(name = "score_conventionnel") private Integer scoreConventionnel = 0;
    @Column(name = "suggestions_metiers", columnDefinition = "TEXT") private String suggestionsMetiers;
    @Column(name = "date_passation") @Builder.Default private java.time.LocalDateTime datePassation = java.time.LocalDateTime.now();
    @PrePersist protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
