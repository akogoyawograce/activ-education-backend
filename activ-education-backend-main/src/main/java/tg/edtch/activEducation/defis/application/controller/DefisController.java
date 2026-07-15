package tg.edtch.activEducation.defis.application.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.defis.domain.entite.DefiOrientation;
import tg.edtch.activEducation.defis.domain.entite.DefiReleve;
import tg.edtch.activEducation.defis.domain.service.DefisService;
import java.util.List;
@RestController @RequestMapping("/api/v1/defis")
@PreAuthorize("isAuthenticated()")
public class DefisController {
    private final DefisService service;
    public DefisController(DefisService service) { this.service = service; }
    @GetMapping public ResponseEntity<List<DefiOrientation>> getDefis() { return ResponseEntity.ok(service.getDefis()); }
    @PostMapping("/{eleveId}/relever") public ResponseEntity<DefiReleve> relever(@PathVariable String eleveId, @RequestParam String defiCode) { return ResponseEntity.ok(service.relever(eleveId, defiCode)); }
    @GetMapping("/{eleveId}/progression") public ResponseEntity<List<DefiReleve>> getProgression(@PathVariable String eleveId) { return ResponseEntity.ok(service.getProgression(eleveId)); }
}
