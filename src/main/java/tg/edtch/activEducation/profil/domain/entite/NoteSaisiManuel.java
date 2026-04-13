package tg.edtch.activEducation.profil.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.UUID;

/**
 * Entité représentant une note saisie manuellement par un élève.
 * Hérite de {@link BaseEntity} pour l'audit (createdAt, updatedAt, etc.).
 */
@Entity
@Table(name = "notes_saisies_manuellement", indexes = {
        @Index(name = "idx_note_tracking_id", columnList = "tracking_id", unique = true)
})
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

    /**
     * Identifiant public non-séquentiel — seul identifiant exposé à l'extérieur.
     */
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "matiere", nullable = false, length = 150)
    private String matiere;

    @Column(name = "note", nullable = false)
    private Double note;

    @Column(name = "coefficient")
    private Integer coefficient;

    /** Année scolaire concernée, ex. "2023-2024". */
    @Column(name = "annee_scolaire", length = 20)
    private String anneeScolaire;

    /** Semestre ou trimestre concerné, ex. "Trimestre 1". */
    @Column(name = "semestre_ou_trimestre", length = 50)
    private String semestreOuTrimestre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eleve_id", nullable = false)
    private Eleve eleve;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null) {
            this.trackingId = UUID.randomUUID();
        }
    }
}
