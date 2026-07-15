package tg.edtch.activEducation.vae.application.controller;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.vae.domain.dto.DossierVAERequest;
import tg.edtch.activEducation.vae.domain.entite.DossierVAE;
import tg.edtch.activEducation.vae.domain.service.VAEService;
import java.util.List; import java.util.UUID;
@RestController @RequestMapping("/api/v1/vae")
@PreAuthorize("isAuthenticated()")
public class VAEController {
    private final VAEService service;
    public VAEController(VAEService service) { this.service = service; }
    @GetMapping("/{eleveId}") public ResponseEntity<List<DossierVAE>> getDossiers(@PathVariable String eleveId) { return ResponseEntity.ok(service.getDossiers(eleveId)); }
    @PostMapping public ResponseEntity<DossierVAE> creer(@Valid @RequestBody DossierVAERequest req) { return ResponseEntity.ok(service.creer(req)); }
    @PutMapping("/{trackingId}") public ResponseEntity<DossierVAE> mettreAJour(@PathVariable UUID trackingId, @RequestParam(required=false) String statut, @RequestParam(required=false) String conseillerId) { return ResponseEntity.ok(service.mettreAJour(trackingId, statut, conseillerId)); }
}
