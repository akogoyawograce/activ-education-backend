package tg.edtch.activEducation.shared.security.email2fa;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tg.edtch.activEducation.shared.security.auth.AuthService;
import tg.edtch.activEducation.shared.security.auth.dto.TokenResponse;
import tg.edtch.activEducation.shared.security.userdetails.CustomUserDetails;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/2fa/email")
@RequiredArgsConstructor
@Tag(name = "2FA Email", description = "Double authentification via OTP reçu par email")
public class Email2FaController {

    private final AuthService authService;

    @PostMapping("/enable")
    @Operation(summary = "Étape 1 — envoyer un code à l'email du compte (2FA email)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> enable(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.activer2faEmail(userDetails);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Un code a été envoyé à votre adresse email");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/enable/confirm")
    @Operation(summary = "Étape 2 — valider le code reçu et activer la 2FA email")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> confirmEnable(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EmailOtpCodeRequest request) {
        authService.confirmerActivation2faEmail(userDetails, request.code());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Double authentification par email activée");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/disable")
    @Operation(summary = "Désactiver la 2FA email après validation d'un code")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> disable(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EmailOtpCodeRequest request) {
        authService.desactiver2faEmail(userDetails, request.code());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "2FA par email désactivée");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @Operation(summary = "Statut de la 2FA email de l'utilisateur connecté")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> status(@AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean enabled = authService.statut2faEmail(userDetails);
        Map<String, Object> response = new HashMap<>();
        response.put("enabled", enabled);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    @Operation(summary = "Valider un code OTP email pendant le login (deuxième facteur)")
    public ResponseEntity<TokenResponse> validate(
            @Valid @RequestBody EmailOtpValidateRequest request) {
        return ResponseEntity.ok(authService.completeEmail2faLogin(request.challengeToken(), request.code()));
    }

    public record EmailOtpCodeRequest(@NotBlank String code) {}

    public record EmailOtpValidateRequest(@NotBlank String challengeToken, @NotBlank String code) {}
}