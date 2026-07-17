package tg.edtch.activEducation.reseau.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentaireRequest(
    @NotBlank @Size(min = 1, max = 1000) String contenu
) {}
