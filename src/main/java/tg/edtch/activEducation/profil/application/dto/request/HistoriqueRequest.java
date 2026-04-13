package tg.edtch.activEducation.profil.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de requête pour l'enregistrement d'une entrée dans l'historique.
 * L'utilisateur est identifié via son trackingId dans l'URL — pas dans ce DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueRequest {

    /**
     * Type d'action : ex. "CONNEXION", "TEST_RIASEC", "UPLOAD_DOCUMENT",
     * "SAISIE_NOTE".
     */
    @NotBlank(message = "L'action est obligatoire")
    @Size(max = 100, message = "L'action ne peut pas dépasser 100 caractères")
    private String action;

    /**
     * Détails optionnels ou métadonnées JSON associées à l'action.
     */
    private String details;
}
