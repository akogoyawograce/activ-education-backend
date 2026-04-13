package tg.edtch.activEducation.profil.application.controller;

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
import tg.edtch.activEducation.profil.application.dto.request.HistoriqueRequest;
import tg.edtch.activEducation.profil.application.dto.response.HistoriqueResponse;
import tg.edtch.activEducation.profil.domain.service.HistoriqueService;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST pour la gestion de l'historique d'activité.
 * Historique append-only : pas de modification, pas de soft-delete individuel.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Historique", description = "API d'accès à l'historique d'activité des utilisateurs. Lecture seule (append-only).")
public class HistoriqueController {

    private final HistoriqueService historiqueService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/utilisateurs/{utilisateurTrackingId}/historique
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/utilisateurs/{utilisateurTrackingId}/historique")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enregistrer une entrée d'historique", description = "Ajoute un événement dans l'historique d'activité de l'utilisateur identifié par son UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Entrée enregistrée", content = @Content(schema = @Schema(implementation = HistoriqueResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable", content = @Content)
    })
    public ResponseEntity<HistoriqueResponse> enregistrer(
            @Parameter(description = "UUID public de l'utilisateur", required = true) @PathVariable UUID utilisateurTrackingId,
            @Valid @RequestBody HistoriqueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(historiqueService.enregistrer(utilisateurTrackingId, request));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/historique/{trackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/historique/{trackingId}")
    @Operation(summary = "Récupérer une entrée d'historique par son UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrée trouvée", content = @Content(schema = @Schema(implementation = HistoriqueResponse.class))),
            @ApiResponse(responseCode = "404", description = "Entrée introuvable", content = @Content)
    })
    public ResponseEntity<HistoriqueResponse> getEntree(
            @Parameter(description = "UUID public de l'entrée d'historique", required = true) @PathVariable UUID trackingId) {
        return ResponseEntity.ok(historiqueService.getEntree(trackingId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/utilisateurs/{utilisateurTrackingId}/historique
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/utilisateurs/{utilisateurTrackingId}/historique")
    @Operation(summary = "Lister tout l'historique d'un utilisateur", description = "Retourne toutes les entrées d'historique triées par date décroissante.")
    @ApiResponse(responseCode = "200", description = "Historique retourné")
    public ResponseEntity<List<HistoriqueResponse>> getHistoriqueUtilisateur(
            @Parameter(description = "UUID public de l'utilisateur", required = true) @PathVariable UUID utilisateurTrackingId) {
        return ResponseEntity.ok(historiqueService.getHistoriqueUtilisateur(utilisateurTrackingId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/utilisateurs/{utilisateurTrackingId}/historique/pagine
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/utilisateurs/{utilisateurTrackingId}/historique/pagine")
    @Operation(summary = "Historique paginé d'un utilisateur")
    @ApiResponse(responseCode = "200", description = "Page d'historique retournée")
    public ResponseEntity<Page<HistoriqueResponse>> getHistoriquePagine(
            @PathVariable UUID utilisateurTrackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                historiqueService.getHistoriqueUtilisateurPagine(
                        utilisateurTrackingId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/utilisateurs/{utilisateurTrackingId}/historique?action=CONNEXION
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/utilisateurs/{utilisateurTrackingId}/historique/action")
    @Operation(summary = "Filtrer l'historique par type d'action", description = "Ex: ?action=CONNEXION retourne toutes les connexions de l'utilisateur.")
    @ApiResponse(responseCode = "200", description = "Historique filtré retourné")
    public ResponseEntity<List<HistoriqueResponse>> getHistoriqueParAction(
            @PathVariable UUID utilisateurTrackingId,
            @Parameter(description = "Type d'action à filtrer (ex: CONNEXION, TEST_RIASEC)", required = true) @RequestParam String action) {
        return ResponseEntity.ok(historiqueService.getHistoriqueParAction(utilisateurTrackingId, action));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/utilisateurs/{utilisateurTrackingId}/historique
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/utilisateurs/{utilisateurTrackingId}/historique")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Purger l'historique d'un utilisateur", description = "⚠️ ADMIN UNIQUEMENT — Supprime définitivement toutes les entrées d'historique d'un utilisateur. Opération irréversible.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Historique purgé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable", content = @Content)
    })
    public ResponseEntity<Void> effacerHistorique(
            @Parameter(description = "UUID public de l'utilisateur", required = true) @PathVariable UUID utilisateurTrackingId) {
        historiqueService.effacerHistoriqueUtilisateur(utilisateurTrackingId);
        return ResponseEntity.noContent().build();
    }
}
