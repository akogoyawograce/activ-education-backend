package tg.edtch.activEducation.profil.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de requête pour la saisie ou la modification d'une note manuelle.
 * L'élève propriétaire est identifié via son trackingId dans l'URL (path
 * variable), pas ici.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteSaisiManuelRequest {

    @NotBlank(message = "La matière est obligatoire")
    @Size(max = 150, message = "La matière ne peut pas dépasser 150 caractères")
    private String matiere;

    @NotNull(message = "La note est obligatoire")
    @DecimalMin(value = "0.0", message = "La note ne peut pas être inférieure à 0")
    @DecimalMax(value = "20.0", message = "La note ne peut pas dépasser 20")
    private Double note;

    @Positive(message = "Le coefficient doit être positif")
    private Integer coefficient;

    /** Ex. "2023-2024". */
    @Size(max = 20)
    private String anneeScolaire;

    /** Ex. "Trimestre 1", "Semestre 2". */
    @Size(max = 50)
    private String semestreOuTrimestre;
}
