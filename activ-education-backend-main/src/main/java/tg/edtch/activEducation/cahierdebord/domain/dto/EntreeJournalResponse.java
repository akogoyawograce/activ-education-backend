package tg.edtch.activEducation.cahierdebord.domain.dto;
public record EntreeJournalResponse(String trackingId, String eleveTrackingId, String titre, String contenu, String humeur, String typeEntree, String tags, Boolean estPublic, String dateEntree, String createdAt) {}
