package tg.edtch.activEducation.bibliotheque.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheMetierRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheMetierResponse;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheMetierService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bibliotheque/metiers")
@RequiredArgsConstructor
@Tag(name = "Bibliothèque : Métiers", description = "API de gestion des fiches de métiers")
public class FicheMetierController {

    private final FicheMetierService metierService;

    @PostMapping
    @Operation(summary = "Créer une nouvelle fiche métier")
    public ResponseEntity<FicheMetierResponse> creer(@Valid @RequestBody FicheMetierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metierService.creerMetier(request));
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Récupérer une fiche métier par son trackingId")
    public ResponseEntity<FicheMetierResponse> get(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(metierService.getMetier(trackingId));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les fiches métiers (paginé)")
    public ResponseEntity<Page<FicheMetierResponse>> lister(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(metierService.listerTous(PageRequest.of(page, size)));
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier une fiche métier existante")
    public ResponseEntity<FicheMetierResponse> modifier(
            @PathVariable UUID trackingId,
            @Valid @RequestBody FicheMetierRequest request) {
        return ResponseEntity.ok(metierService.modifierMetier(trackingId, request));
    }

    @DeleteMapping("/{trackingId}")
    @Operation(summary = "Supprimer une fiche métier")
    public ResponseEntity<Void> supprimer(@PathVariable UUID trackingId) {
        metierService.supprimerMetier(trackingId);
        return ResponseEntity.noContent().build();
    }
}
