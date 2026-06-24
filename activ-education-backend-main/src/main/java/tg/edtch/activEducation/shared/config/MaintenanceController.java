package tg.edtch.activEducation.shared.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.shared.util.ParametreApplication;
import tg.edtch.activEducation.shared.util.ParametreApplicationRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/maintenance")
@RequiredArgsConstructor
@Tag(name = "Maintenance", description = "Gestion du mode maintenance")
public class MaintenanceController {

    private final ParametreApplicationRepository parametreRepository;

    @GetMapping
    @Operation(summary = "Vérifier le statut du mode maintenance")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "maintenance", MaintenanceFilter.isMaintenanceMode()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activer/désactiver le mode maintenance")
    public ResponseEntity<Map<String, Object>> setMaintenance(@RequestBody Map<String, Object> body) {
        boolean enabled = (boolean) body.getOrDefault("enabled", false);
        String message = (String) body.getOrDefault("message", null);

        MaintenanceFilter.setMaintenanceMode(enabled);
        MaintenanceFilter.setMaintenanceMessage(message);

        persistMaintenanceParam("maintenance.enabled", String.valueOf(enabled), "État du mode maintenance");
        if (message != null) {
            persistMaintenanceParam("maintenance.message", message, "Message affiché en mode maintenance");
        }

        return ResponseEntity.ok(Map.of(
                "maintenance", enabled,
                "message", enabled
                        ? (message != null ? message : "Mode maintenance activé")
                        : "Mode maintenance désactivé"));
    }

    private void persistMaintenanceParam(String cle, String valeur, String description) {
        parametreRepository.findByCle(cle).ifPresentOrElse(
                param -> {
                    param.setValeur(valeur);
                    parametreRepository.save(param);
                },
                () -> parametreRepository.save(ParametreApplication.builder()
                        .cle(cle)
                        .valeur(valeur)
                        .description(description)
                        .categorie("MAINTENANCE")
                        .build()));
    }
}
