package tg.edtch.activEducation.accompagnement.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.profil.domain.entite.Conseiller;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.time.LocalTime;

/**
 * Entité représentant un créneau de disponibilité récurrent d'un conseiller.
 */
@Entity
@Table(name = "disponibilites")
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
     * Jour de la semaine (1 = Lundi, 7 = Dimanche, ou enum).
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
}
