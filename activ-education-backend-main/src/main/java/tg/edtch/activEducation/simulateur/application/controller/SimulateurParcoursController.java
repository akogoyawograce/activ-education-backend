package tg.edtch.activEducation.simulateur.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.simulateur.domain.dto.ScenarioRequest;
import tg.edtch.activEducation.simulateur.domain.dto.ScenarioResult;
import tg.edtch.activEducation.simulateur.domain.service.SimulateurParcoursService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/simulateur")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Simulateur de parcours", description = "Explorer et comparer des scénarios d'orientation (What-If)")
public class SimulateurParcoursController {

    private final SimulateurParcoursService simulateurService;

    @PostMapping("/explorer")
    @Operation(summary = "Explorer un scénario d'orientation")
    public ResponseEntity<ScenarioResult> explorer(@RequestBody ScenarioRequest request) {
        ScenarioResult result = simulateurService.explorer(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/comparer")
    @Operation(summary = "Comparer plusieurs scénarios d'orientation")
    public ResponseEntity<List<ScenarioResult>> comparer(@RequestBody List<ScenarioRequest> scenarios) {
        if (scenarios.size() < 2 || scenarios.size() > 4) {
            return ResponseEntity.badRequest().build();
        }
        List<ScenarioResult> resultats = simulateurService.comparer(scenarios);
        return ResponseEntity.ok(resultats);
    }
}
