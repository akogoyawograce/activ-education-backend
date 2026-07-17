package tg.edtch.activEducation.datahub.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheEtablissement;
import tg.edtch.activEducation.bibliotheque.repository.FicheEtablissementRepository;
import tg.edtch.activEducation.datahub.domain.dto.DataHubResponse;
import tg.edtch.activEducation.datahub.domain.dto.RegionStat;
import tg.edtch.activEducation.datahub.domain.dto.RegionTogo;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DataHubService {

    private final FicheEtablissementRepository etablissementRepository;

    public DataHubService(FicheEtablissementRepository etablissementRepository) {
        this.etablissementRepository = etablissementRepository;
    }

    public DataHubResponse getDataHub() {
        var etablissements = etablissementRepository.findAll();
        return aggreguer(etablissements);
    }

    public DataHubResponse getDataHubParVille(String ville) {
        var all = etablissementRepository.findAll();
        var filtres = all.stream()
            .filter(e -> e.getVille() != null && e.getVille().equalsIgnoreCase(ville))
            .toList();
        return aggreguer(filtres);
    }

    private DataHubResponse aggreguer(List<FicheEtablissement> etablissements) {
        var etablissementsParTypeGlobal = etablissements.stream()
            .collect(Collectors.groupingBy(
                e -> e.getTypeEtablissement() != null ? e.getTypeEtablissement().name() : "AUTRE",
                Collectors.summingInt(e -> 1)
            ));

        var filieresSet = etablissements.stream()
            .flatMap(e -> e.getFilieresProposees() != null ? e.getFilieresProposees().stream() : java.util.stream.Stream.empty())
            .map(f -> f.getTitre())
            .collect(Collectors.toSet());

        Map<String, List<FicheEtablissement>> parRegion = new LinkedHashMap<>();
        for (var r : RegionTogo.values()) {
            var matching = etablissements.stream()
                .filter(e -> RegionTogo.fromVille(e.getVille()) == r)
                .toList();
            if (!matching.isEmpty()) {
                parRegion.put(r.getNom(), matching);
            }
        }

        var nonAffectes = etablissements.stream()
            .filter(e -> RegionTogo.fromVille(e.getVille()) == null)
            .toList();

        var regionsList = Arrays.stream(RegionTogo.values())
            .map(r -> {
                var list = parRegion.getOrDefault(r.getNom(), List.of());
                var parType = list.stream()
                    .collect(Collectors.groupingBy(
                        e -> e.getTypeEtablissement() != null ? e.getTypeEtablissement().name() : "AUTRE",
                        Collectors.summingInt(e -> 1)
                    ));
                return new RegionStat(r.getNom(), r.getNomComplet(), list.size(), parType, r.getLatitude(), r.getLongitude());
            })
            .filter(rs -> rs.nombreEtablissements() > 0)
            .collect(Collectors.toCollection(ArrayList::new));

        if (!nonAffectes.isEmpty()) {
            var details = nonAffectes.stream()
                .collect(Collectors.groupingBy(e -> e.getVille() != null ? e.getVille() : "Non spécifiée", Collectors.counting()))
                .entrySet().stream()
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .collect(Collectors.joining(", "));
            var parTypeAutres = nonAffectes.stream()
                .collect(Collectors.groupingBy(
                    e -> e.getTypeEtablissement() != null ? e.getTypeEtablissement().name() : "AUTRE",
                    Collectors.summingInt(e -> 1)
                ));
            regionsList.add(new RegionStat("Autres", "Villes non mappées: " + details,
                nonAffectes.size(), parTypeAutres, 8.5, 0.5));
        }

        return new DataHubResponse(regionsList, etablissements.size(), filieresSet.size(), etablissementsParTypeGlobal);
    }
}
