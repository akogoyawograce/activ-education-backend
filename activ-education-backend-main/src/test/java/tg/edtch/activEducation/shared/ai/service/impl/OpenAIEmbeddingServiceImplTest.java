package tg.edtch.activEducation.shared.ai.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests sur le fallback Ollama de {@link OpenAIEmbeddingServiceImpl}.
 *
 * Contexte : ajouté 2026-08-03 pour vérifier que le RAG continue de fonctionner
 * quand la clé OpenAI est invalide/révoquée (cf. JOURNAL_BORD_IA.md).
 *
 * Hypothèse : Ollama local répond avec `{"embedding": [0.1, 0.2, ...]}`.
 */
class OpenAIEmbeddingServiceImplTest {

    private RestTemplate restTemplate;
    private OpenAIEmbeddingServiceImpl service;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        service = new OpenAIEmbeddingServiceImpl();
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(service, "ollamaEmbeddingUrl", "http://mock-ollama:11434");
        ReflectionTestUtils.setField(service, "ollamaEmbeddingModel", "nomic-embed-text");
    }

    @Test
    @DisplayName("Clé OpenAI REVOKED → fallback Ollama direct, pas d'appel OpenAI")
    void revokedKeyTriggersOllamaFallback() {
        ReflectionTestUtils.setField(service, "openaiApiKey", "REVOKED_abc123");
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenReturn(ResponseEntity.ok("{\"embedding\":[0.1,0.2,0.3]}"));

        float[] result = service.generateEmbedding("test text");

        assertThat(result).containsExactly(0.1f, 0.2f, 0.3f);
        // Vérifie que l'URL appelée est bien celle d'Ollama, pas OpenAI
        verify(restTemplate).postForEntity(
            eq("http://mock-ollama:11434/api/embeddings"),
            any(),
            eq(String.class)
        );
    }

    @Test
    @DisplayName("Clé OpenAI vide → fallback Ollama direct")
    void blankKeyTriggersOllamaFallback() {
        ReflectionTestUtils.setField(service, "openaiApiKey", "  ");

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenReturn(ResponseEntity.ok("{\"embedding\":[0.5]}"));

        float[] result = service.generateEmbedding("bonjour");

        assertThat(result).containsExactly(0.5f);
    }

    @Test
    @DisplayName("Clé OpenAI valide mais appel échoue → fallback Ollama automatique")
    void openaiFailureTriggersOllamaFallback() {
        ReflectionTestUtils.setField(service, "openaiApiKey", "sk-valid-looking-but-401");

        // OpenAI plante au premier appel, Ollama réussit au second
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenThrow(new org.springframework.web.client.HttpClientErrorException(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized"))
            .thenReturn(ResponseEntity.ok("{\"embedding\":[0.7,0.8]}"));

        float[] result = service.generateEmbedding("recommandation");

        assertThat(result).containsExactly(0.7f, 0.8f);
        // 2 appels : OpenAI (échoue) + Ollama (succès)
        verify(restTemplate, org.mockito.Mockito.times(2))
            .postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("Clé OpenAI valide ET appel OK → Ollama n'est PAS appelé")
    void validKeyUsesOpenAIDirectly() {
        // Mock OpenAI → on intercepte via captor
        ReflectionTestUtils.setField(service, "openaiApiKey", "sk-valid");

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenReturn(ResponseEntity.ok("{\"data\":[{\"embedding\":[0.9,0.8,0.7]}]}"));

        float[] result = service.generateEmbedding("anything");

        assertThat(result).containsExactly(0.9f, 0.8f, 0.7f);
        // Vérifie que l'URL OpenAI est appelée (pas Ollama)
        verify(restTemplate).postForEntity(
            eq("https://api.openai.com/v1/embeddings"),
            any(),
            eq(String.class)
        );
    }

    @Test
    @DisplayName("Ollama renvoie un body invalide → exception explicite")
    void ollamaInvalidResponseThrows() {
        ReflectionTestUtils.setField(service, "openaiApiKey", "REVOKED_x");

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenReturn(ResponseEntity.ok("{\"error\":\"model not found\"}"));

        assertThatThrownBy(() -> service.generateEmbedding("test"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Ollama");
    }

    @Test
    @DisplayName("Ollama totalement indisponible → exception remontée")
    void ollamaNetworkFailureThrows() {
        ReflectionTestUtils.setField(service, "openaiApiKey", "REVOKED_x");

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenThrow(new org.springframework.web.client.ResourceAccessException("Connection refused"));

        assertThatThrownBy(() -> service.generateEmbedding("test"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Ollama");
    }
}
