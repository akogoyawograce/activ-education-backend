package tg.edtch.activEducation.shared.security.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpVerifyRequest {
    @NotBlank
    private String email;

    @NotBlank
    private String code;
}
