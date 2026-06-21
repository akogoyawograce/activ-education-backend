package tg.edtch.activEducation.profil.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de requête pour la création ou la modification d'un Conseiller.
 * Ne contient aucun identifiant interne (Long id).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConseillerRequest {

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

    private String motDePasse;

    /**
     * Spécialités : ex. "orientation, psychologie scolaire, insertion
     * professionnelle".
     */
    private String specialites;

    /**
     * Biographie / description professionnelle.
     */
    private String biographie;

    /**
     * Diplôme(s) ou qualification(s) du conseiller.
     */
    @Size(max = 300)
    private String qualifications;

    /**
     * Années d'expérience professionnelle.
     */
    @Min(value = 0, message = "Les années d'expérience ne peuvent pas être négatives")
    private Integer anneesExperience;
}
