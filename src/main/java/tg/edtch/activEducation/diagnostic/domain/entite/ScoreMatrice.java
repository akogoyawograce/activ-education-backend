package tg.edtch.activEducation.diagnostic.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.UUID;

/**
 * Entité contenant les pondérations d'évaluation d'un profil selon le cahier
 * des charges.
 * Exemple : goûts personnels, compétences académiques, marché de l'emploi.
 */
@Entity
@Table(name = "score_matrices", indexes = {
        @Index(name = "idx_score_matrice_tracking_id", columnList = "tracking_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ScoreMatrice extends BaseEntity {

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

    /** Ex: "Pondération Scientifique C", "Pondération Littéraire A". */
    @Column(name = "titre_matrice", nullable = false, length = 150)
    private String titreMatrice;

    /** Score associé aux désirs et goûts personnels de l'élève. */
    @Column(name = "score_gouts_personnel")
    @Builder.Default
    private Double scoreGoutsPersonnel = 0.0;

    /** Score basé sur les notes académiques. */
    @Column(name = "score_academique")
    @Builder.Default
    private Double scoreAcademique = 0.0;

    /** Score lié au marché de l'emploi / viabilité de la filière. */
    @Column(name = "score_marche_travail")
    @Builder.Default
    private Double scoreMarcheTravail = 0.0;

    /** Poids total combiné pour cette matrice (calculé ou stocké). */
    @Column(name = "score_total_estime")
    private Double scoreTotalEstime;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null)
            this.trackingId = UUID.randomUUID();
        if (this.scoreGoutsPersonnel == null)
            this.scoreGoutsPersonnel = 0.0;
        if (this.scoreAcademique == null)
            this.scoreAcademique = 0.0;
        if (this.scoreMarcheTravail == null)
            this.scoreMarcheTravail = 0.0;
    }
}
