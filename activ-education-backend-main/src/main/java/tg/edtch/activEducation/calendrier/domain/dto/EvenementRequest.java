package tg.edtch.activEducation.calendrier.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record EvenementRequest(@NotBlank String titre, String description, @NotBlank String dateDebut, String dateFin, String typeEvenement, String urlOfficielle, String region, Boolean estNational, Boolean estPublie) {}
