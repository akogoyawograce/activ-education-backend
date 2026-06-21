package tg.edtch.activEducation.profil.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO de requête pour la création ou la modification d'un Parent.
 * Les enfants sont référencés par leur {@code trackingId} (UUID) public —
 * jamais par leur Long id.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentRequest {

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
     * Liste des trackingId (UUID) des élèves-enfants à rattacher.
     * Optionnel à la création — peut être géré séparément via l'endpoint dédié.
     */
    @Builder.Default
    private List<UUID> enfantsTrackingIds = new ArrayList<>();
}
