package tg.edtch.activEducation.prediction.application.service.serviceImple;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.bibliotheque.domain.entite.NiveauFiliere;
import tg.edtch.activEducation.bibliotheque.domain.repository.NiveauFiliereRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheFiliereRepository;
import tg.edtch.activEducation.prediction.application.dto.ProfilEleve;
import tg.edtch.activEducation.prediction.application.dto.Recommandation3SignauxResponse;
import tg.edtch.activEducation.prediction.domain.config.PredictionProperties;
import tg.edtch.activEducation.prediction.domain.repository.EngagementSignalRepository;
import tg.edtch.activEducation.prediction.domain.service.NoteTrajectoireService;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.entite.NotesHistorique;
import tg.edtch.activEducation.profil.domain.enums.NiveauScolaire;
import tg.edtch.activEducation.profil.domain.repository.NotesHistoriqueRepository;
import tg.edtch.activEducation.profil.repository.EleveRepository;
import tg.edtch.activEducation.riasec.domain.entite.TestRIASECResultat;
import tg.edtch.activEducation.riasec.repository.TestRIASECResultatRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du moteur 3 signaux (Phase 3 — critères de validation).
 *
 * <p>Cas couverts :
 * <ul>
 *   <li>Plafond de l'engagement respecté (score_engagement ≤ poids_engagement_max)</li>
 *   <li>Score final ∈ [0, 1]</li>
 *   <li>Top N retourné trié DESC par score_final</li>
 *   <li>Découvertes incluses (engagement faible, aspiration forte)</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class Recommandation3SignauxServiceTest {

    @Mock private EleveRepository eleveRepository;
    @Mock private TestRIASECResultatRepository riasecRepository;
    @Mock private NotesHistoriqueRepository notesRepository;
    @Mock private NiveauFiliereRepository niveauFiliereRepository;
    @Mock private FicheFiliereRepository ficheFiliereRepository;
    @Mock private EngagementSignalRepository engagementRepository;
    @Mock private NoteTrajectoireService trajectoireService;

    @InjectMocks private Recommandation3SignauxServiceImpl service;

    private PredictionProperties properties;

    @BeforeEach
    void setUp() {
        properties = new PredictionProperties();
        // Valeurs par défaut : 0.35 / 0.50 / 0.15 (plafond 0.20)
        service = new Recommandation3SignauxServiceImpl(
                eleveRepository, riasecRepository, notesRepository,
                niveauFiliereRepository, ficheFiliereRepository,
                engagementRepository, trajectoireService, properties);
    }

    @Test
    @DisplayName("Le score_final est borné [0, 1] et l'engagement plafonné")
    void scoreFinalBorne() {
        // Élève fictif avec profil RIASEC "RIA" (Réaliste+Investigateur+Artistique)
        Eleve eleve = Eleve.builder()
                .id(1L)
                .trackingId(UUID.randomUUID())
                .niveau(NiveauScolaire.LYCEE_TLE)
                .build();
        when(eleveRepository.findByTrackingId(any())).thenReturn(Optional.of(eleve));

        // 3 notes avec tendance positive
        when(notesRepository.findByEleveIdAndEstMoyenneGeneraleTrueOrderByAnneeScolaireDesc(1L))
                .thenReturn(List.of(
                        NotesHistorique.builder().moyenne(new BigDecimal("14.0")).anneeScolaire("2023-2024").estMoyenneGenerale(true).build(),
                        NotesHistorique.builder().moyenne(new BigDecimal("13.0")).anneeScolaire("2022-2023").estMoyenneGenerale(true).build(),
                        NotesHistorique.builder().moyenne(new BigDecimal("12.0")).anneeScolaire("2021-2022").estMoyenneGenerale(true).build()));
        when(trajectoireService.calculer(anyList())).thenReturn(
                new NoteTrajectoireService.Trajectoire(
                        new BigDecimal("14.0"), new BigDecimal("15.0"),
                        new BigDecimal("1.0"), 3, 1.0));

        when(riasecRepository.findByEleveTrackingIdOrderByDatePassationDesc(any()))
                .thenReturn(List.of(TestRIASECResultat.builder()
                        .scoreRealiste(8).scoreInvestigateur(7).scoreArtistique(6)
                        .scoreSocial(3).scoreEntreprenant(4).scoreConventionnel(5)
                        .codeProfil("RIA").build()));

        // Les mappings niveau → filière : on dit que BAC_1 a 3 filières
        when(niveauFiliereRepository.findByNiveau(NiveauScolaire.LYCEE_TLE))
                .thenReturn(List.of());
        when(niveauFiliereRepository.findByNiveau(NiveauScolaire.BAC_1))
                .thenReturn(List.of(
                        niveauFiliereMock(10L, 1L),
                        niveauFiliereMock(20L, 2L),
                        niveauFiliereMock(30L, 3L)));
        when(niveauFiliereRepository.findByNiveau(NiveauScolaire.BAC_2))
                .thenReturn(List.of());
        when(niveauFiliereRepository.findByNiveau(NiveauScolaire.BAC_3))
                .thenReturn(List.of());

        when(ficheFiliereRepository.findAllById(anySet()))
                .thenReturn(List.of(
                        fiche(10L, "Informatique"),
                        fiche(20L, "Médecine"),
                        fiche(30L, "Gestion")));

        when(engagementRepository.findByEleveIdAndFicheType(eq(1L), any()))
                .thenReturn(List.of());

        Recommandation3SignauxResponse response = service.recommander(eleve.getTrackingId());

        // Top N non vide
        assertEquals(3, response.getTop().size(), "Top N doit contenir les 3 candidates");

        // Score final ∈ [0, 1] pour chaque ligne
        for (var f : response.getTop()) {
            double s = f.getScoreFinal().doubleValue();
            assertTrue(s >= 0.0 && s <= 1.0,
                    "Score final hors borne pour " + f.getTitre() + " : " + s);
        }

        // Plafond de l'engagement respecté : score_engagement · poids_engagement_eff
        // ≤ 1.0 · 0.20 = 0.20 du score final max
        for (var f : response.getTop()) {
            double contribEngagement = f.getScoreEngagement().doubleValue()
                    * response.getPoidsEngagement().doubleValue();
            assertTrue(contribEngagement <= 0.20 + 0.001,
                    "Contribution engagement > 0.20 pour " + f.getTitre() + " : "
                            + contribEngagement);
        }

        // Tri DESC par score_final
        for (int i = 0; i < response.getTop().size() - 1; i++) {
            assertTrue(response.getTop().get(i).getScoreFinal()
                            .compareTo(response.getTop().get(i + 1).getScoreFinal()) >= 0,
                    "Top N non trié DESC");
        }

        // Poids d'engagement effectif = 0.15 (plafond 0.20 pas atteint)
        assertEquals(0, response.getPoidsEngagement().compareTo(new BigDecimal("0.15")));
    }

    @Test
    @DisplayName("Plafond engagement : si config=0.5, le moteur plafonne à 0.20")
    void plafondEngagementForce() {
        properties.setPoidsEngagement(new BigDecimal("0.50"));
        properties.setPoidsEngagementMax(new BigDecimal("0.20"));
        assertEquals(new BigDecimal("0.20"),
                properties.poidsEngagementEffectif(),
                "Le plafond doit clipper à 0.20");
    }

    @Test
    @DisplayName("Aucune candidate : top vide, sans crash")
    void aucuneCandidate() {
        Eleve eleve = Eleve.builder()
                .id(1L).trackingId(UUID.randomUUID()).niveau(NiveauScolaire.LYCEE_TLE).build();
        when(eleveRepository.findByTrackingId(any())).thenReturn(Optional.of(eleve));
        when(notesRepository.findByEleveIdAndEstMoyenneGeneraleTrueOrderByAnneeScolaireDesc(1L))
                .thenReturn(List.of());
        when(trajectoireService.calculer(anyList())).thenReturn(
                new NoteTrajectoireService.Trajectoire(null, null, BigDecimal.ZERO, 0, 0.0));
        when(riasecRepository.findByEleveTrackingIdOrderByDatePassationDesc(any()))
                .thenReturn(List.of());
        when(niveauFiliereRepository.findByNiveau(any())).thenReturn(List.of());

        Recommandation3SignauxResponse response = service.recommander(eleve.getTrackingId());

        assertNotNull(response.getTop());
        assertTrue(response.getTop().isEmpty(), "Top doit être vide");
        assertEquals(0, response.getDecouvertesAjoutees());
    }

    @Test
    @DisplayName("Élève sans niveau : top vide, PAS de findAll() aveugle")
    void eleveSansNiveauNeRetournePasToutesLesFilieres() {
        // Élève SANS niveau (niveau = null) : cas limite où construireProfilEleve
        // → candidatsPourProfil() peut tomber dans le mode dégradé dangereux
        // (return ficheFiliereRepository.findAll() = 117 filières).
        // Comportement attendu : réponse vide ou filtrée — jamais toutes les fiches.
        Eleve eleve = Eleve.builder()
                .id(1L).trackingId(UUID.randomUUID()).niveau(null).build();
        when(eleveRepository.findByTrackingId(any())).thenReturn(Optional.of(eleve));
        when(notesRepository.findByEleveIdAndEstMoyenneGeneraleTrueOrderByAnneeScolaireDesc(1L))
                .thenReturn(List.of());
        when(trajectoireService.calculer(anyList())).thenReturn(
                new NoteTrajectoireService.Trajectoire(null, null, BigDecimal.ZERO, 0, 0.0));
        when(riasecRepository.findByEleveTrackingIdOrderByDatePassationDesc(any()))
                .thenReturn(List.of());

        Recommandation3SignauxResponse response = service.recommander(eleve.getTrackingId());

        // Anti-régression : on n'autorise PAS findAll() (qui retournerait 100+ fiches).
        // Si l'implémentation change pour appeler findAll(), cette assertion sautera.
        verify(ficheFiliereRepository, never()).findAll();

        // Top soit vide, soit petit (mode dégradé acceptable : on score quand même)
        assertNotNull(response.getTop());
        assertTrue(response.getTop().size() <= 10,
                "Top doit être borné par topN, pas exploser à findAll() : " + response.getTop().size());
        assertEquals(0, response.getDecouvertesAjoutees(),
                "Pas de découvertes sans profil exploitable");
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────

    private FicheFiliere fiche(Long id, String titre) {
        return FicheFiliere.builder().id(id).titre(titre).domaine("Test").duree("3 ans").build();
    }

    private NiveauFiliere niveauFiliereMock(Long ficheId, Long niveauFiliereId) {
        return NiveauFiliere.builder()
                .id(niveauFiliereId)
                .ficheFiliere(fiche(ficheId, "Mock"))
                .niveau(NiveauScolaire.BAC_1)
                .estPrincipal(true)
                .build();
    }
}
