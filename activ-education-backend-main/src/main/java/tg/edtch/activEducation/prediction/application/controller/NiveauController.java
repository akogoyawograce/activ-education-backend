package tg.edtch.activEducation.prediction.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.edtch.activEducation.prediction.application.dto.NiveauResponse;
import tg.edtch.activEducation.prediction.application.service.PredictionLookupService;

import java.util.List;

/**
 * Liste des niveaux scolaires disponibles — utilisée par l'écran
 * "sélection du niveau actuel" côté mobile (Phase 4).
 */
@RestController
@RequestMapping("/api/v1/niveaux")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Prédiction : Niveaux",
     description = "Liste des 7 niveaux canoniques gérés par la plateforme.")
public class NiveauController {

    private final PredictionLookupService predictionLookupService;

    @Operation(summary = "Lister tous les niveaux disponibles")
    @GetMapping
    public List<NiveauResponse> lister() {
        return predictionLookupService.listerNiveaux();
    }
}
