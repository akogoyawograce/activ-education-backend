package tg.edtch.activEducation.reorientation.application.controller;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.reorientation.domain.dto.DemandeReorientationRequest;
import tg.edtch.activEducation.reorientation.domain.entite.DemandeReorientation;
import tg.edtch.activEducation.reorientation.domain.service.ReorientationService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1/reorientation")
@PreAuthorize("isAuthenticated()")
public class ReorientationController {
    private final ReorientationService service;
    public ReorientationController(ReorientationService service) { this.service = service; }
    @PostMapping("/{eleveId}") public ResponseEntity<DemandeReorientation> soumettre(@PathVariable String eleveId, @Valid @RequestBody DemandeReorientationRequest req) { return ResponseEntity.ok(service.soumettre(eleveId, req)); }
    @PutMapping("/{trackingId}/traiter") public ResponseEntity<DemandeReorientation> traiter(@PathVariable UUID trackingId, @RequestParam String conseillerId, @RequestParam String statut, @RequestParam(required=false) String commentaire) { return ResponseEntity.ok(service.traiter(trackingId, conseillerId, statut, commentaire)); }
    @GetMapping("/{eleveId}") public ResponseEntity<List<DemandeReorientation>> getDemandes(@PathVariable String eleveId) { return ResponseEntity.ok(service.getDemandes(eleveId)); }
}
