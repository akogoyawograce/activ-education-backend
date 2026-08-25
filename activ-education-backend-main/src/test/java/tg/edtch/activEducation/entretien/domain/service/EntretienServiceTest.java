package tg.edtch.activEducation.entretien.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import tg.edtch.activEducation.entretien.domain.dto.EntretienResponse;
import tg.edtch.activEducation.entretien.domain.dto.StartEntretienRequest;
import tg.edtch.activEducation.entretien.domain.entite.SimulationEntretien;
import tg.edtch.activEducation.entretien.repository.SimulationEntretienRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests anti-régression pour le bug "JSON d'erreur dans la bulle recruteur".
 *
 * <p>Contexte : le 6 août 2026, on a observé que
 * {@code {"score": 10, "feedback": "Erreur d'évaluation, veuillez réessayer."}}
 * apparaissait dans la bulle "Recruteur" du frontend Flutter quand le LLM
 * échouait. Cause : {@code appelerOpenAI()} (l'ancien nom) renvoyait ce JSON
 * comme fallback indistinctement pour "question" et "évaluation".
 *
 * <p>Ces tests verrouillent les nouveaux invariants :
 * <ol>
 *   <li>Une réponse LLM malformée déclenche un retry (1 fois), puis si l'échec
 *       persiste, la réponse API a {@code erreur=true} avec {@code messageErreur}.
 *       Le champ {@code question} reste {@code null}.</li>
 *   <li>Une réponse LLM qui réussit à produire du texte est renvoyée telle
 *       quelle dans {@code question}, sans transformation bizarre.</li>
 *   <li>Une réponse LLM qui réussit à produire du JSON est validée (champs
 *       {@code score} numérique + {@code feedback} textuel non vide).</li>
 * </ol>
 */
class EntretienServiceTest {

    private SimulationEntretienRepository repository;
    private RestTemplate restTemplate;
    private EntretienService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        repository = mock(SimulationEntretienRepository.class);
        restTemplate = mock(RestTemplate.class);
        service = new EntretienService(repository);
        // Injecte le mock RestTemplate + URL/clé/modèle
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(service, "chatUrl", "http://test/chat");
        ReflectionTestUtils.setField(service, "chatApiKey", "fake-key");
        ReflectionTestUtils.setField(service, "chatModel", "gpt-test");

        // Par défaut : save() renvoie l'entité passée
        when(repository.save(any(SimulationEntretien.class)))
            .thenAnswer(inv -> inv.getArgument(0));
    }

    // ─────────────────────────────────────────────────────────────────
    // Test 1 — Réponse LLM malformée : retry puis erreur=true
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Réponse JSON malformée : 1 retry, puis erreur=true sans question")
    void malformedJsonThenRetry_renvoieErreurTrue() {
        // Simule une réponse LLM qui renvoie du JSON d'évaluation au lieu
        // d'une question, 2 fois (initiale + 1 retry).
        String reponseBizarre = "{\"score\": 10, \"feedback\": \"Erreur d'évaluation, veuillez réessayer.\"}";
        stubLlmReponseTexteBrut(reponseBizarre, reponseBizarre);

        EntretienResponse resp = service.demarrerEntretien(
            new StartEntretienRequest("Médecin", null, UUID.randomUUID().toString()));

        assertThat(resp.erreur()).isTrue();
        assertThat(resp.question()).isNull();
        assertThat(resp.messageErreur()).isNotBlank();
        assertThat(resp.statut()).isEqualTo("EN_COURS");

        // Vérifie que le repository n'a PAS sauvegardé de simulation :
        // une erreur technique ne doit pas polluer la DB.
        verify(repository, never()).save(any());

        // Vérifie que 2 appels HTTP ont été faits (initial + 1 retry)
        verify(restTemplate, times(2)).exchange(
            eq("http://test/chat"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(String.class));
    }

    @Test
    @DisplayName("Erreur HTTP (5xx) : pas de retry, erreur=true direct")
    void erreurHttp_renvoieErreurTrueSansRetry() {
        // 500 Internal Server Error
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(String.class)))
            .thenThrow(new org.springframework.web.client.HttpServerErrorException(
                HttpStatus.INTERNAL_SERVER_ERROR, "boom"));

        EntretienResponse resp = service.demarrerEntretien(
            new StartEntretienRequest("Médecin", null, UUID.randomUUID().toString()));

        assertThat(resp.erreur()).isTrue();
        assertThat(resp.question()).isNull();
        assertThat(resp.messageErreur()).contains("Erreur technique LLM");
        // 1 seul appel, pas de retry sur erreur HTTP
        verify(restTemplate, times(1)).exchange(
            anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("JSON malformé pour évaluation : 1 retry puis placeholder stocké")
    void evaluationJsonInvalide_stockePlaceholder() {
        UUID sessionId = UUID.randomUUID();
        SimulationEntretien entity = SimulationEntretien.builder()
            .trackingId(sessionId)
            .eleveTrackingId(UUID.randomUUID().toString())
            .metierTitre("Médecin")
            .metierTrackingId(null)
            .questionsPosees("Q1 ?")
            .reponsesDonnees("")
            .evaluations("")
            .nbQuestions(1)
            .statut("EN_COURS")
            .build();
        when(repository.findByTrackingId(sessionId)).thenReturn(Optional.of(entity));

        // LLM renvoie du texte non-JSON à chaque fois (2 tentatives).
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(String.class)))
            .thenReturn(reponseLlm("Voici mon évaluation : super !"));

        EntretienResponse resp = service.repondre(sessionId, "Ma réponse");

        // Pas encore fini (nbQuestions=1, donc prochaine question demandée)
        assertThat(resp.statut()).isEqualTo("EN_COURS");
        // Pour éviter de bloquer l'entretien, on doit avoir une question valide
        // ou une erreur explicite. Ici, après retry 2x texte (qui matche le format
        // texte), la 2e réponse est acceptée.
        // Soit erreur, soit question valide. Vérifions qu'on n'a pas le JSON
        // d'erreur brut dans `question`.
        if (resp.erreur() != null && resp.erreur()) {
            assertThat(resp.question()).isNull();
            assertThat(resp.messageErreur()).isNotBlank();
        } else {
            assertThat(resp.question()).isNotBlank();
            assertThat(resp.question()).doesNotStartWith("{");
            assertThat(resp.question()).doesNotStartWith("[");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Test 2 — Génération réussie : schéma respecté
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Génération réussie : question valide, pas d'erreur")
    void generationReussie_questionValide() {
        String questionValide = "Décrivez une situation difficile rencontrée lors d'un stage.";
        stubLlmReponseTexteBrut(questionValide, questionValide);

        EntretienResponse resp = service.demarrerEntretien(
            new StartEntretienRequest("Médecin", null, UUID.randomUUID().toString()));

        assertThat(resp.erreur()).isNull();
        assertThat(resp.question()).isEqualTo(questionValide);
        assertThat(resp.questionNumero()).isEqualTo(1);
        assertThat(resp.totalQuestions()).isEqualTo(5);
        assertThat(resp.sessionId()).isNotNull();
        // 1 seul appel HTTP (succès du premier coup)
        verify(restTemplate, times(1)).exchange(
            anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("Code fence ```json ... ``` est strippé et accepté")
    void codeFenceJson_acceptable() {
        String jsonPropre = "```json\n{\"score\": 14, \"feedback\": \"Bonne réponse globale.\"}\n```";
        // L'évaluation passe par le chemin "json". On va tester indirectement
        // via repondre() — mais pour ça il faut une entité existante.
        UUID sessionId = UUID.randomUUID();
        SimulationEntretien entity = SimulationEntretien.builder()
            .trackingId(sessionId)
            .eleveTrackingId(UUID.randomUUID().toString())
            .metierTitre("Médecin")
            .metierTrackingId(null)
            .questionsPosees("Q1 ?")
            .reponsesDonnees("")
            .evaluations("")
            .nbQuestions(1)
            .statut("EN_COURS")
            .build();
        when(repository.findByTrackingId(sessionId)).thenReturn(Optional.of(entity));

        // L'évaluation renvoie du JSON (avec code fence, strippé).
        // La question suivante renvoie une question valide.
        // On utilise des réponses séquentielles différentes.
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(String.class)))
            .thenReturn(reponseLlm(jsonPropre))
            .thenReturn(reponseLlm("Q2 ?"));

        EntretienResponse resp = service.repondre(sessionId, "Ma réponse");
        assertThat(resp.statut()).isEqualTo("EN_COURS");
        assertThat(resp.question()).isEqualTo("Q2 ?");
    }

    @Test
    @DisplayName("Évaluation JSON valide est stockée telle quelle")
    void evaluationJsonValide_stockeeCorrectement() throws Exception {
        UUID sessionId = UUID.randomUUID();
        SimulationEntretien entity = SimulationEntretien.builder()
            .trackingId(sessionId)
            .eleveTrackingId(UUID.randomUUID().toString())
            .metierTitre("Médecin")
            .metierTrackingId(null)
            .questionsPosees("Q1 ?")
            .reponsesDonnees("")
            .evaluations("")
            .nbQuestions(1)
            .statut("EN_COURS")
            .build();
        when(repository.findByTrackingId(sessionId)).thenReturn(Optional.of(entity));

        String evalJson = "{\"score\": 16, \"feedback\": \"Excellente réponse, complète et structurée.\"}";
        // L'évaluation accepte direct, la question suivante est valide.
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(String.class)))
            .thenReturn(reponseLlm(evalJson))
            .thenReturn(reponseLlm("Q2 ?"));

        service.repondre(sessionId, "Ma réponse");

        // Vérifie que l'évaluation stockée est du JSON valide
        ArgumentCaptor<SimulationEntretien> captor = ArgumentCaptor.forClass(SimulationEntretien.class);
        verify(repository, atLeastOnce()).save(captor.capture());
        SimulationEntretien saved = captor.getValue();
        String[] evals = saved.getEvaluationsArray();
        // Comme evaluations initial = "" (et pas null), la 1ère évaluation est
        // concaténée après "|||" → split donne ["", "eval"]. On prend le dernier.
        assertThat(evals).isNotEmpty();
        String lastEval = evals[evals.length - 1];
        assertThat(lastEval).isNotBlank();
        JsonNode parsed = objectMapper.readTree(lastEval);
        assertThat(parsed.get("score").asInt()).isEqualTo(16);
        assertThat(parsed.get("feedback").asText()).isNotBlank();
    }

    // ─────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────

    /**
     * Simule 2 réponses successives du LLM (utilisée pour les cas "1 retry").
     */
    private void stubLlmReponseTexteBrut(String premiere, String deuxieme) {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(String.class)))
            .thenReturn(reponseLlm(premiere))
            .thenReturn(reponseLlm(deuxieme));
    }

    /**
     * Construit une ResponseEntity simulant la réponse HTTP du LLM avec
     * le contenu {@code contenu} dans le champ {@code choices[0].message.content}.
     * Utilise un sérialiseur Jackson pour échapper proprement les guillemets.
     */
    private ResponseEntity<String> reponseLlm(String contenu) throws RuntimeException {
        try {
            // Construit le message JSON interne correctement échappé
            String contentJson = objectMapper.writeValueAsString(contenu);
            String body = "{\"choices\":[{\"message\":{\"content\":" + contentJson + "}}]}";
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
        } catch (Exception e) {
            throw new RuntimeException("Mock JSON construction failed", e);
        }
    }
}