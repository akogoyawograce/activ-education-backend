package tg.edtch.activEducation.diagnostic.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de requête pour la création ou modification d'une ScoreMatrice.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreMatriceRequest {

    @NotBlank(message = "Le titre de la matrice est obligatoire")
    @Size(max = 150)
    private String titreMatrice;

    @DecimalMin(value = "0.0", message = "Le score goûts personnel doit être ≥ 0")
    private Double scoreGoutsPersonnel;

    @DecimalMin(value = "0.0", message = "Le score académique doit être ≥ 0")
    private Double scoreAcademique;

    @DecimalMin(value = "0.0", message = "Le score marché du travail doit être ≥ 0")
    private Double scoreMarcheTravail;

    /** Score total estimé (optionnel — peut être calculé automatiquement). */
    private Double scoreTotalEstime;
}
