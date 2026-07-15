package tg.edtch.activEducation.diagnostic.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.diagnostic.application.dto.request.ScoreMatriceRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.ScoreMatriceResponse;
import tg.edtch.activEducation.diagnostic.domain.service.ScoreMatriceService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/score-matrices")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Score Matrices", description = "API de gestion des matrices de pondération du diagnostic d'orientation.")
public class ScoreMatriceController {

    private final ScoreMatriceService scoreMatriceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une matrice de score", description = "Le scoreTotalEstime est calculé automatiquement (somme des 3 dimensions) si non fourni.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Matrice créée", content = @Content(schema = @Schema(implementation = ScoreMatriceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content)
    })
    public ResponseEntity<ScoreMatriceResponse> creerMatrice(@Valid @RequestBody ScoreMatriceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scoreMatriceService.creerMatrice(request));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les matrices (paginé)")
    @ApiResponse(responseCode = "200", description = "Page de matrices")
    public ResponseEntity<Page<ScoreMatriceResponse>> listerMatrices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                scoreMatriceService.listerMatrices(
                        PageRequest.of(page, size, Sort.by("titreMatrice").ascending())));
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Récupérer une matrice par UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matrice trouvée", content = @Content(schema = @Schema(implementation = ScoreMatriceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Matrice introuvable", content = @Content)
    })
    public ResponseEntity<ScoreMatriceResponse> getMatrice(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(scoreMatriceService.getMatrice(trackingId));
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier une matrice de score", description = "Si les scores individuels sont modifiés sans fournir scoreTotalEstime, le total est recalculé automatiquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matrice mise à jour", content = @Content(schema = @Schema(implementation = ScoreMatriceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Matrice introuvable", content = @Content)
    })
    public ResponseEntity<ScoreMatriceResponse> modifierMatrice(
            @PathVariable UUID trackingId, @Valid @RequestBody ScoreMatriceRequest request) {
        return ResponseEntity.ok(scoreMatriceService.modifierMatrice(trackingId, request));
    }

    @DeleteMapping("/{trackingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une matrice de score")
    @ApiResponse(responseCode = "204", description = "Matrice supprimée")
    public ResponseEntity<Void> supprimerMatrice(@PathVariable UUID trackingId) {
        scoreMatriceService.supprimerMatrice(trackingId);
        return ResponseEntity.noContent().build();
    }
}
