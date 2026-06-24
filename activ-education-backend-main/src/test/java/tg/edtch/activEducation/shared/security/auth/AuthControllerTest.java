package tg.edtch.activEducation.shared.security.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tg.edtch.activEducation.shared.security.auth.dto.ForgotPasswordRequest;
import tg.edtch.activEducation.shared.security.auth.dto.LoginRequest;
import tg.edtch.activEducation.shared.security.auth.dto.OtpResponse;
import tg.edtch.activEducation.shared.security.auth.dto.OtpVerifyRequest;
import tg.edtch.activEducation.shared.security.auth.dto.ResetPasswordRequest;
import tg.edtch.activEducation.shared.security.auth.dto.TokenResponse;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private AuthService authService;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        controller = new AuthController(authService);
    }

    @Test
    void login_shouldReturnTokenResponse() {
        TokenResponse response = TokenResponse.builder()
                .accessToken("token")
                .refreshToken("refresh")
                .trackingId(UUID.randomUUID())
                .typeUtilisateur("ELEVE")
                .roles(List.of("ROLE_ELEVE"))
                .expiresInMs(900000L)
                .build();
        when(authService.login(any(LoginRequest.class), anyString())).thenReturn(response);

        jakarta.servlet.http.HttpServletRequest mockRequest = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(mockRequest.getHeader("User-Agent")).thenReturn("test-agent");

        LoginRequest request = new LoginRequest("test@test.com", "password123");
        var result = controller.login(request, mockRequest);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("token", result.getBody().getAccessToken());
        verify(authService).login(eq(request), eq("test-agent"));
    }

    @Test
    void forgotPassword_shouldReturn200() {
        doNothing().when(authService).forgotPassword("test@test.com");

        var result = controller.forgotPassword(new ForgotPasswordRequest("test@test.com"));

        assertEquals(200, result.getStatusCode().value());
        verify(authService).forgotPassword("test@test.com");
    }

    @Test
    void verifyOtp_shouldReturnOtpResponse() {
        OtpResponse mockResponse = OtpResponse.builder()
                .success(true)
                .message("OK")
                .resetToken("reset-token")
                .build();
        when(authService.verifyOtp("test@test.com", "1234")).thenReturn(mockResponse);

        var result = controller.verifyOtp(new OtpVerifyRequest("test@test.com", "1234"));

        assertEquals(200, result.getStatusCode().value());
        assertTrue(result.getBody().isSuccess());
        assertEquals("reset-token", result.getBody().getResetToken());
    }

    @Test
    void resetPassword_shouldReturnMessage() {
        doNothing().when(authService).resetPassword(anyString(), anyString(), anyString());

        var result = controller.resetPassword(
                new ResetPasswordRequest("test@test.com", "reset-token", "new-password-123"));

        assertEquals(200, result.getStatusCode().value());
        verify(authService).resetPassword("test@test.com", "reset-token", "new-password-123");
    }
}