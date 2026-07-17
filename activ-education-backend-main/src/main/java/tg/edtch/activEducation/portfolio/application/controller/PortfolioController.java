package tg.edtch.activEducation.portfolio.application.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.portfolio.domain.dto.AnalysePortfolioResponse;
import tg.edtch.activEducation.portfolio.domain.dto.CompetenceRequest;
import tg.edtch.activEducation.portfolio.domain.dto.CompetenceResponse;
import tg.edtch.activEducation.portfolio.domain.service.PortfolioService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {

    private final PortfolioService service;

    public PortfolioController(PortfolioService service) {
        this.service = service;
    }

    @GetMapping("/{eleveTrackingId}")
    @PreAuthorize("@security.isOwner(#eleveTrackingId) or @security.isOwnChild(#eleveTrackingId)")
    public ResponseEntity<List<CompetenceResponse>> listerCompetences(@PathVariable String eleveTrackingId) {
        return ResponseEntity.ok(service.listerCompetences(eleveTrackingId));
    }

    @PostMapping("/{eleveTrackingId}")
    @PreAuthorize("@security.isOwner(#eleveTrackingId) or @security.isOwnChild(#eleveTrackingId)")
    public ResponseEntity<CompetenceResponse> ajouterCompetence(
            @PathVariable String eleveTrackingId,
            @Valid @RequestBody CompetenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.ajouterCompetence(eleveTrackingId, request));
    }

    @PutMapping("/{eleveTrackingId}/{trackingId}")
    @PreAuthorize("@security.isOwner(#eleveTrackingId) or @security.isOwnChild(#eleveTrackingId)")
    public ResponseEntity<CompetenceResponse> modifierCompetence(
            @PathVariable String eleveTrackingId,
            @PathVariable UUID trackingId,
            @Valid @RequestBody CompetenceRequest request) {
        return ResponseEntity.ok(service.modifierCompetence(trackingId, request));
    }

    @DeleteMapping("/{eleveTrackingId}/{trackingId}")
    @PreAuthorize("@security.isOwner(#eleveTrackingId) or @security.isOwnChild(#eleveTrackingId)")
    public ResponseEntity<Void> supprimerCompetence(
            @PathVariable String eleveTrackingId,
            @PathVariable UUID trackingId) {
        service.supprimerCompetence(trackingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{eleveTrackingId}/analyse")
    @PreAuthorize("@security.isOwner(#eleveTrackingId) or @security.isOwnChild(#eleveTrackingId)")
    public ResponseEntity<AnalysePortfolioResponse> analyser(@PathVariable String eleveTrackingId) {
        return ResponseEntity.ok(service.analyser(eleveTrackingId));
    }
}
