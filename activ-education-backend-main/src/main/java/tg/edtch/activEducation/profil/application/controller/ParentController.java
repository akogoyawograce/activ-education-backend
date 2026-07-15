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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.profil.application.dto.request.ParentRequest;
import tg.edtch.activEducation.profil.application.dto.response.ParentResponse;
import tg.edtch.activEducation.profil.domain.service.ParentService;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST pour la gestion des Parents.
 * Toutes les opérations utilisent le {@code trackingId} (UUID) — jamais la clé
 * primaire interne.
 */
@RestController
@RequestMapping("/api/v1/parents")
@RequiredArgsConstructor
@Tag(name = "Parents", description = "API de gestion des parents d'élèves. Relation parent-enfant gérée via UUID public.")
public class ParentController {

    private final ParentService parentService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/parents
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un nouveau parent", description = "Crée un compte parent avec le rôle ROLE_PARENT. Les enfants peuvent être rattachés via leurs trackingId UUID lors de la création ou séparément.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Parent créé", content = @Content(schema = @Schema(implementation = ParentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides ou email déjà utilisé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Un des trackingId enfant introuvable", content = @Content)
    })
    public ResponseEntity<ParentResponse> creerParent(
            @Valid @RequestBody ParentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parentService.creerParent(request));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/parents/{trackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{trackingId}")
    @Operation(summary = "Récupérer un parent par UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parent trouvé", content = @Content(schema = @Schema(implementation = ParentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Parent introuvable", content = @Content)
    })
    public ResponseEntity<ParentResponse> getParent(
            @Parameter(description = "Identifiant public (UUID) du parent", required = true) @PathVariable UUID trackingId) {
        return ResponseEntity.ok(parentService.getParent(trackingId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/parents
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping
    @Operation(summary = "Lister tous les parents actifs (paginé)")
    @ApiResponse(responseCode = "200", description = "Page retournée avec succès")
    public ResponseEntity<Page<ParentResponse>> listerTous(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page", example = "10") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                parentService.listerTous(PageRequest.of(page, size, Sort.by("nom").ascending())));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/parents/par-eleve/{eleveTrackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/par-eleve/{eleveTrackingId}")
    @Operation(summary = "Récupérer les parents d'un élève", description = "Retourne tous les parents rattachés à un élève identifié par son trackingId UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des parents retournée"),
            @ApiResponse(responseCode = "404", description = "Élève introuvable", content = @Content)
    })
    public ResponseEntity<List<ParentResponse>> getParentsParEleve(
            @Parameter(description = "Identifiant public (UUID) de l'élève", required = true) @PathVariable UUID eleveTrackingId) {
        return ResponseEntity.ok(parentService.getParentsParEleve(eleveTrackingId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/v1/parents/{trackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/{trackingId}")
    @PreAuthorize("@security.isOwner(#trackingId) or hasRole('ADMIN')")
    @Operation(summary = "Modifier le profil d'un parent", description = "Met à jour nom, prénom, téléphone. L'email est non modifiable.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parent mis à jour", content = @Content(schema = @Schema(implementation = ParentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Parent introuvable", content = @Content)
    })
    public ResponseEntity<ParentResponse> modifierParent(
            @PathVariable UUID trackingId,
            @Valid @RequestBody ParentRequest request) {
        return ResponseEntity.ok(parentService.modifierParent(trackingId, request));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/parents/{trackingId}/enfants/{eleveTrackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/{trackingId}/enfants/{eleveTrackingId}")
    @PreAuthorize("@security.isOwner(#trackingId) or hasRole('ADMIN')")
    @Operation(summary = "Rattacher un élève à un parent", description = "Crée le lien ManyToMany entre le parent et l'élève via leurs trackingId UUID publics.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lien créé, parent retourné mis à jour", content = @Content(schema = @Schema(implementation = ParentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Parent ou élève introuvable", content = @Content),
            @ApiResponse(responseCode = "409", description = "Lien déjà existant", content = @Content)
    })
    public ResponseEntity<ParentResponse> ajouterEnfant(
            @Parameter(description = "UUID du parent", required = true) @PathVariable UUID trackingId,
            @Parameter(description = "UUID de l'élève", required = true) @PathVariable UUID eleveTrackingId) {
        return ResponseEntity.ok(parentService.ajouterEnfant(trackingId, eleveTrackingId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/parents/{trackingId}/enfants/{eleveTrackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{trackingId}/enfants/{eleveTrackingId}")
    @PreAuthorize("@security.isOwner(#trackingId) or hasRole('ADMIN')")
    @Operation(summary = "Retirer le lien entre un parent et un élève", description = "Supprime la relation ManyToMany — n'affecte pas les comptes eux-mêmes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lien supprimé, parent retourné mis à jour"),
            @ApiResponse(responseCode = "404", description = "Parent ou lien introuvable", content = @Content)
    })
    public ResponseEntity<ParentResponse> retirerEnfant(
            @Parameter(description = "UUID du parent", required = true) @PathVariable UUID trackingId,
            @Parameter(description = "UUID de l'élève", required = true) @PathVariable UUID eleveTrackingId) {
        return ResponseEntity.ok(parentService.retirerEnfant(trackingId, eleveTrackingId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/parents/{trackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{trackingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @org.springframework.security.access.prepost.PreAuthorize("@security.isOwner(#trackingId) or hasRole('ADMIN')")
    @Operation(summary = "Désactiver un compte parent", description = "Soft-delete : estActif = false, données conservées en base.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Compte désactivé"),
            @ApiResponse(responseCode = "404", description = "Parent introuvable", content = @Content)
    })
    public ResponseEntity<Void> desactiverParent(
            @Parameter(description = "UUID du parent", required = true) @PathVariable UUID trackingId) {
        parentService.desactiverParent(trackingId);
        return ResponseEntity.noContent().build();
    }
}
