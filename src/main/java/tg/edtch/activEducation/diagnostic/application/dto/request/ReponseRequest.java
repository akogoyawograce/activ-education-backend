package tg.edtch.activEducation.diagnostic.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de requête pour la création ou modification d'une Réponse.
 * La question est identifiée via son trackingId dans l'URL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReponseRequest {

    @NotBlank(message = "Le texte de la réponse est obligatoire")
    private String texteReponse;

    /**
     * Code de catégorie RIASEC ou domaine (ex: "R", "I", "A", "S", "E", "C").
     * Optionnel — peut être null pour les questions sans catégorie.
     */
    @Size(max = 50, message = "La catégorie ne peut pas dépasser 50 caractères")
    private String categoriePoint;

    /**
     * Points apportés par cette réponse. Doit être ≥ 0.
     */
    @Min(value = 0, message = "Les points doivent être ≥ 0")
    private Integer points;
}
