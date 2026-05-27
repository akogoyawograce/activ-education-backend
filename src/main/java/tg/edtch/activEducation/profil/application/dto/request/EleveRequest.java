package tg.edtch.activEducation.profil.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import tg.edtch.activEducation.profil.domain.enums.TypeApprenant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de requête pour la création ou la modification d'un Élève.
 * Ne contient aucun identifiant interne (Long id) — uniquement les données
 * métier.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EleveRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Le format de l'email est invalide")
    @Size(max = 150, message = "L'email ne peut pas dépasser 150 caractères")
    private String email;

    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String telephone;

    private String motDePasse;

    /** Niveau scolaire : ex. "Terminale C", "Licence 2". */
    @Size(max = 100)
    private String niveauEtude;

    /** Nom de l'établissement actuel. */
    @Size(max = 200)
    private String etablissementActuel;

    /** Filière actuelle : ex. "Scientifique", "Littéraire". */
    @Size(max = 150)
    private String filiere;

    @NotNull(message = "Le type d'apprenant est obligatoire")
    private TypeApprenant typeApprenant;

    private List<String> matieresPreferees;

    private String styleApprentissage;
}
