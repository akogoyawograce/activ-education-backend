package tg.edtch.activEducation.prediction.application.service.serviceImple;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tg.edtch.activEducation.bibliotheque.domain.entite.NiveauFiliere;
import tg.edtch.activEducation.bibliotheque.domain.repository.NiveauFiliereRepository;
import tg.edtch.activEducation.profil.domain.enums.NiveauScolaire;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests du filtrage par niveau (Phase 3 — critères de validation).
 */
@ExtendWith(MockitoExtension.class)
class PredictionLookupServiceTest {

    @Mock private NiveauFiliereRepository niveauFiliereRepository;

    private PredictionLookupServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PredictionLookupServiceImpl(niveauFiliereRepository);
    }

    @Test
    @DisplayName("listerNiveaux : 7 niveaux canoniques")
    void listerNiveaux() {
        var result = service.listerNiveaux();
        assertEquals(7, result.size());
        assertTrue(result.stream().anyMatch(n -> "LYCEE_TLE".equals(n.getCode())));
        assertTrue(result.stream().anyMatch(n -> "BAC_1".equals(n.getCode())));
    }

    @Test
    @DisplayName("filieresPourNiveau : niveau inconnu → liste vide, pas d'exception")
    void niveauInconnu() {
        // Le service parse d'abord, et n'appelle findByNiveau que si le parsing
        // a réussi. Donc pas de stub nécessaire ici.
        var result = service.filieresPourNiveau("inconnu_xyz");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("filieresPourNiveau : libellé lisible résolu par parse()")
    void libelleLisible() {
        when(niveauFiliereRepository.findByNiveau(NiveauScolaire.LYCEE_TLE))
                .thenReturn(List.of());
        // "Terminale" doit être parsé en LYCEE_TLE
        var result = service.filieresPourNiveau("Terminale");
        assertNotNull(result);
    }

    @Test
    @DisplayName("filieresPourNiveau : niveau null → liste vide, pas d'exception")
    void niveauNull() {
        var result = service.filieresPourNiveau(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
