package tg.edtch.activEducation.shared.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.shared.ai.service.RecommandationIAService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/eleves/{trackingId}/recommandation-ia")
@RequiredArgsConstructor
@Tag(name = "Recommandation IA", description = "Recommandations personnalisées par IA OpenAI")
public class RecommandationIAController {

    private final RecommandationIAService recommandationIAService;

    @GetMapping
    @Operation(summary = "Générer une recommandation personnalisée par IA")
    @PreAuthorize("@security.isOwner(#trackingId) or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> generer(
            @PathVariable UUID trackingId) {
        String recommandation = recommandationIAService.genererRecommandation(trackingId);
        return ResponseEntity.ok(Map.of("recommandation", recommandation));
    }
}
