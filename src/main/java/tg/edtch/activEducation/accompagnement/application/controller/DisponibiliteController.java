package tg.edtch.activEducation.accompagnement.application.controller;

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
import tg.edtch.activEducation.accompagnement.application.dto.request.DisponibiliteRequest;
import tg.edtch.activEducation.accompagnement.application.dto.response.DisponibiliteResponse;
import tg.edtch.activEducation.accompagnement.domain.service.DisponibiliteService;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST pour la gestion des disponibilités des conseillers.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Disponibilités", description = "API de gestion des créneaux de disponibilité des conseillers. Identifiants publics UUID.")
public class DisponibiliteController {

    private final DisponibiliteService disponibiliteService;

    // POST /api/v1/conseillers/{conseillerTrackingId}/disponibilites
    @PostMapping("/conseillers/{conseillerTrackingId}/disponibilites")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ajouter un créneau de disponibilité", description = "Enregistre un créneau récurrent (ex: Lundi 09h–12h) pour un conseiller. Jour ISO : 1=Lundi, 7=Dimanche.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Créneau créé", content = @Content(schema = @Schema(implementation = DisponibiliteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides ou heure de fin ≤ heure de début", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conseiller introuvable", content = @Content)
    })
    public ResponseEntity<DisponibiliteResponse> ajouterDisponibilite(
            @Parameter(description = "UUID public du conseiller", required = true) @PathVariable UUID conseillerTrackingId,
            @Valid @RequestBody DisponibiliteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(disponibiliteService.ajouterDisponibilite(conseillerTrackingId, request));
    }

    // GET /api/v1/disponibilites/{trackingId}
    @GetMapping("/disponibilites/{trackingId}")
    @Operation(summary = "Récupérer un créneau par son UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Créneau trouvé", content = @Content(schema = @Schema(implementation = DisponibiliteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Créneau introuvable", content = @Content)
    })
    public ResponseEntity<DisponibiliteResponse> getDisponibilite(
            @PathVariable UUID trackingId) {
        return ResponseEntity.ok(disponibiliteService.getDisponibilite(trackingId));
    }

    // GET /api/v1/conseillers/{conseillerTrackingId}/disponibilites
    @GetMapping("/conseillers/{conseillerTrackingId}/disponibilites")
    @Operation(summary = "Lister tous les créneaux d'un conseiller", description = "Triés par jour puis heure de début.")
    @ApiResponse(responseCode = "200", description = "Liste des créneaux")
    public ResponseEntity<List<DisponibiliteResponse>> getDisponibilitesConseiller(
            @PathVariable UUID conseillerTrackingId) {
        return ResponseEntity.ok(disponibiliteService.getDisponibilitesConseiller(conseillerTrackingId));
    }

    // GET /api/v1/conseillers/{conseillerTrackingId}/disponibilites/pagine
    @GetMapping("/conseillers/{conseillerTrackingId}/disponibilites/pagine")
    @Operation(summary = "Créneaux paginés d'un conseiller")
    @ApiResponse(responseCode = "200", description = "Page de créneaux")
    public ResponseEntity<Page<DisponibiliteResponse>> getDisponibilitesPagine(
            @PathVariable UUID conseillerTrackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                disponibiliteService.getDisponibilitesConseilleurPagine(
                        conseillerTrackingId,
                        PageRequest.of(page, size, Sort.by("jourSemaine", "heureDebut"))));
    }

    // GET
    // /api/v1/conseillers/{conseillerTrackingId}/disponibilites/jour/{jourSemaine}
    @GetMapping("/conseillers/{conseillerTrackingId}/disponibilites/jour/{jourSemaine}")
    @Operation(summary = "Créneaux d'un conseiller pour un jour donné", description = "jourSemaine : 1=Lundi, 2=Mardi, ..., 7=Dimanche.")
    @ApiResponse(responseCode = "200", description = "Créneaux du jour")
    public ResponseEntity<List<DisponibiliteResponse>> getDisponibilitesParJour(
            @PathVariable UUID conseillerTrackingId,
            @Parameter(description = "Jour ISO (1=Lundi … 7=Dimanche)", required = true) @PathVariable Integer jourSemaine) {
        return ResponseEntity.ok(disponibiliteService.getDisponibilitesParJour(conseillerTrackingId, jourSemaine));
    }

    // PUT /api/v1/disponibilites/{trackingId}
    @PutMapping("/disponibilites/{trackingId}")
    @Operation(summary = "Modifier un créneau de disponibilité")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Créneau mis à jour", content = @Content(schema = @Schema(implementation = DisponibiliteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "404", description = "Créneau introuvable", content = @Content)
    })
    public ResponseEntity<DisponibiliteResponse> modifierDisponibilite(
            @PathVariable UUID trackingId,
            @Valid @RequestBody DisponibiliteRequest request) {
        return ResponseEntity.ok(disponibiliteService.modifierDisponibilite(trackingId, request));
    }

    // DELETE /api/v1/disponibilites/{trackingId}
    @DeleteMapping("/disponibilites/{trackingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un créneau", description = "Suppression définitive (hard-delete).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Créneau supprimé"),
            @ApiResponse(responseCode = "404", description = "Créneau introuvable", content = @Content)
    })
    public ResponseEntity<Void> supprimerDisponibilite(@PathVariable UUID trackingId) {
        disponibiliteService.supprimerDisponibilite(trackingId);
        return ResponseEntity.noContent().build();
    }
}
