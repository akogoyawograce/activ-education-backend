package tg.edtch.activEducation.shared.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import tg.edtch.activEducation.shared.ai.domain.dto.RecommandationIAResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecommandationIAService {

    private final EleveRepository eleveRepository;
    private final NoteSaisiManuelRepository noteRepository;
    private final ResultatDiagnosticRepository resultatRepository;
    private final QuizRepository quizRepository;
    private final FicheFiliereRepository filiereRepository;
    private final FicheMetierRepository metierRepository;
    private final FicheEtablissementRepository etablissementRepository;
    private final AIEmbeddingService aiService;

    /**
     * Construit la recommandation IA d'un élève.
     *
     * <p>Retourne désormais un {@link RecommandationIAResponse} typé pour
     * distinguer 3 cas (cf. JOURNAL_BORD_IA.md, 6 août 2026) :
     * <ul>
     *   <li>{@code OK} : recommandation générée par le LLM</li>
     *   <li>{@code PROFIL_INCOMPLET} : profil trop vide pour faire une
     *       recommandation pertinente, message déterministe invitant à
     *       compléter le profil</li>
     *   <li>{@code ERREUR_LLM} : le LLM (OpenAI, Ollama, …) a échoué
     *       malgré un profil complet, message invitant à réessayer</li>
     * </ul>
     * </p>
     */
    public RecommandationIAResponse genererRecommandation(UUID eleveTrackingId) {
        Eleve eleve = eleveRepository.findByTrackingId(eleveTrackingId)
                .orElseThrow(() -> new RuntimeException("Élève introuvable"));

        boolean profilRempli = false;
        StringBuilder profilBuilder = new StringBuilder();
        profilBuilder.append("Profil de l'élève :\n");
        profilBuilder.append("- Niveau : ").append(eleve.getNiveau() != null ? eleve.getNiveau() : "Non renseigné").append("\n");
        profilBuilder.append("- Type : ").append(eleve.getTypeApprenant() != null ? eleve.getTypeApprenant().name() : "Non renseigné").append("\n");
        profilBuilder.append("- Établissement : ").append(eleve.getEtablissement() != null ? eleve.getEtablissement() : "Non renseigné").append("\n");
        profilBuilder.append("- Filière actuelle : ").append(eleve.getFiliere() != null ? eleve.getFiliere() : "Non renseigné").append("\n");
        profilBuilder.append("- Métier souhaité : ").append(eleve.getMetierSouhaite() != null ? eleve.getMetierSouhaite() : "Non renseigné").append("\n");
        profilBuilder.append("- Matières préférées : ").append(eleve.getMatieresPreferees() != null ? eleve.getMatieresPreferees() : "Non renseigné").append("\n");

        boolean aDesNotes = false;
        List<NoteSaisiManuel> notes = noteRepository.findByEleveTrackingIdOrderByAnneeScolaireDesc(eleveTrackingId);
        if (!notes.isEmpty()) {
            aDesNotes = true;
            profilBuilder.append("\nNotes scolaires :\n");
            for (NoteSaisiManuel note : notes) {
                profilBuilder.append("- ").append(note.getMatiere()).append(" : ").append(note.getNote()).append("/20\n");
            }
        }

        boolean aDesQuiz = false;
        List<ResultatDiagnostic> resultats = resultatRepository
                .findByEleveTrackingIdOrderByDatePassageDesc(eleveTrackingId,
                        org.springframework.data.domain.PageRequest.of(0, 5))
                .getContent();
        if (!resultats.isEmpty()) {
            aDesQuiz = true;
            profilBuilder.append("\nRésultats de quiz d'orientation :\n");
            for (ResultatDiagnostic r : resultats) {
                Optional<Quiz> quiz = quizRepository.findByTrackingId(r.getQuiz().getTrackingId());
                String nomQuiz = quiz.map(Quiz::getTitre).orElse("Quiz inconnu");
                profilBuilder.append("- ").append(nomQuiz).append(" : score ").append(r.getScoreFinal()).append("\n");
            }
        }

        profilRempli = eleve.getNiveau() != null || eleve.getFiliere() != null
                || eleve.getMetierSouhaite() != null || eleve.getMatieresPreferees() != null
                || aDesNotes || aDesQuiz;

        if (!profilRempli) {
            // Cas "profil incomplet" : message déterministe, AUCUN appel LLM.
            // Le frontend affichera un bandeau bleu "complète ton profil" plutôt
            // qu'un message d'erreur générique.
            log.info("Recommandation IA : profil incomplet pour élève {} "
                            + "(niveau={}, filiere={}, metier={}, matieres={}, notes={}, quiz={}) — "
                            + "retour PROFIL_INCOMPLET sans appel LLM",
                    eleveTrackingId,
                    eleve.getNiveau(), eleve.getFiliere(), eleve.getMetierSouhaite(),
                    eleve.getMatieresPreferees(), aDesNotes, aDesQuiz);
            return RecommandationIAResponse.profilIncomplet(
                "Je n'ai pas encore assez d'informations sur toi pour te faire une recommandation personnalisée. "
                + "Pour obtenir ta recommandation, je te propose de :\n\n"
                + "1️⃣ Compléter ton profil (niveau, filière, métier souhaité)\n"
                + "2️⃣ Renseigner tes notes scolaires\n"
                + "3️⃣ Passer les quiz d'orientation (RIASEC, personnalité)\n\n"
                + "Une fois ces informations remplies, reviens ici et je pourrai te générer "
                + "une recommandation sur mesure adaptée à ton profil ! 🎯");
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

        try {
            log.info("Recommandation IA : appel LLM pour élève {} (contexte : {} filières, {} métiers, {} établissements publiés)",
                    eleveTrackingId, filieres.size(), metiers.size(), etablissements.size());
            String reponse = aiService.generateAnswer(question, List.of(
                    profilBuilder.toString(),
                    contexteBuilder.toString()));
            log.info("Recommandation IA : LLM OK pour élève {} ({} caractères)",
                    eleveTrackingId, reponse.length());
            return RecommandationIAResponse.ok(reponse);
        } catch (Exception e) {
            // Cas "erreur LLM" : on a essayé d'appeler le LLM, il a échoué.
            // On distingue maintenant ce cas de "profil incomplet" pour que
            // le frontend affiche un bandeau "service indisponible, réessayez"
            // plutôt qu'un message générique confus.
            log.error("Erreur génération recommandation IA pour élève {}", eleveTrackingId, e);
            return RecommandationIAResponse.erreurLlm(
                "Le service de recommandation IA est momentanément indisponible. "
                + "Réessaie dans quelques instants, ou complète ton profil pour "
                + "améliorer la qualité des prochaines recommandations.");
        }
    }
}
