package tg.edtch.activEducation.parrainage.application.controller;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.parrainage.domain.dto.ParrainageRequest;
import tg.edtch.activEducation.parrainage.domain.entite.Parrainage;
import tg.edtch.activEducation.parrainage.domain.service.ParrainageService;
import java.util.List; import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1/parrainage")
@PreAuthorize("hasRole('ADMIN')")
public class ParrainageController {
    private final ParrainageService service;
    public ParrainageController(ParrainageService service) { this.service = service; }
    @PostMapping public ResponseEntity<Parrainage> creer(@Valid @RequestBody ParrainageRequest req) { return ResponseEntity.ok(service.creer(req)); }
    @GetMapping("/{parrainId}") public ResponseEntity<List<Parrainage>> getParrainages(@PathVariable String parrainId) { return ResponseEntity.ok(service.getParrainages(parrainId)); }
    @PutMapping("/{trackingId}") public ResponseEntity<Parrainage> mettreAJour(@PathVariable UUID trackingId, @RequestParam String statut) { return ResponseEntity.ok(service.mettreAJour(trackingId, statut)); }
}
