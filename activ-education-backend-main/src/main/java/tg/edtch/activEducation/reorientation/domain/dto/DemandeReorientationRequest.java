package tg.edtch.activEducation.reorientation.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record DemandeReorientationRequest(@NotBlank String filiereActuelle, @NotBlank String nouvelleFiliere, String metierVise, String raison) {}
