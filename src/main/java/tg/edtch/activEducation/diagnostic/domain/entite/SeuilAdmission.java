package tg.edtch.activEducation.diagnostic.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.shared.util.BaseEntity;

/**
 * Entité définissant les seuils académiques requis pour accéder à une filière.
 */
@Entity
@Table(name = "seuils_admission")
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
     * Matière spécifique concernée (ex: "Mathématiques", "Physique").
     */
    @Column(name = "matiere_requise", nullable = false, length = 100)
    private String matiereRequise;

    /**
     * Note ou moyenne minimale exigée.
     */
    @Column(name = "note_minimum", nullable = false)
    private Double noteMinimum;

    /**
     * Requis obligatoires (ex: "Bac Scientifique", "Mention Bien").
     */
    @Column(name = "conditions_textuelles", columnDefinition = "TEXT")
    private String conditionsTextuelles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filiere_id")
    private FicheFiliere filiere;
}
