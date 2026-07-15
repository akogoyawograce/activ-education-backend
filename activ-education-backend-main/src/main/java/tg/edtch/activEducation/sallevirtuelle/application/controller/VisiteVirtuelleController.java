package tg.edtch.activEducation.sallevirtuelle.application.controller;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.sallevirtuelle.domain.entite.VisiteVirtuelle;
import tg.edtch.activEducation.sallevirtuelle.domain.service.VisiteVirtuelleService;
import java.util.List;
@RestController @RequestMapping("/api/v1/salle-virtuelle")
@PreAuthorize("isAuthenticated()")
public class VisiteVirtuelleController {
    private final VisiteVirtuelleService service;
    public VisiteVirtuelleController(VisiteVirtuelleService service) { this.service = service; }
    @GetMapping public ResponseEntity<List<VisiteVirtuelle>> getAll() { return ResponseEntity.ok(service.getAll()); }
    @PostMapping public ResponseEntity<VisiteVirtuelle> creer(@Valid @RequestBody VisiteVirtuelle v) { return ResponseEntity.ok(service.creer(v)); }
}
