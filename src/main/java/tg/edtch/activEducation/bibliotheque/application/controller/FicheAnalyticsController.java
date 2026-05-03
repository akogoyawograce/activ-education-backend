package tg.edtch.activEducation.bibliotheque.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.bibliotheque.application.dto.response.RechercheGlobaleResponse;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheAnalyticsService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bibliotheque/analytics")
@RequiredArgsConstructor
@Tag(name = "Bibliothèque : Analytics", description = "API pour les statistiques de consultation des fiches")
public class FicheAnalyticsController {

    private final FicheAnalyticsService analyticsService;

    @GetMapping("/tendances")
    @Operation(summary = "Récupérer la liste des fiches les plus consultées sur les 7 derniers jours")
    public ResponseEntity<List<RechercheGlobaleResponse>> getTendances(
            @RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(analyticsService.getTendances(limite));
    }

    @GetMapping("/recentes/{utilisateurTrackingId}")
    @Operation(summary = "Récupérer la liste des fiches récemment consultées par un utilisateur")
    public ResponseEntity<List<RechercheGlobaleResponse>> getConsultationsRecentes(
            @PathVariable UUID utilisateurTrackingId,
            @RequestParam(defaultValue = "5") int limite) {
        return ResponseEntity.ok(analyticsService.getConsultationsRecentes(utilisateurTrackingId, limite));
    }
}
