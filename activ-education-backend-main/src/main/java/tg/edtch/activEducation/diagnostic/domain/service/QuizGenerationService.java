package tg.edtch.activEducation.diagnostic.domain.service;

import tg.edtch.activEducation.diagnostic.application.dto.response.QuizGenerationResponse;

import java.util.UUID;

public interface QuizGenerationService {

    QuizGenerationResponse genererOuRecupererQuiz(String type, UUID entityId, int nombre);
}
