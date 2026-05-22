package tg.edtch.activEducation.shared.security.auth;

import tg.edtch.activEducation.shared.security.auth.dto.LoginRequest;
import tg.edtch.activEducation.shared.security.auth.dto.RefreshTokenRequest;
import tg.edtch.activEducation.shared.security.auth.dto.TokenResponse;
import tg.edtch.activEducation.shared.security.userdetails.CustomUserDetails;

public interface AuthService {
    TokenResponse login(LoginRequest request, String deviceInfo);

    TokenResponse refreshToken(RefreshTokenRequest request, String deviceInfo);

    void logout(String accessToken, String refreshToken);

    void logoutAll(CustomUserDetails userDetails);

    // Pour l'exemple, l'enregistrement doit être détaillé dans les services métiers
    // (Profil)
    // Void register(RegisterRequest request);
}
