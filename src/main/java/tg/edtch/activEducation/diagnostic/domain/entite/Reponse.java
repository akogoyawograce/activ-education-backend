package tg.edtch.activEducation.diagnostic.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

/**
 * Entité représentant une option de réponse pour une question.
 */
@Entity
@Table(name = "reponses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Reponse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "texte_reponse", nullable = false, columnDefinition = "TEXT")
    private String texteReponse;

    /**
     * Catégorie associée à la réponse (ex: 'R', 'I', 'A', 'S', 'E', 'C' pour
     * Riasec,
     * ou le domaine d'intérêt). Permettra de cumuler les points.
     */
    @Column(name = "categorie_point", length = 50)
    private String categoriePoint;

    /**
     * Pondération ou points apportés par cette réponse.
     */
    @Column(name = "points", nullable = false)
    @Builder.Default
    private Integer points = 1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
}
