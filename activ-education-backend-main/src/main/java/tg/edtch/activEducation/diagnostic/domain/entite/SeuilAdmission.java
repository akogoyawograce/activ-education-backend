package tg.edtch.activEducation.diagnostic.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.UUID;

/**
 * Entité définissant les seuils académiques requis pour accéder à une filière.
 */
@Entity
@Table(name = "seuils_admission", indexes = {
        @Index(name = "idx_seuil_tracking_id", columnList = "tracking_id", unique = true),
        @Index(name = "idx_seuil_filiere_id", columnList = "filiere_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SeuilAdmission extends BaseEntity {

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

    /** Matière spécifique concernée (ex: "Mathématiques", "Physique"). */
    @Column(name = "matiere_requise", nullable = false, length = 100)
    private String matiereRequise;

    /** Note ou moyenne minimale exigée. */
    @Column(name = "note_minimum", nullable = false)
    private Double noteMinimum;

    /** Prérequis textuels (ex: "Bac Scientifique", "Mention Bien"). */
    @Column(name = "conditions_textuelles", columnDefinition = "TEXT")
    private String conditionsTextuelles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filiere_id")
    private FicheFiliere filiere;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null)
            this.trackingId = UUID.randomUUID();
    }
}
