package tg.edtch.activEducation.portfolio.domain.dto;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.*;

public record CompetenceRequest(
    @NotBlank @Size(max = 100) String titre,
    @Size(max = 500) String description,
    @NotBlank @Size(max = 50) String categorie,
    @NotNull @Min(1) @Max(5) Integer niveauEstime,
    @Size(max = 200) String source,
    Boolean estVisible
) {}
