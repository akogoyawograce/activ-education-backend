package tg.edtch.activEducation.shared.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheEtablissement;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheMetier;
import tg.edtch.activEducation.bibliotheque.repository.FicheEtablissementRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheFiliereRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheMetierRepository;
import tg.edtch.activEducation.diagnostic.domain.entite.Quiz;
import tg.edtch.activEducation.diagnostic.domain.entite.ResultatDiagnostic;
import tg.edtch.activEducation.diagnostic.repository.QuizRepository;
import tg.edtch.activEducation.diagnostic.repository.ResultatDiagnosticRepository;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.entite.NoteSaisiManuel;
import tg.edtch.activEducation.profil.repository.EleveRepository;
import tg.edtch.activEducation.profil.repository.NoteSaisiManuelRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommandationIAService {

    private final EleveRepository eleveRepository;
    private final NoteSaisiManuelRepository noteRepository;
    private final ResultatDiagnosticRepository resultatRepository;
    private final QuizRepository quizRepository;
    private final FicheFiliereRepository filiereRepository;
    private final FicheMetierRepository metierRepository;
    private final FicheEtablissementRepository etablissementRepository;
    private final AIEmbeddingService aiService;

    public String genererRecommandation(UUID eleveTrackingId) {
        Eleve eleve = eleveRepository.findByTrackingId(eleveTrackingId)
                .orElseThrow(() -> new RuntimeException("Élève introuvable"));

        StringBuilder profilBuilder = new StringBuilder();
        profilBuilder.append("Profil de l'élève :\n");
        profilBuilder.append("- Niveau : ").append(eleve.getNiveau() != null ? eleve.getNiveau() : "Non renseigné").append("\n");
        profilBuilder.append("- Type : ").append(eleve.getTypeApprenant() != null ? eleve.getTypeApprenant().name() : "Non renseigné").append("\n");
        profilBuilder.append("- Établissement : ").append(eleve.getEtablissement() != null ? eleve.getEtablissement() : "Non renseigné").append("\n");
        profilBuilder.append("- Filière actuelle : ").append(eleve.getFiliere() != null ? eleve.getFiliere() : "Non renseigné").append("\n");
        profilBuilder.append("- Métier souhaité : ").append(eleve.getMetierSouhaite() != null ? eleve.getMetierSouhaite() : "Non renseigné").append("\n");
        profilBuilder.append("- Matières préférées : ").append(eleve.getMatieresPreferees() != null ? eleve.getMatieresPreferees() : "Non renseigné").append("\n");

        List<NoteSaisiManuel> notes = noteRepository.findByEleveTrackingIdOrderByAnneeScolaireDesc(eleveTrackingId);
        if (!notes.isEmpty()) {
            profilBuilder.append("\nNotes scolaires :\n");
            for (NoteSaisiManuel note : notes) {
                profilBuilder.append("- ").append(note.getMatiere()).append(" : ").append(note.getNote()).append("/20\n");
            }
        }

        List<ResultatDiagnostic> resultats = resultatRepository
                .findByEleveTrackingIdOrderByDatePassageDesc(eleveTrackingId,
                        org.springframework.data.domain.PageRequest.of(0, 5))
                .getContent();
        if (!resultats.isEmpty()) {
            profilBuilder.append("\nRésultats de quiz d'orientation :\n");
            for (ResultatDiagnostic r : resultats) {
                Optional<Quiz> quiz = quizRepository.findByTrackingId(r.getQuiz().getTrackingId());
                String nomQuiz = quiz.map(Quiz::getTitre).orElse("Quiz inconnu");
                profilBuilder.append("- ").append(nomQuiz).append(" : score ").append(r.getScoreFinal()).append("\n");
            }
        }

        List<FicheFiliere> filieres = filiereRepository.findAllByEstPublieTrue(
                org.springframework.data.domain.PageRequest.of(0, 10)).getContent();
        List<FicheMetier> metiers = metierRepository.findAllByEstPublieTrue(
                org.springframework.data.domain.PageRequest.of(0, 10)).getContent();
        List<FicheEtablissement> etablissements = etablissementRepository.findAllByEstPublieTrue(
                org.springframework.data.domain.PageRequest.of(0, 10)).getContent();

        StringBuilder contexteBuilder = new StringBuilder();
        contexteBuilder.append("\nFilières disponibles au Togo :\n");
        for (FicheFiliere f : filieres) {
            contexteBuilder.append("- ").append(f.getTitre()).append(" : ").append(f.getResume()).append("\n");
        }
        contexteBuilder.append("\nMétiers disponibles au Togo :\n");
        for (FicheMetier m : metiers) {
            contexteBuilder.append("- ").append(m.getTitre()).append(" : ").append(m.getResume()).append("\n");
        }
        contexteBuilder.append("\nÉtablissements au Togo :\n");
        for (FicheEtablissement e : etablissements) {
            contexteBuilder.append("- ").append(e.getTitre()).append(" (").append(e.getVille()).append(")\n");
        }

        String question = "En tant que conseiller d'orientation, fais une recommandation personnalisée pour cet élève. "
                + "Propose-lui 3 filières d'études adaptées à son profil, 3 métiers qui correspondent, "
                + "et les établissements où il peut les étudier au Togo. "
                + "Justifie chaque recommandation en t'appuyant sur son profil, ses notes et ses résultats de quiz. "
                + "Sois encourageant et concret.";

        String prompt = contexteBuilder.toString() + "\n\n" + profilBuilder.toString() + "\n\nQUESTION : " + question;

        try {
            return aiService.generateAnswer(question, List.of(
                    profilBuilder.toString(),
                    contexteBuilder.toString()));
        } catch (Exception e) {
            log.error("Erreur génération recommandation IA", e);
            return "Désolé, je n'ai pas pu générer une recommandation pour le moment. "
                    + "Veuillez réessayer plus tard ou consulter un conseiller.";
        }
    }
}
