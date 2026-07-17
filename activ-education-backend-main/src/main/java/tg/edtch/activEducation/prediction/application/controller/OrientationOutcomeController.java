package tg.edtch.activEducation.prediction.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.prediction.application.dto.OrientationOutcomeRequest;
import tg.edtch.activEducation.prediction.application.dto.OrientationOutcomeResponse;
import tg.edtch.activEducation.prediction.application.service.OrientationOutcomeService;

import java.util.List;
import java.util.UUID;

/**
 * Suivi des choix d'orientation des élèves.
 *
 * <p>Endpoints sobres : c'est l'input pour l'entraînement supervisé (Phase 5).
 * La mutation du statut est séparée sur l'admin/conseiller.</p>
 */
@RestController
@RequestMapping("/api/v1/eleves/{eleveTrackingId}/orientation-outcome")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Prédiction : Orientation Outcome",
     description = "Suivi des choix d'orientation d'un élève (input Phase 5).")
public class OrientationOutcomeController {

    private final OrientationOutcomeService service;

    @Operation(summary = "Créer ou mettre à jour un suivi de choix d'orientation")
    @PostMapping
    @PreAuthorize("@security.isOwner(#eleveTrackingId) or @security.isOwnChild(#eleveTrackingId) "
                + "or @security.isOwnConseiller(#eleveTrackingId) or hasRole('ADMIN')")
    public ResponseEntity<OrientationOutcomeResponse> creerOuMaj(
            @PathVariable UUID eleveTrackingId,
            @Valid @RequestBody OrientationOutcomeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.creerOuMettreAJour(eleveTrackingId, request));
    }

    @Operation(summary = "Lister les choix d'orientation d'un élève")
    @GetMapping
    @PreAuthorize("@security.isOwner(#eleveTrackingId) or @security.isOwnChild(#eleveTrackingId) "
                + "or @security.isOwnConseiller(#eleveTrackingId) or hasRole('ADMIN')")
    public List<OrientationOutcomeResponse> lister(@PathVariable UUID eleveTrackingId) {
        return service.listerParEleve(eleveTrackingId);
    }

    @Operation(summary = "Mettre à jour le statut d'un outcome (ADMIS / RECALE / ...)")
    @PatchMapping("/{outcomeTrackingId}")
    @PreAuthorize("hasAnyRole('CONSEILLER','ADMIN','SUPER_ADMIN')")
    public OrientationOutcomeResponse majStatut(
            @PathVariable UUID eleveTrackingId,
            @PathVariable UUID outcomeTrackingId,
            @RequestParam(name = "statut", required = false) String statut,
            @RequestParam(name = "satisfaction", required = false) Integer satisfaction,
            @RequestParam(name = "commentaire", required = false) String commentaire) {
        return service.mettreAJourStatut(outcomeTrackingId, statut, satisfaction, commentaire);
    }
}
