package tg.edtch.activEducation.profil.domain.service.serviceImple;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import tg.edtch.activEducation.prediction.application.dto.Recommandation3SignauxResponse;
import tg.edtch.activEducation.profil.application.dto.request.BulletinUploadRequest;
import tg.edtch.activEducation.profil.application.dto.response.BulletinUploadResponse;
import tg.edtch.activEducation.profil.application.dto.response.DocumentResponse;
import tg.edtch.activEducation.profil.application.dto.response.NoteSaisiManuelResponse;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.enums.Periode;
import tg.edtch.activEducation.profil.domain.enums.TypePeriode;
import tg.edtch.activEducation.profil.domain.service.DocumentService;
import tg.edtch.activEducation.profil.domain.service.NoteSaisiManuelService;
import tg.edtch.activEducation.profil.domain.service.OcrService;
import tg.edtch.activEducation.profil.repository.EleveRepository;
import tg.edtch.activEducation.prediction.application.service.Recommandation3SignauxService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du {@link BulletinUploadOrchestratorImpl} — Chantier C.
 *
 * <p>Stratégie : on mocke les 5 dépendances (EleveRepository, OcrService,
 * DocumentService, NoteSaisiManuelService, Recommandation3SignauxService)
 * et on vérifie que {@code orchestrer(...)} les appelle dans le bon ordre
 * avec les bons arguments. Mockito strict mode = on déclare explicitement
 * les stubs utilisés dans chaque test (pas de {@code @BeforeEach} global).</p>
 */
@ExtendWith(MockitoExtension.class)
class BulletinUploadOrchestratorImplTest {

    @Mock private EleveRepository eleveRepository;
    @Mock private OcrService ocrService;
    @Mock private DocumentService documentService;
    @Mock private NoteSaisiManuelService noteSaisiManuelService;
    @Mock private Recommandation3SignauxService recommandationService;

    private BulletinUploadOrchestratorImpl orchestrator;

    private static final UUID ELEVE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final String ANNEE = "2024-2025";

    @BeforeEach
    void setUp() {
        orchestrator = new BulletinUploadOrchestratorImpl(
                eleveRepository, ocrService, documentService,
                noteSaisiManuelService, recommandationService);
    }

    @Test
    @DisplayName("Chantier C / orchestrer : chemin nominal — 3 notes extraites → 3 notes créées + reco")
    void orchestrerCheminNominal() {
        // Setup : élève existant, OCR retourne 3 notes, reco non vide
        Eleve eleve = Eleve.builder().trackingId(ELEVE_ID).build();
        when(eleveRepository.findByTrackingId(ELEVE_ID)).thenReturn(Optional.of(eleve));

        MultipartFile file = new MockMultipartFile(
                "file", "bulletin.pdf", "application/pdf", "fake-pdf".getBytes());
        when(documentService.uploadDocument(eq(ELEVE_ID), eq(file),
                eq("BULLETIN"), any(String.class), eq(null)))
                .thenReturn(DocumentResponse.builder().id(42L).build());

        when(ocrService.extraireNotes(file)).thenReturn(List.of(
                new OcrService.NoteExtraite("Mathématiques", 14.5, 4.0),
                new OcrService.NoteExtraite("Français", 12.0, 3.0),
                new OcrService.NoteExtraite("Anglais", 16.0, 2.0)
        ));

        // Pour chaque note ajoutée, le service renvoie une NoteSaisiManuelResponse
        when(noteSaisiManuelService.ajouterNote(eq(ELEVE_ID), any()))
                .thenReturn(NoteSaisiManuelResponse.builder()
                        .trackingId(UUID.randomUUID())
                        .eleveTrackingId(ELEVE_ID)
                        .build());

        Recommandation3SignauxResponse reco = Recommandation3SignauxResponse.builder()
                .eleveTrackingId(ELEVE_ID)
                .top(List.of())  // peu importe, on vérifie juste qu'on récupère la reco
                .build();
        when(recommandationService.recommander(ELEVE_ID)).thenReturn(reco);

        // Act
        BulletinUploadRequest request = BulletinUploadRequest.builder()
                .file(file)
                .anneeScolaire(ANNEE)
                .periode(Periode.MILIEU)
                .typePeriode(TypePeriode.TRIMESTRE)
                .numeroPeriode(2)
                .build();
        BulletinUploadResponse result = orchestrator.orchestrer(ELEVE_ID, request);

        // Assert
        assertNotNull(result);
        assertEquals(ANNEE, result.getAnneeScolaire());
        assertEquals(Periode.MILIEU, result.getPeriode());
        assertEquals("Trimestre 2", result.getSemestreOuTrimestre());
        assertEquals(3, result.getNotesExtraites().size());
        assertEquals(3, result.getNotesCrees().size());
        assertNotNull(result.getRecommandation());
        assertTrue(result.getMessage().contains("3 note(s)"));

        // Vérifier que chaque service a été appelé
        verify(documentService, times(1)).uploadDocument(eq(ELEVE_ID), eq(file),
                eq("BULLETIN"), any(String.class), eq(null));
        verify(ocrService, times(1)).extraireNotes(file);
        verify(noteSaisiManuelService, times(3)).ajouterNote(eq(ELEVE_ID), any());
        verify(recommandationService, times(1)).recommander(ELEVE_ID);
    }

