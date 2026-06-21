package tg.edtch.activEducation.diagnostic.domain.service;

import tg.edtch.activEducation.diagnostic.application.dto.response.QuestionResponse;

import java.util.List;
import java.util.UUID;

public interface QuizRecommendationService {

    List<QuestionResponse> recommanderQuestions(UUID eleveTrackingId, UUID quizTrackingId, int nombreQuestions);
}
