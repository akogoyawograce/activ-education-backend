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
import tg.edtch.activEducation.shared.security.auth.dto.ForgotPasswordRequest;
import tg.edtch.activEducation.shared.security.auth.dto.LoginRequest;
import tg.edtch.activEducation.shared.security.auth.dto.OtpResponse;
import tg.edtch.activEducation.shared.security.auth.dto.OtpVerifyRequest;
import tg.edtch.activEducation.shared.security.auth.dto.RefreshTokenRequest;
import tg.edtch.activEducation.shared.security.auth.dto.ResetPasswordRequest;
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
            @RequestBody(required = false) RefreshTokenRequest request) {
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

    @PostMapping("/forgot-password")
    @Operation(summary = "Demander un code de réinitialisation de mot de passe")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Si un compte existe avec cet email, un code vous a été envoyé");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Vérifier le code OTP reçu par email")
    public ResponseEntity<OtpResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        OtpResponse response = authService.verifyOtp(request.getEmail(), request.getCode());
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialiser le mot de passe avec le token de validation")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getEmail(), request.getResetToken(), request.getNouveauMotDePasse());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Mot de passe réinitialisé avec succès");
        return ResponseEntity.ok(response);
    }
}
