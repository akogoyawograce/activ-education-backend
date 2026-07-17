package tg.edtch.activEducation.horsligne.application.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.horsligne.domain.entite.SyncLog;
import tg.edtch.activEducation.horsligne.domain.service.HorsLigneService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1/hors-ligne")
@PreAuthorize("isAuthenticated()")
public class HorsLigneController {
    private final HorsLigneService service;
    public HorsLigneController(HorsLigneService service) { this.service = service; }
    @PostMapping("/sync/{eleveId}") public ResponseEntity<SyncLog> sync(@PathVariable String eleveId, @RequestParam(required=false) String typeDonnees, @RequestParam(required=false) Integer tailleKb) { return ResponseEntity.ok(service.enregistrerSync(eleveId, typeDonnees, tailleKb)); }
    @GetMapping("/sync/{eleveId}") public ResponseEntity<List<SyncLog>> getSyncLogs(@PathVariable String eleveId) { return ResponseEntity.ok(service.getSyncLogs(eleveId)); }
}