    @Test
    @DisplayName("Chantier C / orchestrer : élève introuvable → NoSuchElementException, court-circuit")
    void orchestrerEleveIntrouvable() {
        when(eleveRepository.findByTrackingId(ELEVE_ID)).thenReturn(Optional.empty());

        BulletinUploadRequest request = BulletinUploadRequest.builder()
                .file(new MockMultipartFile("file", "f.pdf", "application/pdf", "x".getBytes()))
                .anneeScolaire(ANNEE)
                .periode(Periode.DEBUT)
                .typePeriode(TypePeriode.TRIMESTRE)
                .numeroPeriode(1)
                .build();

        assertThrows(NoSuchElementException.class,
                () -> orchestrator.orchestrer(ELEVE_ID, request));

        // Aucun service en aval ne doit être appelé
        verify(documentService, never()).uploadDocument(any(), any(), any(), any(), any());
        verify(ocrService, never()).extraireNotes(any());
        verify(noteSaisiManuelService, never()).ajouterNote(any(), any());
        verify(recommandationService, never()).recommander(any());
    }

    @Test
    @DisplayName("Chantier C / orchestrer : OCR vide → 0 note créée, reco quand même")
    void orchestrerAucuneNoteExtraite() {
        Eleve eleve = Eleve.builder().trackingId(ELEVE_ID).build();
        when(eleveRepository.findByTrackingId(ELEVE_ID)).thenReturn(Optional.of(eleve));

        MultipartFile file = new MockMultipartFile(
                "file", "blurry.jpg", "image/jpeg", "x".getBytes());
        when(documentService.uploadDocument(eq(ELEVE_ID), eq(file),
                eq("BULLETIN"), any(String.class), eq(null)))
                .thenReturn(DocumentResponse.builder().id(7L).build());
        when(ocrService.extraireNotes(file)).thenReturn(List.of());  // OCR vide
        when(recommandationService.recommander(ELEVE_ID))
                .thenReturn(Recommandation3SignauxResponse.builder()
                        .eleveTrackingId(ELEVE_ID).top(List.of()).build());

        BulletinUploadRequest request = BulletinUploadRequest.builder()
                .file(file)
                .anneeScolaire(ANNEE)
                .periode(Periode.FIN)
                .typePeriode(TypePeriode.SEMESTRE)
                .numeroPeriode(2)
                .build();

        BulletinUploadResponse result = orchestrator.orchestrer(ELEVE_ID, request);

        assertEquals(0, result.getNotesExtraites().size());
        assertEquals(0, result.getNotesCrees().size());
        // La reco est quand même appelée (peut être utile à l'élève
        // même sans nouvelles notes — recommandations existantes).
        verify(recommandationService, times(1)).recommander(ELEVE_ID);
        verify(noteSaisiManuelService, never()).ajouterNote(any(), any());
    }

