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
import tg.edtch.activEducation.diagnostic.application.dto.request.ResultatDiagnosticRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.ResultatDiagnosticResponse;
import tg.edtch.activEducation.diagnostic.domain.service.ResultatDiagnosticService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Résultats Diagnostic", description = "API d'enregistrement et consultation des résultats de diagnostic. Append-only (pas de modification).")
public class ResultatDiagnosticController {

    private final ResultatDiagnosticService resultatService;

    // POST /api/v1/resultats-diagnostic
    @PostMapping("/resultats-diagnostic")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enregistrer un résultat de diagnostic", description = "Stocke le résultat final d'un quiz passé par un élève, avec son profil découvert et sa recommandation.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Résultat enregistré", content = @Content(schema = @Schema(implementation = ResultatDiagnosticResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides ou quiz désactivé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Élève ou quiz introuvable", content = @Content)
    })
    public ResponseEntity<ResultatDiagnosticResponse> enregistrerResultat(
            @Valid @RequestBody ResultatDiagnosticRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resultatService.enregistrerResultat(request));
    }

    // GET /api/v1/resultats-diagnostic/{trackingId}
    @GetMapping("/resultats-diagnostic/{trackingId}")
    @Operation(summary = "Récupérer un résultat par UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Résultat trouvé", content = @Content(schema = @Schema(implementation = ResultatDiagnosticResponse.class))),
            @ApiResponse(responseCode = "404", description = "Résultat introuvable", content = @Content)
    })
    public ResponseEntity<ResultatDiagnosticResponse> getResultat(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(resultatService.getResultat(trackingId));
    }

    // GET /api/v1/eleves/{eleveTrackingId}/resultats-diagnostic
    @GetMapping("/eleves/{eleveTrackingId}/resultats-diagnostic")
    @Operation(summary = "Historique paginé des résultats de diagnostic d'un élève")
    @ApiResponse(responseCode = "200", description = "Page de résultats")
    public ResponseEntity<Page<ResultatDiagnosticResponse>> getResultatsEleve(
            @PathVariable UUID eleveTrackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                resultatService.getResultatsEleve(
                        eleveTrackingId,
                        PageRequest.of(page, size, Sort.by("datePassage").descending())));
    }

    // GET
    // /api/v1/eleves/{eleveTrackingId}/resultats-diagnostic/dernier?quizTrackingId=...
    @GetMapping("/eleves/{eleveTrackingId}/resultats-diagnostic/dernier")
    @Operation(summary = "Dernier résultat d'un élève pour un quiz donné", description = "Utile pour afficher le profil RIASEC courant de l'élève. Retourne 204 si aucun résultat trouvé.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dernier résultat trouvé", content = @Content(schema = @Schema(implementation = ResultatDiagnosticResponse.class))),
            @ApiResponse(responseCode = "204", description = "Aucun résultat pour ce quiz", content = @Content)
    })
    public ResponseEntity<ResultatDiagnosticResponse> getDernierResultat(
            @PathVariable UUID eleveTrackingId,
            @Parameter(description = "UUID public du quiz", required = true) @RequestParam UUID quizTrackingId) {
        return resultatService.getDernierResultat(eleveTrackingId, quizTrackingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // DELETE /api/v1/resultats-diagnostic/{trackingId}
    @DeleteMapping("/resultats-diagnostic/{trackingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un résultat", description = "⚠️ ADMIN UNIQUEMENT — Suppression définitive et irréversible.")
    @ApiResponse(responseCode = "204", description = "Résultat supprimé")
    public ResponseEntity<Void> supprimerResultat(@PathVariable UUID trackingId) {
        resultatService.supprimerResultat(trackingId);
        return ResponseEntity.noContent().build();
    }
}
