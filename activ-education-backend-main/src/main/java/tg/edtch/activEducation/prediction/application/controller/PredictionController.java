package tg.edtch.activEducation.prediction.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.prediction.application.service.PredictionService;
import tg.edtch.activEducation.prediction.domain.dto.PredictionReussiteRequest;
import tg.edtch.activEducation.prediction.domain.dto.PredictionReussiteResponse;

import java.util.List;
import java.util.UUID;

/**
 * Historique des prédictions de réussite d'un élève.
 *
 * <p>Une prédiction est un <strong>snapshot</strong> d'une évaluation émise
 * par le moteur 3 signaux (Phase 3) ou par le modèle supervisé (Phase 5).
 * Elle est persistée pour pouvoir, à terme, comparer la prédiction au
 * résultat réel (cf. {@code orientation_outcome}).</p>
 *
 * <p>Sécurité : un élève ne voit/écrit que ses propres prédictions (ou
 * celles de ses enfants pour un parent, ou de ses élèves pour un
 * conseiller). L'admin passe-partout.</p>
 */
@RestController
@RequestMapping("/api/v1/eleves/{eleveTrackingId}/predictions")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Prédiction : Historique",
     description = "Historique des prédictions de réussite émises pour un élève (input Phase 5).")
public class PredictionController {

    private final PredictionService service;

    @Operation(summary = "Lister les prédictions d'un élève (de la plus récente à la plus ancienne)")
    @GetMapping
    @PreAuthorize("@security.isOwner(#eleveTrackingId) or @security.isOwnChild(#eleveTrackingId) "
                + "or @security.isOwnConseiller(#eleveTrackingId) or hasRole('ADMIN')")
    public List<PredictionReussiteResponse> lister(@PathVariable UUID eleveTrackingId) {
        return service.listerParEleve(eleveTrackingId);
    }

    @Operation(summary = "Enregistrer une nouvelle prédiction pour un élève")
    @PostMapping
    @PreAuthorize("@security.isOwner(#eleveTrackingId) or @security.isOwnChild(#eleveTrackingId) "
                + "or @security.isOwnConseiller(#eleveTrackingId) or hasRole('ADMIN')")
    public ResponseEntity<PredictionReussiteResponse> creer(
            @PathVariable UUID eleveTrackingId,
            @Valid @RequestBody PredictionReussiteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.creer(eleveTrackingId, request));
    }
}
