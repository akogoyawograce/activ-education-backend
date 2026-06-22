package tg.edtch.activEducation.shared.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.shared.util.AuditLog;
import tg.edtch.activEducation.shared.util.AuditLogService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin : Audit Logs", description = "Consultation des journaux d'audit")
public class AdminLogsController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Liste paginée des logs d'audit")
    public ResponseEntity<Page<AuditLog>> getLogs(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(auditLogService.getLogs(email, action, fromDate, toDate, pageable));
    }

    @GetMapping("/counts")
    @Operation(summary = "Compteurs par type d'action")
    public ResponseEntity<java.util.Map<String, Long>> getCounts() {
        java.util.Map<String, Long> counts = new java.util.LinkedHashMap<>();
        for (String action : new String[]{"CONNEXION", "CREATION", "MODIFICATION", "SUPPRESSION", "CONSULTATION", "EXPORT", "TENTATIVE_ECHEC"}) {
            counts.put(action, auditLogService.countByAction(action));
        }
        return ResponseEntity.ok(counts);
    }
}
