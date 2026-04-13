package tg.edtch.activEducation.diagnostic.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.diagnostic.application.dto.request.ResultatDiagnosticRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.ResultatDiagnosticResponse;
import tg.edtch.activEducation.diagnostic.domain.entite.Quiz;
import tg.edtch.activEducation.diagnostic.domain.entite.ResultatDiagnostic;
import tg.edtch.activEducation.profil.domain.entite.Eleve;

import java.util.UUID;

/**
 * Mapper dédié à l'entité {@link ResultatDiagnostic}.
 */
@Component
public class ResultatDiagnosticMapper {

    public ResultatDiagnostic toEntity(ResultatDiagnosticRequest request, Eleve eleve, Quiz quiz) {
        if (request == null)
            return null;
        return ResultatDiagnostic.builder()
                .trackingId(UUID.randomUUID())
                .scoreFinal(request.getScoreFinal())
                .profilDecouvert(request.getProfilDecouvert())
                .recommandation(request.getRecommandation())
                .eleve(eleve)
                .quiz(quiz)
                .build();
    }

    public ResultatDiagnosticResponse toResponse(ResultatDiagnostic resultat) {
        if (resultat == null)
            return null;
        return ResultatDiagnosticResponse.builder()
                .trackingId(resultat.getTrackingId())
                .datePassage(resultat.getDatePassage())
                .scoreFinal(resultat.getScoreFinal())
                .profilDecouvert(resultat.getProfilDecouvert())
                .recommandation(resultat.getRecommandation())
                .eleveTrackingId(resultat.getEleve() != null ? resultat.getEleve().getTrackingId() : null)
                .quizTrackingId(resultat.getQuiz() != null ? resultat.getQuiz().getTrackingId() : null)
                .createdAt(resultat.getCreatedAt())
                .build();
    }
}
