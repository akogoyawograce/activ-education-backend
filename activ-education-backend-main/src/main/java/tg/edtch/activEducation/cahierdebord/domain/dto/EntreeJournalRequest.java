package tg.edtch.activEducation.cahierdebord.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record EntreeJournalRequest(@NotBlank String titre, @NotBlank String contenu, String humeur, String typeEntree, String tags, Boolean estPublic) {}
