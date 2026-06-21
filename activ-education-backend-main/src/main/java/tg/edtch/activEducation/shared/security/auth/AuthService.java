package tg.edtch.activEducation.shared.security.auth;

import tg.edtch.activEducation.shared.security.auth.dto.LoginRequest;
import tg.edtch.activEducation.shared.security.auth.dto.OtpResponse;
import tg.edtch.activEducation.shared.security.auth.dto.RefreshTokenRequest;
import tg.edtch.activEducation.shared.security.auth.dto.TokenResponse;
import tg.edtch.activEducation.shared.security.userdetails.CustomUserDetails;

public interface AuthService {
    TokenResponse login(LoginRequest request, String deviceInfo);

    TokenResponse refreshToken(RefreshTokenRequest request, String deviceInfo);

    void logout(String accessToken, String refreshToken);

    void logoutAll(CustomUserDetails userDetails);

    void forgotPassword(String email);

    OtpResponse verifyOtp(String email, String code);

    void resetPassword(String email, String resetToken, String nouveauMotDePasse);

    TokenResponse completeTotpLogin(String challengeToken, int code);
}
