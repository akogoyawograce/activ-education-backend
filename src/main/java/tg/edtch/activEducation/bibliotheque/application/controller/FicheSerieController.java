package tg.edtch.activEducation.bibliotheque.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheSerieRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheSerieResponse;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheSerieService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bibliotheque/series")
@RequiredArgsConstructor
@Tag(name = "Bibliothèque : Séries", description = "API de gestion des fiches de séries d'études")
public class FicheSerieController {

    private final FicheSerieService serieService;

    @PostMapping
    @Operation(summary = "Créer une nouvelle fiche série")
    public ResponseEntity<FicheSerieResponse> creer(@Valid @RequestBody FicheSerieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serieService.creerSerie(request));
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Récupérer une fiche série par son trackingId")
    public ResponseEntity<FicheSerieResponse> get(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(serieService.getSerie(trackingId));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les fiches séries (paginé)")
    public ResponseEntity<Page<FicheSerieResponse>> lister(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity
                .ok(serieService.listerToutes(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/public")
    @Operation(summary = "Lister les fiches séries publiques (paginé)")
    public ResponseEntity<Page<FicheSerieResponse>> listerPublies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity
                .ok(serieService.listerPublies(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/non-public")
    @Operation(summary = "Lister les fiches séries non publiques (paginé)")
    public ResponseEntity<Page<FicheSerieResponse>> listerNonPublies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity
                .ok(serieService.listerNonPublies(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier une fiche série existante")
    public ResponseEntity<FicheSerieResponse> modifier(
            @PathVariable UUID trackingId,
            @Valid @RequestBody FicheSerieRequest request) {
        return ResponseEntity.ok(serieService.modifierSerie(trackingId, request));
    }

    @DeleteMapping("/{trackingId}")
    @Operation(summary = "Supprimer une fiche série")
    public ResponseEntity<Void> supprimer(@PathVariable UUID trackingId) {
        serieService.supprimerSerie(trackingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recherche")
    @Operation(summary = "Rechercher des séries par mot-clé")
    public ResponseEntity<Page<FicheSerieResponse>> rechercher(
            @RequestParam String motCle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                serieService.rechercher(motCle, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }
}
