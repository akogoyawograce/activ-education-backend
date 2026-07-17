package tg.edtch.activEducation.riasec.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record PassageRIASECRequest(@NotBlank String eleveTrackingId, @NotBlank String reponses) {}
