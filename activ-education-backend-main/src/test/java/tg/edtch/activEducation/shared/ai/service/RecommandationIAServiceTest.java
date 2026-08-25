package tg.edtch.activEducation.shared.ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import tg.edtch.activEducation.shared.ai.domain.dto.RecommandationIAResponse.Statut;
import tg.edtch.activEducation.shared.ai.service.RecommandationIAService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests anti-régression pour le bug widget "Ma recommandation" (6 août 2026).
 *
 * <p>Contexte : avant cette correction, le service renvoyait
 * systématiquement le même message "Désolé, je n'ai pas pu générer..."
 * pour deux cas très différents : (1) profil vide (cas normal, pas d'appel
 * LLM) et (2) LLM en panne (erreur technique). Le frontend ne pouvait
 * pas afficher un bandeau adapté à chaque cas.</p>
 *
 * <p>Ces tests verrouillent les nouveaux invariants :
 * <ol>
 *   <li>Profil vide (niveau, filière, métier, notes, quiz tous null)
 *       → statut {@code PROFIL_INCOMPLET}, AUCUN appel LLM.</li>
 *   <li>Profil complet + LLM OK → statut {@code OK}, texte = réponse LLM.</li>
 *   <li>Profil complet + LLM throw → statut {@code ERREUR_LLM}, message
 *       dédié, AUCUNE exception remontée au controller (sinon → 500).</li>
 * </ol>
 * </p>
 */
class RecommandationIAServiceTest {

    private EleveRepository eleveRepository;
    private NoteSaisiManuelRepository noteRepository;
    private ResultatDiagnosticRepository resultatRepository;
    private QuizRepository quizRepository;
    private FicheFiliereRepository filiereRepository;
    private FicheMetierRepository metierRepository;
    private FicheEtablissementRepository etablissementRepository;
    private AIEmbeddingService aiService;
    private RecommandationIAService service;

    private static final UUID ELEVE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        eleveRepository = mock(EleveRepository.class);
        noteRepository = mock(NoteSaisiManuelRepository.class);
        resultatRepository = mock(ResultatDiagnosticRepository.class);
        quizRepository = mock(QuizRepository.class);
        filiereRepository = mock(FicheFiliereRepository.class);
        metierRepository = mock(FicheMetierRepository.class);
        etablissementRepository = mock(FicheEtablissementRepository.class);
        aiService = mock(AIEmbeddingService.class);

        service = new RecommandationIAService(
            eleveRepository, noteRepository, resultatRepository,
            quizRepository, filiereRepository, metierRepository,
            etablissementRepository, aiService);

