package tg.edtch.activEducation.parrainage.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record ParrainageRequest(@NotBlank String parrainTrackingId, @NotBlank String filleulTrackingId) {}
