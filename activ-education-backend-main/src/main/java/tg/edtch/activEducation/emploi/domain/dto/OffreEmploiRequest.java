package tg.edtch.activEducation.emploi.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record OffreEmploiRequest(@NotBlank String titre, @NotBlank String entreprise, String description, String type, String lieu, String region, String secteur, String metierTrackingId, String salaire, String dateLimite) {}
