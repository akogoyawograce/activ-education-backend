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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.profil.application.dto.request.ConseillerRequest;
import tg.edtch.activEducation.profil.application.dto.response.ConseillerResponse;
import tg.edtch.activEducation.profil.domain.service.ConseillerService;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST pour la gestion des Conseillers en orientation.
 * Toutes les opérations utilisent le {@code trackingId} (UUID) — jamais la clé
 * primaire interne.
 */
@RestController
@RequestMapping("/api/v1/conseillers")
@RequiredArgsConstructor
@Tag(name = "Conseillers", description = "API de gestion des conseillers en orientation. Sécurisée par identifiant public UUID.")
public class ConseillerController {

    private final ConseillerService conseillerService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/conseillers
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un nouveau conseiller", description = "Crée un compte conseiller avec le rôle ROLE_CONSEILLER. L'email doit être unique sur toute la plateforme.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conseiller créé avec succès", content = @Content(schema = @Schema(implementation = ConseillerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides ou email déjà utilisé", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content)
    })
    public ResponseEntity<ConseillerResponse> creerConseiller(
            @Valid @RequestBody ConseillerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(conseillerService.creerConseiller(request));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/conseillers/{trackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{trackingId}")
    @Operation(summary = "Récupérer un conseiller par UUID", description = "Retourne le profil complet d'un conseiller identifié par son trackingId public.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conseiller trouvé", content = @Content(schema = @Schema(implementation = ConseillerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Conseiller introuvable", content = @Content)
    })
    public ResponseEntity<ConseillerResponse> getConseiller(
            @Parameter(description = "Identifiant public (UUID) du conseiller", required = true) @PathVariable UUID trackingId) {
        return ResponseEntity.ok(conseillerService.getConseiller(trackingId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/conseillers
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping
    @Operation(summary = "Lister tous les conseillers actifs (paginé)", description = "Retourne une page de conseillers actifs triés par nom.")
    @ApiResponse(responseCode = "200", description = "Page retournée avec succès")
    public ResponseEntity<Page<ConseillerResponse>> listerTous(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page", example = "10") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                conseillerService.listerTous(PageRequest.of(page, size, Sort.by("nom").ascending())));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/conseillers/disponibles
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/disponibles")
    @Operation(summary = "Lister les conseillers disponibles", description = "Retourne les conseillers dont la charge de travail est inférieure au seuil donné (défaut : 10 dossiers).")
    @ApiResponse(responseCode = "200", description = "Liste des conseillers disponibles")
    public ResponseEntity<List<ConseillerResponse>> listerDisponibles(
            @Parameter(description = "Seuil maximum de dossiers actifs", example = "10") @RequestParam(defaultValue = "10") int seuil) {
        return ResponseEntity.ok(conseillerService.listerConseillersDispo(seuil));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/v1/conseillers/{trackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/{trackingId}")
    @PreAuthorize("@security.isOwner(#trackingId) or hasRole('ADMIN')")
    @Operation(summary = "Modifier le profil d'un conseiller", description = "Met à jour les informations d'un conseiller. L'email est non modifiable.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conseiller mis à jour", content = @Content(schema = @Schema(implementation = ConseillerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Conseiller introuvable", content = @Content)
    })
    public ResponseEntity<ConseillerResponse> modifierConseiller(
            @Parameter(description = "Identifiant public (UUID) du conseiller", required = true) @PathVariable UUID trackingId,
            @Valid @RequestBody ConseillerRequest request) {
        return ResponseEntity.ok(conseillerService.modifierConseiller(trackingId, request));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/conseillers/{trackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{trackingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @org.springframework.security.access.prepost.PreAuthorize("@security.isOwner(#trackingId) or hasRole('ADMIN')")
    @Operation(summary = "Désactiver un compte conseiller", description = "Soft-delete : le compte est désactivé (estActif = false) mais conservé en base de données.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Compte désactivé"),
            @ApiResponse(responseCode = "404", description = "Conseiller introuvable", content = @Content)
    })
    public ResponseEntity<Void> desactiverConseiller(
            @Parameter(description = "Identifiant public (UUID) du conseiller", required = true) @PathVariable UUID trackingId) {
        conseillerService.desactiverConseiller(trackingId);
        return ResponseEntity.noContent().build();
    }
}
