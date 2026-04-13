package tg.edtch.activEducation.diagnostic.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de requête pour l'enregistrement d'un résultat de diagnostic.
 * L'élève et le quiz sont identifiés par leurs trackingIds publics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultatDiagnosticRequest {

    @NotNull(message = "L'élève est obligatoire")
    private UUID eleveTrackingId;

    @NotNull(message = "Le quiz est obligatoire")
    private UUID quizTrackingId;

    /** Score global calculé côté client ou service. */
    private Double scoreFinal;

    /** Profil dominant détecté (ex: "Riasec: R-I-A", "Profil Scientifique"). */
    private String profilDecouvert;

    /** Texte de recommandation d'orientation. */
    private String recommandation;
}
