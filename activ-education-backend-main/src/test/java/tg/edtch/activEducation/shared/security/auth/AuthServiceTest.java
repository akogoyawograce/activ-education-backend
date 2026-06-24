package tg.edtch.activEducation.shared.security.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import tg.edtch.activEducation.profil.domain.entite.Utilisateur;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.repository.UtilisateurRepository;
import tg.edtch.activEducation.shared.security.auth.dto.*;
import tg.edtch.activEducation.shared.security.jwt.JwtService;
import tg.edtch.activEducation.shared.security.totp.TotpService;
import tg.edtch.activEducation.shared.security.userdetails.CustomUserDetails;
import tg.edtch.activEducation.shared.util.AuditLogService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ValueOperations<String, String> valueOps;
    @Mock
    private TotpService totpService;
    @Mock
    private AuditLogService auditLogService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                refreshTokenRepository, utilisateurRepository, jwtService,
                authenticationManager, redisTemplate, passwordEncoder,
                totpService, auditLogService);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void login_shouldReturnTokenResponse() {
        LoginRequest request = new LoginRequest("test@test.com", "password");
        Utilisateur user = Eleve.builder()
                .id(1L)
                .trackingId(UUID.randomUUID())
                .email("test@test.com")
                .estActif(true)
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication auth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateAccessToken(any(), any(), any(), anyList()))
                .thenReturn("access-token");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);
        lenient().doReturn("refresh-token").when(jwtService).extractJti(anyString());
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TokenResponse result = authService.login(request, "device-info");

        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals(user.getTrackingId(), result.getTrackingId());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void forgotPassword_shouldStoreOtpInRedis() {
        Utilisateur user = Eleve.builder()
                .id(1L)
                .email("test@test.com")
                .estActif(true)
                .build();

        when(utilisateurRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        authService.forgotPassword("test@test.com");

        verify(valueOps).set(eq("otp:test@test.com"), anyString(), eq(300L), eq(TimeUnit.SECONDS));
    }

    @Test
    void forgotPassword_shouldThrowWhenUserNotFound() {
        when(utilisateurRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> authService.forgotPassword("unknown@test.com"));
    }

    @Test
    void verifyOtp_shouldReturnSuccessWithResetToken() {
        when(valueOps.get("otp:test@test.com")).thenReturn("1234");

        OtpResponse result = authService.verifyOtp("test@test.com", "1234");

        assertTrue(result.isSuccess());
        assertNotNull(result.getResetToken());
        verify(redisTemplate).delete("otp:test@test.com");
        verify(valueOps).set(eq("reset_token:test@test.com"), anyString(), eq(300L), eq(TimeUnit.SECONDS));
    }

    @Test
    void verifyOtp_shouldReturnFailureForWrongCode() {
        when(valueOps.get("otp:test@test.com")).thenReturn("1234");

        OtpResponse result = authService.verifyOtp("test@test.com", "0000");

        assertFalse(result.isSuccess());
        assertEquals("Code incorrect", result.getMessage());
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void verifyOtp_shouldReturnFailureForExpiredCode() {
        when(valueOps.get("otp:test@test.com")).thenReturn(null);

        OtpResponse result = authService.verifyOtp("test@test.com", "1234");

        assertFalse(result.isSuccess());
        assertEquals("Code invalide ou expiré", result.getMessage());
    }

    @Test
    void resetPassword_shouldUpdatePasswordAndRevokeTokens() {
        Utilisateur user = Eleve.builder()
                .id(1L)
                .email("test@test.com")
                .estActif(true)
                .build();

        when(valueOps.get("reset_token:test@test.com")).thenReturn("valid-reset-token");
        when(utilisateurRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password-123")).thenReturn("hashed-password");

        authService.resetPassword("test@test.com", "valid-reset-token", "new-password-123");

        assertEquals("hashed-password", user.getMotDePasseHash());
        verify(utilisateurRepository).save(user);
        verify(refreshTokenRepository).revokeAllUserTokens(1L);
        verify(redisTemplate).delete("reset_token:test@test.com");
    }
}
