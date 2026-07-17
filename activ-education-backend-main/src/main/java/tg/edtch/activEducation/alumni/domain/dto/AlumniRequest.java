package tg.edtch.activEducation.alumni.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record AlumniRequest(@NotBlank String ancienEleveTrackingId, @NotBlank String nom, String email, String telephone, String promotion, String filiereSuivie, String metierActuel, String entreprise, String secteur, String bio, Boolean estMentor) {}
