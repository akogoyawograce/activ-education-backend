package tg.edtch.activEducation.datahub.domain.dto;

import java.util.Map;

public record RegionStat(
    String nom,
    String nomComplet,
    int nombreEtablissements,
    Map<String, Integer> etablissementsParType,
    double latitude,
    double longitude
) {}
