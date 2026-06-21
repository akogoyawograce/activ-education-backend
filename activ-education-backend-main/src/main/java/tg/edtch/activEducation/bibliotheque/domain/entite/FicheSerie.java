package tg.edtch.activEducation.bibliotheque.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Fiche décrivant une série du secondaire au Togo (ex : Série C, D, A, G, F…).
 * Table jointe : fiches_serie (id référence fiches.id)
 *
 * <p>
 * <strong>Relations :</strong>
 * <ul>
 * <li>→ {@link FicheFiliere} : filières accessibles après cette série
 * (débouchés post-bac)</li>
 * </ul>
 */
@Entity
@Table(name = "fiches_serie")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FicheSerie extends Fiche {

    /**
     * Niveau scolaire concerné : "Lycée", "Collège".
     */
    @Column(name = "niveau", length = 100)
    private String niveau;

    /**
     * Matières principales enseignées dans cette série (liste texte).
     * Ex : "Mathématiques, Physique-Chimie, Sciences de la vie"
     */
    @Column(name = "matieres_principales", columnDefinition = "TEXT")
    private String matieresPrincipales;

    /**
     * Débouchés et orientations générales possibles après cette série (texte
     * libre).
     */
    @Column(name = "debouches", columnDefinition = "TEXT")
    private String debouches;

    /**
     * Coefficient / pondération des matières phares dans cette série.
     * Ex : "Maths : coef 7, Physique : coef 5"
     */
    @Column(name = "coefficients", columnDefinition = "TEXT")
    private String coefficients;

    // ─────────────────────────────────────────────────────────────────────────
    // Relations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Filières d'études accessibles après avoir obtenu le baccalauréat dans cette
     * série.
     * Relation propriétaire : la table de jointure {@code serie_filiere} est
     * définie ici.
     */
    @ManyToMany(mappedBy = "seriesAssociees", fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private Set<FicheFiliere> filieresAssociees = new HashSet<>();

    @Override
    @Transient
    public String getTypeResultat() {
        return "SERIE";
    }
}
