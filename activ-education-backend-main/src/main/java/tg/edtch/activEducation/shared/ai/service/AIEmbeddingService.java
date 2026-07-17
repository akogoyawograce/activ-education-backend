package tg.edtch.activEducation.shared.ai.service;

import java.util.List;

public interface AIEmbeddingService {
    float[] generateEmbedding(String text);

    String generateAnswer(String question, List<String> contextes);

    String extractTextFromImage(byte[] imageData, String mimeType);

    String generateQuizQuestions(String context, int nombre);

    String transcribeAudio(byte[] audioData, String filename);

    byte[] generateSpeech(String text);
}
