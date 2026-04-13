package tg.edtch.activEducation.diagnostic.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.diagnostic.application.dto.request.ReponseRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.ReponseResponse;
import tg.edtch.activEducation.diagnostic.application.mapper.ReponseMapper;
import tg.edtch.activEducation.diagnostic.domain.entite.Question;
import tg.edtch.activEducation.diagnostic.domain.entite.Reponse;
import tg.edtch.activEducation.diagnostic.domain.service.ReponseService;
import tg.edtch.activEducation.diagnostic.repository.QuestionRepository;
import tg.edtch.activEducation.diagnostic.repository.ReponseRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReponseServiceImpl implements ReponseService {

    private final ReponseRepository reponseRepository;
    private final QuestionRepository questionRepository;
    private final ReponseMapper reponseMapper;

    @Override
    public ReponseResponse ajouterReponse(UUID questionTrackingId, ReponseRequest request) {
        Question question = questionRepository.findByTrackingId(questionTrackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Question introuvable pour le trackingId : " + questionTrackingId));

        Reponse reponse = reponseMapper.toEntity(request, question);
        Reponse saved = reponseRepository.save(reponse);
        log.info("Réponse ajoutée à la question {} : categorie={} points={} trackingId={}",
                questionTrackingId, saved.getCategoriePoint(), saved.getPoints(), saved.getTrackingId());
        return reponseMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReponseResponse getReponse(UUID trackingId) {
        return reponseMapper.toResponse(findOrThrow(trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReponseResponse> getReponsesParQuestion(UUID questionTrackingId) {
        return reponseRepository.findByQuestionTrackingId(questionTrackingId)
                .stream()
                .map(reponseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReponseResponse modifierReponse(UUID trackingId, ReponseRequest request) {
        Reponse reponse = findOrThrow(trackingId);
        reponseMapper.updateFromRequest(request, reponse);
        Reponse saved = reponseRepository.save(reponse);
        log.info("Réponse modifiée : trackingId={}", trackingId);
        return reponseMapper.toResponse(saved);
    }

    @Override
    public void supprimerReponse(UUID trackingId) {
        Reponse reponse = findOrThrow(trackingId);
        reponseRepository.delete(reponse);
        log.info("Réponse supprimée (hard-delete) : trackingId={}", trackingId);
    }

    private Reponse findOrThrow(UUID trackingId) {
        return reponseRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Réponse introuvable pour le trackingId : " + trackingId));
    }
}
