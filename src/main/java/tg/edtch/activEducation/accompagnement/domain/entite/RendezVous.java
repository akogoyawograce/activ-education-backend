package tg.edtch.activEducation.accompagnement.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.profil.domain.entite.Conseiller;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant un rendez-vous entre un élève et un conseiller.
 */
@Entity
@Table(name = "rendez_vous", indexes = {
        @Index(name = "idx_rdv_tracking_id", columnList = "tracking_id", unique = true),
        @Index(name = "idx_rdv_eleve_id", columnList = "eleve_id"),
        @Index(name = "idx_rdv_conseiller_id", columnList = "conseiller_id"),
        @Index(name = "idx_rdv_statut", columnList = "statut")
})
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

    /**
     * Identifiant public — seul identifiant exposé à l'extérieur.
     */
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "date_heure_prevue", nullable = false)
    private LocalDateTime dateHeurePrevue;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 50)
    @Builder.Default
    private StatutRendezVous statut = StatutRendezVous.PLANIFIE;

    /** Lien de visioconférence (optionnel). */
    @Column(name = "lien_visio", length = 500)
    private String lienVisio;

    /** Notes ou compte-rendu du rendez-vous. */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eleve_id", nullable = false)
    private Eleve eleve;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conseiller_id", nullable = false)
    private Conseiller conseiller;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null)
            this.trackingId = UUID.randomUUID();
        if (this.statut == null)
            this.statut = StatutRendezVous.PLANIFIE;
    }

    public enum StatutRendezVous {
        PLANIFIE,
        TERMINE,
        ANNULE
    }
}
