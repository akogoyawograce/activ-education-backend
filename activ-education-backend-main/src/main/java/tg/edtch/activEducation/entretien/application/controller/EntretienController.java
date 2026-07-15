package tg.edtch.activEducation.entretien.application.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.entretien.domain.dto.*;
import tg.edtch.activEducation.entretien.domain.service.EntretienService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/entretien")
@PreAuthorize("isAuthenticated()")
public class EntretienController {

    private final EntretienService service;

    public EntretienController(EntretienService service) {
        this.service = service;
    }

    @PostMapping("/start")
    public ResponseEntity<EntretienResponse> demarrerEntretien(
            @Valid @RequestBody StartEntretienRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.demarrerEntretien(request));
    }

    @PostMapping("/{sessionId}/repondre")
    public ResponseEntity<EntretienResponse> repondre(
            @PathVariable UUID sessionId,
            @Valid @RequestBody RepondreRequest request) {
        return ResponseEntity.ok(service.repondre(sessionId, request.reponse()));
    }

    @GetMapping("/{sessionId}/resultat")
    public ResponseEntity<ResultatEntretienResponse> getResultat(
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(service.getResultat(sessionId));
    }
}
