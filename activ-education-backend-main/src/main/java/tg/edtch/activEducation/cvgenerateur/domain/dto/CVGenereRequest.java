package tg.edtch.activEducation.cvgenerateur.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record CVGenereRequest(@NotBlank String titre, String contenuJson, String template) {}
