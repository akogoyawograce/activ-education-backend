package tg.edtch.activEducation.accompagnement.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.profil.domain.entite.Conseiller;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.time.LocalDateTime;

/**
 * Entité représentant un rendez-vous entre un élève et un conseiller.
 */
@Entity
@Table(name = "rendez_vous")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RendezVous extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "date_heure_prevue", nullable = false)
    private LocalDateTime dateHeurePrevue;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 50)
    @Builder.Default
    private StatutRendezVous statut = StatutRendezVous.PLANIFIE;

    @Column(name = "lien_visio", length = 500)
    private String lienVisio;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eleve_id", nullable = false)
    private Eleve eleve;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conseiller_id", nullable = false)
    private Conseiller conseiller;

    public enum StatutRendezVous {
        PLANIFIE,
        TERMINE,
        ANNULE
    }
}
