package tg.edtch.activEducation.shared.security.supabase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Service
public class SupabaseJwtService {

    private final String supabaseUrl;
    private final String jwksUrl;
    private final String issuer;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private volatile PublicKey cachedPublicKey;
    private volatile Instant cacheExpiry = Instant.MIN;

    public SupabaseJwtService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.jwks-url:}") String jwksUrl) {
        this.supabaseUrl = supabaseUrl;
        this.jwksUrl = jwksUrl != null && !jwksUrl.isBlank()
                ? jwksUrl
                : supabaseUrl + "/auth/v1/.well-known/jwks.json";
        this.issuer = supabaseUrl + "/auth/v1";
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public Claims validateToken(String token) {
        try {
            PublicKey publicKey = getPublicKey();
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.debug("Supabase JWT validation failed: {}", e.getMessage());
            return null;
        }
    }

    public String extractEmail(Claims claims) {
        return claims.get("email", String.class);
    }

    public String extractRole(Claims claims) {
        var appMetadata = claims.get("app_metadata", java.util.Map.class);
        if (appMetadata != null && appMetadata.containsKey("role")) {
            return (String) appMetadata.get("role");
        }
        return claims.get("role", String.class);
    }

    private PublicKey getPublicKey() {
        if (cachedPublicKey != null && Instant.now().isBefore(cacheExpiry)) {
            return cachedPublicKey;
        }
        return fetchAndCachePublicKey();
    }

    private synchronized PublicKey fetchAndCachePublicKey() {
        if (cachedPublicKey != null && Instant.now().isBefore(cacheExpiry)) {
            return cachedPublicKey;
        }
        try {
            String jwksJson = restTemplate.getForObject(jwksUrl, String.class);
            JsonNode root = objectMapper.readTree(jwksJson);
            JsonNode keyNode = root.get("keys").get(0);

            String kty = keyNode.get("kty").asText();
            String alg = keyNode.get("alg").asText();
            KeyFactory keyFactory = KeyFactory.getInstance("EC");

            switch (kty) {
                case "EC" -> {
                    String crv = keyNode.get("crv").asText();
                    String ecSpecName = switch (crv) {
                        case "P-256" -> "secp256r1";
                        case "P-384" -> "secp384r1";
                        case "P-521" -> "secp521r1";
                        default -> throw new IllegalArgumentException("Unsupported curve: " + crv);
                    };
                    AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
                    params.init(new ECGenParameterSpec(ecSpecName));
                    ECParameterSpec ecParamSpec = params.getParameterSpec(ECParameterSpec.class);

                    byte[] xBytes = Base64.getUrlDecoder().decode(keyNode.get("x").asText());
                    byte[] yBytes = Base64.getUrlDecoder().decode(keyNode.get("y").asText());
                    ECPoint ecPoint = new ECPoint(
                            new BigInteger(1, xBytes),
                            new BigInteger(1, yBytes));

                    cachedPublicKey = keyFactory.generatePublic(new ECPublicKeySpec(ecPoint, ecParamSpec));
                }
                case "RSA" -> {
                    keyFactory = KeyFactory.getInstance("RSA");
                    BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(keyNode.get("n").asText()));
                    BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(keyNode.get("e").asText()));
                    cachedPublicKey = keyFactory.generatePublic(new RSAPublicKeySpec(modulus, exponent));
                }
                default -> throw new IllegalArgumentException("Unsupported key type: " + kty);
            }

            cacheExpiry = Instant.now().plus(Duration.ofHours(1));
            log.info("Supabase JWKS fetched (kty={}, alg={}), cached 1h", kty, alg);
            return cachedPublicKey;
        } catch (Exception e) {
            log.error("Failed to fetch Supabase JWKS: {}", e.getMessage());
            if (cachedPublicKey != null) {
                return cachedPublicKey;
            }
            throw new RuntimeException("No cached Supabase public key available", e);
        }
    }
}
