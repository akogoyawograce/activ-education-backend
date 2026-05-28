package tg.edtch.activEducation.diagnostic.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entité représentant une question dans un quiz.
 */
@Entity
@Table(name = "questions", indexes = {
        @Index(name = "idx_question_tracking_id", columnList = "tracking_id", unique = true),
        @Index(name = "idx_question_quiz_id", columnList = "quiz_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Question extends BaseEntity {

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

    @Column(name = "texte_question", nullable = false, columnDefinition = "TEXT")
    private String texteQuestion;

    /** Ordre d'affichage de la question dans le quiz. */
    @Column(name = "ordre")
    private Integer ordre;

    /**
     * Niveau scolaire cible (ex: "Collégien", "Bachelier", "Lycéen").
     * Permet d'adapter le quiz au profil de l'utilisateur.
     */
    @Column(name = "niveau_cible", length = 100)
    private String niveauCible;

    /** Domaine de la question (ex: "Sciences", "Lettres", "Technique", "Arts", "Sport"). */
    @Column(name = "domaine", length = 100)
    private String domaine;

    /** Difficulté de la question (1 = facile, 5 = très difficile). */
    @Column(name = "difficulte")
    @Builder.Default
    private Integer difficulte = 1;

    /** Mots-clés CSV pour identification (ex: "mathematiques,algebre,geometrie"). */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    /** Type de question : RIASEC, CONNAISSANCE, INTERET, PERSONNALITE. */
    @Column(name = "type_question", length = 50)
    @Builder.Default
    private String typeQuestion = "RIASEC";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Reponse> reponses = new HashSet<>();

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null)
            this.trackingId = UUID.randomUUID();
        if (this.difficulte == null)
            this.difficulte = 1;
        if (this.typeQuestion == null)
            this.typeQuestion = "RIASEC";
    }
}
