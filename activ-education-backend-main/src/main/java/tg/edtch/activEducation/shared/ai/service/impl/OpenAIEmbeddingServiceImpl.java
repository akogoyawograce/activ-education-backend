package tg.edtch.activEducation.shared.ai.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tg.edtch.activEducation.shared.ai.service.AIEmbeddingService;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIEmbeddingServiceImpl implements AIEmbeddingService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.embedding.model:text-embedding-3-small}")
    private String embeddingModel;

    @Value("${openai.api.chat.model:gpt-4o-mini}")
    private String chatModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public float[] generateEmbedding(String text) {
        String url = "https://api.openai.com/v1/embeddings";

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", embeddingModel);
            payload.put("input", text);
            payload.put("dimensions", 768);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            var response = restTemplate.postForEntity(url, requestEntity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode valuesNode = root.path("data").get(0).path("embedding");

            if (valuesNode.isArray()) {
                float[] embedding = new float[valuesNode.size()];
                for (int i = 0; i < valuesNode.size(); i++) {
                    embedding[i] = (float) valuesNode.get(i).asDouble();
                }
                return embedding;
            } else {
                log.error("Réponse inattendue de l'API OpenAI Embedding : {}", response.getBody());
                throw new RuntimeException("Format de réponse invalide de l'API OpenAI");
            }

        } catch (Exception e) {
            log.error("Erreur lors de la génération de l'embedding OpenAI", e);
            throw new RuntimeException("Erreur de génération d'embedding: " + e.getMessage());
        }
    }

    @Override
    public String generateAnswer(String question, List<String> contextes) {
        String url = "https://api.openai.com/v1/chat/completions";

        try {
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("Tu es le conseiller d'un établissement scolaire. ")
                    .append("Réponds à la question de l'élève en te basant UNIQUEMENT sur le contexte suivant. ")
                    .append("Si l'information ne s'y trouve pas, dis simplement que tu ne sais pas.\n\n")
                    .append("CONTEXTE :\n");

            for (int i = 0; i < contextes.size(); i++) {
                promptBuilder.append("[").append(i + 1).append("] ").append(contextes.get(i)).append("\n");
            }
            promptBuilder.append("\nQUESTION : ").append(question);

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", chatModel);
            payload.put("messages", List.of(Map.of("role", "user", "content", promptBuilder.toString())));
            payload.put("temperature", 0.7);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            var response = restTemplate.postForEntity(url, requestEntity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode textNode = root.path("choices").get(0).path("message").path("content");

            if (textNode.isMissingNode()) {
                throw new RuntimeException("Aucun texte retourné par OpenAI");
            }
            return textNode.asText();

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                log.warn("Quota OpenAI dépassé (429). Modèle: {}", chatModel);
                return "Notre assistant IA est momentanément indisponible (quota atteint). Veuillez réessayer dans quelques instants ou demain.";
            }
            log.error("Erreur HTTP lors de la génération OpenAI: {} - {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Erreur de génération RAG: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur lors de la génération de réponse OpenAI", e);
            throw new RuntimeException("Erreur de génération RAG: " + e.getMessage());
        }
    }

    @Override
    public String extractTextFromImage(byte[] imageData, String mimeType) {
        String url = "https://api.openai.com/v1/chat/completions";

        try {
            String base64Image = Base64.getEncoder().encodeToString(imageData);

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", chatModel);
            payload.put("temperature", 0.1);

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("type", "text");
            textPart.put("text", "Extrais UNIQUEMENT les noms de matières et les notes chiffrées de ce bulletin scolaire. " +
                    "Retourne-les sous format JSON : [{\"matiere\": \"Mathématiques\", \"note\": 15.5}, ...]. " +
                    "Si la note a un coefficient, inclue-le : [{\"matiere\": \"Maths\", \"note\": 14, \"coefficient\": 3}, ...]. " +
                    "Ne retourne que le JSON, rien d'autre.");

            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");
            imageContent.put("image_url", Map.of("url", "data:" + mimeType + ";base64," + base64Image));

            payload.put("messages", List.of(Map.of(
                    "role", "user",
                    "content", List.of(textPart, imageContent)
            )));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            var response = restTemplate.postForEntity(url, requestEntity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode textNode = root.path("choices").get(0).path("message").path("content");

            if (textNode.isMissingNode()) {
                throw new RuntimeException("Aucun texte extrait par OpenAI Vision");
            }
            return textNode.asText();

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                log.warn("Quota OpenAI dépassé (429) pour OCR");
                return "[]";
            }
            log.error("Erreur HTTP OpenAI Vision: {} - {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Erreur OCR: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur OCR OpenAI", e);
            throw new RuntimeException("Erreur OCR: " + e.getMessage());
        }
    }
}
