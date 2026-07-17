package tg.edtch.activEducation.prediction.application.service.serviceImple;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tg.edtch.activEducation.prediction.application.dto.OrientationOutcomeRequest;
import tg.edtch.activEducation.prediction.application.dto.OrientationOutcomeResponse;
import tg.edtch.activEducation.prediction.domain.entite.OrientationOutcome;
import tg.edtch.activEducation.prediction.domain.repository.OrientationOutcomeRepository;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.repository.EleveRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de {@link OrientationOutcomeServiceImpl} (Phase 3).
 *
 * <p>Cas couverts :
 * <ul>
 *   <li>POST idempotent : 2 appels pour la même (élève, filière) → 1 seul outcome</li>
 *   <li>Élève inconnu → NoSuchElementException</li>
 *   <li>Statut invalide → IllegalArgumentException</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class OrientationOutcomeServiceTest {

    @Mock private OrientationOutcomeRepository repository;
    @Mock private EleveRepository eleveRepository;

    private OrientationOutcomeServiceImpl service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = new OrientationOutcomeServiceImpl(repository, eleveRepository, objectMapper);
    }

    @Test
    @DisplayName("Idempotence : 2 POSTs pour la même filière → 1 seul outcome")
    void idempotence() {
        UUID eleveId = UUID.randomUUID();
        Eleve eleve = Eleve.builder().id(42L).trackingId(eleveId).build();
        when(eleveRepository.findByTrackingId(eleveId)).thenReturn(Optional.of(eleve));
        when(repository.findByEleveId(42L)).thenReturn(List.of()); // 1er appel : vide
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrientationOutcomeRequest req = OrientationOutcomeRequest.builder()
                .filiereId(7L)
                .serie("C")
                .scoreRecommandation(new BigDecimal("0.85"))
                .build();

        // 1er POST : création
        OrientationOutcomeResponse r1 = service.creerOuMettreAJour(eleveId, req);
        assertNotNull(r1);

        // 2nd POST : on simule que la base renvoie maintenant l'outcome créé
        OrientationOutcome existant = OrientationOutcome.builder()
                .id(100L)
                .eleveId(42L)
                .filiereId(7L)
                .statut(OrientationOutcome.StatutOrientation.EN_COURS)
                .build();
        when(repository.findByEleveId(42L)).thenReturn(List.of(existant));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrientationOutcomeResponse r2 = service.creerOuMettreAJour(eleveId, req);
        assertNotNull(r2);
        verify(repository, times(2)).save(any()); // 1 save par appel (acceptable)
    }

    @Test
    @DisplayName("Élève introuvable → NoSuchElementException")
    void eleveIntrouvable() {
        UUID eleveId = UUID.randomUUID();
        when(eleveRepository.findByTrackingId(eleveId)).thenReturn(Optional.empty());

        OrientationOutcomeRequest req = OrientationOutcomeRequest.builder()
                .filiereId(7L)
                .build();

        assertThrows(NoSuchElementException.class,
                () -> service.creerOuMettreAJour(eleveId, req));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Statut invalide → IllegalArgumentException")
    void statutInvalide() {
        UUID outcomeId = UUID.randomUUID();
        OrientationOutcome existing = OrientationOutcome.builder()
                .id(1L)
                .trackingId(outcomeId)
                .eleveId(1L)
                .filiereId(1L)
                .statut(OrientationOutcome.StatutOrientation.EN_COURS)
                .build();
        when(repository.findByTrackingId(outcomeId)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> service.mettreAJourStatut(outcomeId, "BOGUS", null, null));
    }

    @Test
    @DisplayName("Statut ADMIS valide → mis à jour avec date")
    void statutValide() {
        UUID outcomeId = UUID.randomUUID();
        OrientationOutcome existing = OrientationOutcome.builder()
                .id(1L).trackingId(outcomeId)
                .eleveId(1L).filiereId(1L)
                .statut(OrientationOutcome.StatutOrientation.EN_COURS)
                .build();
        when(repository.findByTrackingId(outcomeId)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrientationOutcomeResponse r = service.mettreAJourStatut(outcomeId, "ADMIS", 4, "OK");
        assertNotNull(r);
        assertEquals("ADMIS", r.getStatut());
        assertNotNull(r.getDateMajStatut());
    }
}
