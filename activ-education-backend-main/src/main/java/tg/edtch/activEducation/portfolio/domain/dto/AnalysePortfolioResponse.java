package tg.edtch.activEducation.portfolio.domain.dto;

import java.util.List;
import java.util.Map;

public record AnalysePortfolioResponse(
    Map<String, Integer> repartitionParCategorie,
    int totalCompetences,
    double scoreGlobal,
    List<RecommandationMetier> metiersRecommandes
) {
    public record RecommandationMetier(
        String nomMetier,
        String trackingId,
        double scoreCompatibilite,
        int competencesAcquises,
        int competencesRequises,
        List<String> competencesManquantes
    ) {}
}
