package tg.edtch.activEducation.sallevirtuelle.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record VisiteVirtuelleRequest(@NotBlank String code, @NotBlank String nom, String urlVideo, String embedCode, String metierTrackingId, String filiereTrackingId, String etablissementTrackingId, String description, Integer dureeSecondes, Boolean estPublie) {}
