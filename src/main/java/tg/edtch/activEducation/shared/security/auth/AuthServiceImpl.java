package tg.edtch.activEducation.shared.security.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.profil.domain.entite.Utilisateur;
import tg.edtch.activEducation.profil.repository.UtilisateurRepository;
import tg.edtch.activEducation.shared.security.auth.dto.LoginRequest;
import tg.edtch.activEducation.shared.security.auth.dto.RefreshTokenRequest;
import tg.edtch.activEducation.shared.security.auth.dto.TokenResponse;
import tg.edtch.activEducation.shared.security.exception.InvalidTokenException;
import tg.edtch.activEducation.shared.security.jwt.JwtService;
import tg.edtch.activEducation.shared.security.userdetails.CustomUserDetails;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request, String deviceInfo) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (!userDetails.isEnabled()) {
            throw new RuntimeException("Compte inactif");
        }

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
