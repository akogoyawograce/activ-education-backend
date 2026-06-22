package tg.edtch.activEducation.shared.security.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tg.edtch.activEducation.shared.util.AuditLogService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.profil.domain.entite.Utilisateur;
import tg.edtch.activEducation.profil.repository.UtilisateurRepository;
import tg.edtch.activEducation.shared.security.auth.dto.ForgotPasswordRequest;
import tg.edtch.activEducation.shared.security.auth.dto.LoginRequest;
import tg.edtch.activEducation.shared.security.auth.dto.OtpResponse;
import tg.edtch.activEducation.shared.security.auth.dto.OtpVerifyRequest;
import tg.edtch.activEducation.shared.security.auth.dto.RefreshTokenRequest;
import tg.edtch.activEducation.shared.security.auth.dto.ResetPasswordRequest;
import tg.edtch.activEducation.shared.security.auth.dto.TokenResponse;
import tg.edtch.activEducation.shared.security.exception.InvalidTokenException;
import tg.edtch.activEducation.shared.security.jwt.JwtService;
import tg.edtch.activEducation.shared.security.totp.TotpService;
import tg.edtch.activEducation.shared.security.userdetails.CustomUserDetails;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final TotpService totpService;
    private final AuditLogService auditLogService;

    private static final String OTP_PREFIX = "otp:";
    private static final String TOTP_CHALLENGE_PREFIX = "totp_challenge:";
    private static final String RESET_TOKEN_PREFIX = "reset_token:";
    private static final long OTP_TTL_SECONDS = 300;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request, String deviceInfo) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (!userDetails.isEnabled()) {
            throw new RuntimeException("Compte inactif");
        }

        if (totpService.isTotpEnabled(userDetails.getId())) {
            String challengeToken = UUID.randomUUID().toString();
            String challengeKey = TOTP_CHALLENGE_PREFIX + challengeToken;
            redisTemplate.opsForValue().set(challengeKey, String.valueOf(userDetails.getId()),
                    300, TimeUnit.SECONDS);

            auditLogService.log(request.getEmail(), "", "CONNEXION", "/api/v1/auth/login",
                    "2FA required", deviceInfo, null);

            return TokenResponse.builder()
                    .requires2fa(true)
                    .challengeToken(challengeToken)
                    .build();
        }

        auditLogService.log(request.getEmail(), "", "CONNEXION", "/api/v1/auth/login",
                "", deviceInfo, null);
        return generateTokens(userDetails, deviceInfo);
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request, String deviceInfo) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh token introuvable"));

        if (storedToken.getRevoque()) {
            log.warn("Tentative d'utilisation d'un refresh token révoqué pour userId: {}",
                    storedToken.getUtilisateurId());
            refreshTokenRepository.revokeAllUserTokens(storedToken.getUtilisateurId());
            throw new InvalidTokenException("Refresh token révoqué, compromission potentielle.");
        }

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token expiré");
        }

        storedToken.setRevoque(true);
        refreshTokenRepository.save(storedToken);

        Utilisateur utilisateur = utilisateurRepository.findById(storedToken.getUtilisateurId())
                .orElseThrow(() -> new InvalidTokenException("Utilisateur introuvable"));

        if (!utilisateur.getEstActif()) {
            throw new RuntimeException("Compte inactif");
        }

        return generateTokens(new CustomUserDetails(utilisateur), deviceInfo);
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
                token.setRevoque(true);
                refreshTokenRepository.save(token);
            });
        }

        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
            try {
                String jti = jwtService.extractJti(accessToken);
                Date expDate = jwtService.extractClaim(accessToken, claims -> claims.getExpiration());
                long ttl = expDate.getTime() - System.currentTimeMillis();

                if (ttl > 0) {
                    redisTemplate.opsForValue().set("blacklist:" + jti, "revoked", ttl, TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                log.error("Erreur lors de la mise en blacklist du token", e);
            }
        }
    }

    @Override
    @Transactional
    public void logoutAll(CustomUserDetails userDetails) {
        refreshTokenRepository.revokeAllUserTokens(userDetails.getId());
    }

    @Override
    public void forgotPassword(String email) {
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("Aucun compte trouvé avec cet email"));

        if (!user.getEstActif()) {
            throw new RuntimeException("Compte inactif");
        }

        String otp = String.format("%04d", RANDOM.nextInt(10000));
        String otpKey = OTP_PREFIX + email;

        redisTemplate.opsForValue().set(otpKey, otp, OTP_TTL_SECONDS, TimeUnit.SECONDS);

        log.info("OTP pour {} : {} (valide {}s)", email, otp, OTP_TTL_SECONDS);
    }

    @Override
    public OtpResponse verifyOtp(String email, String code) {
        String otpKey = OTP_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null) {
            return OtpResponse.builder()
                    .success(false)
                    .message("Code invalide ou expiré")
                    .build();
        }

        if (!storedOtp.equals(code)) {
            return OtpResponse.builder()
                    .success(false)
                    .message("Code incorrect")
                    .build();
        }

        redisTemplate.delete(otpKey);

        String resetToken = UUID.randomUUID().toString();
        String resetKey = RESET_TOKEN_PREFIX + email;
        redisTemplate.opsForValue().set(resetKey, resetToken, OTP_TTL_SECONDS, TimeUnit.SECONDS);

        return OtpResponse.builder()
                .success(true)
                .message("Code vérifié avec succès")
                .resetToken(resetToken)
                .build();
    }

    @Override
    @Transactional
    public void resetPassword(String email, String resetToken, String nouveauMotDePasse) {
        String resetKey = RESET_TOKEN_PREFIX + email;
        String storedToken = redisTemplate.opsForValue().get(resetKey);

        if (storedToken == null || !storedToken.equals(resetToken)) {
            throw new InvalidTokenException("Token de réinitialisation invalide ou expiré");
        }

        redisTemplate.delete(resetKey);

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("Utilisateur introuvable"));

        user.setMotDePasseHash(passwordEncoder.encode(nouveauMotDePasse));
        utilisateurRepository.save(user);

        refreshTokenRepository.revokeAllUserTokens(user.getId());

        log.info("Mot de passe réinitialisé pour {}", email);
    }

    @Override
    @Transactional
    public TokenResponse completeTotpLogin(String challengeToken, int code) {
        String challengeKey = TOTP_CHALLENGE_PREFIX + challengeToken;
        String userIdStr = redisTemplate.opsForValue().get(challengeKey);

        if (userIdStr == null) {
            throw new InvalidTokenException("Challenge TOTP invalide ou expiré");
        }

        redisTemplate.delete(challengeKey);

        Long userId = Long.parseLong(userIdStr);
        if (!totpService.validateCode(userId, code)) {
            throw new InvalidTokenException("Code TOTP incorrect");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("Utilisateur introuvable"));

        return generateTokens(new CustomUserDetails(utilisateur), "TOTP");
    }

    private TokenResponse generateTokens(CustomUserDetails userDetails, String deviceInfo) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String accessToken = jwtService.generateAccessToken(
                userDetails,
                userDetails.getTrackingId(),
                userDetails.getTypeUtilisateur(),
                roles);

        String refreshTokenStr = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .utilisateurId(userDetails.getId())
                .expiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiration() / 1000))
                .revoque(false)
                .deviceInfo(deviceInfo)
                .build();

        refreshTokenRepository.save(refreshToken);

        Utilisateur user = userDetails.getUtilisateur();
        user.setDerniereConnexion(LocalDateTime.now());
        utilisateurRepository.save(user);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .trackingId(userDetails.getTrackingId())
                .typeUtilisateur(userDetails.getTypeUtilisateur())
                .roles(roles)
                .expiresInMs(jwtService.getAccessTokenExpiration())
                .build();
    }
}
