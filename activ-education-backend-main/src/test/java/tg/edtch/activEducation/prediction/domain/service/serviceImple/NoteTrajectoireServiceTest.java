package tg.edtch.activEducation.prediction.domain.service.serviceImple;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tg.edtch.activEducation.prediction.domain.service.NoteTrajectoireService;
import tg.edtch.activEducation.prediction.domain.service.NoteTrajectoireService.Trajectoire;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires du calcul de trajectoire (Phase 3 § 3 du brief).
 *
 * <p>Couvre les 3 cas exigés :
 * <ul>
 *   <li>3 points : régression linéaire, confiance 1.0</li>
 *   <li>2 points : pente simple, confiance 0.7</li>
 *   <li>1 point : pas de projection, confiance 0.5</li>
 * </ul>
 */
class NoteTrajectoireServiceTest {

    private NoteTrajectoireService service;

    @BeforeEach
    void setUp() {
        service = new NoteTrajectoireServiceImpl();
    }

    @Test
    @DisplayName("3 points : régression linéaire avec confiance 1.0")
    void troisPoints() {
        // Progression régulière +0.5 par an
        List<BigDecimal> notes = List.of(
                new BigDecimal("12.0"),
                new BigDecimal("13.0"),
                new BigDecimal("14.0"));

        Trajectoire t = service.calculer(notes);

        assertEquals(3, t.nbPointsUtilisés());
        assertEquals(1.0, t.confiance(), 0.001);
        assertEquals(new BigDecimal("14.0"), t.noteActuelle());
        // Pente ≈ 1.0 (progression de +1 par an sur 3 points)
        assertEquals(1.0, t.pente().doubleValue(), 0.01);
        // Extrapolation à X=3 : 15.0
        assertEquals(new BigDecimal("15.00"), t.noteExtrapolée());
    }

    @Test
    @DisplayName("2 points : pente simple, confiance 0.7")
    void deuxPoints() {
        List<BigDecimal> notes = List.of(
                new BigDecimal("10.0"),
                new BigDecimal("13.0"));

        Trajectoire t = service.calculer(notes);

        assertEquals(2, t.nbPointsUtilisés());
        assertEquals(0.7, t.confiance(), 0.001);
        assertEquals(new BigDecimal("13.0"), t.noteActuelle());
        // Pente = 3.0 sur 1 an
        assertEquals(3.0, t.pente().doubleValue(), 0.01);
        // Extrapolation : 16.0 (scale 2 dans le service)
        assertEquals(0, t.noteExtrapolée().compareTo(new BigDecimal("16.00")));
    }

    @Test
    @DisplayName("1 point : pas de projection, confiance 0.5")
    void unPoint() {
        List<BigDecimal> notes = List.of(new BigDecimal("11.5"));

        Trajectoire t = service.calculer(notes);

        assertEquals(1, t.nbPointsUtilisés());
        assertEquals(0.5, t.confiance(), 0.001);
        assertEquals(new BigDecimal("11.5"), t.noteActuelle());
        assertEquals(new BigDecimal("11.5"), t.noteExtrapolée());
        assertEquals(0, t.pente().doubleValue(), 0.001);
    }

    @Test
    @DisplayName("Liste vide : trajectoire nulle")
    void listeVide() {
        Trajectoire t = service.calculer(List.of());

        assertEquals(0, t.nbPointsUtilisés());
        assertNull(t.noteActuelle());
        assertNull(t.noteExtrapolée());
        assertEquals(0.0, t.confiance(), 0.001);
    }

    @Test
    @DisplayName("3 points en régression : pente négative, score baisse")
    void regression() {
        List<BigDecimal> notes = List.of(
                new BigDecimal("16.0"),
                new BigDecimal("14.0"),
                new BigDecimal("12.0"));

        Trajectoire t = service.calculer(notes);

        assertEquals(-2.0, t.pente().doubleValue(), 0.01);
        // Extrapolation : 10.0
        assertEquals(new BigDecimal("10.00"), t.noteExtrapolée());
    }

    @Test
    @DisplayName("Extrapolation clampée à [0, 20]")
    void extrapolationsBornees() {
        // Cas extrême : pente énorme, on clamp à 20
        List<BigDecimal> notes = List.of(
                new BigDecimal("5.0"),
                new BigDecimal("15.0"),
                new BigDecimal("19.0"));

        Trajectoire t = service.calculer(notes);

        assertTrue(t.noteExtrapolée().doubleValue() <= 20.0);
        assertTrue(t.noteExtrapolée().doubleValue() >= 0.0);
    }
}
