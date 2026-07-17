package tg.edtch.activEducation.reseau.domain.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentaireResponse(
    UUID trackingId,
    String publicationTrackingId,
    String auteurTrackingId,
    String auteurNom,
    String contenu,
    LocalDateTime createdAt
) {}
