package tg.edtch.activEducation.prediction.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tg.edtch.activEducation.prediction.application.dto.OrientationOutcomeSuiviResponse;
import tg.edtch.activEducation.prediction.application.dto.PredictionDatasetRow;
import tg.edtch.activEducation.prediction.application.service.OrientationOutcomeService;
import tg.edtch.activEducation.prediction.application.service.PredictionDatasetService;
import tg.edtch.activEducation.prediction.domain.entite.OrientationOutcome;

import java.time.LocalDate;
import java.util.List;

/**
 * Export du dataset d'entraînement supervisé pour le module Prédiction.
 *
 * <p>Endpoint admin (cf. brief Phase 2) : renvoie un CSV téléchargeable
 * contenant les outcomes fermés (ADMIS / RECALE) avec un identifiant
 * anonyme (SHA-256 tronqué).</p>
 */
@RestController
@RequestMapping("/api/v1/admin/prediction")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CONSEILLER','ADMIN','SUPER_ADMIN')")
@Tag(name = "Admin : Dataset Prédiction",
     description = "Export du dataset d'entraînement supervisé (CSV) et suivi des outcomes.")
public class PredictionDatasetController {

    private final PredictionDatasetService service;
    private final OrientationOutcomeService outcomeService;

    @Operation(summary = "Télécharger le dataset d'entraînement (CSV)")
    @GetMapping(value = "/dataset", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<String> telechargerDataset() {
        List<PredictionDatasetRow> rows = service.construireDataset();
        String csv = service.serialiserCsv(rows);

        String filename = "prediction-dataset-"
                + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @Operation(summary = "Suivi conseiller : liste paginée des outcomes (filtre statut optionnel)")
    @GetMapping("/orientation-outcomes")
    public ResponseEntity<Page<OrientationOutcomeSuiviResponse>> listerOutcomes(
            @RequestParam(name = "statut", required = false) String statut,
            @PageableDefault(size = 20, sort = "dateChoix",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        OrientationOutcome.StatutOrientation s = null;
        if (statut != null && !statut.isBlank()) {
            try {
                s = OrientationOutcome.StatutOrientation.valueOf(statut);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Statut invalide : " + statut
                        + " (attendu : EN_COURS, ADMIS, RECALE, ABANDON, REORIENTE)");
            }
        }
        return ResponseEntity.ok(outcomeService.listerSuivi(s, pageable));
    }
}
