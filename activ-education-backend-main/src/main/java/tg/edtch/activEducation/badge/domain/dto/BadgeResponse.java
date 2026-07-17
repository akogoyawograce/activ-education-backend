package tg.edtch.activEducation.badge.domain.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BadgeResponse(
    UUID trackingId,
    String code,
    String nom,
    String description,
    String icone,
    String categorie,
    String conditionExplication,
    boolean estObtenu,
    LocalDateTime dateObtention,
    int totalObtenus
) {}
