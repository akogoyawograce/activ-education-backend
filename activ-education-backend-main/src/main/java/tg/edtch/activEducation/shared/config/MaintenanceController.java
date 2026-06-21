package tg.edtch.activEducation.shared.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/maintenance")
@RequiredArgsConstructor
@Tag(name = "Maintenance", description = "Gestion du mode maintenance")
public class MaintenanceController {

    @GetMapping
    @Operation(summary = "Vérifier le statut du mode maintenance")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "maintenance", MaintenanceFilter.isMaintenanceMode()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activer/désactiver le mode maintenance")
    public ResponseEntity<Map<String, Object>> setMaintenance(@RequestBody Map<String, Boolean> body) {
        boolean enabled = body.getOrDefault("enabled", false);
        MaintenanceFilter.setMaintenanceMode(enabled);
        return ResponseEntity.ok(Map.of(
                "maintenance", enabled,
                "message", enabled ? "Mode maintenance activé" : "Mode maintenance désactivé"));
    }
}
