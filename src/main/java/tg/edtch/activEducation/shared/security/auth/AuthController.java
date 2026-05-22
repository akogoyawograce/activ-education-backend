package tg.edtch.activEducation.shared.security.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.profil.domain.entite.Utilisateur;
import tg.edtch.activEducation.shared.security.auth.dto.LoginRequest;
import tg.edtch.activEducation.shared.security.auth.dto.RefreshTokenRequest;
import tg.edtch.activEducation.shared.security.auth.dto.TokenResponse;
import tg.edtch.activEducation.shared.security.userdetails.CustomUserDetails;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Endpoints d'authentification et gestion de session")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authentifier un utilisateur (Login)")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String deviceInfo = httpRequest.getHeader("User-Agent");
        return ResponseEntity.ok(authService.login(request, deviceInfo));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraichir un access token via un refresh token")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        String deviceInfo = httpRequest.getHeader("User-Agent");
        return ResponseEntity.ok(authService.refreshToken(request, deviceInfo));
    }

    @PostMapping("/logout")
    @Operation(summary = "Se déconnecter de l'appareil courant")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody(required = false) RefreshTokenRequest request) { // In case they send it in body. Best practice
                                                                          // is to extract it
        String refreshToken = request != null ? request.getRefreshToken() : null;
        authService.logout(authHeader, refreshToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Se déconnecter de TOUS les appareils")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logoutAll(userDetails);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Récupérer les informations de l'utilisateur connecté")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Utilisateur u = userDetails.getUtilisateur();
        Map<String, Object> data = new HashMap<>();
        data.put("trackingId", u.getTrackingId());
        data.put("email", u.getEmail());
        data.put("nom", u.getNom());
        data.put("prenom", u.getPrenom());
        data.put("type", userDetails.getTypeUtilisateur());
        data.put("roles", userDetails.getAuthorities());
        return ResponseEntity.ok(data);
    }
}
