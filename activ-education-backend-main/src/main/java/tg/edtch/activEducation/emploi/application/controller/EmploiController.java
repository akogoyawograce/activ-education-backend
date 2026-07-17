package tg.edtch.activEducation.emploi.application.controller;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.emploi.domain.entite.OffreEmploi;
import tg.edtch.activEducation.emploi.domain.entite.Candidature;
import tg.edtch.activEducation.emploi.domain.service.EmploiService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1/emploi")
@PreAuthorize("hasRole('ADMIN')")
public class EmploiController {
    private final EmploiService service;
    public EmploiController(EmploiService service) { this.service = service; }
    @GetMapping("/offres") public ResponseEntity<List<OffreEmploi>> getOffres() { return ResponseEntity.ok(service.getOffres()); }
    @PostMapping("/offres") public ResponseEntity<OffreEmploi> creerOffre(@Valid @RequestBody OffreEmploi req) { return ResponseEntity.status(HttpStatus.CREATED).body(service.creerOffre(req)); }
    @PostMapping("/offres/{offreId}/postuler") public ResponseEntity<Candidature> postuler(@PathVariable String offreId, @RequestParam String eleveId, @RequestParam(required=false) String message) { return ResponseEntity.ok(service.postuler(offreId, eleveId, message)); }
    @GetMapping("/candidatures/{eleveId}") public ResponseEntity<List<Candidature>> getCandidatures(@PathVariable String eleveId) { return ResponseEntity.ok(service.getCandidatures(eleveId)); }
}
