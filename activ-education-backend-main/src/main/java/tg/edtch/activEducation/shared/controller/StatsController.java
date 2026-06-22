package tg.edtch.activEducation.shared.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
@Tag(name = "Admin : Stats", description = "Indicateurs clés pour le dashboard backoffice")
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/kpi")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Indicateurs clés (total élèves, conseillers, quiz, résultats, établissements)")
    public ResponseEntity<Map<String, Long>> getKPIs() {
        return ResponseEntity.ok(statsService.getKPIs());
    }

    @GetMapping("/inscriptions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inscriptions par jour (X derniers jours)")
    public ResponseEntity<List<Map<String, Object>>> getInscriptions(
            @RequestParam(defaultValue = "30") int jours) {
        return ResponseEntity.ok(statsService.getInscriptionsParJour(jours));
    }

    @GetMapping("/quiz-completes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Quiz complétés par jour (X derniers jours)")
    public ResponseEntity<List<Map<String, Object>>> getQuizCompletes(
            @RequestParam(defaultValue = "30") int jours) {
        return ResponseEntity.ok(statsService.getQuizCompletesParJour(jours));
    }

    @GetMapping("/rdv")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "RDV par mois (X derniers mois)")
    public ResponseEntity<List<Map<String, Object>>> getRDV(
            @RequestParam(defaultValue = "12") int mois) {
        return ResponseEntity.ok(statsService.getRDVParsMois(mois));
    }

    @GetMapping("/quiz-par-domaine")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Répartition des quiz par domaine")
    public ResponseEntity<Map<String, Long>> getQuizParDomaine() {
        return ResponseEntity.ok(statsService.getQuizParDomaine());
    }

    @GetMapping("/type-apprenant")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Répartition des élèves par type d'apprenant (collégien, lycéen, etc.)")
    public ResponseEntity<Map<String, Long>> getTypeApprenant() {
        return ResponseEntity.ok(statsService.getTypeApprenantDistribution());
    }

    @GetMapping("/fiches")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Dernières fiches modifiées")
    public ResponseEntity<List<Map<String, Object>>> getFichesRecentes(
            @RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(statsService.getFichesModifieesRecentes(limite));
    }
}
