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
import tg.edtch.activEducation.profil.domain.service.EleveService;
import tg.edtch.activEducation.profil.application.dto.request.EleveRequest;
import tg.edtch.activEducation.profil.application.dto.response.EleveResponse;

import java.util.UUID;

/**
 * Controller REST pour la gestion des Élèves.
 * Toutes les opérations utilisent le {@code trackingId} (UUID) — jamais la clé
 * primaire interne.
 */
@RestController
@RequestMapping("/api/v1/eleves")
@RequiredArgsConstructor
@Tag(name = "Élèves", description = "API de gestion des élèves. Sécurisée par identifiant public UUID.")
public class EleveController {

        private final EleveService eleveService;

        // ─────────────────────────────────────────────────────────────────────────
        // POST /api/v1/eleves
        // ─────────────────────────────────────────────────────────────────────────
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        @Operation(summary = "Inscrire un nouvel élève", description = "Crée un compte élève avec le rôle ROLE_ELEVE. L'email doit être unique sur toute la plateforme.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Élève créé avec succès", content = @Content(schema = @Schema(implementation = EleveResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Données invalides ou email déjà utilisé", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content)
        })
        public ResponseEntity<EleveResponse> inscrireEleve(
                        @Valid @RequestBody EleveRequest request) {
                EleveResponse response = eleveService.inscrireEleve(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        // ─────────────────────────────────────────────────────────────────────────
        // GET /api/v1/eleves/{trackingId}
        // ─────────────────────────────────────────────────────────────────────────
        @GetMapping("/{trackingId}")
        @org.springframework.security.access.prepost.PreAuthorize("@security.isOwner(#trackingId) or @security.isOwnChild(#trackingId) or @security.isOwnConseiller(#trackingId) or hasRole('ADMIN')")
        @Operation(summary = "Récupérer un élève par son UUID", description = "Retourne le profil complet d'un élève identifié par son trackingId public.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Élève trouvé", content = @Content(schema = @Schema(implementation = EleveResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Élève introuvable pour ce trackingId", content = @Content)
        })
        public ResponseEntity<EleveResponse> getEleve(
                        @Parameter(description = "Identifiant public (UUID) de l'élève", required = true) @PathVariable UUID trackingId) {
                return ResponseEntity.ok(eleveService.getEleve(trackingId));
        }

        // ─────────────────────────────────────────────────────────────────────────
        // GET /api/v1/eleves
        // ─────────────────────────────────────────────────────────────────────────
        @GetMapping
        @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'CONSEILLER')")
        @Operation(summary = "Lister tous les élèves actifs (paginé)", description = "Retourne une page d'élèves actifs triés par nom. Supporte la pagination via `page` et `size`.")
        @ApiResponse(responseCode = "200", description = "Page d'élèves retournée avec succès")
        public ResponseEntity<Page<EleveResponse>> listerTous(
                        @Parameter(description = "Numéro de la page (commence à 0)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Nombre d'éléments par page", example = "10") @RequestParam(defaultValue = "10") int size) {
                Page<EleveResponse> result = eleveService.listerTous(
                                PageRequest.of(page, size, Sort.by("nom").ascending()));
                return ResponseEntity.ok(result);
        }

        // ─────────────────────────────────────────────────────────────────────────
        // PUT /api/v1/eleves/{trackingId}
        // ─────────────────────────────────────────────────────────────────────────
        @PutMapping("/{trackingId}")
        @Operation(summary = "Modifier le profil d'un élève", description = "Met à jour les informations d'un élève. L'email est non modifiable pour des raisons de sécurité.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Élève mis à jour", content = @Content(schema = @Schema(implementation = EleveResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Élève introuvable", content = @Content)
        })
        public ResponseEntity<EleveResponse> modifierEleve(
                        @Parameter(description = "Identifiant public (UUID) de l'élève", required = true) @PathVariable UUID trackingId,
                        @Valid @RequestBody EleveRequest request) {
                return ResponseEntity.ok(eleveService.modifierEleve(trackingId, request));
        }

        // ─────────────────────────────────────────────────────────────────────────
        // DELETE /api/v1/eleves/{trackingId}
        // ─────────────────────────────────────────────────────────────────────────
        @DeleteMapping("/{trackingId}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        @Operation(summary = "Désactiver un compte élève", description = "Effectue un soft-delete : le compte est désactivé (estActif = false) mais conservé en base.")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Compte désactivé avec succès"),
                        @ApiResponse(responseCode = "404", description = "Élève introuvable", content = @Content)
        })
        public ResponseEntity<Void> desactiverEleve(
                        @Parameter(description = "Identifiant public (UUID) de l'élève", required = true) @PathVariable UUID trackingId) {
                eleveService.desactiverEleve(trackingId);
                return ResponseEntity.noContent().build();
        }
}
