package tg.edtch.activEducation.entretien.domain.dto;

import java.util.List;
import java.util.UUID;

public record ResultatEntretienResponse(
    UUID sessionId,
    String metierTitre,
    double scoreFinal,
    int nbQuestions,
    String appreciation,
    List<EchangeDTO> echanges
) {
    public record EchangeDTO(
        int numero,
        String question,
        String reponse,
        String evaluation,
        double score
    ) {}
}
