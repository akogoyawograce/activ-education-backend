package tg.edtch.activEducation.riasec.application.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.riasec.domain.dto.RIASECResultatResponse;
import tg.edtch.activEducation.riasec.domain.service.RIASECService;
import java.util.List;
@RestController @RequestMapping("/api/v1/riasec")
@PreAuthorize("isAuthenticated()")
public class RIASECController {
    private final RIASECService service;
    public RIASECController(RIASECService service) { this.service = service; }
    @PostMapping("/{eleveId}/passer")
    public ResponseEntity<RIASECResultatResponse> passer(@PathVariable String eleveId, @RequestBody String reponses) throws Exception {
        return ResponseEntity.ok(service.passerTest(eleveId, reponses));
    }
    @GetMapping("/{eleveId}/resultats")
    public ResponseEntity<List<RIASECResultatResponse>> resultats(@PathVariable String eleveId) {
        return ResponseEntity.ok(service.getResultats(eleveId));
    }
}
