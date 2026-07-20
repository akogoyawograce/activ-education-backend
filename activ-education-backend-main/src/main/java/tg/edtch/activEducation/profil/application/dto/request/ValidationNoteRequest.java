package tg.edtch.activEducation.profil.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationNoteRequest {

    @NotBlank(message = "La matière est obligatoire")
    private String matiere;

    @Min(value = 0, message = "La note doit être >= 0")
    @Max(value = 20, message = "La note doit être <= 20")
    private double note;

    @Min(value = 1, message = "Le coefficient doit être >= 1")
    private int coefficient;
}
