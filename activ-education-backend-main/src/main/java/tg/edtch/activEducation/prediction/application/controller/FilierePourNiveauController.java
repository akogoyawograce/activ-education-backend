package tg.edtch.activEducation.prediction.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tg.edtch.activEducation.prediction.application.dto.FilierePourNiveauResponse;
import tg.edtch.activEducation.prediction.application.service.PredictionLookupService;

import java.util.List;

/**
 * Filtrage des fiches Filière par niveau scolaire.
 *
 * <p>Endpoint raccourci {@code /api/v1/filieres?niveau=...} (vs. l'endpoint
 * CRUD complet {@code /api/v1/bibliotheque/filieres}) — usage mobile pour
 * l'écran "sélection de la filière selon ton niveau".</p>
 */
@RestController
@RequestMapping("/api/v1/filieres")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Prédiction : Filières par niveau",
     description = "Liste des filières éligibles pour un niveau donné.")
public class FilierePourNiveauController {

    private final PredictionLookupService predictionLookupService;

    @Operation(summary = "Lister les filières éligibles pour un niveau")
    @GetMapping
    public List<FilierePourNiveauResponse> parNiveau(
            @Parameter(description = "Niveau canonique (LYCEE_TLE, BAC_1, ...) "
                                  + "ou libellé lisible (Terminale, Licence 2).",
                       example = "LYCEE_TLE")
            @RequestParam(name = "niveau", required = false) String niveau) {
        return predictionLookupService.filieresPourNiveau(niveau);
    }
}
