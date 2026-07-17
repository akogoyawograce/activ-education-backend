package tg.edtch.activEducation.mentorat.application.controller;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.alumni.domain.entite.Mentorat;
import tg.edtch.activEducation.mentorat.domain.dto.MentoratRequest;
import tg.edtch.activEducation.mentorat.domain.service.MentoratService;
import java.util.List; import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1/mentorat")
@PreAuthorize("isAuthenticated()")
public class MentoratController {
    private final MentoratService service;
    public MentoratController(MentoratService service) { this.service = service; }
    @GetMapping("/{personneId}") public ResponseEntity<List<Mentorat>> getMentorats(@PathVariable String personneId) { return ResponseEntity.ok(service.getMentorats(personneId)); }
    @PostMapping public ResponseEntity<Mentorat> creer(@Valid @RequestBody MentoratRequest req) { return ResponseEntity.ok(service.creer(req)); }
    @PutMapping("/{trackingId}") public ResponseEntity<Mentorat> mettreAJour(@PathVariable UUID trackingId, @RequestParam(required=false) String statut, @RequestParam(required=false) Integer nbSeances) { return ResponseEntity.ok(service.mettreAJour(trackingId, statut, nbSeances)); }
}
