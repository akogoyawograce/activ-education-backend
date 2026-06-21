package tg.edtch.activEducation.shared.ai.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tg.edtch.activEducation.shared.ai.service.GeminiEmbeddingService;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiEmbeddingServiceImpl implements GeminiEmbeddingService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.generate.base-url}")
    private String geminiGenerateBaseUrl;

    @Value("${gemini.api.generate.model}")
    private String geminiGenerateModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public float[] generateEmbedding(String text) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-2:embedContent?key="
                + geminiApiKey;

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "models/gemini-embedding-2");
            payload.put("outputDimensionality", 768);
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", text);
            content.put("parts", List.of(part));
            payload.put("content", content);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            JsonNode rootResponse = objectMapper.readTree(response.getBody());
            JsonNode valuesNode = rootResponse.path("embedding").path("values");

            if (valuesNode.isArray()) {
                float[] embedding = new float[valuesNode.size()];
                for (int i = 0; i < valuesNode.size(); i++) {
                    embedding[i] = (float) valuesNode.get(i).asDouble();
                }
                return embedding;
            } else {
                log.error("Réponse inattendue de l'API Gemini : {}", response.getBody());
                throw new RuntimeException("Format de réponse invalide de l'API Gemini");
            }

        } catch (Exception e) {
            log.error("Erreur lors de la génération de l'embedding Gemini", e);
            throw new RuntimeException("Erreur de génération d'embedding: " + e.getMessage());
        }
    }

    @Override
    public String generateAnswer(String question, List<String> contextes) {
        String url = geminiGenerateBaseUrl + "/models/" + geminiGenerateModel + ":generateContent?key="
                + geminiApiKey;

        try {
            // Construction du prompt RAG
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("Tu es le conseiller d'un établissement scolaire. ")
                    .append("Réponds à la question de l'élève en te basant UNIQUEMENT sur le contexte suivant. ")
                    .append("Si l'information ne s'y trouve pas, dis simplement que tu ne sais pas.\n\n")
                    .append("CONTEXTE :\n");

            for (int i = 0; i < contextes.size(); i++) {
                promptBuilder.append("[").append(i + 1).append("] ").append(contextes.get(i)).append("\n");
            }
            promptBuilder.append("\nQUESTION : ").append(question);

            // Construction du JSON payload
            Map<String, Object> payload = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", promptBuilder.toString());
            content.put("parts", List.of(part));
            payload.put("contents", List.of(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            JsonNode rootResponse = objectMapper.readTree(response.getBody());

            JsonNode textNode = rootResponse.path("candidates").get(0).path("content").path("parts").get(0)
                    .path("text");
            if (textNode.isMissingNode()) {
                throw new RuntimeException("Aucun texte retourné par Gemini RAG");
            }
            return textNode.asText();

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Gestion spécifique des erreurs 4xx de l'API Gemini
            if (e.getStatusCode().value() == 429) {
                log.warn("Quota Gemini RAG dépassé (429). Modèle: {}", geminiGenerateModel);
                return "Notre assistant IA est momentanément indisponible (quota atteint). Veuillez réessayer dans quelques instants ou demain.";
            }
            log.error("Erreur HTTP lors de la génération RAG Gemini: {} - {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Erreur de génération RAG: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur lors de la génération de réponse RAG Gemini", e);
            throw new RuntimeException("Erreur de génération RAG: " + e.getMessage());
        }
    }

    @Override
    public String extractTextFromImage(byte[] imageData, String mimeType) {
        String url = geminiGenerateBaseUrl + "/models/" + geminiGenerateModel + ":generateContent?key="
                + geminiApiKey;

        try {
            String base64Image = Base64.getEncoder().encodeToString(imageData);

            Map<String, Object> payload = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", "Extrais UNIQUEMENT les noms de matières et les notes chiffrées de ce bulletin scolaire. " +
                    "Retourne-les sous format JSON : [{\"matiere\": \"Mathématiques\", \"note\": 15.5}, ...]. " +
                    "Si la note a un coefficient, inclue-le : [{\"matiere\": \"Maths\", \"note\": 14, \"coefficient\": 3}, ...]. " +
                    "Ne retourne que le JSON, rien d'autre.");

            Map<String, Object> imagePart = new HashMap<>();
            Map<String, String> inlineData = new HashMap<>();
            inlineData.put("mimeType", mimeType);
            inlineData.put("data", base64Image);
            imagePart.put("inlineData", inlineData);

            content.put("parts", List.of(textPart, imagePart));
            payload.put("contents", List.of(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            JsonNode rootResponse = objectMapper.readTree(response.getBody());

            JsonNode textNode = rootResponse.path("candidates").get(0).path("content").path("parts").get(0)
                    .path("text");
            if (textNode.isMissingNode()) {
                throw new RuntimeException("Aucun texte extrait par Gemini Vision");
            }
            return textNode.asText();

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                log.warn("Quota Gemini dépassé (429) pour OCR");
                return "[]";
            }
            log.error("Erreur HTTP Gemini Vision: {} - {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Erreur OCR: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur OCR Gemini", e);
            throw new RuntimeException("Erreur OCR: " + e.getMessage());
        }
    }
}
