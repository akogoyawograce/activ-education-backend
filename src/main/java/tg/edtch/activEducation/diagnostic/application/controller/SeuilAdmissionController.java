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
import tg.edtch.activEducation.diagnostic.application.dto.request.SeuilAdmissionRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.SeuilAdmissionResponse;
import tg.edtch.activEducation.diagnostic.domain.service.SeuilAdmissionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Seuils Admission", description = "API de gestion des seuils académiques d'admission par filière.")
public class SeuilAdmissionController {

    private final SeuilAdmissionService seuilAdmissionService;

    // POST /api/v1/seuils-admission
    @PostMapping("/seuils-admission")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un seuil d'admission", description = "Associe une note minimum requise pour une matière donnée, optionnellement rattachée à une filière (par son UUID).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Seuil créé", content = @Content(schema = @Schema(implementation = SeuilAdmissionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides (note hors [0,20])", content = @Content),
            @ApiResponse(responseCode = "404", description = "Filière introuvable", content = @Content)
    })
    public ResponseEntity<SeuilAdmissionResponse> creerSeuil(
            @Valid @RequestBody SeuilAdmissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seuilAdmissionService.creerSeuil(request));
    }

    // GET /api/v1/seuils-admission/{trackingId}
    @GetMapping("/seuils-admission/{trackingId}")
    @Operation(summary = "Récupérer un seuil par UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seuil trouvé", content = @Content(schema = @Schema(implementation = SeuilAdmissionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Seuil introuvable", content = @Content)
    })
    public ResponseEntity<SeuilAdmissionResponse> getSeuil(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(seuilAdmissionService.getSeuil(trackingId));
    }

    // GET /api/v1/filieres/{filiereTrackingId}/seuils-admission
    @GetMapping("/filieres/{filiereTrackingId}/seuils-admission")
    @Operation(summary = "Seuils d'admission d'une filière", description = "Retourne tous les seuils associés à la filière identifiée par son UUID public.")
    @ApiResponse(responseCode = "200", description = "Seuils retournés")
    public ResponseEntity<List<SeuilAdmissionResponse>> getSeuilsParFiliere(
            @Parameter(description = "UUID public de la filière") @PathVariable UUID filiereTrackingId) {
        return ResponseEntity.ok(seuilAdmissionService.getSeuilsParFiliere(filiereTrackingId));
    }

    // PUT /api/v1/seuils-admission/{trackingId}
    @PutMapping("/seuils-admission/{trackingId}")
    @Operation(summary = "Modifier un seuil d'admission")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seuil mis à jour", content = @Content(schema = @Schema(implementation = SeuilAdmissionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Seuil ou filière introuvable", content = @Content)
    })
    public ResponseEntity<SeuilAdmissionResponse> modifierSeuil(
            @PathVariable UUID trackingId, @Valid @RequestBody SeuilAdmissionRequest request) {
        return ResponseEntity.ok(seuilAdmissionService.modifierSeuil(trackingId, request));
    }

    // DELETE /api/v1/seuils-admission/{trackingId}
    @DeleteMapping("/seuils-admission/{trackingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un seuil d'admission")
    @ApiResponse(responseCode = "204", description = "Seuil supprimé")
    public ResponseEntity<Void> supprimerSeuil(@PathVariable UUID trackingId) {
        seuilAdmissionService.supprimerSeuil(trackingId);
        return ResponseEntity.noContent().build();
    }
}
