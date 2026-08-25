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
        @Index(name = "idx_fetab_type", columnList = "type_etablissement"),
        @Index(name = "idx_fetab_region", columnList = "region")
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

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

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

    // ── Champs enrichis depuis XLSX national (08/2026) ──────────────────────

    /** Sigle officiel (ex. UL, UK, ESGIS). */
    @Column(name = "sigle", length = 50)
    private String sigle;

    /** Année de création. */
    @Column(name = "annee_creation")
    private Integer anneeCreation;

    /** Région administrative (Maritime, Plateaux, Kara, Savanes, etc.). */
    @Column(name = "region", length = 100)
    private String region;

    /** Préfecture. */
    @Column(name = "prefecture", length = 100)
    private String prefecture;

    /** Commune. */
    @Column(name = "commune", length = 100)
    private String commune;

    /** Quartier. */
    @Column(name = "quartier", length = 200)
    private String quartier;

    /** Statut juridique complet (texte libre). */
    @Column(name = "statut_juridique", columnDefinition = "TEXT")
    private String statutJuridique;

    /** Numéro d'agrément. */
    @Column(name = "numero_agrement", length = 100)
    private String numeroAgrement;

    /** Date d'agrément (texte brut). */
    @Column(name = "date_agrement", length = 50)
    private String dateAgrement;

    /** Autorité de tutelle (ex. Ministère de l'Enseignement Supérieur). */
    @Column(name = "autorite_tutelle", length = 200)
    private String autoriteTutelle;

    /** Présentation institutionnelle (texte libre). */
    @Column(name = "presentation", columnDefinition = "TEXT")
    private String presentation;

    /** Mission de l'établissement. */
    @Column(name = "mission", columnDefinition = "TEXT")
    private String mission;

    /** Vision de l'établissement. */
    @Column(name = "vision", columnDefinition = "TEXT")
    private String vision;

    /** Valeurs de l'établissement. */
    @Column(name = "valeurs", columnDefinition = "TEXT")
    private String valeurs;

    /** Frais d'inscription (texte libre). */
    @Column(name = "frais_inscription", length = 100)
    private String fraisInscription;

    /** Scolarité annuelle (texte libre). */
    @Column(name = "scolarite_annuelle", length = 100)
    private String scolariteAnnuelle;

    /** Devise (FCFA, EUR, etc.). */
    @Column(name = "frais_devise", length = 20)
    private String fraisDevise;

    /** Synthèse des infrastructures (texte libre). */
    @Column(name = "infrastructures_synthese", columnDefinition = "TEXT")
    private String infrastructuresSynthese;

    /** Nombre de bâtiments. */
    @Column(name = "nb_batiments")
    private Integer nbBatiments;

    /** Nombre d'amphithéâtres. */
    @Column(name = "nb_amphitheatres")
    private Integer nbAmphitheatres;

    /** Présence d'une bibliothèque. */
    @Column(name = "bibliotheque")
    private Boolean bibliotheque;

    /** Wi-Fi disponible. */
    @Column(name = "wifi")
    private Boolean wifi;

    /** Internat (cité universitaire). */
    @Column(name = "internat")
    private Boolean internat;

    /** Restaurant universitaire. */
    @Column(name = "restaurant")
    private Boolean restaurant;

    /** Cafétéria. */
    @Column(name = "cafeteria")
    private Boolean cafeteria;

    /** Terrain de sport. */
    @Column(name = "terrain_sport")
    private Boolean terrainSport;

    /** Infirmerie. */
    @Column(name = "infirmerie")
    private Boolean infirmerie;

    /** Parking disponible. */
    @Column(name = "parking")
    private Boolean parking;

    /** Nombre d'enseignants. */
    @Column(name = "nb_enseignants")
    private Integer nbEnseignants;

    /** Nombre d'étudiants. */
    @Column(name = "nb_etudiants")
    private Integer nbEtudiants;

    /** Nombre de diplômés. */
    @Column(name = "nb_diplomes")
    private Integer nbDiplomes;

    /** Taux de réussite (texte brut %). */
    @Column(name = "taux_reussite", length = 50)
    private String tauxReussite;

    /** Taux d'insertion professionnelle. */
    @Column(name = "taux_insertion_pro", length = 50)
    private String tauxInsertionPro;

    /** Universités partenaires (texte libre). */
    @Column(name = "universites_partenaires", columnDefinition = "TEXT")
    private String universitesPartenaires;

    /** Entreprises partenaires (texte libre). */
    @Column(name = "entreprises_partenaires", columnDefinition = "TEXT")
    private String entreprisesPartenaires;

    /** Programmes de mobilité. */
    @Column(name = "programmes_mobilite")
    private Boolean programmesMobilite;

    /** Bourses internes. */
    @Column(name = "bourses_internes", columnDefinition = "TEXT")
    private String boursesInternes;

    /** Bourses gouvernementales. */
    @Column(name = "bourses_gouvernementales", columnDefinition = "TEXT")
    private String boursesGouvernementales;

    /** Bourses internationales. */
    @Column(name = "bourses_internationales", columnDefinition = "TEXT")
    private String boursesInternationales;

    /** URL Google Maps. */
    @Column(name = "lien_google_maps", columnDefinition = "TEXT")
    private String lienGoogleMaps;

    /** Clubs et associations. */
    @Column(name = "clubs", columnDefinition = "TEXT")
    private String clubs;

    /** Sports proposés. */
    @Column(name = "sports", columnDefinition = "TEXT")
    private String sports;

    /** Reconnaissance CAMES (oui/non). */
    @Column(name = "reconnaissance_cames")
    private Boolean reconnaissanceCames;

    /** Note moyenne des avis. */
    @Column(name = "note_moyenne_avis")
    private Double noteMoyenneAvis;

    /** Nombre d'avis. */
    @Column(name = "nb_avis")
    private Integer nbAvis;

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