        // Par défaut : listes vides, quiz vide
        when(noteRepository.findByEleveTrackingIdOrderByAnneeScolaireDesc(any(UUID.class)))
            .thenReturn(List.of());
        Page<ResultatDiagnostic> pageVide = new PageImpl<>(List.of());
        when(resultatRepository.findByEleveTrackingIdOrderByDatePassageDesc(
            any(UUID.class), any(Pageable.class)))
            .thenReturn(pageVide);
        when(filiereRepository.findAllByEstPublieTrue(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));
        when(metierRepository.findAllByEstPublieTrue(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));
        when(etablissementRepository.findAllByEstPublieTrue(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));
    }

    private Eleve eleveVide() {
        Eleve e = new Eleve();
        // Tous les champs restent null → profilRempli = false
        return e;
    }

    private Eleve eleveAvecNiveau() {
        Eleve e = eleveVide();
        e.setNiveau(tg.edtch.activEducation.profil.domain.enums.NiveauScolaire.LYCEE_TLE);
        return e;
    }

    // ─────────────────────────────────────────────────────────────────
    // Test 1 — Profil vide : pas d'appel LLM, statut PROFIL_INCOMPLET
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Élève avec profil vide → statut PROFIL_INCOMPLET, AUCUN appel LLM")
    void profilVide_renvoieProfilIncompletSansAppelLlm() {
        when(eleveRepository.findByTrackingId(ELEVE_ID))
            .thenReturn(Optional.of(eleveVide()));

        RecommandationIAResponse resp = service.genererRecommandation(ELEVE_ID);

        assertThat(resp.type()).isEqualTo(Statut.PROFIL_INCOMPLET);
        assertThat(resp.recommandation()).isNotBlank();
        // Le message invite explicitement à compléter le profil.
        assertThat(resp.recommandation())
            .containsAnyOf("compléter ton profil", "renseigner tes notes", "quiz");

        // Le LLM ne doit PAS être appelé pour un profil vide
        // (économie d'API + correction du bug "message d'erreur générique
        // même quand l'élève n'a pas de profil").
        verify(aiService, never()).generateAnswer(anyString(), any());
    }

    // ─────────────────────────────────────────────────────────────────
    // Test 2 — Profil complet + LLM OK : statut OK avec texte LLM
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Élève avec niveau + LLM OK → statut OK, texte = réponse LLM")
    void profilComplet_llmOk_renvoieOk() {
        when(eleveRepository.findByTrackingId(ELEVE_ID))
            .thenReturn(Optional.of(eleveAvecNiveau()));
        when(aiService.generateAnswer(anyString(), any()))
            .thenReturn("Voici 3 filières adaptées à ton profil : Info, Maths, Physique.");

        RecommandationIAResponse resp = service.genererRecommandation(ELEVE_ID);

        assertThat(resp.type()).isEqualTo(Statut.OK);
        assertThat(resp.recommandation())
            .contains("3 filières adaptées");
        verify(aiService).generateAnswer(anyString(), any());
    }

    // ─────────────────────────────────────────────────────────────────
    // Test 2b — Profil complet (niveau + notes + quiz + filières publiées)
    // → statut OK avec une recommandation NON vide (scénario du widget home)
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Élève avec niveau + notes + quiz + filières publiées → statut OK, recommandation non vide")
    void profilCompletAvecNotesEtQuiz_llmOk_renvoieRecommandationNonVide() {
        Eleve e = eleveAvecNiveau();
        e.setFiliere("Informatique");
        e.setMetierSouhaite("Développeur");
        when(eleveRepository.findByTrackingId(ELEVE_ID)).thenReturn(Optional.of(e));

        // Notes scolaires présentes
        NoteSaisiManuel note = new NoteSaisiManuel();
        note.setMatiere("Maths");
        note.setNote(16.0);
        when(noteRepository.findByEleveTrackingIdOrderByAnneeScolaireDesc(any(UUID.class)))
            .thenReturn(List.of(note));

        // Résultat de quiz présent (RIASEC)
        Quiz quiz = new Quiz();
        quiz.setTitre("Test RIASEC");
        when(quizRepository.findByTrackingId(any(UUID.class))).thenReturn(Optional.of(quiz));
        ResultatDiagnostic resultat = new ResultatDiagnostic();
        resultat.setScoreFinal(78.0);
        resultat.setQuiz(quiz);
        when(resultatRepository.findByEleveTrackingIdOrderByDatePassageDesc(
            any(UUID.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(resultat)));

        // Filières publiées disponibles en base
        FicheFiliere f1 = new FicheFiliere();
        f1.setTitre("Informatique");
        f1.setResume("Développement logiciel et data");
        when(filiereRepository.findAllByEstPublieTrue(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(f1)));

        when(aiService.generateAnswer(anyString(), any()))
            .thenReturn("Recommandation complète : 3 filières, 3 métiers et 3 établissements adaptés à ton profil.");

        RecommandationIAResponse resp = service.genererRecommandation(ELEVE_ID);

        assertThat(resp.type()).isEqualTo(Statut.OK);
        assertThat(resp.recommandation()).isNotBlank();
        // Le contexte passé au LLM contient bien les notes et le quiz
        verify(aiService).generateAnswer(
            argThat(question -> question.contains("3 filières d'études")),
            argThat(contextes -> {
                String join = String.join("\n", contextes);
                return join.contains("Maths : 16.0") && join.contains("Test RIASEC : score 78.0");
            }));
    }

    // ─────────────────────────────────────────────────────────────────
    // Test 3 — Profil complet + LLM throw : statut ERREUR_LLM, pas de 500
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Élève avec niveau + LLM throw → statut ERREUR_LLM, AUCUNE exception")
    void profilComplet_llmThrow_renvoieErreurLlm() {
        when(eleveRepository.findByTrackingId(ELEVE_ID))
            .thenReturn(Optional.of(eleveAvecNiveau()));
        when(aiService.generateAnswer(anyString(), any()))
            .thenThrow(new RuntimeException("OpenAI quota exceeded"));

        RecommandationIAResponse resp = service.genererRecommandation(ELEVE_ID);

        // Pas d'exception remontée (sinon 500 côté controller, ce qui était
        // le comportement avant le 6 août 2026).
        assertThat(resp.type()).isEqualTo(Statut.ERREUR_LLM);
        assertThat(resp.recommandation())
            .containsAnyOf("indisponible", "réessai", "réessaie");
        // Distinct de "profil incomplet" — l'élève A un profil, c'est l'IA qui a échoué.
        assertThat(resp.type()).isNotEqualTo(Statut.PROFIL_INCOMPLET);
    }

    // ─────────────────────────────────────────────────────────────────
    // Test 4 — Distinction stricte des 3 statuts
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Les 3 statuts sont mutuellement exclusifs (le frontend distingue OK/PROFIL_INCOMPLET/ERREUR_LLM)")
    void troisStatutsSontDistincts() {
        // Cas 1 : profil vide
        when(eleveRepository.findByTrackingId(ELEVE_ID))
            .thenReturn(Optional.of(eleveVide()));
        RecommandationIAResponse r1 = service.genererRecommandation(ELEVE_ID);
        assertThat(r1.type()).isEqualTo(Statut.PROFIL_INCOMPLET);

        // Cas 2 : profil complet + LLM OK
        when(eleveRepository.findByTrackingId(ELEVE_ID))
            .thenReturn(Optional.of(eleveAvecNiveau()));
        when(aiService.generateAnswer(anyString(), any()))
            .thenReturn("Bonne reco");
        RecommandationIAResponse r2 = service.genererRecommandation(ELEVE_ID);
        assertThat(r2.type()).isEqualTo(Statut.OK);

        // Cas 3 : profil complet + LLM throw
        when(aiService.generateAnswer(anyString(), any()))
            .thenThrow(new RuntimeException("boom"));
        RecommandationIAResponse r3 = service.genererRecommandation(ELEVE_ID);
        assertThat(r3.type()).isEqualTo(Statut.ERREUR_LLM);

        // Les 3 statuts sont tous différents
        assertThat(r1.type()).isNotEqualTo(r2.type());
        assertThat(r1.type()).isNotEqualTo(r3.type());
        assertThat(r2.type()).isNotEqualTo(r3.type());
    }
}
