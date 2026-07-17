package tg.edtch.activEducation.cahierdebord.application.controller;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.cahierdebord.domain.dto.EntreeJournalRequest;
import tg.edtch.activEducation.cahierdebord.domain.dto.EntreeJournalResponse;
import tg.edtch.activEducation.cahierdebord.domain.service.CahierBordService;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1/cahier-bord")
@PreAuthorize("isAuthenticated()")
public class CahierBordController {
    private final CahierBordService service;
    public CahierBordController(CahierBordService service) { this.service = service; }
    @GetMapping("/{eleveId}")
    public ResponseEntity<Page<EntreeJournalResponse>> lister(@PathVariable String eleveId, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) { return ResponseEntity.ok(service.listerEntrees(eleveId, page, size)); }
    @PostMapping("/{eleveId}")
    public ResponseEntity<EntreeJournalResponse> creer(@PathVariable String eleveId, @Valid @RequestBody EntreeJournalRequest req) { return ResponseEntity.status(HttpStatus.CREATED).body(service.creerEntree(eleveId, req)); }
    @PutMapping("/{trackingId}")
    public ResponseEntity<EntreeJournalResponse> modifier(@PathVariable UUID trackingId, @Valid @RequestBody EntreeJournalRequest req) { return ResponseEntity.ok(service.modifierEntree(trackingId, req)); }
    @DeleteMapping("/{trackingId}")
    public ResponseEntity<Void> supprimer(@PathVariable UUID trackingId) { service.supprimerEntree(trackingId); return ResponseEntity.noContent().build(); }
    @GetMapping("/{eleveId}/stats")
    public ResponseEntity<Map<String, Object>> stats(@PathVariable String eleveId) { return ResponseEntity.ok(service.getStats(eleveId)); }
}
