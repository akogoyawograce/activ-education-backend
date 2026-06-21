package tg.edtch.activEducation.diagnostic.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de requête pour la création ou modification d'un Quiz.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200)
    private String titre;

    private String description;

    /** Par défaut true si non fourni. */
    private Boolean estActif;
}
