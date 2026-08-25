package tg.edtch.activEducation.shared.ai.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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
 * <p>Contexte : ajouté 2026-08-03 pour vérifier que le RAG continue de fonctionner
 * quand la clé OpenAI est invalide/révoquée (cf. JOURNAL_BORD_IA.md).
 *
 * <p>Hypothèses :
 * <ul>
 *   <li>Ollama embeddings : {@code {"embedding": [0.1, 0.2, ...]}}</li>
 *   <li>Ollama chat : {@code {"response": "..."}}</li>
 *   <li>OpenAI chat : {@code {"choices":[{"message":{"content":"..."}}]}}</li>
 * </ul>
 * </p>
 */
class OpenAIEmbeddingServiceImplTest {

    private RestTemplate restTemplate;
    private OpenAIEmbeddingServiceImpl service;

    private static final String OLLAMA_EMB_URL = "http://mock-ollama:11434";
    private static final String OLLAMA_CHAT_URL = "http://mock-ollama:11434";
    private static final String OLLAMA_CHAT_MODEL = "qwen2:0.5b";

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        service = new OpenAIEmbeddingServiceImpl();
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(service, "ollamaEmbeddingUrl", OLLAMA_EMB_URL);
        ReflectionTestUtils.setField(service, "ollamaEmbeddingModel", "nomic-embed-text");
        ReflectionTestUtils.setField(service, "ollamaChatUrl", OLLAMA_CHAT_URL);
        ReflectionTestUtils.setField(service, "ollamaChatModel", OLLAMA_CHAT_MODEL);
        ReflectionTestUtils.setField(service, "chatUrl", "http://test-openai/chat");
        ReflectionTestUtils.setField(service, "chatModel", "gpt-test");
    }

    // ─────────────────────────────────────────────────────────────────
    // generateEmbedding — tests existants (3 août 2026)
    // ─────────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────────
    // generateAnswer — tests ajoutés le 6 août 2026
    // (cf. JOURNAL_BORD_IA.md §6 — bug widget "Ma recommandation")
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateAnswer — fallback Ollama")
    class GenerateAnswerFallback {

        @Test
        @DisplayName("Clé OpenAI REVOKED → fallback Ollama direct, pas d'appel OpenAI")
        void revokedKeyTriggersOllamaFallback() {
            ReflectionTestUtils.setField(service, "openaiApiKey", "REVOKED_abc");
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(
                    "{\"response\":\"Voici 3 filières adaptées...\"}"));

            String result = service.generateAnswer(
                "Recommandation pour l'élève",
                List.of("Profil: Terminale S", "Filières dispo au Togo"));

            assertThat(result).isEqualTo("Voici 3 filières adaptées...");
            // Vérifie que l'URL appelée est bien Ollama chat, pas OpenAI
            verify(restTemplate).postForEntity(
                eq("http://mock-ollama:11434/api/generate"),
                any(),
                eq(String.class)
            );
        }

        @Test
        @DisplayName("Clé OpenAI valide mais 401 → fallback Ollama automatique")
        void openaiUnauthorizedTriggersOllamaFallback() {
            ReflectionTestUtils.setField(service, "openaiApiKey", "sk-looks-valid");

            // 1er appel (OpenAI) → 401, 2e appel (Ollama) → succès
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new org.springframework.web.client.HttpClientErrorException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized"))
                .thenReturn(ResponseEntity.ok(
                    "{\"response\":\"Recommandation générée par Ollama\"}"));

            String result = service.generateAnswer("Q?", List.of("ctx"));

            assertThat(result).isEqualTo("Recommandation générée par Ollama");
            // 2 appels : OpenAI (échoue) + Ollama (succès)
            verify(restTemplate, org.mockito.Mockito.times(2))
                .postForEntity(anyString(), any(), eq(String.class));
        }

        @Test
        @DisplayName("Clé OpenAI valide ET appel OK → Ollama n'est PAS appelé")
        void validKeyUsesOpenAIDirectlyForAnswer() {
            ReflectionTestUtils.setField(service, "openaiApiKey", "sk-valid");

            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(
                    "{\"choices\":[{\"message\":{\"content\":\"Réponse OpenAI\"}}]}"));

            String result = service.generateAnswer("Q?", List.of("ctx"));

            assertThat(result).isEqualTo("Réponse OpenAI");
            // Vérifie que l'URL OpenAI est appelée (pas Ollama)
            verify(restTemplate).postForEntity(
                eq("http://test-openai/chat"),
                any(),
                eq(String.class)
            );
        }

        @Test
        @DisplayName("Quota OpenAI (429) → exception dédiée, pas de fallback Ollama")
        void openaiQuotaThrowsDedicatedMessage() {
            ReflectionTestUtils.setField(service, "openaiApiKey", "sk-valid-but-quota");

            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new org.springframework.web.client.HttpClientErrorException(
                    org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, "quota"));

            // 429 = quota, on NE tente PAS Ollama (Ollama ne résout pas le quota).
            // Le service appelant (RecommandationIAService) catche l'exception
            // et renvoie un statut ERREUR_LLM.
            assertThatThrownBy(() -> service.generateAnswer("Q?", List.of("ctx")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("quota");

            // Vérifie qu'on n'a fait qu'un seul appel (pas de fallback Ollama)
            verify(restTemplate, org.mockito.Mockito.times(1))
                .postForEntity(anyString(), any(), eq(String.class));
        }
    }
}
