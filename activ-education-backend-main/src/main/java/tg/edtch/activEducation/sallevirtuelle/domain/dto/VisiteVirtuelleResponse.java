package tg.edtch.activEducation.sallevirtuelle.domain.dto;
public record VisiteVirtuelleResponse(String trackingId, String code, String nom, String urlVideo, String embedCode, String metierTrackingId, String filiereTrackingId, String etablissementTrackingId, String description, Integer dureeSecondes, Boolean estPublie) {}
