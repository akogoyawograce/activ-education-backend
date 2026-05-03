package tg.edtch.activEducation.bibliotheque.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Fiche décrivant un métier / profession accessible au Togo.
 * Table jointe : fiches_metier (id référence fiches.id)
 *
 * <p>
 * <strong>Relations :</strong>
 * <ul>
 * <li>← {@link FicheFiliere} : filières qui préparent à ce métier
 * (inverse)</li>
 * </ul>
 */
@Entity
@Table(name = "fiches_metier")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FicheMetier extends Fiche {

    /**
     * Secteur d'activité : ex. "Santé", "Informatique", "Agriculture", "BTP".
     */
    @Column(name = "secteur", length = 150)
    private String secteur;

    /**
     * Description des missions principales du métier au quotidien.
     */
    @Column(name = "missions", columnDefinition = "TEXT")
    private String missions;

    /**
     * Compétences clés (techniques et comportementales) requises pour ce métier.
     */
    @Column(name = "competences", columnDefinition = "TEXT")
    private String competences;

    /**
     * Formations permettant d'accéder à ce métier
     * (complément texte libre à la relation {@code filieresPreparantes}).
     */
    @Column(name = "formations_acces", columnDefinition = "TEXT")
    private String formationsAcces;

    /**
     * Contexte et réalité des débouchés professionnels spécifiquement au Togo.
     */
    @Column(name = "debouches_togo", columnDefinition = "TEXT")
    private String debouchesTogo;

    /**
     * Fourchette de rémunération indicative au Togo.
     * Ex : "80 000 – 250 000 FCFA/mois"
     */
    @Column(name = "fourchette_salaire", length = 100)
    private String fourchetteSalaire;

    // ─────────────────────────────────────────────────────────────────────────
    // Relations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Filières d'études qui préparent à ce métier.
     * Relation inverse : la table de jointure est gérée par
     * {@link FicheFiliere#metiersPrepares}.
     */
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "filiere_metier", joinColumns = @JoinColumn(name = "metier_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "filiere_id", referencedColumnName = "id"))
    @Builder.Default
    private Set<FicheFiliere> filieresPreparantes = new HashSet<>();

    @Override
    @Transient
    public String getTypeResultat() {
        return "METIER";
    }
}
