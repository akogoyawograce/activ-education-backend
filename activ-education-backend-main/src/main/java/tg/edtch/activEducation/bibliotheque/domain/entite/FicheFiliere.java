package tg.edtch.activEducation.bibliotheque.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Fiche décrivant une filière d'études supérieures ou professionnelles.
 * Table jointe : fiches_filiere (id référence fiches.id)
 *
 * <p>
 * <strong>Relations :</strong>
 * <ul>
 * <li>← {@link FicheSerie} : séries qui donnent accès à cette filière
 * (inverse)</li>
 * <li>→ {@link FicheMetier} : métiers préparés par cette filière
 * (propriétaire)</li>
 * <li>← {@link FicheEtablissement} : établissements proposant cette filière
 * (inverse)</li>
 * </ul>
 */
@Entity
@Table(name = "fiches_filiere")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FicheFiliere extends Fiche {

    /**
     * Durée de la formation : ex. "3 ans", "5 ans (Master)".
     */
    @Column(name = "duree", length = 100)
    private String duree;

    /**
     * Niveau d'études requis pour intégrer cette filière.
     * Ex : "Baccalauréat", "BEPC + formation professionnelle", "Licence".
     */
    @Column(name = "niveau_requis", length = 100)
    private String niveauRequis;

    /**
     * Conditions et procédures d'admission détaillées.
     */
    @Column(name = "conditions_admission", columnDefinition = "TEXT")
    private String conditionsAdmission;

    /**
     * Aperçu du programme d'études (matières, modules, semestres…).
     */
    @Column(name = "programme", columnDefinition = "TEXT")
    private String programme;

    /**
     * Texte libre listant les métiers accessibles après cette filière
     * (complément à la relation {@code metiersPrepares}).
     */
    @Column(name = "debouches_metiers", columnDefinition = "TEXT")
    private String debouchesMetiers;

    /**
     * Domaine d'étude principal : ex. "Sciences", "Lettres", "Technologie",
     * "Commerce".
     */
    @Column(name = "domaine", length = 100)
    private String domaine;

    // ─────────────────────────────────────────────────────────────────────────
    // Relations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Séries du baccalauréat qui donnent accès à cette filière.
     * Relation inverse : la table de jointure est gérée par
     * {@link FicheSerie#filieresAssociees}.
     */
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "serie_filiere", joinColumns = @JoinColumn(name = "filiere_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "serie_id", referencedColumnName = "id"))
    @Builder.Default
    private Set<FicheSerie> seriesAssociees = new HashSet<>();

    /**
     * Métiers auxquels cette filière prépare concrètement les diplômés.
     * Relation propriétaire : la table de jointure {@code filiere_metier} est
     * définie ici.
     */
    @ManyToMany(mappedBy = "filieresPreparantes", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<FicheMetier> metiersPrepares = new HashSet<>();

    /**
     * Établissements qui proposent cette filière.
     * Relation inverse : la table de jointure est gérée par
     * {@link FicheEtablissement#filieresProposees}.
     */
    @ManyToMany(mappedBy = "filieresProposees", fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private Set<FicheEtablissement> etablissements = new HashSet<>();

    @Override
    @Transient
    public String getTypeResultat() {
        return "FILIERE";
    }
}
