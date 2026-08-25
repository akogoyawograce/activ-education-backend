package tg.edtch.activEducation.entretien.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tg.edtch.activEducation.entretien.domain.dto.*;
import tg.edtch.activEducation.entretien.domain.entite.SimulationEntretien;
import tg.edtch.activEducation.entretien.repository.SimulationEntretienRepository;

import java.util.*;

/**
 * Service de simulation d'entretien (recruteur IA).
 *
 * <p>
 * <b>Bug corrigé le 6 août 2026</b> : la méthode {@code appelerOpenAI()} renvoyait
 * {@code {"score": 10, "feedback": "Erreur d'évaluation, veuillez réessayer."}}
 * comme fallback quand le LLM échouait. Ce JSON d'<i>évaluation</i> était
 * ensuite utilisé comme <i>question</i> du recruteur (via {@code genererQuestion()}),
 * ce qui faisait apparaître le JSON brut dans la bulle de conversation du
 * frontend. Cause : le catch ne distinguait pas "question" de "évaluation".
 *
 * <p>
 * Refactor :
 * <ul>
 *   <li>{@link ResultatAppelLLM} encapsule succès/erreur — le frontend reçoit
 *       {@code erreur=true} au lieu d'un JSON malformé.</li>
 *   <li>1 retry automatique sur réponse LLM malformée (avant le fallback).</li>
 *   <li>Prompt système renforcé : instruction stricte "réponds uniquement avec
 *       la question, sans JSON, sans préambule".</li>
 *   <li>Méthodes séparées : {@code appelerLLMTexte()} et {@code appelerLLMJson()}.</li>
 * </ul>
 */
@Service
@Transactional
@Slf4j
public class EntretienService {

    private static final int NB_QUESTIONS = 5;
    private static final int MAX_RETRY = 1;

    private static final String SYSTEM_PROMPT = """
        Tu es un recruteur qui mène un entretien d'embauche pour le métier de %s.
        Pose UNE question professionnelle, spécifique au métier et au contexte togolais.
        La question doit être en français, concise (max 30 mots), adaptée à un jeune diplômé.

        ⚠️ FORMAT STRICT :
        - Réponds UNIQUEMENT avec la question elle-même (une phrase).
        - AUCUN préambule du type "Voici ma question :", "Question 1 :", etc.
        - AUCUNE balise JSON, AUCUN guillemet, AUCUN formatage Markdown.
        - Pas de "?" initiale (commence directement par le contenu).
        """;

    private static final String EVALUATION_PROMPT = """
        Tu évalues la réponse d'un candidat à un entretien pour le métier de %s.
        Question posée: "%s"
        Réponse du candidat: "%s"

        ⚠️ FORMAT STRICT JSON :
        Réponds UNIQUEMENT avec ce JSON exact (sans texte avant/après, sans ```):
        {"score": <entier entre 0 et 20>, "feedback": "<feedback constructif en 2 phrases max>"}
        Exemple: {"score": 14, "feedback": "Bonne réponse, mais précise ton expérience avec X."}
        """;

