package tg.edtch.activEducation.diagnostic.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de requête pour la création ou modification d'un SeuilAdmission.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeuilAdmissionRequest {

    @NotBlank(message = "La matière requise est obligatoire")
    @Size(max = 100)
    private String matiereRequise;

    @NotNull(message = "La note minimum est obligatoire")
    @DecimalMin(value = "0.0", message = "La note minimum doit être ≥ 0")
    @DecimalMax(value = "20.0", message = "La note minimum doit être ≤ 20")
    private Double noteMinimum;

    /** Conditions textuelles supplémentaires (optionnel). */
    private String conditionsTextuelles;

    /**
     * trackingId public de la filière concernée (optionnel — un seuil peut exister
     * sans filière liée).
     */
    private UUID filiereTrackingId;
}
