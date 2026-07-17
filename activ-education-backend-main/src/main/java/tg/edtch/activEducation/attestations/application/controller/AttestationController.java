package tg.edtch.activEducation.attestations.application.controller;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.attestations.domain.dto.AttestationRequest;
import tg.edtch.activEducation.attestations.domain.entite.Attestation;
import tg.edtch.activEducation.attestations.domain.service.AttestationService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1/attestations")
@PreAuthorize("hasRole('ADMIN')")
public class AttestationController {
    private final AttestationService service;
    public AttestationController(AttestationService service) { this.service = service; }
    @PostMapping public ResponseEntity<Attestation> creer(@Valid @RequestBody AttestationRequest req) { return ResponseEntity.ok(service.creer(req)); }
    @GetMapping("/verifier/{code}") public ResponseEntity<Attestation> verifier(@PathVariable String code) { return ResponseEntity.ok(service.getByCode(code)); }
    @GetMapping("/eleve/{eleveId}") public ResponseEntity<List<Attestation>> getByEleve(@PathVariable String eleveId) { return ResponseEntity.ok(service.getByEleve(eleveId)); }
}
