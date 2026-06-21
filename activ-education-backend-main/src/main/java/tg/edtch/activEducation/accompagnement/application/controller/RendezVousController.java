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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.accompagnement.application.dto.request.RendezVousRequest;
import tg.edtch.activEducation.accompagnement.application.dto.response.RendezVousResponse;
import tg.edtch.activEducation.accompagnement.domain.service.RendezVousService;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST pour la gestion des rendez-vous élève-conseiller.
 * Cycle de vie : PLANIFIE → TERMINE | ANNULE.
 */
@RestController
@RequestMapping("/api/v1/rendez-vous")
@RequiredArgsConstructor
@Tag(name = "Rendez-Vous", description = "API de gestion des rendez-vous entre élèves et conseillers. Identifiants UUID publics.")
public class RendezVousController {

    private final RendezVousService rendezVousService;

    // POST /api/v1/rendez-vous
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Planifier un rendez-vous", description = "Crée un rendez-vous PLANIFIÉ entre un élève et un conseiller (identifiés par leur UUID). La date doit être dans le futur.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rendez-vous planifié", content = @Content(schema = @Schema(implementation = RendezVousResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides (date passée, UUID manquant)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Élève ou conseiller introuvable", content = @Content)
    })
    public ResponseEntity<RendezVousResponse> planifier(@Valid @RequestBody RendezVousRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rendezVousService.planifier(request));
    }

    // GET /api/v1/rendez-vous/{trackingId}
    @GetMapping("/{trackingId}")
    @PreAuthorize("@security.isRdvParticipant(#trackingId) or hasRole('ADMIN')")
    @Operation(summary = "Récupérer un rendez-vous par UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rendez-vous trouvé", content = @Content(schema = @Schema(implementation = RendezVousResponse.class))),
            @ApiResponse(responseCode = "404", description = "Rendez-vous introuvable", content = @Content)
    })
    public ResponseEntity<RendezVousResponse> getRendezVous(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(rendezVousService.getRendezVous(trackingId));
    }

    @GetMapping("/eleve/{eleveTrackingId}")
    @PreAuthorize("@security.isOwner(#eleveTrackingId) or @security.isOwnChild(#eleveTrackingId) or @security.isOwnConseiller(#eleveTrackingId) or hasRole('ADMIN')")
    @Operation(summary = "Rendez-vous d'un élève", description = "Triés par date décroissante.")
    @ApiResponse(responseCode = "200", description = "Liste des RDV de l'élève")
    public ResponseEntity<List<RendezVousResponse>> getRendezVousEleve(
            @PathVariable UUID eleveTrackingId) {
        return ResponseEntity.ok(rendezVousService.getRendezVousEleve(eleveTrackingId));
    }

    // GET /api/v1/rendez-vous/conseiller/{conseillerTrackingId}
    @GetMapping("/conseiller/{conseillerTrackingId}")
    @PreAuthorize("@security.isOwner(#conseillerTrackingId) or hasRole('ADMIN')")
    @Operation(summary = "Rendez-vous d'un conseiller", description = "Triés par date décroissante.")
    @ApiResponse(responseCode = "200", description = "Liste des RDV du conseiller")
    public ResponseEntity<List<RendezVousResponse>> getRendezVousConseiller(
            @PathVariable UUID conseillerTrackingId) {
        return ResponseEntity.ok(rendezVousService.getRendezVousConseiller(conseillerTrackingId));
    }

    @GetMapping("/eleve/{eleveTrackingId}/pagine")
    @PreAuthorize("@security.isOwner(#eleveTrackingId) or @security.isOwnChild(#eleveTrackingId) or @security.isOwnConseiller(#eleveTrackingId) or hasRole('ADMIN')")
    @Operation(summary = "Rendez-vous d'un élève (paginés)")
    @ApiResponse(responseCode = "200", description = "Page de RDV")
    public ResponseEntity<Page<RendezVousResponse>> getRendezVousElevePagine(
            @PathVariable UUID eleveTrackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(rendezVousService.getRendezVousElevePagine(
                eleveTrackingId, PageRequest.of(page, size, Sort.by("dateHeurePrevue").descending())));
    }

    // GET /api/v1/rendez-vous/conseiller/{conseillerTrackingId}/pagine
    @GetMapping("/conseiller/{conseillerTrackingId}/pagine")
    @PreAuthorize("@security.isOwner(#conseillerTrackingId) or hasRole('ADMIN')")
    @Operation(summary = "Rendez-vous d'un conseiller (paginés)")
    @ApiResponse(responseCode = "200", description = "Page de RDV")
    public ResponseEntity<Page<RendezVousResponse>> getRendezVousConseillerPagine(
            @PathVariable UUID conseillerTrackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(rendezVousService.getRendezVousConseillerPagine(
                conseillerTrackingId, PageRequest.of(page, size, Sort.by("dateHeurePrevue").descending())));
    }

    @GetMapping("/eleve/{eleveTrackingId}/statut/{statut}")
    @PreAuthorize("@security.isOwner(#eleveTrackingId) or @security.isOwnChild(#eleveTrackingId) or @security.isOwnConseiller(#eleveTrackingId) or hasRole('ADMIN')")
    @Operation(summary = "RDV d'un élève filtrés par statut", description = "statut : PLANIFIE | TERMINE | ANNULE")
    @ApiResponse(responseCode = "200", description = "RDV filtrés")
    public ResponseEntity<List<RendezVousResponse>> getEleveParStatut(
            @PathVariable UUID eleveTrackingId,
            @Parameter(description = "PLANIFIE, TERMINE ou ANNULE") @PathVariable String statut) {
        return ResponseEntity.ok(rendezVousService.getRendezVousEleveParStatut(eleveTrackingId, statut));
    }

    // GET /api/v1/rendez-vous/conseiller/{conseillerTrackingId}/statut/{statut}
    @GetMapping("/conseiller/{conseillerTrackingId}/statut/{statut}")
    @PreAuthorize("@security.isOwner(#conseillerTrackingId) or hasRole('ADMIN')")
    @Operation(summary = "RDV d'un conseiller filtrés par statut")
    @ApiResponse(responseCode = "200", description = "RDV filtrés")
    public ResponseEntity<List<RendezVousResponse>> getConseillerParStatut(
            @PathVariable UUID conseillerTrackingId,
            @Parameter(description = "PLANIFIE, TERMINE ou ANNULE") @PathVariable String statut) {
        return ResponseEntity.ok(rendezVousService.getRendezVousConseillerParStatut(conseillerTrackingId, statut));
    }

    // PUT /api/v1/rendez-vous/{trackingId}
    @PutMapping("/{trackingId}")
    @PreAuthorize("@security.isRdvParticipant(#trackingId) or hasRole('ADMIN')")
    @Operation(summary = "Modifier un rendez-vous", description = "Modification possible uniquement si le statut est PLANIFIÉ.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rendez-vous mis à jour", content = @Content(schema = @Schema(implementation = RendezVousResponse.class))),
            @ApiResponse(responseCode = "409", description = "RDV non modifiable (déjà TERMINE ou ANNULE)", content = @Content)
    })
    public ResponseEntity<RendezVousResponse> modifierRendezVous(
            @PathVariable UUID trackingId,
            @Valid @RequestBody RendezVousRequest request) {
        return ResponseEntity.ok(rendezVousService.modifierRendezVous(trackingId, request));
    }

    // PATCH /api/v1/rendez-vous/{trackingId}/terminer
    @PatchMapping("/{trackingId}/terminer")
    @PreAuthorize("@security.isRdvParticipant(#trackingId) or hasRole('ADMIN')")
    @Operation(summary = "Marquer un rendez-vous comme terminé", description = "Transition : PLANIFIE → TERMINE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "RDV marqué comme terminé", content = @Content(schema = @Schema(implementation = RendezVousResponse.class))),
            @ApiResponse(responseCode = "409", description = "RDV déjà terminé ou annulé", content = @Content)
    })
    public ResponseEntity<RendezVousResponse> terminer(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(rendezVousService.terminer(trackingId));
    }

    // PATCH /api/v1/rendez-vous/{trackingId}/annuler
    @PatchMapping("/{trackingId}/annuler")
    @PreAuthorize("@security.isRdvParticipant(#trackingId) or hasRole('ADMIN')")
    @Operation(summary = "Annuler un rendez-vous", description = "Transition : PLANIFIE → ANNULE. Un RDV TERMINÉ ne peut pas être annulé.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "RDV annulé", content = @Content(schema = @Schema(implementation = RendezVousResponse.class))),
            @ApiResponse(responseCode = "409", description = "RDV déjà terminé", content = @Content)
    })
    public ResponseEntity<RendezVousResponse> annuler(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(rendezVousService.annuler(trackingId));
    }
}
