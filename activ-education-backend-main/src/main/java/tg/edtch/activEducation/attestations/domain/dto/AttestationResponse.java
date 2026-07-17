package tg.edtch.activEducation.attestations.domain.dto;
public record AttestationResponse(String trackingId, String eleveTrackingId, String typeAttestation, String titre, String contenuJson, String codeVerification, String urlPdf, String dateEmission, String createdAt) {}
