package tg.edtch.activEducation.diagnostic.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.diagnostic.application.dto.request.QuizRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.QuizResponse;
import tg.edtch.activEducation.diagnostic.application.mapper.QuizMapper;
import tg.edtch.activEducation.diagnostic.domain.entite.Quiz;
import tg.edtch.activEducation.diagnostic.domain.service.QuizService;
import tg.edtch.activEducation.diagnostic.repository.QuizRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;

    @Override
    public QuizResponse creerQuiz(QuizRequest request) {
        Quiz quiz = quizMapper.toEntity(request);
        Quiz saved = quizRepository.save(quiz);
        log.info("Quiz créé : titre='{}' trackingId={}", saved.getTitre(), saved.getTrackingId());
        return quizMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResponse getQuiz(UUID trackingId) {
        return quizMapper.toResponse(findOrThrow(trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizResponse> listerActifs(Pageable pageable) {
        return quizRepository.findAllByEstActifTrue(pageable).map(quizMapper::toResponse);
    }

    @Override
    public QuizResponse modifierQuiz(UUID trackingId, QuizRequest request) {
        Quiz quiz = findOrThrow(trackingId);
        quizMapper.updateFromRequest(request, quiz);
        Quiz saved = quizRepository.save(quiz);
        log.info("Quiz modifié : trackingId={}", trackingId);
        return quizMapper.toResponse(saved);
    }

    @Override
    public void desactiverQuiz(UUID trackingId) {
        Quiz quiz = findOrThrow(trackingId);
        quiz.setEstActif(false);
        quizRepository.save(quiz);
        log.info("Quiz désactivé (soft-delete) : trackingId={}", trackingId);
    }

    private Quiz findOrThrow(UUID trackingId) {
        return quizRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Quiz introuvable pour le trackingId : " + trackingId));
    }
}
