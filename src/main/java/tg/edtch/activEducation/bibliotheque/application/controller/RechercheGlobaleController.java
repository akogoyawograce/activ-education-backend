package tg.edtch.activEducation.bibliotheque.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tg.edtch.activEducation.bibliotheque.application.dto.response.RechercheGlobaleResponse;
import tg.edtch.activEducation.bibliotheque.domain.service.RechercheGlobaleService;

import java.util.List;

/**
 * Controller exposant l'endpoint de recherche sémantique globale sur toutes les
 * fiches.
 * Utilise les embeddings Gemini et pgvector pour une recherche en langage
 * naturel.
 */
@RestController
@RequestMapping("/api/v1/bibliotheque/recherche-fiche-ia")
@RequiredArgsConstructor
@Tag(name = "Recherche Globale IA", description = "Recherche sémantique sur toutes les fiches de la bibliothèque")
public class RechercheGlobaleController {

    private final RechercheGlobaleService rechercheGlobaleService;

    @GetMapping("/globale")
    @Operation(summary = "Recherche sémantique globale via l'IA Gemini", description = "Recherche parmi toutes les fiches (Métiers, Filières, Établissements, Séries) "
            +
            "via une phrase en langage naturel. Utilise l'embedding Gemini + pgvector.")
    public ResponseEntity<List<RechercheGlobaleResponse>> rechercherGlobalement(
            @Parameter(description = "Phrase de recherche en langage naturel", required = true) @RequestParam String phrase,
            @Parameter(description = "Nombre maximum de résultats (1-20)") @RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(rechercheGlobaleService.rechercherFichesParPhrase(phrase, limite));
    }
}
