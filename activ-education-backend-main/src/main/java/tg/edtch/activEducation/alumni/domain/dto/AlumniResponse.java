package tg.edtch.activEducation.alumni.domain.dto;
public record AlumniResponse(String trackingId, String ancienEleveTrackingId, String nom, String email, String telephone, String promotion, String filiereSuivie, String metierActuel, String entreprise, String secteur, String bio, Boolean estMentor, String createdAt) {}
