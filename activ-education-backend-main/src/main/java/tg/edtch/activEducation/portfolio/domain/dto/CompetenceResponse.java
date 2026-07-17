package tg.edtch.activEducation.portfolio.domain.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompetenceResponse(
    UUID trackingId,
    String titre,
    String description,
    String categorie,
    Integer niveauEstime,
    String source,
    Boolean estVisible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
