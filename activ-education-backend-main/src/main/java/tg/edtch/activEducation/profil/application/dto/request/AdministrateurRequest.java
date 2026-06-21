package tg.edtch.activEducation.profil.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de requête pour la création ou la modification d'un Administrateur.
 * Ne contient aucun identifiant interne (Long id).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdministrateurRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String telephone;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String motDePasse;

    /**
     * Niveau d'accès : "SUPER_ADMIN", "MODERATEUR" ou "GESTIONNAIRE_CONSEILLER".
     * Défaut : "MODERATEUR".
     */
    @Pattern(regexp = "SUPER_ADMIN|MODERATEUR|GESTIONNAIRE_CONSEILLER", message = "niveauAcces doit être l'un de : SUPER_ADMIN, MODERATEUR, GESTIONNAIRE_CONSEILLER")
    private String niveauAcces;
}
