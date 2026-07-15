package tg.edtch.activEducation.alumni.application.controller;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.alumni.domain.dto.AlumniRequest;
import tg.edtch.activEducation.alumni.domain.entite.Alumni;
import tg.edtch.activEducation.alumni.domain.service.AlumniService;
import java.util.List; import java.util.UUID;
@RestController @RequestMapping("/api/v1/alumni")
@PreAuthorize("isAuthenticated()")
public class AlumniController {
    private final AlumniService service;
    public AlumniController(AlumniService service) { this.service = service; }
    @GetMapping public ResponseEntity<List<Alumni>> getAll() { return ResponseEntity.ok(service.getAll()); }
    @GetMapping("/mentors") public ResponseEntity<List<Alumni>> getMentors() { return ResponseEntity.ok(service.getMentors()); }
    @PostMapping public ResponseEntity<Alumni> creer(@Valid @RequestBody AlumniRequest req) { return ResponseEntity.ok(service.creer(req)); }
    @PutMapping("/{trackingId}") public ResponseEntity<Alumni> modifier(@PathVariable UUID trackingId, @Valid @RequestBody AlumniRequest req) { return ResponseEntity.ok(service.modifier(trackingId, req)); }
}
