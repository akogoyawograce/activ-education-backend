package tg.edtch.activEducation.mentorat.domain.dto;
public record MentoratResponse(String trackingId, String mentorTrackingId, String mentoreTrackingId, String dateDebut, String statut, String domaine, String objectifs, Integer nbSeances, String createdAt) {}
