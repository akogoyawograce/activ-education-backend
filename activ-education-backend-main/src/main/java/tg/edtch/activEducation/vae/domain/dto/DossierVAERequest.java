package tg.edtch.activEducation.vae.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record DossierVAERequest(@NotBlank String eleveTrackingId, @NotBlank String diplomeVise, String niveauVise, String experiences) {}
