package tg.edtch.activEducation.shared.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tg.edtch.activEducation.shared.security.auth.dto.ForgotPasswordRequest;
import tg.edtch.activEducation.shared.security.auth.dto.LoginRequest;
import tg.edtch.activEducation.shared.security.auth.dto.OtpResponse;
import tg.edtch.activEducation.shared.security.auth.dto.OtpVerifyRequest;
import tg.edtch.activEducation.shared.security.auth.dto.ResetPasswordRequest;
import tg.edtch.activEducation.shared.security.auth.dto.TokenResponse;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void login_shouldReturn200() throws Exception {
        LoginRequest request = new LoginRequest("test@test.com", "password");
        TokenResponse response = TokenResponse.builder()
                .accessToken("token")
                .refreshToken("refresh")
                .trackingId(UUID.randomUUID())
                .typeUtilisateur("ELEVE")
                .roles(List.of("ROLE_ELEVE"))
                .expiresInMs(900000L)
                .build();

        when(authService.login(any(LoginRequest.class), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token"));
    }

    @Test
    void forgotPassword_shouldReturn200() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@test.com");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void verifyOtp_shouldReturn200() throws Exception {
        OtpVerifyRequest request = new OtpVerifyRequest("test@test.com", "1234");
        when(authService.verifyOtp("test@test.com", "1234"))
                .thenReturn(OtpResponse.builder()
                        .success(true)
                        .message("OK")
                        .resetToken("reset-token")
                        .build());

        mockMvc.perform(post("/api/v1/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void resetPassword_shouldReturn200() throws Exception {
        ResetPasswordRequest request =
                new ResetPasswordRequest("test@test.com", "reset-token", "new-password-123");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isString());
    }
}
