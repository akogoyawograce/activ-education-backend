package tg.edtch.activEducation.reseau.domain.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PublicationResponse(
    UUID trackingId,
    String auteurTrackingId,
    String auteurNom,
    String auteurRole,
    String contenu,
    String typePublication,
    String tags,
    int nombreReactions,
    int nombreCommentaires,
    boolean estAuteur,
    LocalDateTime createdAt
) {}
