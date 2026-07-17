package tg.edtch.activEducation.entretien.domain.dto;

import java.util.UUID;

public record EntretienResponse(
    UUID sessionId,
    String metierTitre,
    String question,
    int questionNumero,
    int totalQuestions,
    String statut
) {}
