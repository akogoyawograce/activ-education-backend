package tg.edtch.activEducation.emploi.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record CandidatureRequest(@NotBlank String offreTrackingId, @NotBlank String eleveTrackingId, String message) {}
