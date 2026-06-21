package tg.edtch.activEducation.profil.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Entité représentant un administrateur de la plateforme.
 * Table jointe : administrateurs (id référence utilisateurs.id)
 */
@Entity
@Table(name = "administrateurs")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Administrateur extends Utilisateur {

    /**
     * Niveau d'accès de l'administrateur.
     * Ex : "SUPER_ADMIN", "MODERATEUR", "GESTIONNAIRE_CONSEILLER"
     */
    @Column(name = "niveau_acces", nullable = false, length = 50)
    @Builder.Default
    private String niveauAcces = "MODERATEUR";
}
