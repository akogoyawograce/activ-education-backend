package tg.edtch.activEducation.diagnostic.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.diagnostic.application.dto.response.ResultatDiagnosticResponse;
import tg.edtch.activEducation.diagnostic.application.dto.response.QuestionResponse;
import tg.edtch.activEducation.diagnostic.application.mapper.ResultatDiagnosticMapper;
import tg.edtch.activEducation.diagnostic.application.mapper.QuestionMapper;
import tg.edtch.activEducation.diagnostic.domain.entite.Question;
import tg.edtch.activEducation.diagnostic.domain.entite.Quiz;
import tg.edtch.activEducation.diagnostic.domain.entite.Reponse;
import tg.edtch.activEducation.diagnostic.domain.entite.ResultatDiagnostic;
import tg.edtch.activEducation.diagnostic.domain.service.MoteurDiagnosticService;
import tg.edtch.activEducation.diagnostic.repository.QuestionRepository;
import tg.edtch.activEducation.diagnostic.repository.QuizRepository;
import tg.edtch.activEducation.diagnostic.repository.ReponseRepository;
import tg.edtch.activEducation.diagnostic.repository.ResultatDiagnosticRepository;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.entite.NoteSaisiManuel;
import tg.edtch.activEducation.profil.repository.EleveRepository;
import tg.edtch.activEducation.profil.repository.NoteSaisiManuelRepository;

