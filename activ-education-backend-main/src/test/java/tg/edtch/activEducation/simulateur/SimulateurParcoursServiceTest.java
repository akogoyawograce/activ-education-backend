package tg.edtch.activEducation.simulateur;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheSerie;
import tg.edtch.activEducation.bibliotheque.repository.FicheEtablissementRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheFiliereRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheMetierRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheSerieRepository;
import tg.edtch.activEducation.diagnostic.domain.entite.SeuilAdmission;
import tg.edtch.activEducation.diagnostic.repository.SeuilAdmissionRepository;
import tg.edtch.activEducation.simulateur.domain.dto.ScenarioRequest;
import tg.edtch.activEducation.simulateur.domain.dto.ScenarioResult;
import tg.edtch.activEducation.simulateur.domain.service.SimulateurParcoursService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du {@link SimulateurParcoursService} — Chantier A.
 *
 * <p>On vérifie ici la nouvelle logique de comparaison : le
 * {@code /comparer} doit produire une {@link ScenarioResult.ComparaisonAnalyse}
 * avec le bon meilleur/pire scénario, le bon delta, et la bonne synthèse.</p>
 *
 * <p>Pour les tests, on part d'un setup minimal : on injecte 1 filière
 * (Mathématiques) dans chaque scénario, avec un score différent via
 * le mécanisme de seuils. Le service est mocké sur les repositories,
 * pas sur les entités.</p>
 */
@ExtendWith(MockitoExtension.class)
class SimulateurParcoursServiceTest {

    @Mock private FicheSerieRepository serieRepository;
    @Mock private FicheFiliereRepository filiereRepository;
    @Mock private FicheMetierRepository metierRepository;
    @Mock private FicheEtablissementRepository etablissementRepository;
    @Mock private SeuilAdmissionRepository seuilRepository;

    private SimulateurParcoursService service;

    private FicheFiliere filiereMaths;
    private SeuilAdmission seuilMaths;

    @BeforeEach
    void setUp() {
        service = new SimulateurParcoursService(serieRepository, filiereRepository,
                metierRepository, etablissementRepository, seuilRepository);

        // 1 filière unique : Mathématiques, accessible depuis tous niveaux
        filiereMaths = FicheFiliere.builder()
                .trackingId(UUID.randomUUID())
                .titre("Mathématiques")
                .domaine("Sciences")
                .duree("3 ans")
                .niveauRequis("BAC_1,BAC_2,BAC_3")
                .estPublie(true)
                .metiersPrepares(java.util.Set.of())
                .etablissements(java.util.Set.of())
                .build();

        // 1 seuil d'admission : Maths ≥ 12/20
        seuilMaths = SeuilAdmission.builder()
                .trackingId(UUID.randomUUID())
                .filiere(filiereMaths)
                .matiereRequise("Mathématiques")
                .noteMinimum(12.0)
                .build();

        // Les seuils et fiches sont toujours retournés par les repos
        when(seuilRepository.findAll()).thenReturn(List.of(seuilMaths));

        // Si pas de série précisée, on prend toutes les filières publiées
        Page<FicheFiliere> page = new PageImpl<>(List.of(filiereMaths));
        when(filiereRepository.findAllByEstPublieTrue(any())).thenReturn(page);
    }

    @Test
    @DisplayName("Chantier A / comparer : 2 scénarios avec 1 filière commune → analyse calculée")
    void comparerAvecFiliereCommune() {
        // Setup minimal : 1 filière (Maths, seuil ≥ 12). Les 2 scénarios
        // valident ce seuil → même filière dans les 2, donc filière commune.
        // Mais les scénarios ont des notes différentes → delta de score
        // sur la filière commune observable.
        ScenarioRequest s1 = new ScenarioRequest();
        s1.setTitre("Scénario A : maths excellentes");
        s1.setNotesSimulees(java.util.Map.of("Mathematiques", 18.0));

        ScenarioRequest s2 = new ScenarioRequest();
        s2.setTitre("Scénario B : maths juste au seuil");
        s2.setNotesSimulees(java.util.Map.of("Mathematiques", 12.0));

        List<ScenarioResult> resultats = service.comparer(List.of(s1, s2));

        assertEquals(2, resultats.size());

        ScenarioResult.ComparaisonAnalyse analyse = resultats.get(0).getComparaison();
        assertNotNull(analyse, "comparer doit produire une analyse");
        assertEquals(2, analyse.getNombreScenarios());
        assertNotNull(analyse.getMeilleurScenario());
        assertNotNull(analyse.getPireScenario());
        assertEquals(1, analyse.getNombreFilieresCommunes(),
                "Mathématiques doit être commune aux 2 scénarios");
        assertNotNull(analyse.getDeltasParFiliere());
        assertTrue(analyse.getDeltasParFiliere().containsKey("mathématiques"),
                "la clé doit être lowercase");
        assertEquals(2, analyse.getDeltasParFiliere().get("mathématiques").size());
        assertNotNull(analyse.getSynthese());
        assertTrue(analyse.getSynthese().length() > 0);
    }

    @Test
    @DisplayName("Chantier A / comparer : aucun scénario commun → synthèse 'Aucun point commun'")
    void comparerAucunCommun() {
        // Aucun des 2 scénarios n'a de notes : aucune filière ne matche.
        ScenarioRequest s1 = new ScenarioRequest();
        s1.setTitre("Scénario vide 1");

        ScenarioRequest s2 = new ScenarioRequest();
        s2.setTitre("Scénario vide 2");

        List<ScenarioResult> resultats = service.comparer(List.of(s1, s2));

        ScenarioResult.ComparaisonAnalyse analyse = resultats.get(0).getComparaison();
        assertNotNull(analyse);
        assertEquals(0, analyse.getNombreFilieresCommunes());
        // La synthèse doit signaler l'absence de points communs
        assertTrue(analyse.getSynthese().contains("Aucun point commun")
                || analyse.getSynthese().contains("aucune filière"),
                "la synthèse doit expliquer l'absence de comparaison");
    }

    @Test
    @DisplayName("Chantier A / explorer reste vert (non-régression)")
    void explorerNonRegression() {
        ScenarioRequest s = new ScenarioRequest();
        s.setTitre("Mon exploration");
        s.setNotesSimulees(java.util.Map.of("Mathematiques", 15.0));

        ScenarioResult result = service.explorer(s);

        assertNotNull(result);
        assertEquals("Mon exploration", result.getTitre());
        assertNull(result.getComparaison(),
                "explorer() ne doit pas produire d'analyse comparative");
        assertNotNull(result.getFilieres());
        assertTrue(result.getFilieres().size() >= 1,
                "au moins 1 filière doit matcher (Mathématiques avec 15 ≥ 12)");
    }

    @Test
    @DisplayName("Chantier A / comparer : 1 seul scénario → pas d'analyse (early return)")
    void comparerUnSeulScenario() {
        ScenarioRequest s = new ScenarioRequest();
        s.setTitre("Seul");

        List<ScenarioResult> resultats = service.comparer(List.of(s));
        assertEquals(1, resultats.size());
        assertNull(resultats.get(0).getComparaison(),
                "avec 1 seul scénario, pas d'analyse");
    }
}
