package tg.edtch.activEducation.prediction.application.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.prediction.application.mapper.PredictionReussiteMapper;
import tg.edtch.activEducation.prediction.application.service.PredictionService;
import tg.edtch.activEducation.prediction.domain.dto.PredictionReussiteRequest;
import tg.edtch.activEducation.prediction.domain.dto.PredictionReussiteResponse;
import tg.edtch.activEducation.prediction.domain.entite.PredictionReussite;
import tg.edtch.activEducation.prediction.repository.PredictionReussiteRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation applicative du service de prédictions de réussite.
 *
 * <p>Source de vérité : {@link PredictionReussiteRepository}. Le mapper est
 * stateless et instancié à la demande (cohérence avec
 * {@code OrientationOutcomeServiceImpl}).</p>
 *
 * <p>Le {@code eleveTrackingId} est passé en argument et jamais lu depuis
 * le body — c'est le path variable qui fait foi. Toute incohérence
 * déclenche {@link IllegalArgumentException}.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PredictionServiceImpl implements PredictionService {

    private final PredictionReussiteRepository repository;

    private PredictionReussiteMapper mapper() {
        return new PredictionReussiteMapper();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PredictionReussiteResponse> listerParEleve(UUID eleveTrackingId) {
        if (eleveTrackingId == null) {
            throw new IllegalArgumentException("eleveTrackingId est requis");
        }
        return repository.findByEleveTrackingIdOrderByDatePredictionDesc(eleveTrackingId.toString())
                .stream()
                .map(this.mapper()::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PredictionReussiteResponse creer(UUID eleveTrackingId, PredictionReussiteRequest request) {
        if (eleveTrackingId == null) {
            throw new IllegalArgumentException("eleveTrackingId (path) est requis");
        }
        if (request == null) {
            throw new IllegalArgumentException("request (body) est requis");
        }
        if (request.filiereTrackingId() == null || request.filiereTrackingId().isBlank()) {
            throw new IllegalArgumentException("filiereTrackingId est requis");
        }

        PredictionReussite entity = mapper().toEntity(eleveTrackingId.toString(), request);
        PredictionReussite saved = repository.save(entity);
        log.info("Prédiction créée eleve={} filiere={} score={}",
                eleveTrackingId, saved.getFiliereTrackingId(), saved.getScorePrediction());
        return mapper().toResponse(saved);
    }
}
