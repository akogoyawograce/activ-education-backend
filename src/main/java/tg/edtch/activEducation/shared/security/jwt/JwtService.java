package tg.edtch.activEducation.shared.security.jwt;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface JwtService {
    String extractUsername(String token);

    UUID extractTrackingId(String token);

    String extractJti(String token);

    String generateAccessToken(UserDetails userDetails, UUID trackingId, String typeUtilisateur, List<String> roles);

    String generateAccessToken(Map<String, Object> extraClaims, UserDetails userDetails);

    boolean isTokenValid(String token, UserDetails userDetails);

    long getAccessTokenExpiration();

    long getRefreshTokenExpiration();

    <T> T extractClaim(String token, java.util.function.Function<io.jsonwebtoken.Claims, T> claimsResolver);
}
