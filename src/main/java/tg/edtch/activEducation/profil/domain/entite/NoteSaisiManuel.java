package tg.edtch.activEducation.profil.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

/**
 * Entité représentant une note saisie manuellement par un élève
 * (remplace la fonctionnalité d'OCR pour l'instant).
 */
@Entity
@Table(name = "notes_saisies_manuellement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NoteSaisiManuel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "matiere", nullable = false, length = 150)
    private String matiere;

    @Column(name = "note", nullable = false)
    private Double note;

    @Column(name = "coefficient")
    private Integer coefficient;

    /**
     * Année scolaire concernée, par exemple "2023-2024".
     */
    @Column(name = "annee_scolaire", length = 20)
    private String anneeScolaire;

    /**
     * Semestre ou trimestre concerné, par exemple "Trimestre 1" ou "Semestre 2".
     */
    @Column(name = "semestre_ou_trimestre", length = 50)
    private String semestreOuTrimestre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eleve_id", nullable = false)
    private Eleve eleve;
}
