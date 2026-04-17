package tg.edtch.activEducation.bibliotheque.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheEtablissementRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheEtablissementResponse;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheEtablissementService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bibliotheque/etablissements")
@RequiredArgsConstructor
@Tag(name = "Bibliothèque : Établissements", description = "API de gestion des fiches d'établissements")
public class FicheEtablissementController {

    private final FicheEtablissementService etablissementService;

    @PostMapping
    @Operation(summary = "Créer une nouvelle fiche établissement")
    public ResponseEntity<FicheEtablissementResponse> creer(@Valid @RequestBody FicheEtablissementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(etablissementService.creerEtablissement(request));
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Récupérer une fiche établissement par son trackingId")
    public ResponseEntity<FicheEtablissementResponse> get(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(etablissementService.getEtablissement(trackingId));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les fiches établissements (paginé)")
    public ResponseEntity<Page<FicheEtablissementResponse>> lister(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(etablissementService.listerTous(PageRequest.of(page, size)));
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier une fiche établissement existante")
    public ResponseEntity<FicheEtablissementResponse> modifier(
            @PathVariable UUID trackingId,
            @Valid @RequestBody FicheEtablissementRequest request) {
        return ResponseEntity.ok(etablissementService.modifierEtablissement(trackingId, request));
    }

    @DeleteMapping("/{trackingId}")
    @Operation(summary = "Supprimer une fiche établissement")
    public ResponseEntity<Void> supprimer(@PathVariable UUID trackingId) {
        etablissementService.supprimerEtablissement(trackingId);
        return ResponseEntity.noContent().build();
    }
}
