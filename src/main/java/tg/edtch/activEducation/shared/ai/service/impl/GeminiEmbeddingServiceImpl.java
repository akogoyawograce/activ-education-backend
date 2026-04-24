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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiEmbeddingServiceImpl implements GeminiEmbeddingService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public float[] generateEmbedding(String text) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent?key="
                + geminiApiKey;

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "models/text-embedding-004");
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
}
