package tg.edtch.activEducation.diagnostic.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant le résultat final d'un quiz de diagnostic passé par un
 * élève.
 */
@Entity
@Table(name = "resultats_diagnostic", indexes = {
        @Index(name = "idx_resultat_tracking_id", columnList = "tracking_id", unique = true),
        @Index(name = "idx_resultat_eleve_id", columnList = "eleve_id"),
        @Index(name = "idx_resultat_quiz_id", columnList = "quiz_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ResultatDiagnostic extends BaseEntity {

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

    @Column(name = "date_passage")
    private LocalDateTime datePassage;

    /** Score global obtenu au diagnostic. */
    @Column(name = "score_final")
    private Double scoreFinal;

    /** Texte de recommandation d'orientation généré. */
    @Column(name = "recommandation", columnDefinition = "TEXT")
    private String recommandation;

    /**
     * Profil dominant détecté (ex: "Profil Scientifique", "Riasec: R-I-A").
     */
    @Column(name = "profil_decouvert", length = 150)
    private String profilDecouvert;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eleve_id", nullable = false)
    private Eleve eleve;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null)
            this.trackingId = UUID.randomUUID();
        if (this.datePassage == null)
            this.datePassage = LocalDateTime.now();
    }
}
