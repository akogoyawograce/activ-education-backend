package tg.edtch.activEducation.mentorat.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record MentoratRequest(@NotBlank String mentorTrackingId, @NotBlank String mentoreTrackingId, String domaine, String objectifs) {}
