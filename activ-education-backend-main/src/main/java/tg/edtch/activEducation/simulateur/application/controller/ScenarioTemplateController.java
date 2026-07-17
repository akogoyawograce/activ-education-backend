package tg.edtch.activEducation.simulateur.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.simulateur.application.dto.ScenarioTemplate;
import tg.edtch.activEducation.simulateur.application.service.ScenarioTemplateRegistry;
import tg.edtch.activEducation.simulateur.domain.dto.ScenarioRequest;
import tg.edtch.activEducation.simulateur.domain.dto.ScenarioResult;
import tg.edtch.activEducation.simulateur.domain.service.SimulateurParcoursService;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints pour explorer les scénarios préfabriqués (Chantier B).
 *
 * <p>Le front appelle {@code GET /scenarios-types} pour afficher une
 * liste de cartes "Et si je montais ma moyenne de maths ?", "Et si
 * j'allais à Lomé ?", etc. Quand l'élève en choisit un, le front appelle
 * {@code POST /scenarios-types/{id}/executer} pour obtenir un
 * {@link ScenarioResult} prêt à afficher.</p>
 *
 * <p>Le registre est un bean Spring (@Component). Le service de
 * simulation est mockable pour les tests d'intégration.</p>
 */
@RestController
@RequestMapping("/api/v1/simulateur/scenarios-types")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Simulateur — scénarios types", description = "Templates What-If préfabriqués pour aider les élèves à démarrer")
public class ScenarioTemplateController {

    private final ScenarioTemplateRegistry registry;
    private final SimulateurParcoursService simulateurService;

    @GetMapping
    @Operation(summary = "Lister tous les scénarios types disponibles")
    public ResponseEntity<List<ScenarioTemplate>> lister() {
        return ResponseEntity.ok(registry.lister());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un scénario type (404 si introuvable)")
    public ResponseEntity<ScenarioTemplate> get(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(registry.get(id));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/executer")
    @Operation(summary = "Exécuter un scénario type et renvoyer un ScenarioResult")
    public ResponseEntity<ScenarioResult> executer(
            @PathVariable UUID id,
            @RequestParam(name = "eleveTrackingId", required = false) UUID eleveTrackingId) {
        // 1) Récupérer le template (404 si l'id est mauvais)
        ScenarioTemplate template;
        try {
            template = registry.get(id);
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }

        // 2) Construire un ScenarioRequest frais à partir du template
        //    (le Supplier garantit qu'on a une instance neuve, pas de
        //    risque de mutation croisée entre deux exécutions).
        ScenarioRequest request = template.requestSupplier().get();

        // 3) (Optionnel) Forcer la série à partir de l'élève si fournie
        //    — non implémenté ici : on laisse le front fournir le
        //    ScenarioRequest complet si nécessaire, et eleveTrackingId
        //    est accepté pour traçabilité / logs futurs.

        // 4) Exécuter via le service de simulation
        ScenarioResult result = simulateurService.explorer(request);
        return ResponseEntity.ok(result);
    }
}
