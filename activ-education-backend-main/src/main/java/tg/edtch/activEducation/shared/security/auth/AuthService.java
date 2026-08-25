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

    /**
     * Envoie un code OTP à l'email fourni (sans condition préalable).
     */
    void envoyerCode(String email);

    /**
     * Étape 1 du changement d'email : vérifie la disponibilité du nouvel email
     * et envoie un OTP au nouveau mail.
     */
    void demanderChangementEmail(CustomUserDetails userDetails, String nouveauEmail);

    /**
     * Étape 2 du changement d'email : valide l'OTP reçu au nouvel email puis
     * met à jour le mail du compte.
     */
    void confirmerChangementEmail(CustomUserDetails userDetails, String nouveauEmail, String code);

    /**
     * Étape 1 de l'activation de la 2FA email : envoie un code à l'email du compte.
     */
    void activer2faEmail(CustomUserDetails userDetails);

    /**
     * Étape 2 de l'activation de la 2FA email : valide le code puis active le facteur.
     */
    void confirmerActivation2faEmail(CustomUserDetails userDetails, String code);

    /**
     * Désactive la 2FA email après validation d'un code.
     */
    void desactiver2faEmail(CustomUserDetails userDetails, String code);

    /**
     * Statut de la 2FA email de l'utilisateur connecté.
     */
    boolean statut2faEmail(CustomUserDetails userDetails);

    /**
     * Termine un login protégé par OTP email (deuxième facteur).
     */
    TokenResponse completeEmail2faLogin(String challengeToken, String code);
}
