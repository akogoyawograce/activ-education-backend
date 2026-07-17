package tg.edtch.activEducation.diagnostic.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizGenerationRequest {

    @NotBlank
    private String type;

    @NotNull
    private UUID entityId;

    @Min(1)
    @Builder.Default
    private Integer nombre = 5;
}
