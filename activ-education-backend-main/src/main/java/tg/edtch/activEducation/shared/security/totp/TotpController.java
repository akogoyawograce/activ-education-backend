package tg.edtch.activEducation.shared.security.totp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.shared.security.auth.AuthService;
import tg.edtch.activEducation.shared.security.auth.dto.TokenResponse;
import tg.edtch.activEducation.shared.security.userdetails.CustomUserDetails;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/2fa")
@RequiredArgsConstructor
@Tag(name = "2FA TOTP", description = "Double authentification via TOTP")
public class TotpController {

    private final TotpService totpService;
    private final AuthService authService;

    @PostMapping("/generate")
    @Operation(summary = "Générer un secret TOTP pour l'utilisateur connecté")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> generate(@AuthenticationPrincipal CustomUserDetails userDetails) {
        TotpService.TotpSetupData setup = totpService.generateSecret(
                userDetails.getId(), userDetails.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("secretKey", setup.secretKey());
        response.put("qrUri", setup.qrUri());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    @Operation(summary = "Vérifier un code TOTP et activer la 2FA")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> verify(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TotpCodeRequest request) {
        boolean success = totpService.verifyAndEnable(userDetails.getId(), request.code());

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "TOTP activé avec succès" : "Code invalide");
        return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/disable")
    @Operation(summary = "Désactiver la 2FA")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> disable(@AuthenticationPrincipal CustomUserDetails userDetails) {
        totpService.disable(userDetails.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "TOTP désactivé");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @Operation(summary = "Vérifier le statut de la 2FA")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> status(@AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean enabled = totpService.isTotpEnabled(userDetails.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("enabled", enabled);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    @Operation(summary = "Valider un code TOTP pendant le login (deuxième facteur)")
    public ResponseEntity<TokenResponse> validateTotp(
            @Valid @RequestBody TotpValidateRequest request) {
        TokenResponse response = authService.completeTotpLogin(
                request.challengeToken(), request.code());
        return ResponseEntity.ok(response);
    }

    public record TotpCodeRequest(@Min(100000) @Max(999999) int code) {}
    public record TotpValidateRequest(@NotBlank String challengeToken, @Min(100000) @Max(999999) int code) {}
}