    private static final String RESULTAT_PROMPT = """
        Voici les échanges d'un entretien d'embauche pour le métier de %s:
        %s

        Donne une appréciation globale en français (max 3 phrases) sur la performance du candidat.
        Réponds UNIQUEMENT avec le texte de l'appréciation, sans préambule.
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
        ResultatAppelLLM<String> res = genererQuestion(req.metierTitre(), null);

        if (!res.isOk()) {
            // Erreur technique : on renvoie EntretienResponse avec erreur=true.
            // Le frontend affichera un message + bouton "Réessayer", pas le JSON brut.
            log.warn("Échec génération question (start) pour métier '{}': {}",
                     req.metierTitre(), res.messageErreur());
            return EntretienResponse.erreur(null, req.metierTitre(),
                                            0, NB_QUESTIONS, "EN_COURS",
                                            res.messageErreur());
        }

        var entity = SimulationEntretien.builder()
            .eleveTrackingId(req.eleveTrackingId())
            .metierTitre(req.metierTitre())
            .metierTrackingId(req.metierTrackingId())
            .questionsPosees(res.valeur())
            .nbQuestions(1)
            .build();
        entity = repository.save(entity);

        return new EntretienResponse(entity.getTrackingId(), req.metierTitre(),
            res.valeur(), 1, NB_QUESTIONS, "EN_COURS");
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

        ResultatAppelLLM<String> resQuestion = genererQuestion(entity.getMetierTitre(), entity.getQuestions());
        if (!resQuestion.isOk()) {
            log.warn("Échec génération question (session={}, nb={}): {}",
                     sessionId, entity.getNbQuestions(), resQuestion.messageErreur());
            return EntretienResponse.erreur(sessionId, entity.getMetierTitre(),
                                            entity.getNbQuestions(), NB_QUESTIONS,
                                            "EN_COURS", resQuestion.messageErreur());
        }

        entity.ajouterEchange(questionActuelle, reponse, evaluation);
        entity.setQuestionsPosees(entity.getQuestionsPosees() + "|||" + resQuestion.valeur());
        repository.save(entity);

        return new EntretienResponse(sessionId, entity.getMetierTitre(),
            resQuestion.valeur(), entity.getNbQuestions() + 1, NB_QUESTIONS, "EN_COURS");
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
                score = extraireScore(evaluations[i]).orElse(0.0);
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

    // ─────────────────────────────────────────────────────────────────────
    // Génération
    // ─────────────────────────────────────────────────────────────────────

    private ResultatAppelLLM<String> genererQuestion(String metier, String[] questionsExistantes) {
        var prompt = String.format(SYSTEM_PROMPT, metier);
        if (questionsExistantes != null && questionsExistantes.length > 0) {
            prompt += "\nQuestions déjà posées: " + String.join("; ", questionsExistantes);
            prompt += "\nPose une question différente des précédentes.";
        }
        return appelerLLMTexteAvecRetry(prompt, "question pour métier=" + metier);
    }

    /**
     * Évalue une réponse. Renvoie le JSON brut (ex: {@code {"score": 14, "feedback": "..."}}).
     * <p>
     * En cas d'échec technique (LLM down, JSON invalide après retry), on stocke
     * un placeholder {@code {"score": 10, "feedback": "..."}} pour ne pas perdre
     * l'échange — c'est cohérent avec le comportement historique côté DB. Mais
     * ce placeholder n'est JAMAIS utilisé comme question.
     */
    private String evaluerReponse(String metier, String question, String reponse) {
        var prompt = String.format(EVALUATION_PROMPT, metier, question, reponse);
        ResultatAppelLLM<String> res = appelerLLMJsonAvecRetry(prompt, "évaluation");
        if (!res.isOk()) {
            log.warn("Échec évaluation LLM : {} — stockage placeholder", res.messageErreur());
            return "{\"score\": 10, \"feedback\": \"Évaluation indisponible, voir détail plus tard.\"}";
        }
        return res.valeur();
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
        ResultatAppelLLM<String> res = appelerLLMTexteAvecRetry(prompt, "appréciation finale");
        return res.isOk() ? res.valeur()
                          : "Appréciation globale indisponible pour le moment.";
    }

    private double calculerScoreFinal(SimulationEntretien entity) {
        var evaluations = entity.getEvaluationsArray();
        double total = 0;
        int count = 0;
        for (var eval : evaluations) {
            Optional<Double> s = extraireScore(eval);
            if (s.isPresent()) {
                total += s.get();
                count++;
            }
        }
        return count > 0 ? (total / count) * 5 : 0;
    }

    private Optional<Double> extraireScore(String json) {
        try {
            var node = objectMapper.readTree(json);
            if (node.has("score") && node.get("score").isNumber()) {
                return Optional.of(node.get("score").asDouble());
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Appels LLM avec retry et détection de format
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Appelle le LLM pour une réponse TEXTE BRUT (question, appréciation).
     * Retry 1 fois si la réponse semble être du JSON (échec de respect du prompt).
     */
    private ResultatAppelLLM<String> appelerLLMTexteAvecRetry(String prompt, String contexte) {
        ResultatAppelLLM<String> res = appelerLLMBrut(prompt);
        log.info("LLM [{}] réponse brute : {}", contexte, tronquer(res.messageBrute()));

        if (!res.isOk()) {
            return res; // Erreur HTTP, pas de retry
        }

        // Si la réponse ressemble à du JSON mais on attend du texte → likely erreur de format
        String valeur = res.valeur().trim();
        if (valeur.startsWith("{") || valeur.startsWith("[") || valeur.startsWith("```")) {
            log.warn("LLM [{}] a renvoyé du JSON/balisage au lieu de texte — retry", contexte);
            for (int i = 0; i < MAX_RETRY; i++) {
                ResultatAppelLLM<String> res2 = appelerLLMBrut(prompt + "\n\nRappel: réponse en texte brut UNIQUEMENT.");
                if (res2.isOk()) {
                    String v2 = res2.valeur().trim();
                    if (!v2.startsWith("{") && !v2.startsWith("[") && !v2.startsWith("```")
                        && !v2.isEmpty()) {
                        log.info("LLM [{}] retry {} OK : {}", contexte, i + 1, tronquer(res2.messageBrute()));
                        return ResultatAppelLLM.ok(v2, res2.messageBrute());
                    }
                }
            }
            return ResultatAppelLLM.ko(
                "Le modèle n'a pas généré une question valide après retry. Réessayez.",
                valeur);
        }

        // Texte vide
        if (valeur.isEmpty()) {
            return ResultatAppelLLM.ko("Le modèle a renvoyé une réponse vide. Réessayez.", "");
        }

        return ResultatAppelLLM.ok(valeur, res.messageBrute());
    }

    /**
     * Appelle le LLM pour une réponse JSON strict.
     * Retry 1 fois si le JSON est invalide.
     */
    private ResultatAppelLLM<String> appelerLLMJsonAvecRetry(String prompt, String contexte) {
        ResultatAppelLLM<String> res = appelerLLMBrut(prompt);
        log.info("LLM [{}] réponse brute : {}", contexte, tronquer(res.messageBrute()));

        if (!res.isOk()) {
            return res;
        }

        String valeur = res.valeur().trim();
        // Strip ```json ... ``` au cas où
        String nettoye = stripCodeFence(valeur);

        if (validerJsonEvaluation(nettoye)) {
            return ResultatAppelLLM.ok(nettoye, res.messageBrute());
        }

        log.warn("LLM [{}] JSON invalide, retry", contexte);
        for (int i = 0; i < MAX_RETRY; i++) {
            ResultatAppelLLM<String> res2 = appelerLLMBrut(prompt + "\n\nRappel: JSON STRICT, rien d'autre.");
            if (res2.isOk() && validerJsonEvaluation(stripCodeFence(res2.valeur().trim()))) {
                log.info("LLM [{}] retry {} OK", contexte, i + 1);
                return ResultatAppelLLM.ok(stripCodeFence(res2.valeur().trim()), res2.messageBrute());
            }
        }
        return ResultatAppelLLM.ko(
            "Le modèle n'a pas généré une évaluation JSON valide après retry.", nettoye);
    }

    private boolean validerJsonEvaluation(String json) {
        try {
            var node = objectMapper.readTree(json);
            if (!node.has("score") || !node.has("feedback")) return false;
            if (!node.get("score").isNumber()) return false;
            if (!node.get("feedback").isTextual()) return false;
            String fb = node.get("feedback").asText();
            return !fb.isBlank();
        } catch (Exception e) {
            return false;
        }
    }

    private String stripCodeFence(String s) {
        String t = s.trim();
        if (t.startsWith("```")) {
            int firstNewline = t.indexOf('\n');
            if (firstNewline > 0) t = t.substring(firstNewline + 1);
            if (t.endsWith("```")) t = t.substring(0, t.length() - 3);
        }
        return t.trim();
    }

    /** Appel HTTP bas niveau — renvoie le texte brut du LLM (ou erreur HTTP).
     *  Retry interne x1 si la réponse LLM est mal formée (impossible à parser
     *  en JSON OpenAI-style) — distinct d'une erreur HTTP (5xx, auth, etc.).
     */
    private ResultatAppelLLM<String> appelerLLMBrut(String prompt) {
        return appelerLLMBrutAvecRetry(prompt, 0);
    }

    private ResultatAppelLLM<String> appelerLLMBrutAvecRetry(String prompt, int tentative) {
        try {
            var headers = new HttpHeaders();
            headers.setBearerAuth(chatApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            var messages = List.of(Map.of("role", "user", "content", prompt));
            var body = Map.of(
                "model", chatModel,
                "messages", messages,
                "max_tokens", 300,
                "temperature", 0.7
            );

            var response = restTemplate.exchange(
                chatUrl,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
            );

            String reponseBrute = response.getBody();
            var root = objectMapper.readTree(reponseBrute);
            String contenu = root.path("choices").get(0).path("message").path("content").asText().strip();
            return ResultatAppelLLM.ok(contenu, reponseBrute);
        } catch (Exception e) {
            // Erreur HTTP → pas de retry (erreur de transport/auth/rate-limit)
            if (estErreurTransport(e)) {
                log.warn("Appel LLM échoué (transport) : {}", e.getMessage());
                return ResultatAppelLLM.ko("Erreur technique LLM : " + e.getClass().getSimpleName(), null);
            }
            // Erreur de parsing → retry 1 fois (réponse mal formée)
            if (tentative < MAX_RETRY) {
                log.warn("Appel LLM réponse mal-formée (tentative {}), retry : {}",
                         tentative + 1, e.getMessage());
                return appelerLLMBrutAvecRetry(prompt, tentative + 1);
            }
            log.warn("Appel LLM réponse mal-formée après {} tentatives : {}",
                     tentative, e.getMessage());
            return ResultatAppelLLM.ko("Réponse LLM mal-formée", null);
        }
    }

    private boolean estErreurTransport(Exception e) {
        String name = e.getClass().getName();
        return name.contains("HttpClientError")    // 4xx
            || name.contains("HttpServerError")    // 5xx
            || name.contains("ResourceAccess")      // timeouts, connection refused
            || name.contains("ConnectException");
    }

    private String tronquer(String s) {
        if (s == null) return "(null)";
        return s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Wrapper résultat
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Encapsule le résultat d'un appel LLM : succès (valeur) ou erreur (message).
     * Permet de NE PLUS jamais utiliser un JSON d'erreur comme une question.
     */
    public static final class ResultatAppelLLM<T> {
        private final boolean ok;
        private final T valeur;
        private final String messageErreur;
        private final String messageBrute;

        private ResultatAppelLLM(boolean ok, T valeur, String messageErreur, String messageBrute) {
            this.ok = ok;
            this.valeur = valeur;
            this.messageErreur = messageErreur;
            this.messageBrute = messageBrute;
        }

        public static <T> ResultatAppelLLM<T> ok(T valeur, String brute) {
            return new ResultatAppelLLM<>(true, valeur, null, brute);
        }

        public static <T> ResultatAppelLLM<T> ko(String erreur, String brute) {
            return new ResultatAppelLLM<>(false, null, erreur, brute);
        }

        public boolean isOk() { return ok; }
        public T valeur() { return valeur; }
        public String messageErreur() { return messageErreur; }
        public String messageBrute() { return messageBrute; }
    }
}
