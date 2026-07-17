package tg.edtch.activEducation.badge.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.badge.domain.dto.BadgeResponse;
import tg.edtch.activEducation.badge.domain.service.BadgeService;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/badges")
@PreAuthorize("isAuthenticated()")
public class BadgeController {

    private final BadgeService service;

    public BadgeController(BadgeService service) {
        this.service = service;
    }

    @GetMapping("/{eleveTrackingId}")
    public ResponseEntity<List<BadgeResponse>> getBadges(@PathVariable String eleveTrackingId) {
        return ResponseEntity.ok(service.getBadgesEleve(eleveTrackingId));
    }

    @GetMapping("/{eleveTrackingId}/total")
    public ResponseEntity<Integer> getTotal(@PathVariable String eleveTrackingId) {
        return ResponseEntity.ok(service.getTotalBadges(eleveTrackingId));
    }

    @PostMapping("/{eleveTrackingId}/verifier")
    public ResponseEntity<List<BadgeResponse>> verifierEtAttribuer(@PathVariable String eleveTrackingId) {
        var nouveaux = service.verifierEtAttribuer(eleveTrackingId);
        return ResponseEntity.ok(nouveaux);
    }
}
