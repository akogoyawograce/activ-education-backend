package tg.edtch.activEducation.accompagnement.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.profil.domain.entite.Conseiller;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Entité représentant un créneau de disponibilité récurrent d'un conseiller.
 */
@Entity
@Table(name = "disponibilites", indexes = {
        @Index(name = "idx_disponibilite_tracking_id", columnList = "tracking_id", unique = true),
        @Index(name = "idx_disponibilite_conseiller_id", columnList = "conseiller_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Disponibilite extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /**
     * Identifiant public — seul identifiant exposé à l'extérieur.
     */
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    /**
     * Jour de la semaine : 1 = Lundi, 2 = Mardi, ..., 7 = Dimanche (norme ISO).
     */
    @Column(name = "jour_semaine", nullable = false)
    private Integer jourSemaine;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conseiller_id", nullable = false)
    private Conseiller conseiller;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null)
            this.trackingId = UUID.randomUUID();
    }
}
