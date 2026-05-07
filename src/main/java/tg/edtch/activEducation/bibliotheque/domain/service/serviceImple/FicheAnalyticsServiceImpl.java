package tg.edtch.activEducation.bibliotheque.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.application.dto.response.RechercheGlobaleResponse;
import tg.edtch.activEducation.bibliotheque.domain.entite.Fiche;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheAnalyticsService;
import tg.edtch.activEducation.bibliotheque.repository.FicheRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FicheAnalyticsServiceImpl implements FicheAnalyticsService {

    private final FicheRepository ficheRepository;

    @Override
    public List<RechercheGlobaleResponse> getTendances(int limite) {
        log.debug("Récupération des fiches tendances, limite={}", limite);
        List<Long> ids = ficheRepository.trouverIdsTendances(limite);
        return chargerEtMapper(ids);
    }

    @Override
    public List<RechercheGlobaleResponse> getConsultationsRecentes(UUID utilisateurTrackingId, int limite) {
        log.debug("Récupération des fiches récemment consultées par utilisateur={}, limite={}", utilisateurTrackingId,
                limite);
        List<Long> ids = ficheRepository.trouverIdsConsultationsRecentes(utilisateurTrackingId, limite);
        return chargerEtMapper(ids);
    }

    @Override
    public List<RechercheGlobaleResponse> getFichesSimilaires(UUID trackingId, int limite) {
        log.debug("Récupération des fiches similaires pour trackingId={}, limite={}", trackingId, limite);
        Fiche cible = ficheRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Fiche non trouvée avec ID : " + trackingId));

        if (cible.getEmbedding() == null) {
            return List.of();
        }

        List<Long> ids = ficheRepository.trouverIdsFichesSimilaires(cible.getId(), cible.getEmbedding(), limite);
        return chargerEtMapper(ids);
    }

    private List<RechercheGlobaleResponse> chargerEtMapper(List<Long> ids) {
        if (ids.isEmpty())
            return List.of();

        // Le ORDER BY CASE fonctionne bien jusqu'à 10, sinon tri Java
        List<Fiche> fiches = (ids.size() <= 10)
                ? ficheRepository.trouverParIdsOrdonnes(ids)
                : ficheRepository.findAllById(ids).stream()
                        .sorted(Comparator.comparingInt(f -> ids.indexOf(f.getId())))
                        .collect(Collectors.toList());

        return fiches.stream()
                .map(this::mapperVersResponse)
                .collect(Collectors.toList());
    }

    private RechercheGlobaleResponse mapperVersResponse(Fiche fiche) {
        String imageCouverture = (fiche.getImageUrls() != null && !fiche.getImageUrls().isEmpty())
                ? fiche.getImageUrls().iterator().next()
                : null;

        return RechercheGlobaleResponse.builder()
                .trackingId(fiche.getTrackingId())
                .typeResultat(fiche.getTypeResultat())
                .titre(fiche.getTitre())
                .resume(fiche.getResume())
                .imageCouverture(imageCouverture)
                .build();
    }
}
