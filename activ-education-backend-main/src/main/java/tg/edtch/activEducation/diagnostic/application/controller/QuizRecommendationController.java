package tg.edtch.activEducation.diagnostic.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.diagnostic.application.dto.response.QuestionResponse;
import tg.edtch.activEducation.diagnostic.domain.service.QuizRecommendationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
@Tag(name = "Quiz : Recommandations personnalisées", description = "Questions adaptées au profil de l'élève")
@PreAuthorize("hasAnyRole('ELEVE', 'ADMIN')")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "BearerAuth")
public class QuizRecommendationController {

    private final QuizRecommendationService quizRecommendationService;

    @GetMapping("/recommandations/{eleveTrackingId}/{quizTrackingId}")
    @Operation(summary = "Obtenir des questions recommandées pour un élève",
               description = "Filtre et pondère les questions selon le profil (typeApprenant, notes, métier souhaité, RIASEC)")
    public ResponseEntity<List<QuestionResponse>> recommanderQuestions(
            @Parameter(description = "UUID public de l'élève") @PathVariable UUID eleveTrackingId,
            @Parameter(description = "UUID public du quiz cible") @PathVariable UUID quizTrackingId,
            @RequestParam(defaultValue = "20") int nombre) {
        List<QuestionResponse> questions = quizRecommendationService.recommanderQuestions(
                eleveTrackingId, quizTrackingId, nombre);
        return ResponseEntity.ok(questions);
    }
}