import java.time.LocalDateTime;
import java.util.*;
import tg.edtch.activEducation.shared.ai.service.GeminiEmbeddingService;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoteurDiagnosticServiceImpl implements MoteurDiagnosticService {

    private final EleveRepository eleveRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ReponseRepository reponseRepository;
    private final NoteSaisiManuelRepository noteRepository;
    private final ResultatDiagnosticRepository resultatRepository;
    private final tg.edtch.activEducation.diagnostic.repository.SeuilAdmissionRepository seuilAdmissionRepository;
    private final ResultatDiagnosticMapper resultatMapper;
    private final QuestionMapper questionMapper;
    private final GeminiEmbeddingService geminiService;

    @Override
    @Transactional
    public ResultatDiagnosticResponse analyserQuizAspirations(UUID eleveTrackingId, UUID quizTrackingId,
            List<UUID> reponsesTrackingIds) {
        Eleve eleve = findEleve(eleveTrackingId);
        Quiz quiz = findQuiz(quizTrackingId);

        List<Reponse> reponses = reponsesTrackingIds.stream()
                .map(id -> reponseRepository.findByTrackingId(id)
                        .orElseThrow(() -> new NoSuchElementException("Réponse introuvable : " + id)))
                .collect(Collectors.toList());

        // Calcul des scores par catégorie (ex: RIASEC ou domaine)
        Map<String, Integer> scoreParCategorie = new HashMap<>();
        double scoreTotal = 0;

        for (Reponse r : reponses) {
            String cat = r.getCategoriePoint() != null ? r.getCategoriePoint().toUpperCase() : "GÉNÉRAL";
            scoreParCategorie.put(cat, scoreParCategorie.getOrDefault(cat, 0) + r.getPoints());
            scoreTotal += r.getPoints();
        }

        // Trouver le profil dominant
        String profilDecouvert = "Non Déterminé";
        int maxScore = -1;
        for (Map.Entry<String, Integer> entry : scoreParCategorie.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                profilDecouvert = "Profil " + entry.getKey();
            }
        }

        String prompt = "Tu es conseiller d'orientation expert. Rédige une analyse d'orientation courte et motivante (3 phrases max) pour un élève dont le profil dominant d'aspirations est : "
                + profilDecouvert + " (Score : " + scoreTotal
                + "). Parle à l'élève à la 2ème personne du singulier. Suggère 2 ou 3 grands domaines de métiers compatibles. Ne dis jamais que tu es une intelligence artificielle.";
        String recommandation = "Analyse IA non disponible.";
        try {
            recommandation = geminiService.generateAnswer(prompt, List.of());
        } catch (Exception e) {
            log.error("Erreur Gemini lors du diagnostic aspirations", e);
            recommandation = "Suite à vos réponses, votre profil dominant est : " + profilDecouvert
                    + ". Explorez notre Bibliothèque Centrale pour trouver les métiers correspondants.";
        }

        ResultatDiagnostic res = ResultatDiagnostic.builder()
                .eleve(eleve)
                .quiz(quiz)
                .datePassage(LocalDateTime.now())
                .scoreFinal(scoreTotal)
                .profilDecouvert(profilDecouvert)
                .recommandation(recommandation)
                .build();

        return resultatMapper.toResponse(resultatRepository.save(res));
    }

    @Override
    @Transactional
    public ResultatDiagnosticResponse analyserNotesAcademiques(UUID eleveTrackingId) {
        Eleve eleve = findEleve(eleveTrackingId);
        List<NoteSaisiManuel> notes = noteRepository.findByEleveTrackingIdOrderByAnneeScolaireDesc(eleveTrackingId);

        if (notes.isEmpty()) {
            throw new IllegalStateException("L'élève n'a pas saisi de notes.");
        }

        double scoreTotalMoyen = notes.stream().mapToDouble(NoteSaisiManuel::getNote).average().orElse(0.0);
        String profilDecouvert = scoreTotalMoyen >= 12.0 ? "Profil Académique Solide" : "Profil Académique Standard";

        // Logic de matching avec les seuils d'admission
        List<tg.edtch.activEducation.diagnostic.domain.entite.SeuilAdmission> seuils = seuilAdmissionRepository
                .findAll();
        Set<String> filieresEligibles = new HashSet<>();
        for (NoteSaisiManuel noteSaisie : notes) {
            for (tg.edtch.activEducation.diagnostic.domain.entite.SeuilAdmission seuil : seuils) {
                if (seuil.getMatiereRequise().equalsIgnoreCase(noteSaisie.getMatiere())
                        && noteSaisie.getNote() >= seuil.getNoteMinimum()) {
                    if (seuil.getFiliere() != null) {
                        filieresEligibles.add(seuil.getFiliere().getTitre());
                    }
                }
            }
        }

        String contexte = filieresEligibles.isEmpty()
                ? "Aucune filière ne semble parfaitement matcher tes notes actuelles. Mais ta moyenne générale est de "
                        + String.format("%.2f", scoreTotalMoyen) + "/20."
                : "Filières compatibles selon tes notes : " + String.join(", ", filieresEligibles)
                        + " (Moyenne générale : " + String.format("%.2f", scoreTotalMoyen) + "/20).";

        String prompt = "Tu es conseiller d'orientation. Rédige un bref bilan (3 phrases max) très encourageant pour l'élève. Voici le contexte académique de l'élève : "
                + contexte
                + ". Parle directement à l'élève avec bienveillance, et cite de façon naturelle au moins 2 des filières compatibles si elles existent.";

        String recommandation = "Bilan non disponible.";
        try {
            recommandation = geminiService.generateAnswer(prompt, List.of());
        } catch (Exception e) {
            log.error("Erreur Gemini lors du diagnostic académique", e);
            recommandation = "Avis académique: " + contexte;
        }

        ResultatDiagnostic res = ResultatDiagnostic.builder()
                .eleve(eleve)
                // Nous n'avons pas de quiz spécifique ici. On en prend un par défaut ou
                // système.
                .quiz(quizRepository.findAll().stream().findFirst().orElseThrow())
                .datePassage(LocalDateTime.now())
                .scoreFinal(scoreTotalMoyen)
                .profilDecouvert(profilDecouvert)
                .recommandation(recommandation)
                .build();

        return resultatMapper.toResponse(resultatRepository.save(res));
    }

    @Override
    @Transactional
    public ResultatDiagnosticResponse diagnosticCombine(UUID eleveTrackingId, UUID quizTrackingId,
            List<UUID> reponsesTrackingIds) {
        // Combinaison simple : On appelle l'analyse quiz puis on ajoute une couche
        // académique
        ResultatDiagnosticResponse quizRes = analyserQuizAspirations(eleveTrackingId, quizTrackingId,
                reponsesTrackingIds);
        List<NoteSaisiManuel> notes = noteRepository.findByEleveTrackingIdOrderByAnneeScolaireDesc(eleveTrackingId);

        ResultatDiagnostic res = resultatRepository.findByTrackingId(quizRes.getTrackingId()).orElseThrow();

        if (!notes.isEmpty()) {
            double moyenneNotes = notes.stream().mapToDouble(NoteSaisiManuel::getNote).average().orElse(0.0);
            res.setScoreFinal((res.getScoreFinal() + moyenneNotes) / 2);

            String prompt = "Tu es un AI conseiller d'orientation d'Activ Education. Rédige un Bilan Combiné (3 phrases max). L'élève a eu aux résultats RIASEC le profil "
                    + res.getProfilDecouvert() + " et possède par ailleurs une moyenne générale académique de "
                    + String.format("%.2f", moyenneNotes)
                    + "/20. Fais une fusion de ces informations pour lui donner un conseil impactant (directement adressé à lui, tutoiement).";
            try {
                String recommandationCombine = geminiService.generateAnswer(prompt, List.of());
                res.setRecommandation(recommandationCombine);
            } catch (Exception e) {
                log.error("Erreur Gemini lors du diagnostic combiné", e);
                res.setRecommandation(res.getRecommandation()
                        + " Par ailleurs, votre profil académique confirme cette direction avec une moyenne correcte de "
                        + moyenneNotes + ".");
            }
        }

        return resultatMapper.toResponse(resultatRepository.save(res));
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse determinerProchaineQuestion(UUID quizTrackingId, UUID reponseTrackingId,
            UUID eleveTrackingId) {
        Reponse reponse = reponseRepository.findByTrackingId(reponseTrackingId)
                .orElseThrow(() -> new NoSuchElementException("Réponse introuvable : " + reponseTrackingId));

        Eleve eleve = findEleve(eleveTrackingId);

        // 1. Branchement explicite
        if (reponse.getProchaineQuestion() != null) {
            return questionMapper.toResponse(reponse.getProchaineQuestion());
        }

        // 2. Branchement implicite (ordre naturel + filtrage niveau)
        Question currentQuestion = reponse.getQuestion();
        List<Question> questionsSuivantes = questionRepository.findByQuizTrackingIdOrderByOrdreAsc(quizTrackingId)
                .stream()
                .filter(q -> q.getOrdre() > currentQuestion.getOrdre())
                .filter(q -> {
                    if (q.getNiveauCible() == null || q.getNiveauCible().isEmpty())
                        return true;
                    return q.getNiveauCible().equalsIgnoreCase(eleve.getNiveau());
                })
                .collect(Collectors.toList());

        if (questionsSuivantes.isEmpty())
            return null;

        return questionMapper.toResponse(questionsSuivantes.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsFiltrees(UUID quizTrackingId, UUID eleveTrackingId) {
        Eleve eleve = findEleve(eleveTrackingId);

        return questionRepository.findByQuizTrackingIdOrderByOrdreAsc(quizTrackingId)
                .stream()
                .filter(q -> {
                    if (q.getNiveauCible() == null || q.getNiveauCible().isEmpty())
                        return true;
                    return q.getNiveauCible().equalsIgnoreCase(eleve.getNiveau());
                })
                .map(questionMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Eleve findEleve(UUID trackingId) {
        return eleveRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException("Élève introuvable pour : " + trackingId));
    }

    private Quiz findQuiz(UUID trackingId) {
        return quizRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException("Quiz introuvable pour : " + trackingId));
    }
}
