package tg.edtch.activEducation.entretien.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record StartEntretienRequest(
    @NotBlank String metierTitre,
    String metierTrackingId,
    @NotBlank String eleveTrackingId
) {}