    @Test
    @DisplayName("Chantier C / orchestrerBatch : 2 bulletins → 2 résultats, séquentiels")
    void orchestrerBatch2Bulletins() {
        Eleve eleve = Eleve.builder().trackingId(ELEVE_ID).build();
        when(eleveRepository.findByTrackingId(ELEVE_ID)).thenReturn(Optional.of(eleve));
        when(documentService.uploadDocument(eq(ELEVE_ID), any(MultipartFile.class),
                eq("BULLETIN"), any(String.class), eq(null)))
                .thenReturn(DocumentResponse.builder().id(1L).build());
        when(ocrService.extraireNotes(any(MultipartFile.class)))
                .thenReturn(List.of(new OcrService.NoteExtraite("Maths", 12.0, 1.0)));
        when(noteSaisiManuelService.ajouterNote(eq(ELEVE_ID), any()))
                .thenReturn(NoteSaisiManuelResponse.builder().build());
        when(recommandationService.recommander(ELEVE_ID))
                .thenReturn(Recommandation3SignauxResponse.builder().top(List.of()).build());

        MultipartFile f1 = new MockMultipartFile("files", "b1.pdf", "application/pdf", "x".getBytes());
        MultipartFile f2 = new MockMultipartFile("files", "b2.pdf", "application/pdf", "y".getBytes());

        List<BulletinUploadRequest> requests = List.of(
                BulletinUploadRequest.builder()
                        .file(f1).anneeScolaire("2023-2024")
                        .periode(Periode.FIN).typePeriode(TypePeriode.TRIMESTRE).numeroPeriode(3)
                        .build(),
                BulletinUploadRequest.builder()
                        .file(f2).anneeScolaire("2024-2025")
                        .periode(Periode.MILIEU).typePeriode(TypePeriode.TRIMESTRE).numeroPeriode(2)
                        .build()
        );

        List<BulletinUploadResponse> resultats =
                orchestrator.orchestrerBatch(ELEVE_ID, requests);

        assertEquals(2, resultats.size());
        assertEquals("2023-2024", resultats.get(0).getAnneeScolaire());
        assertEquals("2024-2025", resultats.get(1).getAnneeScolaire());
        // 2 bulletins → 2 appels au moteur 3 signaux
        verify(recommandationService, times(2)).recommander(ELEVE_ID);
    }

    @Test
    @DisplayName("Chantier C / buildSemestreLabel : sanity check sur le mapping")
    void buildSemestreLabel() {
        BulletinUploadRequest trimestriel2 = BulletinUploadRequest.builder()
                .typePeriode(TypePeriode.TRIMESTRE).numeroPeriode(2).build();
        assertEquals("Trimestre 2", orchestrator.buildSemestreLabel(trimestriel2));

        BulletinUploadRequest semestriel1 = BulletinUploadRequest.builder()
                .typePeriode(TypePeriode.SEMESTRE).numeroPeriode(1).build();
        assertEquals("Semestre 1", orchestrator.buildSemestreLabel(semestriel1));

        BulletinUploadRequest trimestriel3 = BulletinUploadRequest.builder()
                .typePeriode(TypePeriode.TRIMESTRE).numeroPeriode(3).build();
        assertEquals("Trimestre 3", orchestrator.buildSemestreLabel(trimestriel3));
    }

    @Test
    @DisplayName("Chantier C / orchestrerBatch : liste vide → IllegalArgumentException")
    void orchestrerBatchVide() {
        assertThrows(IllegalArgumentException.class,
                () -> orchestrator.orchestrerBatch(ELEVE_ID, List.of()));
    }

    @Test
    @DisplayName("Chantier C / orchestrerBatch : > 3 bulletins → IllegalArgumentException")
    void orchestrerBatchTrop() {
        MultipartFile f = new MockMultipartFile("files", "x.pdf", "application/pdf", "x".getBytes());
        List<BulletinUploadRequest> trop = List.of(
                BulletinUploadRequest.builder().file(f).anneeScolaire("2021-2022").periode(Periode.FIN).typePeriode(TypePeriode.TRIMESTRE).numeroPeriode(3).build(),
                BulletinUploadRequest.builder().file(f).anneeScolaire("2022-2023").periode(Periode.FIN).typePeriode(TypePeriode.TRIMESTRE).numeroPeriode(3).build(),
                BulletinUploadRequest.builder().file(f).anneeScolaire("2023-2024").periode(Periode.FIN).typePeriode(TypePeriode.TRIMESTRE).numeroPeriode(3).build(),
                BulletinUploadRequest.builder().file(f).anneeScolaire("2024-2025").periode(Periode.FIN).typePeriode(TypePeriode.TRIMESTRE).numeroPeriode(3).build()
        );
        assertThrows(IllegalArgumentException.class,
                () -> orchestrator.orchestrerBatch(ELEVE_ID, trop));
    }
}
