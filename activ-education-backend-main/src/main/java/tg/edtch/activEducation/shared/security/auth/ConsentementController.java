package tg.edtch.activEducation.shared.security.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.profil.domain.service.ConsentementParentalService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/consentement")
@RequiredArgsConstructor
@Tag(name = "Consentement parental", description = "Validation du consentement parental pour les mineurs")
public class ConsentementController {

    private final ConsentementParentalService consentementService;

    @GetMapping("/valider")
    @Operation(summary = "Valider le consentement parental via le lien envoyé par email")
    public ResponseEntity<Map<String, Object>> valider(@RequestParam("token") String token,
                                                        HttpServletRequest request) {
        boolean success = consentementService.validerConsentement(token, request.getRemoteAddr());
        return success
                ? ResponseEntity.ok(Map.of("message", "Consentement validé avec succès"))
                : ResponseEntity.badRequest().body(Map.of("message", "Lien invalide ou expiré"));
    }
}
