package tg.edtch.activEducation.datahub.domain.dto;

import java.util.List;
import java.util.Map;

public record DataHubResponse(
    List<RegionStat> regions,
    int totalEtablissements,
    int totalFilieres,
    Map<String, Integer> repartitionTypeEtablissement
) {}
