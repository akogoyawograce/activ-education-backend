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
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheFiliereRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheFiliereResponse;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheFiliereService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bibliotheque/filieres")
@RequiredArgsConstructor
@Tag(name = "Bibliothèque : Filières", description = "API de gestion des fiches de filières d'études")
public class FicheFiliereController {

    private final FicheFiliereService filiereService;

    @PostMapping
    @Operation(summary = "Créer une nouvelle fiche filière")
    public ResponseEntity<FicheFiliereResponse> creer(@Valid @RequestBody FicheFiliereRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(filiereService.creerFiliere(request));
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Récupérer une fiche filière par son trackingId")
    public ResponseEntity<FicheFiliereResponse> get(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(filiereService.getFiliere(trackingId));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les fiches filières (paginé)")
    public ResponseEntity<Page<FicheFiliereResponse>> lister(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity
                .ok(filiereService
                    .listerToutes(PageRequest.of(page, size)
                )
                );
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier une fiche filière existante")
    public ResponseEntity<FicheFiliereResponse> modifier(
            @PathVariable UUID trackingId,
            @Valid @RequestBody FicheFiliereRequest request) {
        return ResponseEntity.ok(filiereService.modifierFiliere(trackingId, request));
    }

    @DeleteMapping("/{trackingId}")
    @Operation(summary = "Supprimer une fiche filière")
    public ResponseEntity<Void> supprimer(@PathVariable UUID trackingId) {
        filiereService.supprimerFiliere(trackingId);
        return ResponseEntity.noContent().build();
    }
}
