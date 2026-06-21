package tg.edtch.activEducation.bibliotheque.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.bibliotheque.domain.service.RechercheOrphelineService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/bibliotheque/recherches-orphelines")
@RequiredArgsConstructor
@Tag(name = "Admin : Carences de Contenu", description = "Dashboard d'audit des recherches infructueuses")
public class AdminAnalyticsController {

    private final RechercheOrphelineService orphelineService;

    @GetMapping("/frequentes")
    @Operation(summary = "Récupérer la liste des recherches qui n'ont rien donné, par ordre de fréquence")
    public ResponseEntity<Map<String, Long>> getTermesFrequents(
            @RequestParam(defaultValue = "15") int limite) {
        return ResponseEntity.ok(orphelineService.getTermesFrequents(limite));
    }
}
