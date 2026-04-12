package tg.edtch.activEducation.profil.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

import tg.edtch.activEducation.accompagnement.domain.entite.Disponibilite;

/**
 * Entité représentant un conseiller en orientation.
 * Table jointe : conseillers (id référence utilisateurs.id)
 */
@Entity
@Table(name = "conseillers")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Conseiller extends Utilisateur {

    /**
     * Spécialités du conseiller, stockées en format texte séparé par des virgules.
     * Ex : "orientation, insertion professionnelle, psychologie scolaire"
     * (Pour une approche normalisée, envisager une table dédiée en V3.)
     */
    @Column(name = "specialites", columnDefinition = "TEXT")
    private String specialites;

    /**
     * Charge de travail actuelle : nombre de dossiers/tickets actifs.
     */
    @Column(name = "charge_travail")
    @Builder.Default
    private Integer chargeTravail = 0;

    /**
     * Biographie / description professionnelle du conseiller.
     */
    @Column(name = "biographie", columnDefinition = "TEXT")
    private String biographie;

    /**
     * Diplôme(s) ou qualification(s) du conseiller.
     */
    @Column(name = "qualifications", length = 300)
    private String qualifications;

    /**
     * Années d'expérience professionnelle.
     */
    @Column(name = "annees_experience")
    private Integer anneesExperience;

    /**
     * Disponibilités du conseiller.
     */
    @OneToMany(mappedBy = "conseiller", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Disponibilite> disponibilites = new ArrayList<>();
}
