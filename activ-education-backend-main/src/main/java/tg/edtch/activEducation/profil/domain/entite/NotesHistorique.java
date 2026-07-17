package tg.edtch.activEducation.profil.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.profil.domain.converter.NiveauScolaireConverter;
import tg.edtch.activEducation.profil.domain.enums.NiveauScolaire;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Moyenne annuelle d'un élève sur 3 ans (2 années précédentes + année en cours).
 * Permet le calcul de la trajectoire académique (régression linéaire 3 points)
 * utilisée par le moteur de recommandation Phase 3.
 *
 * <p>Différence avec {@code note_saisi_manuellement} (existant) : l'existant
 * stocke les notes par matière pour la note instantanée ; ce nouveau modèle
 * stocke les moyennes annuelles historiques pour la trajectoire.</p>
 *
 * <p>Voir {@code CHANGELOG_SCHEMA.md} § 3.</p>
 */
@Entity
@Table(name = "notes_historique",
       indexes = {
           @Index(name = "idx_notes_historique_eleve", columnList = "eleve_id"),
           @Index(name = "idx_notes_historique_niveau", columnList = "niveau"),
           @Index(name = "idx_notes_historique_annee",  columnList = "annee_scolaire")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NotesHistorique extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eleve_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_notes_historique_eleve"))
    private Eleve eleve;

    /** Année scolaire au format "2024-2025". */
    @Column(name = "annee_scolaire", nullable = false, length = 9)
    private String anneeScolaire;

    /** Libellé de la classe : ex. "Terminale C", "Licence 2". */
    @Column(name = "classe", nullable = false, length = 50)
    private String classe;

    /** Niveau normalisé (enum). */
    @Convert(converter = NiveauScolaireConverter.class)
    @Column(name = "niveau", nullable = false, length = 20)
    private NiveauScolaire niveau;

    /**
     * Matière concernée, ou {@code null} si la ligne représente la moyenne
     * générale annuelle (cas le plus fréquent).
     */
    @Column(name = "matiere", length = 100)
    private String matiere;

    /** Moyenne sur 20. */
    @Column(name = "moyenne", nullable = false, precision = 5, scale = 2)
    private BigDecimal moyenne;

    /**
     * {@code true} si la note est partielle (ex. seul le 1er trimestre est
     * connu). Le moteur de trajectoire applique alors un poids réduit.
     */
    @Column(name = "est_partielle", nullable = false)
    @Builder.Default
    private Boolean estPartielle = false;

    /** {@code true} si cette ligne est la moyenne générale (et non par matière). */
    @Column(name = "est_moyenne_generale", nullable = false)
    @Builder.Default
    private Boolean estMoyenneGenerale = false;

    /** Origine de la donnée : SAISIE_MANUELLE / OCR / IMPORT_CSV. */
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    @Builder.Default
    private SourceDonnee source = SourceDonnee.SAISIE_MANUELLE;

    public enum SourceDonnee { SAISIE_MANUELLE, OCR, IMPORT_CSV }

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null) {
            this.trackingId = UUID.randomUUID();
        }
    }
}
