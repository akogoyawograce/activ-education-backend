package tg.edtch.activEducation.diagnostic.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.UUID;

/**
 * Entité représentant une option de réponse pour une question de quiz.
 * La {@code categoriePoint} permet d'associer la réponse à un profil RIASEC ou
 * domaine d'intérêt.
 */
@Entity
@Table(name = "reponses", indexes = {
        @Index(name = "idx_reponse_tracking_id", columnList = "tracking_id", unique = true),
        @Index(name = "idx_reponse_question_id", columnList = "question_id")
})
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

    /**
     * Identifiant public — seul identifiant exposé à l'extérieur.
     */
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "texte_reponse", nullable = false, columnDefinition = "TEXT")
    private String texteReponse;

    /**
     * Catégorie RIASEC associée (ex: 'R', 'I', 'A', 'S', 'E', 'C') ou domaine
     * d'intérêt.
     */
    @Column(name = "categorie_point", length = 50)
    private String categoriePoint;

    /**
     * Pondération ou points apportés par cette réponse (défaut = 1).
     */
    @Column(name = "points", nullable = false)
    @Builder.Default
    private Integer points = 1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null)
            this.trackingId = UUID.randomUUID();
        if (this.points == null)
            this.points = 1;
    }
}
