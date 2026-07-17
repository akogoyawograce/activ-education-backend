package tg.edtch.activEducation.calendrier.domain.dto;
public record EvenementResponse(String trackingId, String titre, String description, String dateDebut, String dateFin, String typeEvenement, String urlOfficielle, String region, Boolean estNational, Boolean estPublie, String createdAt) {}
