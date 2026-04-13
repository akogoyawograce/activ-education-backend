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
import tg.edtch.activEducation.diagnostic.application.dto.request.ReponseRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.ReponseResponse;
import tg.edtch.activEducation.diagnostic.domain.service.ReponseService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Réponses", description = "API de gestion des options de réponse d'une question. Imbriquées sous /questions/{UUID}.")
public class ReponseController {

    private final ReponseService reponseService;

    // POST /api/v1/questions/{questionTrackingId}/reponses
    @PostMapping("/questions/{questionTrackingId}/reponses")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ajouter une option de réponse à une question", description = "La catégorie RIASEC (R, I, A, S, E, C) et les points permettent de scorer le résultat du diagnostic.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Réponse ajoutée", content = @Content(schema = @Schema(implementation = ReponseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "404", description = "Question introuvable", content = @Content)
    })
    public ResponseEntity<ReponseResponse> ajouterReponse(
            @Parameter(description = "UUID public de la question") @PathVariable UUID questionTrackingId,
            @Valid @RequestBody ReponseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reponseService.ajouterReponse(questionTrackingId, request));
    }

    // GET /api/v1/questions/{questionTrackingId}/reponses
    @GetMapping("/questions/{questionTrackingId}/reponses")
    @Operation(summary = "Lister toutes les options de réponse d'une question")
    @ApiResponse(responseCode = "200", description = "Options de réponse retournées")
    public ResponseEntity<List<ReponseResponse>> getReponsesParQuestion(
            @PathVariable UUID questionTrackingId) {
        return ResponseEntity.ok(reponseService.getReponsesParQuestion(questionTrackingId));
    }

    // GET /api/v1/reponses/{trackingId}
    @GetMapping("/reponses/{trackingId}")
    @Operation(summary = "Récupérer une option de réponse par son UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réponse trouvée", content = @Content(schema = @Schema(implementation = ReponseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Réponse introuvable", content = @Content)
    })
    public ResponseEntity<ReponseResponse> getReponse(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(reponseService.getReponse(trackingId));
    }

    // PUT /api/v1/reponses/{trackingId}
    @PutMapping("/reponses/{trackingId}")
    @Operation(summary = "Modifier une option de réponse")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réponse mise à jour", content = @Content(schema = @Schema(implementation = ReponseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Réponse introuvable", content = @Content)
    })
    public ResponseEntity<ReponseResponse> modifierReponse(
            @PathVariable UUID trackingId, @Valid @RequestBody ReponseRequest request) {
        return ResponseEntity.ok(reponseService.modifierReponse(trackingId, request));
    }

    // DELETE /api/v1/reponses/{trackingId}
    @DeleteMapping("/reponses/{trackingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une option de réponse", description = "Hard-delete permanent.")
    @ApiResponse(responseCode = "204", description = "Réponse supprimée")
    public ResponseEntity<Void> supprimerReponse(@PathVariable UUID trackingId) {
        reponseService.supprimerReponse(trackingId);
        return ResponseEntity.noContent().build();
    }
}
