package tg.edtch.activEducation.emploi.domain.dto;
public record OffreEmploiResponse(String trackingId, String titre, String entreprise, String description, String type, String lieu, String region, String secteur, String metierTrackingId, String salaire, String dateLimite, Boolean estPublie, Boolean estActif, String createdAt) {}
