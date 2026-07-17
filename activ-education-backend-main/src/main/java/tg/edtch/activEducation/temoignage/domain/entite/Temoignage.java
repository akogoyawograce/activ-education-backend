package tg.edtch.activEducation.temoignage.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "temoignages", indexes = {
    @Index(name = "idx_temoignage_publie", columnList = "est_publie"),
    @Index(name = "idx_temoignage_metier", columnList = "metier_tracking_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Temoignage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "auteur_nom", nullable = false, length = 100)
    private String auteurNom;

    @Column(name = "auteur_photo_url", length = 500)
    private String auteurPhotoUrl;

    @Column(name = "auteur_titre", length = 200)
    private String auteurTitre;

    @Column(name = "filiere_suivie", length = 200)
    private String filiereSuivie;

    @Column(name = "etablissement", length = 200)
    private String etablissement;

    @Column(name = "annee_parcours", length = 20)
    private String anneeParcours;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "metier_tracking_id", length = 36)
    private String metierTrackingId;

    @Column(name = "metier_nom", length = 200)
    private String metierNom;

    @Column(name = "filiere_tracking_id", length = 36)
    private String filiereTrackingId;

    @Column(name = "est_publie", nullable = false)
    @Builder.Default
    private Boolean estPublie = false;

    @Column(name = "nb_vues")
    @Builder.Default
    private Integer nbVues = 0;

    @Column(name = "est_en_vedette")
    @Builder.Default
    private Boolean estEnVedette = false;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null) this.trackingId = UUID.randomUUID();
    }
}
