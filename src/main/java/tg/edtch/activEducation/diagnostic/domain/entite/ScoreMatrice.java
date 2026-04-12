package tg.edtch.activEducation.diagnostic.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

/**
 * Entité contenant les pondérations d'évaluation d'un profil
 * selon le cahier des charges (ex: goûts personnels, compétences académiques,
 * marché de l'emploi).
 */
@Entity
@Table(name = "score_matrices")
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
     * Ex: "Pondération Scientifique C", "Pondération Littéraire A"
     */
    @Column(name = "titre_matrice", nullable = false, length = 150)
    private String titreMatrice;

    /**
     * Score associé aux désirs et goûts de l'élève.
     */
    @Column(name = "score_gouts_personnel")
    @Builder.Default
    private Double scoreGoutsPersonnel = 0.0;

    /**
     * Score basé sur les notes académiques.
     */
    @Column(name = "score_academique")
    @Builder.Default
    private Double scoreAcademique = 0.0;

    /**
     * Score lié au marché de l'emploi / viabilité de la filière.
     */
    @Column(name = "score_marche_travail")
    @Builder.Default
    private Double scoreMarcheTravail = 0.0;

    /**
     * Poids total combiné pour cette matrice (peut être calculé ou stocké).
     */
    @Column(name = "score_total_estime")
    private Double scoreTotalEstime;
}
