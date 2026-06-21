package tg.edtch.activEducation.diagnostic.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.diagnostic.application.dto.request.QuestionRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.QuestionResponse;
import tg.edtch.activEducation.diagnostic.domain.service.QuestionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Questions", description = "API de gestion des questions d'un quiz. Imbriquées sous /quiz/{UUID}.")
public class QuestionController {

    private final QuestionService questionService;

    // POST /api/v1/quiz/{quizTrackingId}/questions
    @PostMapping("/quiz/{quizTrackingId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ajouter une question à un quiz")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Question ajoutée", content = @Content(schema = @Schema(implementation = QuestionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides ou quiz désactivé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Quiz introuvable", content = @Content)
    })
    public ResponseEntity<QuestionResponse> ajouterQuestion(
            @Parameter(description = "UUID public du quiz") @PathVariable UUID quizTrackingId,
            @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.ajouterQuestion(quizTrackingId, request));
    }

    // GET /api/v1/quiz/{quizTrackingId}/questions
    @GetMapping("/quiz/{quizTrackingId}/questions")
    @Operation(summary = "Lister toutes les questions d'un quiz", description = "Triées par numéro d'ordre.")
    @ApiResponse(responseCode = "200", description = "Questions retournées")
    public ResponseEntity<List<QuestionResponse>> getQuestionsParQuiz(
            @PathVariable UUID quizTrackingId) {
        return ResponseEntity.ok(questionService.getQuestionsParQuiz(quizTrackingId));
    }

    // GET /api/v1/questions/{trackingId}
    @GetMapping("/questions/{trackingId}")
    @Operation(summary = "Récupérer une question par UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Question trouvée", content = @Content(schema = @Schema(implementation = QuestionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Question introuvable", content = @Content)
    })
    public ResponseEntity<QuestionResponse> getQuestion(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(questionService.getQuestion(trackingId));
    }

    // PUT /api/v1/questions/{trackingId}
    @PutMapping("/questions/{trackingId}")
    @Operation(summary = "Modifier une question")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Question mise à jour", content = @Content(schema = @Schema(implementation = QuestionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Question introuvable", content = @Content)
    })
    public ResponseEntity<QuestionResponse> modifierQuestion(
            @PathVariable UUID trackingId, @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionService.modifierQuestion(trackingId, request));
    }

    // DELETE /api/v1/questions/{trackingId}
    @DeleteMapping("/questions/{trackingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une question", description = "Hard-delete — supprime aussi toutes les réponses (cascade).")
    @ApiResponse(responseCode = "204", description = "Question supprimée")
    public ResponseEntity<Void> supprimerQuestion(@PathVariable UUID trackingId) {
        questionService.supprimerQuestion(trackingId);
        return ResponseEntity.noContent().build();
    }
}
