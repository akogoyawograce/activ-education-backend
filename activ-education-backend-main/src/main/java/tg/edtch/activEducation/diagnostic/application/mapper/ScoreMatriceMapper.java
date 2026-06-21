package tg.edtch.activEducation.diagnostic.application.mapper;

import org.springframework.stereotype.Component;
import tg.edtch.activEducation.diagnostic.application.dto.request.ScoreMatriceRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.ScoreMatriceResponse;
import tg.edtch.activEducation.diagnostic.domain.entite.ScoreMatrice;

import java.util.UUID;

/**
 * Mapper dédié à l'entité {@link ScoreMatrice}.
 * Le scoreTotalEstime peut être calculé automatiquement comme somme des 3
 * dimensions.
 */
@Component
public class ScoreMatriceMapper {

    public ScoreMatrice toEntity(ScoreMatriceRequest request) {
        if (request == null)
            return null;
        double goutsPersonnel = request.getScoreGoutsPersonnel() != null ? request.getScoreGoutsPersonnel() : 0.0;
        double academique = request.getScoreAcademique() != null ? request.getScoreAcademique() : 0.0;
        double marcheTravail = request.getScoreMarcheTravail() != null ? request.getScoreMarcheTravail() : 0.0;
        // Si scoreTotalEstime non fourni, on calcule la somme des 3 dimensions
        double total = request.getScoreTotalEstime() != null
                ? request.getScoreTotalEstime()
                : goutsPersonnel + academique + marcheTravail;

        return ScoreMatrice.builder()
                .trackingId(UUID.randomUUID())
                .titreMatrice(request.getTitreMatrice())
                .scoreGoutsPersonnel(goutsPersonnel)
                .scoreAcademique(academique)
                .scoreMarcheTravail(marcheTravail)
                .scoreTotalEstime(total)
                .build();
    }

    public ScoreMatriceResponse toResponse(ScoreMatrice matrice) {
        if (matrice == null)
            return null;
        return ScoreMatriceResponse.builder()
                .trackingId(matrice.getTrackingId())
                .titreMatrice(matrice.getTitreMatrice())
                .scoreGoutsPersonnel(matrice.getScoreGoutsPersonnel())
                .scoreAcademique(matrice.getScoreAcademique())
                .scoreMarcheTravail(matrice.getScoreMarcheTravail())
                .scoreTotalEstime(matrice.getScoreTotalEstime())
                .createdAt(matrice.getCreatedAt())
                .build();
    }

    public void updateFromRequest(ScoreMatriceRequest request, ScoreMatrice matrice) {
        if (request.getTitreMatrice() != null)
            matrice.setTitreMatrice(request.getTitreMatrice());
        if (request.getScoreGoutsPersonnel() != null)
            matrice.setScoreGoutsPersonnel(request.getScoreGoutsPersonnel());
        if (request.getScoreAcademique() != null)
            matrice.setScoreAcademique(request.getScoreAcademique());
        if (request.getScoreMarcheTravail() != null)
            matrice.setScoreMarcheTravail(request.getScoreMarcheTravail());
        // Recalcul automatique du total si non fourni explicitement
        if (request.getScoreTotalEstime() != null) {
            matrice.setScoreTotalEstime(request.getScoreTotalEstime());
        } else if (request.getScoreGoutsPersonnel() != null || request.getScoreAcademique() != null
                || request.getScoreMarcheTravail() != null) {
            matrice.setScoreTotalEstime(
                    matrice.getScoreGoutsPersonnel() + matrice.getScoreAcademique() + matrice.getScoreMarcheTravail());
        }
    }
}
