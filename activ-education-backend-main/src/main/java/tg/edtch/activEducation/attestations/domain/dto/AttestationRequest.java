package tg.edtch.activEducation.attestations.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record AttestationRequest(@NotBlank String eleveTrackingId, @NotBlank String titre, String typeAttestation, String contenuJson) {}
