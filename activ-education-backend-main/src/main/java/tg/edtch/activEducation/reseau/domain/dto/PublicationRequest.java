package tg.edtch.activEducation.reseau.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublicationRequest(
    @NotBlank @Size(min = 1, max = 2000) String contenu,
    String typePublication,
    String tags
) {}
