package tg.edtch.activEducation.profil.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un parent rattaché à un ou plusieurs élèves.
 * Table jointe : parents (id référence utilisateurs.id)
 */
@Entity
@Table(name = "parents")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Parent extends Utilisateur {

    /**
     * Liste des élèves enfants rattachés à ce parent.
     * Relation ManyToMany : un parent peut avoir plusieurs enfants et
     * un élève peut avoir plusieurs parents (père, mère, tuteur...).
     */
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "parent_enfants", joinColumns = @JoinColumn(name = "parent_id"), inverseJoinColumns = @JoinColumn(name = "eleve_id"))
    @Builder.Default
    private List<Eleve> enfants = new ArrayList<>();
}
