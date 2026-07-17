package tg.edtch.activEducation.cahierdebord.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cahier_bord_entrees", indexes = {
    @Index(name = "idx_cb_eleve", columnList = "eleve_tracking_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class EntreeJournal extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false) private Long id;
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default private UUID trackingId = UUID.randomUUID();
    @Column(name = "eleve_tracking_id", nullable = false, length = 36) private String eleveTrackingId;
    @Column(length = 200) private String titre;
    @Column(nullable = false, columnDefinition = "TEXT") private String contenu;
    @Column(length = 20) private String humeur;
    @Column(name = "type_entree", length = 20) private String typeEntree;
    @Column(length = 300) private String tags;
    @Column(name = "est_public") @Builder.Default private Boolean estPublic = false;
    @Column(name = "date_entree") @Builder.Default private LocalDateTime dateEntree = LocalDateTime.now();

    @PrePersist
    protected void onPrePersist() { if (this.trackingId == null) this.trackingId = UUID.randomUUID(); }
}
