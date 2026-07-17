package tg.edtch.activEducation.shared.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.prediction.application.dto.Recommandation3SignauxResponse;
import tg.edtch.activEducation.prediction.application.service.Recommandation3SignauxService;
import tg.edtch.activEducation.shared.ai.service.RecommandationIAService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/eleves/{trackingId}/recommandation-ia")
@RequiredArgsConstructor
@Tag(name = "Recommandation IA", description = "Recommandations personnalisées par IA OpenAI")
public class RecommandationIAController {

    private final RecommandationIAService recommandationIAService;
    private final Recommandation3SignauxService recommandation3SignauxService;

    @GetMapping
    @Operation(summary = "v1 : Générer une recommandation textuelle par IA (OpenAI/Groq/Ollama)")
    @PreAuthorize("@security.isOwner(#trackingId) or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> generer(
            @PathVariable UUID trackingId) {
        String recommandation = recommandationIAService.genererRecommandation(trackingId);
        return ResponseEntity.ok(Map.of("recommandation", recommandation));
    }

    /**
     * v2 (Phase 3 du module Prédiction) : moteur algorithmique 3 signaux
     * structuré (aspiration + réalité + engagement), déterministe, et
     * accompagné de ses sous-scores pour affichage explicatif côté mobile.
     *
     * <p>Cohabite avec le v1 — le client choisit l'un ou l'autre selon
     * son besoin (texte LLM vs score structuré). Aucune régression sur
     * l'existant.</p>
     */
    @GetMapping("/v2")
    @Operation(summary = "v2 : Top N de recommandations par moteur 3 signaux (structuré)")
    @PreAuthorize("@security.isOwner(#trackingId) or hasRole('ADMIN')")
    public Recommandation3SignauxResponse genererV2(
            @PathVariable UUID trackingId) {
        return recommandation3SignauxService.recommander(trackingId);
    }
}
