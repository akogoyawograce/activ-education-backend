package tg.edtch.activEducation.diagnostic.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.diagnostic.application.dto.request.ScoreMatriceRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.ScoreMatriceResponse;
import tg.edtch.activEducation.diagnostic.application.mapper.ScoreMatriceMapper;
import tg.edtch.activEducation.diagnostic.domain.entite.ScoreMatrice;
import tg.edtch.activEducation.diagnostic.domain.service.ScoreMatriceService;
import tg.edtch.activEducation.diagnostic.repository.ScoreMatriceRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScoreMatriceServiceImpl implements ScoreMatriceService {

    private final ScoreMatriceRepository scoreMatriceRepository;
    private final ScoreMatriceMapper scoreMatriceMapper;

    @Override
    public ScoreMatriceResponse creerMatrice(ScoreMatriceRequest request) {
        ScoreMatrice matrice = scoreMatriceMapper.toEntity(request);
        ScoreMatrice saved = scoreMatriceRepository.save(matrice);
        log.info("ScoreMatrice créée : titre='{}' total={} trackingId={}",
                saved.getTitreMatrice(), saved.getScoreTotalEstime(), saved.getTrackingId());
        return scoreMatriceMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ScoreMatriceResponse getMatrice(UUID trackingId) {
        return scoreMatriceMapper.toResponse(findOrThrow(trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScoreMatriceResponse> listerMatrices(Pageable pageable) {
        return scoreMatriceRepository.findAllBy(pageable).map(scoreMatriceMapper::toResponse);
    }

    @Override
    public ScoreMatriceResponse modifierMatrice(UUID trackingId, ScoreMatriceRequest request) {
        ScoreMatrice matrice = findOrThrow(trackingId);
        scoreMatriceMapper.updateFromRequest(request, matrice);
        ScoreMatrice saved = scoreMatriceRepository.save(matrice);
        log.info("ScoreMatrice modifiée : trackingId={} totalEstimé={}", trackingId, saved.getScoreTotalEstime());
        return scoreMatriceMapper.toResponse(saved);
    }

    @Override
    public void supprimerMatrice(UUID trackingId) {
        ScoreMatrice matrice = findOrThrow(trackingId);
        scoreMatriceRepository.delete(matrice);
        log.info("ScoreMatrice supprimée : trackingId={}", trackingId);
    }

    private ScoreMatrice findOrThrow(UUID trackingId) {
        return scoreMatriceRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "ScoreMatrice introuvable pour le trackingId : " + trackingId));
    }
}
