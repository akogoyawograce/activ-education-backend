package tg.edtch.activEducation.shared.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.shared.ai.domain.dto.OriaRequest;
import tg.edtch.activEducation.shared.ai.domain.dto.OriaResponse;
import tg.edtch.activEducation.shared.ai.service.OriaService;

import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/oria")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "ORIA", description = "Assistant IA d'orientation scolaire (chat)")
public class OriaController {

    private final OriaService oriaService;

    @PostMapping("/message")
    @Operation(summary = "Envoyer un message à ORIA (persisté en DB)")
    public ResponseEntity<OriaResponse> sendMessage(
            @Valid @RequestBody OriaRequest request,
            Principal principal) {
        String userId = principal != null ? principal.getName() : "anonymous";
        OriaResponse response = oriaService.sendMessageAndPersist(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Récupérer l'historique d'une session ORIA")
    public ResponseEntity<OriaResponse> getSessionHistory(@PathVariable String sessionId) {
        OriaResponse response = oriaService.getSessionHistory(sessionId);
        return ResponseEntity.ok(response);
    }

}
