package tg.edtch.activEducation.profil.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
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
     * Niveau scolaire courant : ex. "Terminale C", "Licence 2", "BEPC", etc.
     */
    @Column(name = "niveau", length = 100)
    private String niveau;

    /**
     * Nom de l'établissement de l'élève.
     */
    @Column(name = "etablissement", length = 200)
    private String etablissement;

    /**
     * Filière actuelle : ex. "Scientifique", "Littéraire", "Technique".
     */
    @Column(name = "filiere", length = 150)
    private String filiere;

    /**
     * Année de fin de cycle prévue.
     */
    @Column(name = "annee_obtention_prevue")
    private LocalDate anneeObtentionPrevue;

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
}
