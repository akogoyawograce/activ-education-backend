package tg.edtch.activEducation.temoignage.application.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.temoignage.domain.dto.TemoignageRequest;
import tg.edtch.activEducation.temoignage.domain.dto.TemoignageResponse;
import tg.edtch.activEducation.temoignage.domain.service.TemoignageService;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/temoignages")
@PreAuthorize("isAuthenticated()")
public class TemoignageController {

    private final TemoignageService service;

    public TemoignageController(TemoignageService service) {
        this.service = service;
    }

    @GetMapping("/publies")
    public ResponseEntity<Page<TemoignageResponse>> getPublies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getTemoignagesPublies(page, size));
    }

    @GetMapping("/vedettes")
    public ResponseEntity<List<TemoignageResponse>> getVedettes() {
        return ResponseEntity.ok(service.getTemoignagesEnVedette());
    }

    @GetMapping("/metier/{metierTrackingId}")
    public ResponseEntity<Page<TemoignageResponse>> getParMetier(
            @PathVariable String metierTrackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getTemoignagesParMetier(metierTrackingId, page, size));
    }

    @GetMapping("/filiere/{filiereTrackingId}")
    public ResponseEntity<Page<TemoignageResponse>> getParFiliere(
            @PathVariable String filiereTrackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getTemoignagesParFiliere(filiereTrackingId, page, size));
    }

    @GetMapping("/{trackingId}")
    public ResponseEntity<TemoignageResponse> getTemoignage(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(service.getTemoignage(trackingId));
    }

    @GetMapping
    public ResponseEntity<Page<TemoignageResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getAllTemoignages(page, size));
    }

    @PostMapping
    public ResponseEntity<TemoignageResponse> creer(@Valid @RequestBody TemoignageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(request));
    }

    @PutMapping("/{trackingId}")
    public ResponseEntity<TemoignageResponse> modifier(
            @PathVariable UUID trackingId,
            @Valid @RequestBody TemoignageRequest request) {
        return ResponseEntity.ok(service.modifier(trackingId, request));
    }

    @DeleteMapping("/{trackingId}")
    public ResponseEntity<Void> supprimer(@PathVariable UUID trackingId) {
        service.supprimer(trackingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/compter")
    public ResponseEntity<Long> compter() {
        return ResponseEntity.ok(service.compterTemoignages());
    }
}
