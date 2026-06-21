package tg.edtch.activEducation.profil.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.profil.domain.enums.TypeApprenant;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un élève / étudiant.
 * Table jointe : eleves (id référence utilisateurs.id)
 */
@Entity
@Table(name = "eleves")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Eleve extends Utilisateur {

    /**
     * Niveau scolaire courant.
     * ATTENTION : Doit être alimenté par une liste déroulante côté Front pour
     * l'algorithme.
     */
    @Column(name = "niveau", length = 100)
    private String niveau;

    /**
     * Type d'apprenant (Ecolier, Collégien, Lycéen, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_apprenant", length = 50)
    private TypeApprenant typeApprenant;

    /**
     * Nom de l'établissement de l'élève.
     */
    @Column(name = "etablissement", length = 200)
    private String etablissement;

    /**
     * Filière ou Série actuelle (ex: "Série D", "Génie Logiciel").
     */
    @Column(name = "filiere", length = 150)
    private String filiere;

    /**
     * Relation avec les documents (bulletins) de l'élève.
     */
    @OneToMany(mappedBy = "eleve", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Document> documents = new ArrayList<>();

    /**
     * Parent(s) rattaché(s) à cet élève.
     */
    @ManyToMany(mappedBy = "enfants", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Parent> parents = new ArrayList<>();

    /**
     * Matières préférées de l'élève (stockées en CSV dans une colonne TEXT).
     */
    @Column(name = "matieres_preferees", columnDefinition = "TEXT")
    private String matieresPreferees;

    /**
     * Style d'apprentissage préféré (ex: "Visuel", "Auditif", "Kinesthésique").
     */
    @Column(name = "style_apprentissage", length = 100)
    private String styleApprentissage;

    /**
     * Métier souhaité par l'élève (ex: "Médecin", "Ingénieur", "Avocat").
     */
    @Column(name = "metier_souhaite", length = 200)
    private String metierSouhaite;
}
