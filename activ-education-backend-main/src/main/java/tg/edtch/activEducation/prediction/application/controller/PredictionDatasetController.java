package tg.edtch.activEducation.prediction.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.edtch.activEducation.prediction.application.dto.PredictionDatasetRow;
import tg.edtch.activEducation.prediction.application.service.PredictionDatasetService;

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
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@Tag(name = "Admin : Dataset Prédiction",
     description = "Export du dataset d'entraînement supervisé (CSV).")
public class PredictionDatasetController {

    private final PredictionDatasetService service;

    @Operation(summary = "Télécharger le dataset d'entraînement (CSV)")
    @GetMapping(value = "/dataset", produces = "text/csv")
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
}
