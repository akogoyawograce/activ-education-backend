package tg.edtch.activEducation.prediction.application.service.serviceImple;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tg.edtch.activEducation.prediction.domain.dto.PredictionReussiteRequest;
import tg.edtch.activEducation.prediction.domain.dto.PredictionReussiteResponse;
import tg.edtch.activEducation.prediction.domain.entite.PredictionReussite;
import tg.edtch.activEducation.prediction.repository.PredictionReussiteRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de {@link PredictionServiceImpl}.
 *
 * <p>Cas couverts :
 * <ul>
 *   <li>creer() persiste avec un trackingId auto-généré et retourne un DTO mappé</li>
 *   <li>listerParEleve() délègue au repo et mappe la sortie</li>
 *   <li>creer() avec un body null ou un filiereTrackingId vide → IllegalArgumentException</li>
 *   <li>listerParEleve() avec un UUID null → IllegalArgumentException</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @Mock private PredictionReussiteRepository repository;

    private PredictionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PredictionServiceImpl(repository);
    }

    @Test
    @DisplayName("creer() : persiste avec trackingId auto et mappe le DTO")
    void creer() {
        UUID eleveId = UUID.randomUUID();
        PredictionReussiteRequest req = new PredictionReussiteRequest(
                eleveId.toString(), // eleveTrackingId du body (sera écrasé par le path)
                UUID.randomUUID().toString(),
                "Informatique",
                0.87,
                "match_riasec=0.9, ecart_notes=2.5"
        );

        when(repository.save(any(PredictionReussite.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PredictionReussiteResponse response = service.creer(eleveId, req);

        assertNotNull(response);
        assertNotNull(response.trackingId(), "trackingId doit être généré");
        assertEquals(eleveId.toString(), response.eleveTrackingId(),
                "le path variable doit primer sur le body");
        assertEquals("Informatique", response.filiereNom());
        assertEquals(0.87, response.scorePrediction());
        assertNotNull(response.datePrediction());
    }

    @Test
    @DisplayName("creer() : body null → IllegalArgumentException, pas de save")
    void creerBodyNull() {
        UUID eleveId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class,
                () -> service.creer(eleveId, null));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("creer() : filiereTrackingId manquant → IllegalArgumentException")
    void creerFiliereTrackingIdManquant() {
        UUID eleveId = UUID.randomUUID();
        PredictionReussiteRequest req = new PredictionReussiteRequest(
                eleveId.toString(), null, "Informatique", 0.5, null);
        assertThrows(IllegalArgumentException.class,
                () -> service.creer(eleveId, req));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("creer() : path null → IllegalArgumentException")
    void creerPathNull() {
        PredictionReussiteRequest req = new PredictionReussiteRequest(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "Lettres", 0.4, null);
        assertThrows(IllegalArgumentException.class,
                () -> service.creer(null, req));
    }

    @Test
    @DisplayName("listerParEleve() : délègue au repo et mappe la liste")
    void lister() {
        UUID eleveId = UUID.randomUUID();
        PredictionReussite p1 = PredictionReussite.builder()
                .trackingId(UUID.randomUUID())
                .eleveTrackingId(eleveId.toString())
                .filiereTrackingId(UUID.randomUUID().toString())
                .filiereNom("Médecine")
                .scorePrediction(0.75)
                .build();
        when(repository.findByEleveTrackingIdOrderByDatePredictionDesc(eleveId.toString()))
                .thenReturn(List.of(p1));

        List<PredictionReussiteResponse> result = service.listerParEleve(eleveId);

        assertEquals(1, result.size());
        assertEquals("Médecine", result.get(0).filiereNom());
        assertEquals(eleveId.toString(), result.get(0).eleveTrackingId());
    }

    @Test
    @DisplayName("listerParEleve() : UUID null → IllegalArgumentException")
    void listerUuidNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.listerParEleve(null));
    }
}
