package tg.edtch.activEducation.cvgenerateur.application.controller;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.cvgenerateur.domain.dto.CVGenereRequest;
import tg.edtch.activEducation.cvgenerateur.domain.entite.CVGenere;
import tg.edtch.activEducation.cvgenerateur.domain.service.CVGenereService;
import java.util.List; import java.util.UUID;
@RestController @RequestMapping("/api/v1/cv")
@PreAuthorize("isAuthenticated()")
public class CVGenereController {
    private final CVGenereService service;
    public CVGenereController(CVGenereService service) { this.service = service; }
    @GetMapping("/{eleveId}") public ResponseEntity<List<CVGenere>> getCVs(@PathVariable String eleveId) { return ResponseEntity.ok(service.getCVs(eleveId)); }
    @PostMapping("/{eleveId}") public ResponseEntity<CVGenere> creer(@PathVariable String eleveId, @Valid @RequestBody CVGenereRequest req) { return ResponseEntity.ok(service.creer(eleveId, req)); }
    @PutMapping("/{trackingId}") public ResponseEntity<CVGenere> modifier(@PathVariable UUID trackingId, @Valid @RequestBody CVGenereRequest req) { return ResponseEntity.ok(service.modifier(trackingId, req)); }
    @DeleteMapping("/{trackingId}") public ResponseEntity<Void> supprimer(@PathVariable UUID trackingId) { service.supprimer(trackingId); return ResponseEntity.noContent().build(); }
}
