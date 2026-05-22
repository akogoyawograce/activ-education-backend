package tg.edtch.activEducation.shared.security.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private UUID trackingId;
    private String typeUtilisateur;
    private List<String> roles;
    private long expiresInMs;
}
