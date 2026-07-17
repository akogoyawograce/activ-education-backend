package tg.edtch.activEducation.profil.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.profil.application.dto.request.NotesHistoriqueRequest;
import tg.edtch.activEducation.profil.application.dto.response.NotesHistoriqueResponse;
import tg.edtch.activEducation.profil.domain.service.NotesHistoriqueService;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST pour l'historique de notes (3 ans glissants).
 *
 * <p>Endpoints conservés volontairement légers : le gros du moteur de
 * trajectoire est dans {@code prediction/} (Phase 3). On expose juste le
 * CRUD de base + 2 lectures utiles.</p>
 */
@RestController
@RequestMapping("/api/v1/eleves/{eleveTrackingId}/notes-historique")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Notes Historique",
     description = "Historique des moyennes annuelles (3 ans) — source du moteur de trajectoire.")
public class NotesHistoriqueController {

    private final NotesHistoriqueService service;

    @Operation(summary = "Ajouter une ligne d'historique de notes")
    @PostMapping
    public ResponseEntity<NotesHistoriqueResponse> ajouter(
            @PathVariable UUID eleveTrackingId,
            @Valid @RequestBody NotesHistoriqueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.ajouter(eleveTrackingId, request));
    }

    @Operation(summary = "Lister l'historique complet d'un élève (toutes matières)")
    @GetMapping
    public List<NotesHistoriqueResponse> lister(@PathVariable UUID eleveTrackingId) {
        return service.listerParEleve(eleveTrackingId);
    }

    @Operation(summary = "Lister uniquement les moyennes générales (utile au moteur)")
    @GetMapping("/moyennes-generales")
    public List<NotesHistoriqueResponse> moyennesGenerales(@PathVariable UUID eleveTrackingId) {
        return service.listerMoyennesGenerales(eleveTrackingId);
    }

    @Operation(summary = "Détail d'une ligne par son trackingId")
    @GetMapping("/{trackingId}")
    public NotesHistoriqueResponse get(@PathVariable UUID eleveTrackingId,
                                       @PathVariable UUID trackingId) {
        return service.get(trackingId);
    }

    @Operation(summary = "Supprimer une ligne d'historique")
    @DeleteMapping("/{trackingId}")
    @PreAuthorize("@security.isOwner(#eleveTrackingId) or @security.isOwnChild(#eleveTrackingId) "
                + "or @security.isOwnConseiller(#eleveTrackingId) or hasRole('ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable UUID eleveTrackingId,
                                          @PathVariable UUID trackingId) {
        service.supprimer(trackingId);
        return ResponseEntity.noContent().build();
    }
}
