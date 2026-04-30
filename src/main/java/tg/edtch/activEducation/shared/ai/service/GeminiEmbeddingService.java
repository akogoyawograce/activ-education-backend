package tg.edtch.activEducation.shared.ai.service;

import java.util.List;

public interface GeminiEmbeddingService {
    float[] generateEmbedding(String text);

    String generateAnswer(String question, List<String> contextes);
}
