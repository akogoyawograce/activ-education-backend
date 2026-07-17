package tg.edtch.activEducation.calendrier.application.controller;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.calendrier.domain.entite.EvenementOrientation;
import tg.edtch.activEducation.calendrier.domain.service.CalendrierService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1/calendrier")
@PreAuthorize("hasRole('ADMIN')")
public class CalendrierController {
    private final CalendrierService service;
    public CalendrierController(CalendrierService service) { this.service = service; }
    @GetMapping("/a-venir") public ResponseEntity<List<EvenementOrientation>> getAVenir() { return ResponseEntity.ok(service.getAVenir()); }
    @PostMapping public ResponseEntity<EvenementOrientation> creer(@Valid @RequestBody EvenementOrientation e) { return ResponseEntity.ok(service.creer(e)); }
}
