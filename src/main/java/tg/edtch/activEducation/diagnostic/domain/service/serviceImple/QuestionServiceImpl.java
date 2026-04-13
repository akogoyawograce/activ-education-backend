package tg.edtch.activEducation.diagnostic.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.diagnostic.application.dto.request.QuestionRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.QuestionResponse;
import tg.edtch.activEducation.diagnostic.application.mapper.QuestionMapper;
import tg.edtch.activEducation.diagnostic.domain.entite.Question;
import tg.edtch.activEducation.diagnostic.domain.entite.Quiz;
import tg.edtch.activEducation.diagnostic.domain.service.QuestionService;
import tg.edtch.activEducation.diagnostic.repository.QuestionRepository;
import tg.edtch.activEducation.diagnostic.repository.QuizRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final QuestionMapper questionMapper;

    @Override
    public QuestionResponse ajouterQuestion(UUID quizTrackingId, QuestionRequest request) {
        Quiz quiz = quizRepository.findByTrackingId(quizTrackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Quiz introuvable pour le trackingId : " + quizTrackingId));

        if (!quiz.getEstActif()) {
            throw new IllegalStateException("Impossible d'ajouter une question à un quiz désactivé.");
        }

        Question question = questionMapper.toEntity(request, quiz);
        Question saved = questionRepository.save(question);
        log.info("Question ajoutée au quiz {} : trackingId={}", quizTrackingId, saved.getTrackingId());
        return questionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getQuestion(UUID trackingId) {
        return questionMapper.toResponse(findOrThrow(trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsParQuiz(UUID quizTrackingId) {
        return questionRepository.findByQuizTrackingIdOrderByOrdreAsc(quizTrackingId)
                .stream()
                .map(questionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionResponse modifierQuestion(UUID trackingId, QuestionRequest request) {
        Question question = findOrThrow(trackingId);
        questionMapper.updateFromRequest(request, question);
        Question saved = questionRepository.save(question);
        log.info("Question modifiée : trackingId={}", trackingId);
        return questionMapper.toResponse(saved);
    }

    @Override
    public void supprimerQuestion(UUID trackingId) {
        Question question = findOrThrow(trackingId);
        questionRepository.delete(question);
        log.info("Question supprimée (+ réponses en cascade) : trackingId={}", trackingId);
    }

    private Question findOrThrow(UUID trackingId) {
        return questionRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Question introuvable pour le trackingId : " + trackingId));
    }
}
