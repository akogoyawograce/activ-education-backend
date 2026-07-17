package tg.edtch.activEducation.recommandation.application.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.recommandation.domain.entite.RecommandationGlobale;
import tg.edtch.activEducation.recommandation.domain.service.RecommandationGlobaleService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1/recommandations-globales")
@PreAuthorize("hasRole('ADMIN')")
public class RecommandationGlobaleController {
    private final RecommandationGlobaleService service;
    public RecommandationGlobaleController(RecommandationGlobaleService service) { this.service = service; }
    @GetMapping("/{eleveId}") public ResponseEntity<List<RecommandationGlobale>> getRecommandations(@PathVariable String eleveId) { return ResponseEntity.ok(service.getRecommandations(eleveId)); }
    @PostMapping public ResponseEntity<RecommandationGlobale> creer(@RequestBody RecommandationGlobale r) { return ResponseEntity.ok(service.creer(r)); }
}
