package tg.edtch.activEducation.diagnostic.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entité représentant un quiz d'évaluation (orientation, personnalité,
 * compétences).
 */
@Entity
@Table(name = "quiz", indexes = {
        @Index(name = "idx_quiz_tracking_id", columnList = "tracking_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Quiz extends BaseEntity {

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

    @Column(name = "titre", nullable = false, length = 200)
    private String titre;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "est_actif", nullable = false)
    @Builder.Default
    private Boolean estActif = true;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Question> questions = new HashSet<>();

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null)
            this.trackingId = UUID.randomUUID();
        if (this.estActif == null)
            this.estActif = true;
    }
}
