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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.diagnostic.application.dto.request.QuizRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.QuizResponse;
import tg.edtch.activEducation.diagnostic.domain.service.QuizService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
@Tag(name = "Quiz", description = "API de gestion des quiz de diagnostic d'orientation.")
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un quiz")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Quiz créé", content = @Content(schema = @Schema(implementation = QuizResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content)
    })
    public ResponseEntity<QuizResponse> creerQuiz(@Valid @RequestBody QuizRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.creerQuiz(request));
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Récupérer un quiz par UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quiz trouvé", content = @Content(schema = @Schema(implementation = QuizResponse.class))),
            @ApiResponse(responseCode = "404", description = "Quiz introuvable", content = @Content)
    })
    public ResponseEntity<QuizResponse> getQuiz(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(quizService.getQuiz(trackingId));
    }

    @GetMapping
    @Operation(summary = "Lister les quiz actifs (paginé)")
    @ApiResponse(responseCode = "200", description = "Page de quiz")
    public ResponseEntity<Page<QuizResponse>> listerActifs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                quizService.listerActifs(PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier un quiz")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quiz mis à jour", content = @Content(schema = @Schema(implementation = QuizResponse.class))),
            @ApiResponse(responseCode = "404", description = "Quiz introuvable", content = @Content)
    })
    public ResponseEntity<QuizResponse> modifierQuiz(
            @PathVariable UUID trackingId, @Valid @RequestBody QuizRequest request) {
        return ResponseEntity.ok(quizService.modifierQuiz(trackingId, request));
    }

    @DeleteMapping("/{trackingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Désactiver un quiz (soft-delete)")
    @ApiResponse(responseCode = "204", description = "Quiz désactivé")
    public ResponseEntity<Void> desactiverQuiz(
            @Parameter(description = "UUID du quiz") @PathVariable UUID trackingId) {
        quizService.desactiverQuiz(trackingId);
        return ResponseEntity.noContent().build();
    }
}
