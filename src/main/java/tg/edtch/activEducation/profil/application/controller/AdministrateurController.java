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
import tg.edtch.activEducation.profil.application.dto.request.AdministrateurRequest;
import tg.edtch.activEducation.profil.application.dto.response.AdministrateurResponse;
import tg.edtch.activEducation.profil.domain.service.AdministrateurService;

import java.util.UUID;

/**
 * Controller REST pour la gestion des Administrateurs.
 * Toutes les opérations utilisent le {@code trackingId} (UUID) — jamais la clé
 * primaire interne.
 */
@RestController
@RequestMapping("/api/v1/administrateurs")
@RequiredArgsConstructor
@Tag(name = "Administrateurs", description = "API de gestion des administrateurs de la plateforme. Sécurisée par identifiant public UUID.")
public class AdministrateurController {

    private final AdministrateurService adminService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/administrateurs
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un administrateur", description = "Crée un compte administrateur avec le rôle ROLE_ADMIN. Le niveauAcces accepte : SUPER_ADMIN, MODERATEUR, GESTIONNAIRE_CONSEILLER.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Administrateur créé", content = @Content(schema = @Schema(implementation = AdministrateurResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides ou email déjà utilisé", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content)
    })
    public ResponseEntity<AdministrateurResponse> creerAdministrateur(
            @Valid @RequestBody AdministrateurRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.creerAdministrateur(request));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/administrateurs/{trackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{trackingId}")
    @Operation(summary = "Récupérer un administrateur par UUID", description = "Retourne le profil d'un administrateur identifié par son trackingId public.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Administrateur trouvé", content = @Content(schema = @Schema(implementation = AdministrateurResponse.class))),
            @ApiResponse(responseCode = "404", description = "Administrateur introuvable", content = @Content)
    })
    public ResponseEntity<AdministrateurResponse> getAdministrateur(
            @Parameter(description = "Identifiant public (UUID) de l'administrateur", required = true) @PathVariable UUID trackingId) {
        return ResponseEntity.ok(adminService.getAdministrateur(trackingId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/administrateurs
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping
    @Operation(summary = "Lister tous les administrateurs actifs (paginé)", description = "Retourne une page d'administrateurs actifs triés par nom.")
    @ApiResponse(responseCode = "200", description = "Page retournée avec succès")
    public ResponseEntity<Page<AdministrateurResponse>> listerTous(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page", example = "10") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                adminService.listerTous(PageRequest.of(page, size, Sort.by("nom").ascending())));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/v1/administrateurs/{trackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier un administrateur", description = "Met à jour les informations d'un administrateur. L'email est non modifiable.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Administrateur mis à jour", content = @Content(schema = @Schema(implementation = AdministrateurResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "404", description = "Administrateur introuvable", content = @Content)
    })
    public ResponseEntity<AdministrateurResponse> modifierAdministrateur(
            @Parameter(description = "Identifiant public (UUID) de l'administrateur", required = true) @PathVariable UUID trackingId,
            @Valid @RequestBody AdministrateurRequest request) {
        return ResponseEntity.ok(adminService.modifierAdministrateur(trackingId, request));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/administrateurs/{trackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{trackingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Désactiver un compte administrateur", description = "Soft-delete : le compte est désactivé (estActif = false) mais conservé en base.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Compte désactivé"),
            @ApiResponse(responseCode = "404", description = "Administrateur introuvable", content = @Content)
    })
    public ResponseEntity<Void> desactiverAdministrateur(
            @Parameter(description = "Identifiant public (UUID) de l'administrateur", required = true) @PathVariable UUID trackingId) {
        adminService.desactiverAdministrateur(trackingId);
        return ResponseEntity.noContent().build();
    }
}
