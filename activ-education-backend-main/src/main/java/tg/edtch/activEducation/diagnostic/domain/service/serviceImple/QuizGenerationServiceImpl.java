package tg.edtch.activEducation.diagnostic.domain.service.serviceImple;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.domain.entite.*;
import tg.edtch.activEducation.bibliotheque.repository.FicheRepository;
import tg.edtch.activEducation.diagnostic.application.dto.response.QuizGenerationResponse;
import tg.edtch.activEducation.diagnostic.domain.entite.Question;
import tg.edtch.activEducation.diagnostic.domain.entite.Quiz;
import tg.edtch.activEducation.diagnostic.domain.entite.QuizIA;
import tg.edtch.activEducation.diagnostic.domain.entite.Reponse;
import tg.edtch.activEducation.diagnostic.domain.service.QuizGenerationService;
import tg.edtch.activEducation.diagnostic.repository.QuestionRepository;
import tg.edtch.activEducation.diagnostic.repository.QuizIaRepository;
import tg.edtch.activEducation.diagnostic.repository.QuizRepository;
import tg.edtch.activEducation.diagnostic.repository.ReponseRepository;
import tg.edtch.activEducation.shared.ai.service.AIEmbeddingService;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuizGenerationServiceImpl implements QuizGenerationService {

    private final QuizRepository quizRepository;
    private final QuizIaRepository quizIaRepository;
    private final QuestionRepository questionRepository;
    private final ReponseRepository reponseRepository;
    private final FicheRepository ficheRepository;
    private final AIEmbeddingService aiEmbeddingService;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Override
    public QuizGenerationResponse genererOuRecupererQuiz(String type, UUID entityId, int nombre) {
        Optional<QuizIA> existing = quizIaRepository.findByEntityTypeAndEntityId(type, entityId);
        if (existing.isPresent()) {
            QuizIA quizIA = existing.get();
            Quiz quiz = quizRepository.findByTrackingId(quizIA.getQuizTrackingId())
                    .orElseThrow(() -> new NoSuchElementException("Quiz introuvable"));
            if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
                return buildResponse(quiz, type, entityId, true);
            }
            log.warn("Quiz IA vide, suppression et regénération pour {} {}", type, entityId);
            quizRepository.delete(quiz);
            quizIaRepository.delete(quizIA);
        }

        Fiche fiche = ficheRepository.findByTrackingId(entityId)
                .orElseThrow(() -> new NoSuchElementException("Entité introuvable : " + entityId));

        String titre = "Quiz - " + fiche.getTitre();
        String description = "Quiz de connaissance sur " + fiche.getTitre();

        String context = fiche.getTitre() + ". " +
                (fiche.getResume() != null ? fiche.getResume() + ". " : "") +
                (fiche.getContenu() != null ? fiche.getContenu() : "");

        if (fiche instanceof FicheFiliere ff) {
            if (ff.getProgramme() != null) context += " Programme: " + ff.getProgramme();
            if (ff.getDebouchesMetiers() != null) context += " Debouchés: " + ff.getDebouchesMetiers();
        } else if (fiche instanceof FicheMetier fm) {
            if (fm.getMissions() != null) context += " Missions: " + fm.getMissions();
            if (fm.getCompetences() != null) context += " Compétences: " + fm.getCompetences();
        } else if (fiche instanceof FicheEtablissement fe) {
            if (fe.getOffreFormation() != null) context += " Offre: " + fe.getOffreFormation();
        } else if (fiche instanceof FicheSerie fs) {
            if (fs.getMatieresPrincipales() != null) context += " Matières: " + fs.getMatieresPrincipales();
            if (fs.getDebouches() != null) context += " Débouchés: " + fs.getDebouches();
        }

        Quiz quiz = Quiz.builder()
                .trackingId(UUID.randomUUID())
                .titre(titre)
                .description(description)
                .domaine(type)
                .estActif(true)
                .build();
        quiz = quizRepository.save(quiz);

        QuizIA quizIA = QuizIA.builder()
                .quizTrackingId(quiz.getTrackingId())
                .entityType(type)
                .entityId(entityId)
                .build();
        quizIaRepository.save(quizIA);

        boolean aiUsed = false;
        if (openaiApiKey != null && !openaiApiKey.isBlank()) {
            try {
                String aiJson = aiEmbeddingService.generateQuizQuestions(context, nombre);
                int saved = saveQuestionsFromAi(quiz, aiJson);
                if (saved > 0) {
                    aiUsed = true;
                    log.info("Quiz généré par IA pour {} {} ({} questions)", type, entityId, saved);
                } else {
                    log.warn("IA n'a généré aucune question, fallback textuel");
                }
            } catch (Exception e) {
                log.warn("Échec génération IA, fallback textuel: {}", e.getMessage());
            }
        }

        if (!aiUsed) {
            saveQuestionsFallback(quiz, fiche, context, nombre);
            log.info("Quiz généré par fallback textuel pour {} {} (quiz={})", type, entityId, quiz.getTrackingId());
        }

        return buildResponse(quiz, type, entityId, aiUsed);
    }

    int saveQuestionsFromAi(Quiz quiz, String aiJson) {
        try {
            JsonNode root = objectMapper.readTree(aiJson);
            JsonNode questions = root.path("questions");
            if (questions.isMissingNode() || !questions.isArray()) return 0;

            int ordre = 1;
            for (JsonNode qNode : questions) {
                String texte = qNode.path("question").asText();
                // skip empty/broken questions
                if (texte.isBlank()) continue;

                String domaine = qNode.path("domaine").asText("CONNAISSANCE");
                int difficulte = qNode.path("difficulte").asInt(2);

                Question question = Question.builder()
                        .trackingId(UUID.randomUUID())
                        .texteQuestion(texte)
                        .ordre(ordre++)
                        .typeQuestion("CONNAISSANCE")
                        .domaine(domaine)
                        .difficulte(difficulte)
                        .quiz(quiz)
                        .build();
                question = questionRepository.save(question);

                JsonNode reponses = qNode.path("reponses");
                if (reponses.isArray()) {
                    for (JsonNode rNode : reponses) {
                        String rTexte = rNode.path("texte").asText();
                        boolean correct = rNode.path("correct").asBoolean(false);
                        Reponse reponse = Reponse.builder()
                                .trackingId(UUID.randomUUID())
                                .texteReponse(rTexte)
                                .categoriePoint(correct ? "CORRECT" : "INCORRECT")
                                .points(correct ? 1 : 0)
                                .question(question)
                                .build();
                        reponseRepository.save(reponse);
                    }
                }
            }
            return ordre - 1;
        } catch (Exception e) {
            log.error("Erreur parsing JSON IA: {}", e.getMessage());
            throw new RuntimeException("Erreur de parsing des questions générées", e);
        }
    }

    void saveQuestionsFallback(Quiz quiz, Fiche fiche, String context, int nombre) {
        List<String> phrases = extrairePhrases(context);
        if (phrases.isEmpty()) {
            phrases = List.of(
                    "Qu'est-ce qui caractérise " + fiche.getTitre() + " ?",
                    "Quel est le domaine de " + fiche.getTitre() + " ?",
                    "Que savez-vous de " + fiche.getTitre() + " ?"
            );
        }

        int maxQ = Math.min(nombre, phrases.size());
        Random rand = new Random();

        for (int i = 0; i < maxQ; i++) {
            String phrase = phrases.get(i).trim();
            if (phrase.length() < 15) continue;

            String questionTexte = "D'après le contenu, " + Character.toLowerCase(phrase.charAt(0)) + phrase.substring(1);
            if (questionTexte.length() > 120) questionTexte = questionTexte.substring(0, 117) + "...";

            Question question = Question.builder()
                    .trackingId(UUID.randomUUID())
                    .texteQuestion("Que dit le contenu à propos de " + fiche.getTitre() + " ?")
                    .ordre(i + 1)
                    .typeQuestion("CONNAISSANCE")
                    .domaine("CONNAISSANCE")
                    .difficulte(2)
                    .quiz(quiz)
                    .build();
            question = questionRepository.save(question);

            String correctAnswer = phrase.length() > 80 ? phrase.substring(0, 77) + "..." : phrase;
            List<String> distracteurs = genererDistracteurs(phrase, fiche.getTitre(), rand);

            Reponse correct = Reponse.builder()
                    .trackingId(UUID.randomUUID())
                    .texteReponse(correctAnswer)
                    .categoriePoint("CORRECT")
                    .points(1)
                    .question(question)
                    .build();
            reponseRepository.save(correct);

            for (String dist : distracteurs) {
                Reponse r = Reponse.builder()
                        .trackingId(UUID.randomUUID())
                        .texteReponse(dist)
                        .categoriePoint("INCORRECT")
                        .points(0)
                        .question(question)
                        .build();
                reponseRepository.save(r);
            }
        }
    }

    List<String> extrairePhrases(String text) {
        if (text == null || text.isBlank()) return List.of();
        List<String> phrases = new ArrayList<>();
        String[] parts = text.split("[.!?]");
        for (String p : parts) {
            String trimmed = p.trim();
            if (trimmed.length() > 20 && !trimmed.toLowerCase().startsWith("http")) {
                phrases.add(trimmed);
            }
        }
        return phrases;
    }

    List<String> genererDistracteurs(String correctAnswer, String titre, Random rand) {
        List<String> templates = List.of(
                "Ce n'est pas lié à " + titre,
                "Cette information concerne un autre sujet",
                "Le contenu ne mentionne pas cette information",
                "Cette affirmation est incorrecte pour " + titre
        );
        List<String> distracteurs = new ArrayList<>(templates);
        Collections.shuffle(distracteurs, rand);
        return distracteurs.subList(0, Math.min(3, distracteurs.size()));
    }

    QuizGenerationResponse buildResponse(Quiz quiz, String type, UUID entityId, boolean aiUsed) {
        List<Question> questions = questionRepository.findByQuizTrackingIdOrderByOrdreAsc(quiz.getTrackingId());
        List<QuizGenerationResponse.QuestionGeneree> questionDtos = new ArrayList<>();

        for (Question q : questions) {
            List<Reponse> reponses = reponseRepository.findByQuestionTrackingId(q.getTrackingId());
            List<QuizGenerationResponse.ReponseGeneree> reponseDtos = reponses.stream()
                    .map(r -> QuizGenerationResponse.ReponseGeneree.builder()
                            .texte(r.getTexteReponse())
                            .correct("CORRECT".equals(r.getCategoriePoint()))
                            .build())
                    .toList();

            questionDtos.add(QuizGenerationResponse.QuestionGeneree.builder()
                    .texte(q.getTexteQuestion())
                    .typeQuestion(q.getTypeQuestion())
                    .domaine(q.getDomaine())
                    .difficulte(q.getDifficulte())
                    .reponses(reponseDtos)
                    .build());
        }

        return QuizGenerationResponse.builder()
                .quizTrackingId(quiz.getTrackingId())
                .titre(quiz.getTitre())
                .description(quiz.getDescription())
                .entityType(type)
                .entityId(entityId)
                .genereParIA(aiUsed)
                .questions(questionDtos)
                .build();
    }
}
