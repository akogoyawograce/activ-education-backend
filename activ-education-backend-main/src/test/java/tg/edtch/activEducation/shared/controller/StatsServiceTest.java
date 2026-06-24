package tg.edtch.activEducation.shared.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tg.edtch.activEducation.accompagnement.repository.RendezVousRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheEtablissementRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheRepository;
import tg.edtch.activEducation.diagnostic.repository.QuizRepository;
import tg.edtch.activEducation.diagnostic.repository.ResultatDiagnosticRepository;
import tg.edtch.activEducation.profil.repository.ConseillerRepository;
import tg.edtch.activEducation.profil.repository.EleveRepository;
import tg.edtch.activEducation.profil.repository.ParentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock private EleveRepository eleveRepository;
    @Mock private ConseillerRepository conseillerRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private ResultatDiagnosticRepository resultatDiagnosticRepository;
    @Mock private RendezVousRepository rendezVousRepository;
    @Mock private FicheEtablissementRepository etablissementRepository;
    @Mock private FicheRepository ficheRepository;
    @Mock private ParentRepository parentRepository;

    private StatsService statsService;

    @BeforeEach
    void setUp() {
        statsService = new StatsService(eleveRepository, conseillerRepository,
                quizRepository, resultatDiagnosticRepository, rendezVousRepository,
                etablissementRepository, ficheRepository, parentRepository);
    }

    @Test
    void getKPIs_shouldReturnAllCounts() {
        when(eleveRepository.count()).thenReturn(100L);
        when(parentRepository.count()).thenReturn(50L);
        when(conseillerRepository.count()).thenReturn(10L);
        when(quizRepository.count()).thenReturn(5L);
        when(resultatDiagnosticRepository.count()).thenReturn(200L);
        when(etablissementRepository.count()).thenReturn(30L);
        when(ficheRepository.count()).thenReturn(80L);

        Map<String, Long> kpis = statsService.getKPIs();

        assertEquals(100L, kpis.get("totalEleves"));
        assertEquals(50L, kpis.get("totalParents"));
        assertEquals(10L, kpis.get("totalConseillers"));
        assertEquals(5L, kpis.get("totalQuiz"));
        assertEquals(200L, kpis.get("totalResultats"));
        assertEquals(30L, kpis.get("totalEtablissements"));
        assertEquals(80L, kpis.get("totalFiches"));
        assertEquals(7, kpis.size());
    }

    @Test
    void getInscriptionsParJour_shouldReturnFormattedData() {
        List<Object[]> raw = List.of(
                new Object[]{LocalDate.of(2026, 6, 1), 5L},
                new Object[]{LocalDate.of(2026, 6, 2), 3L});
        when(eleveRepository.compterInscriptionsParJour(any(LocalDateTime.class)))
                .thenReturn(raw);

        List<Map<String, Object>> result = statsService.getInscriptionsParJour(7);

        assertEquals(2, result.size());
        assertEquals("2026-06-01", result.get(0).get("date"));
        assertEquals(5L, result.get(0).get("count"));
    }

    @Test
    void getTypeApprenantDistribution_shouldReturnMap() {
        List<Object[]> raw = List.of(
                new Object[]{"LYCEEN", 60L},
                new Object[]{"ETUDIANT", 40L});
        when(eleveRepository.countByTypeApprenant()).thenReturn(raw);

        Map<String, Long> result = statsService.getTypeApprenantDistribution();

        assertEquals(2, result.size());
        assertEquals(60L, result.get("LYCEEN"));
        assertEquals(40L, result.get("ETUDIANT"));
    }

    @Test
    void getQuizParDomaine_shouldReturnMap() {
        List<Object[]> raw = List.of(
                new Object[]{"Sciences", 3L},
                new Object[]{"Lettres", 2L});
        when(quizRepository.compterParDomaineRaw()).thenReturn(raw);

        Map<String, Long> result = statsService.getQuizParDomaine();

        assertEquals(2, result.size());
        assertEquals(3L, result.get("Sciences"));
        assertEquals(2L, result.get("Lettres"));
    }
}
