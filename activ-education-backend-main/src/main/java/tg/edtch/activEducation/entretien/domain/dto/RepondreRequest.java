package tg.edtch.activEducation.entretien.domain.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record RepondreRequest(
    @NotBlank String reponse
) {}
