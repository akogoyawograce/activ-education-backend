package tg.edtch.activEducation.diagnostic.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.diagnostic.application.dto.request.QuizGenerationRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.QuizGenerationResponse;
import tg.edtch.activEducation.diagnostic.domain.service.QuizGenerationService;

@RestController
@RequestMapping("/api/v1/quiz/generate")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Quiz IA", description = "Génération de quiz par IA pour filières, métiers, établissements")
public class QuizGenerationController {

    private final QuizGenerationService quizGenerationService;

    @PostMapping
    @Operation(summary = "Générer ou récupérer un quiz pour une entité")
    @ApiResponse(responseCode = "200", description = "Quiz existant retourné",
            content = @Content(schema = @Schema(implementation = QuizGenerationResponse.class)))
    @ApiResponse(responseCode = "201", description = "Nouveau quiz généré",
            content = @Content(schema = @Schema(implementation = QuizGenerationResponse.class)))
    public ResponseEntity<QuizGenerationResponse> genererQuiz(
            @Valid @RequestBody QuizGenerationRequest request) {
        QuizGenerationResponse response = quizGenerationService
                .genererOuRecupererQuiz(request.getType(), request.getEntityId(), request.getNombre());

        boolean isNew = response.getQuizTrackingId() != null && response.getQuestions() != null
                && !response.getQuestions().isEmpty();
        return ResponseEntity.status(isNew ? HttpStatus.CREATED : HttpStatus.OK).body(response);
    }
}
