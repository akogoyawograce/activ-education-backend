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
import tg.edtch.activEducation.profil.application.dto.request.NoteSaisiManuelRequest;
import tg.edtch.activEducation.profil.application.dto.response.NoteSaisiManuelResponse;
import tg.edtch.activEducation.profil.domain.service.NoteSaisiManuelService;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST pour la gestion des notes saisies manuellement.
 * Les notes sont toujours rattachées à un élève via son trackingId public.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Notes Manuelles", description = "API de saisie et gestion des notes manuelles des élèves. Identifiants publics UUID.")
public class NoteSaisiManuelController {

    private final NoteSaisiManuelService noteService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/eleves/{eleveTrackingId}/notes
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/eleves/{eleveTrackingId}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ajouter une note à un élève", description = "Crée une note manuellement saisie pour l'élève identifié par son trackingId UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Note créée", content = @Content(schema = @Schema(implementation = NoteSaisiManuelResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides (note hors [0-20])", content = @Content),
            @ApiResponse(responseCode = "404", description = "Élève introuvable", content = @Content)
    })
    public ResponseEntity<NoteSaisiManuelResponse> ajouterNote(
            @Parameter(description = "UUID public de l'élève", required = true) @PathVariable UUID eleveTrackingId,
            @Valid @RequestBody NoteSaisiManuelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(noteService.ajouterNote(eleveTrackingId, request));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/notes/{trackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/notes/{trackingId}")
    @Operation(summary = "Récupérer une note par son UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note trouvée", content = @Content(schema = @Schema(implementation = NoteSaisiManuelResponse.class))),
            @ApiResponse(responseCode = "404", description = "Note introuvable", content = @Content)
    })
    public ResponseEntity<NoteSaisiManuelResponse> getNote(
            @Parameter(description = "UUID public de la note", required = true) @PathVariable UUID trackingId) {
        return ResponseEntity.ok(noteService.getNote(trackingId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/eleves/{eleveTrackingId}/notes
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/eleves/{eleveTrackingId}/notes")
    @PreAuthorize("@security.isOwner(#eleveTrackingId) or @security.isOwnChild(#eleveTrackingId) or @security.isOwnConseiller(#eleveTrackingId) or hasRole('ADMIN')")
    @Operation(summary = "Lister toutes les notes d'un élève", description = "Retourne toutes les notes d'un élève triées par année scolaire décroissante.")
    @ApiResponse(responseCode = "200", description = "Liste des notes retournée")
    public ResponseEntity<List<NoteSaisiManuelResponse>> getNotesByEleve(
            @Parameter(description = "UUID public de l'élève", required = true) @PathVariable UUID eleveTrackingId) {
        return ResponseEntity.ok(noteService.getNotesByEleve(eleveTrackingId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/eleves/{eleveTrackingId}/notes/pagine
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/eleves/{eleveTrackingId}/notes/pagine")
    @PreAuthorize("@security.isOwner(#eleveTrackingId) or @security.isOwnChild(#eleveTrackingId) or @security.isOwnConseiller(#eleveTrackingId) or hasRole('ADMIN')")
    @Operation(summary = "Lister les notes d'un élève (paginé)", description = "Retourne une page paginée des notes d'un élève.")
    @ApiResponse(responseCode = "200", description = "Page de notes retournée")
    public ResponseEntity<Page<NoteSaisiManuelResponse>> getNotesByElevePagine(
            @Parameter(description = "UUID public de l'élève", required = true) @PathVariable UUID eleveTrackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                noteService.getNotesByElevePagine(
                        eleveTrackingId,
                        PageRequest.of(page, size, Sort.by("anneeScolaire").descending())));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/v1/notes/{trackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/notes/{trackingId}")
    @Operation(summary = "Modifier une note", description = "Met à jour une note identifiée par son trackingId. L'élève rattaché est non modifiable.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note mise à jour", content = @Content(schema = @Schema(implementation = NoteSaisiManuelResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "404", description = "Note introuvable", content = @Content)
    })
    public ResponseEntity<NoteSaisiManuelResponse> modifierNote(
            @Parameter(description = "UUID public de la note", required = true) @PathVariable UUID trackingId,
            @Valid @RequestBody NoteSaisiManuelRequest request) {
        return ResponseEntity.ok(noteService.modifierNote(trackingId, request));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/notes/{trackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/notes/{trackingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une note", description = "Suppression définitive (hard-delete) d'une note manuelle. Opération irréversible.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Note supprimée définitivement"),
            @ApiResponse(responseCode = "404", description = "Note introuvable", content = @Content)
    })
    public ResponseEntity<Void> supprimerNote(
            @Parameter(description = "UUID public de la note", required = true) @PathVariable UUID trackingId) {
        noteService.supprimerNote(trackingId);
        return ResponseEntity.noContent().build();
    }
}
