package tg.edtch.activEducation.prediction.application.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.bibliotheque.domain.entite.NiveauFiliere;
import tg.edtch.activEducation.bibliotheque.domain.repository.NiveauFiliereRepository;
import tg.edtch.activEducation.prediction.application.dto.FilierePourNiveauResponse;
import tg.edtch.activEducation.prediction.application.dto.NiveauResponse;
import tg.edtch.activEducation.prediction.application.service.PredictionLookupService;
import tg.edtch.activEducation.profil.domain.enums.NiveauScolaire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implémentation lecture seule pour la Phase 2 du module Prédiction.
 *
 * <p>Le filtrage par niveau s'appuie sur la table {@code niveaux_filieres}
 * (Phase 1 § 2) — c'est la source de vérité algorithmique. Le champ
 * {@code niveau_requis} String de {@link FicheFiliere} est conservé pour
 * le filtrage par libellé existant mais n'est pas utilisé ici.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PredictionLookupServiceImpl implements PredictionLookupService {

    private final NiveauFiliereRepository niveauFiliereRepository;

    @Override
    public List<NiveauResponse> listerNiveaux() {
        return java.util.Arrays.stream(NiveauScolaire.values())
                .map(NiveauResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<FilierePourNiveauResponse> filieresPourNiveau(String niveau) {
        NiveauScolaire ns = NiveauScolaire.parse(niveau);
        if (ns == null) {
            log.debug("Niveau non reconnu en query param : '{}'", niveau);
            return Collections.emptyList();
        }

        List<NiveauFiliere> mappings = niveauFiliereRepository.findByNiveau(ns);
        if (mappings.isEmpty()) {
            return Collections.emptyList();
        }

        // Dédoublonner par fiche (une même filière peut couvrir plusieurs niveaux).
        // On garde l'entrée avec le plus d'infos utiles (n'importe laquelle, on
        // merge les niveaux éligibles plus bas).
        Map<Long, List<NiveauFiliere>> byFiche = mappings.stream()
                .collect(Collectors.groupingBy(m -> m.getFicheFiliere().getId()));

        List<FilierePourNiveauResponse> result = new ArrayList<>(byFiche.size());
        for (Map.Entry<Long, List<NiveauFiliere>> entry : byFiche.entrySet()) {
            List<NiveauFiliere> groupe = entry.getValue();
            FicheFiliere fiche = groupe.get(0).getFicheFiliere();
            List<String> niveauxEligibles = groupe.stream()
                    .map(m -> m.getNiveau().name())
                    .sorted()
                    .collect(Collectors.toList());
            boolean aPrincipal = groupe.stream().anyMatch(m -> Boolean.TRUE.equals(m.getEstPrincipal()));

            result.add(FilierePourNiveauResponse.builder()
                    .trackingId(fiche.getTrackingId())
                    .titre(fiche.getTitre())
                    .domaine(fiche.getDomaine())
                    .duree(fiche.getDuree())
                    .niveauxEligibles(niveauxEligibles)
                    .aNiveauPrincipal(aPrincipal)
                    .build());
        }

        // Tri : d'abord celles qui ont un niveau principal (= entrée naturelle),
        // puis par titre.
        result.sort(Comparator
                .comparing(FilierePourNiveauResponse::getANiveauPrincipal, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(FilierePourNiveauResponse::getTitre, Comparator.nullsLast(Comparator.naturalOrder())));

        return result;
    }
}
