package tg.edtch.activEducation.bibliotheque.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Fiche décrivant un établissement d'enseignement au Togo.
 * Table jointe : fiches_etablissement (id référence fiches.id)
 *
 * <p>
 * <strong>Relations :</strong>
 * <ul>
 * <li>→ {@link FicheFiliere} : filières proposées par cet établissement
 * (propriétaire)</li>
 * </ul>
 */
@Entity
@Table(name = "fiches_etablissement", indexes = {
        @Index(name = "idx_fetab_ville", columnList = "ville"),
        @Index(name = "idx_fetab_type", columnList = "type_etablissement")
})
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FicheEtablissement extends Fiche {

    /**
     * Adresse physique de l'établissement.
     */
    @Column(name = "adresse", length = 300)
    private String adresse;

    /**
     * Ville où se situe l'établissement.
     */
    @Column(name = "ville", length = 100)
    private String ville;

    /**
     * Niveau d'études proposé (ex: Bac, Licence, Master, Doctorat, Primaire, Secondaire).
     */
    @Column(name = "niveau", length = 100)
    private String niveau;

    /**
     * Type institutionnel de l'établissement.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_etablissement", nullable = false, length = 50)
    @Builder.Default
    private TypeEtablissement typeEtablissement = TypeEtablissement.UNIVERSITE;

    /**
     * Coordonnées de contact : téléphone, email, boîte postale…
     */
    @Column(name = "contacts", length = 300)
    private String contacts;

    /**
     * URL du site web officiel de l'établissement.
     */
    @Column(name = "site_web", length = 255)
    private String siteWeb;

    /**
     * Description globale de l'offre de formation. Complément textuel libre
     * à la relation {@link #filieresProposees}.
     */
    @Column(name = "offre_formation", columnDefinition = "TEXT")
    private String offreFormation;

    /**
     * Indique si l'établissement est public ({@code true}) ou privé
     * ({@code false}).
     */
    @Column(name = "est_public", nullable = false)
    @Builder.Default
    private Boolean estPublic = true;

    // ─────────────────────────────────────────────────────────────────────────
    // Relations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Filières d'études proposées par cet établissement.
     * Relation propriétaire : la table de jointure {@code etablissement_filiere}
     * est définie ici.
     */
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "etablissement_filiere", joinColumns = @JoinColumn(name = "etablissement_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "filiere_id", referencedColumnName = "id"))
    @ToString.Exclude
    @Builder.Default
    private Set<FicheFiliere> filieresProposees = new HashSet<>();

    @Override
    @Transient
    public String getTypeResultat() {
        return "ETABLISSEMENT";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Enum
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Types d'établissements d'enseignement reconnus au Togo.
     */
    public enum TypeEtablissement {
        UNIVERSITE,
        ECOLE_SUPERIEURE,
        LYCEE,
        COLLEGE,
        CENTRE_FORMATION_PROFESSIONNELLE,
        GRANDE_ECOLE,
        AUTRE
    }
}
