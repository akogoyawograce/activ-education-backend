package tg.edtch.activEducation.shared.security.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpResponse {
    private String message;
    private boolean success;
    private String resetToken;
}
