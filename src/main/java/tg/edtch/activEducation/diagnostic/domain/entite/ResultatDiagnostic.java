package tg.edtch.activEducation.diagnostic.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.time.LocalDateTime;

/**
 * Entité représentant le résultat final d'un quiz de diagnostic par un élève.
 */
@Entity
@Table(name = "resultats_diagnostic")
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

    @Column(name = "date_passage")
    private LocalDateTime datePassage;

    /**
     * Score global obtenu au diagnostic.
     */
    @Column(name = "score_final")
    private Double scoreFinal;

    /**
     * Texte ou suggestion de recommandation d'orientation.
     */
    @Column(name = "recommandation", columnDefinition = "TEXT")
    private String recommandation;

    /**
     * Ex: "Profil Scientifique", "Profil Littéraire", "Riasec: R-I-A", etc.
     */
    @Column(name = "profil_decouvert", length = 150)
    private String profilDecouvert;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eleve_id", nullable = false)
    private Eleve eleve;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
}
