package tg.edtch.activEducation.entretien.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tg.edtch.activEducation.entretien.domain.dto.*;
import tg.edtch.activEducation.entretien.domain.entite.SimulationEntretien;
import tg.edtch.activEducation.entretien.repository.SimulationEntretienRepository;

import java.util.*;

@Service
@Transactional
public class EntretienService {

    private static final int NB_QUESTIONS = 5;
    private static final String SYSTEM_PROMPT = """
        Tu es un recruteur qui mène un entretien d'embauche pour le métier de %s.
        Pose une question professionnelle pertinente, spécifique au métier et au contexte togolais.
        La question doit être en français, concise (max 30 mots), et adaptée à un jeune diplômé.
        Réponds UNIQUEMENT avec la question, sans introduction ni commentaire.
        """;

    private static final String EVALUATION_PROMPT = """
        Tu évalues la réponse d'un candidat à un entretien pour le métier de %s.
        Question posée: "%s"
        Réponse du candidat: "%s"

        Donne une note de 0 à 20 et un court feedback constructif en français (max 2 phrases).
        Réponds UNIQUEMENT au format JSON: {"score": <note>, "feedback": "<feedback>"}
        """;

    private static final String RESULTAT_PROMPT = """
        Voici les échanges d'un entretien d'embauche pour le métier de %s:
        %s

        Donne une appréciation globale en français (max 3 phrases) sur la performance du candidat.
        Réponds UNIQUEMENT avec le texte de l'appréciation.
        """;

    @Value("${openai.api.chat.url:https://api.openai.com/v1/chat/completions}")
    private String chatUrl;

    @Value("${openai.api.chat.key:}")
    private String chatApiKey;

    @Value("${openai.api.chat.model:gpt-4o-mini}")
    private String chatModel;

    private final SimulationEntretienRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EntretienService(SimulationEntretienRepository repository) {
        this.repository = repository;
    }

    public EntretienResponse demarrerEntretien(StartEntretienRequest req) {
        var question = genererQuestion(req.metierTitre(), null);

        var entity = SimulationEntretien.builder()
            .eleveTrackingId(req.eleveTrackingId())
            .metierTitre(req.metierTitre())
            .metierTrackingId(req.metierTrackingId())
            .questionsPosees(question)
            .nbQuestions(1)
            .build();
        entity = repository.save(entity);

        return new EntretienResponse(entity.getTrackingId(), req.metierTitre(),
            question, 1, NB_QUESTIONS, "EN_COURS");
    }

    public EntretienResponse repondre(UUID sessionId, String reponse) {
        var entity = repository.findByTrackingId(sessionId)
            .orElseThrow(() -> new NoSuchElementException("Session introuvable"));

        var questionActuelle = entity.getQuestions()[entity.getNbQuestions() - 1];
        var evaluation = evaluerReponse(entity.getMetierTitre(), questionActuelle, reponse);

        if (entity.getNbQuestions() >= NB_QUESTIONS) {
            entity.ajouterEchange(questionActuelle, reponse, evaluation);
            entity.setScoreFinal(calculerScoreFinal(entity));
            entity.setStatut("TERMINE");
            repository.save(entity);
            return new EntretienResponse(sessionId, entity.getMetierTitre(),
                null, entity.getNbQuestions(), NB_QUESTIONS, "TERMINE");
        }

        var prochaineQuestion = genererQuestion(entity.getMetierTitre(), entity.getQuestions());
        entity.ajouterEchange(questionActuelle, reponse, evaluation);
        entity.setQuestionsPosees(entity.getQuestionsPosees() + "|||" + prochaineQuestion);
        repository.save(entity);

        return new EntretienResponse(sessionId, entity.getMetierTitre(),
            prochaineQuestion, entity.getNbQuestions() + 1, NB_QUESTIONS, "EN_COURS");
    }

    public ResultatEntretienResponse getResultat(UUID sessionId) {
        var entity = repository.findByTrackingId(sessionId)
            .orElseThrow(() -> new NoSuchElementException("Session introuvable"));

        var questions = entity.getQuestions();
        var reponses = entity.getReponses();
        var evaluations = entity.getEvaluationsArray();

        List<ResultatEntretienResponse.EchangeDTO> echanges = new ArrayList<>();
        for (int i = 0; i < Math.min(questions.length, NB_QUESTIONS); i++) {
            double score = 0;
            if (i < evaluations.length) {
                try {
                    var json = objectMapper.readTree(evaluations[i]);
                    score = json.has("score") ? json.get("score").asDouble() : 0;
                } catch (Exception e) {
                    score = 10;
                }
            }
            echanges.add(new ResultatEntretienResponse.EchangeDTO(
                i + 1, questions[i],
                i < reponses.length ? reponses[i] : "",
                i < evaluations.length ? evaluations[i] : "", score));
        }

        String appreciation = genererAppreciation(entity);

        return new ResultatEntretienResponse(sessionId, entity.getMetierTitre(),
            entity.getScoreFinal() != null ? entity.getScoreFinal() : 0,
            entity.getNbQuestions() != null ? entity.getNbQuestions() : 0,
            appreciation, echanges);
    }

    private String genererQuestion(String metier, String[] questionsExistantes) {
        var prompt = String.format(SYSTEM_PROMPT, metier);
        if (questionsExistantes != null && questionsExistantes.length > 0) {
            prompt += "\nQuestions déjà posées: " + String.join("; ", questionsExistantes);
            prompt += "\nPose une question différente des précédentes.";
        }
        return appelerOpenAI(prompt);
    }

    private String evaluerReponse(String metier, String question, String reponse) {
        var prompt = String.format(EVALUATION_PROMPT, metier, question, reponse);
        return appelerOpenAI(prompt);
    }

    private String genererAppreciation(SimulationEntretien entity) {
        var echanges = new StringBuilder();
        var questions = entity.getQuestions();
        var reponses = entity.getReponses();
        for (int i = 0; i < Math.min(questions.length, NB_QUESTIONS); i++) {
            echanges.append("Q").append(i + 1).append(": ").append(questions[i]).append("\n");
            if (i < reponses.length) {
                echanges.append("R").append(i + 1).append(": ").append(reponses[i]).append("\n");
            }
        }
        var prompt = String.format(RESULTAT_PROMPT, entity.getMetierTitre(), echanges.toString());
        return appelerOpenAI(prompt);
    }

    private double calculerScoreFinal(SimulationEntretien entity) {
        var evaluations = entity.getEvaluationsArray();
        double total = 0;
        int count = 0;
        for (var eval : evaluations) {
            try {
                var json = objectMapper.readTree(eval);
                total += json.has("score") ? json.get("score").asDouble() : 0;
                count++;
            } catch (Exception ignored) {}
        }
        return count > 0 ? (total / count) * 5 : 0;
    }

    private String appelerOpenAI(String prompt) {
        try {
            var headers = new HttpHeaders();
            headers.setBearerAuth(chatApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            var messages = List.of(
                Map.of("role", "user", "content", prompt)
            );
            var body = Map.of(
                "model", chatModel,
                "messages", messages,
                "max_tokens", 200,
                "temperature", 0.7
            );

            var response = restTemplate.exchange(
                chatUrl,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
            );

            var root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText().strip();
        } catch (Exception e) {
            return "{\"score\": 10, \"feedback\": \"Erreur d'évaluation, veuillez réessayer.\"}";
        }
    }
}
