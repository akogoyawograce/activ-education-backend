package tg.edtch.activEducation.profil.application.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import tg.edtch.activEducation.profil.application.dto.request.BulletinUploadRequest;
import tg.edtch.activEducation.profil.application.dto.response.BulletinUploadResponse;
import tg.edtch.activEducation.profil.domain.enums.Periode;
import tg.edtch.activEducation.profil.domain.enums.TypePeriode;
import tg.edtch.activEducation.profil.domain.service.BulletinUploadOrchestrator;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du {@link BulletinUploadController} — Chantier C.
 *
 * <p>On suit le pattern des autres tests de controller dans le projet
 * (cf. {@code ScenarioTemplateControllerTest}) : on instancie le
 * controller avec un mock du service d'orchestration, et on appelle
 * directement les méthodes. Pas de MockMvc — il faudrait un contexte
 * Spring complet (security, validation) et ça alourdirait les tests
 * pour un controller fin.</p>
 */
@ExtendWith(MockitoExtension.class)
class BulletinUploadControllerTest {

    @Mock private BulletinUploadOrchestrator orchestrator;

    private BulletinUploadController controller;

    private static final UUID ELEVE_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @BeforeEach
    void setUp() {
        controller = new BulletinUploadController(orchestrator);
    }

    @Test
    @DisplayName("Chantier C / POST /bulletins : mono upload → 200 + BulletinUploadResponse")
    void uploadMono() {
        // Mock de l'orchestrateur : renvoie une réponse non-vide
        BulletinUploadResponse attendu = BulletinUploadResponse.builder()
                .anneeScolaire("2024-2025")
                .periode(Periode.MILIEU)
                .semestreOuTrimestre("Trimestre 2")
                .message("Bulletin analysé : 3 note(s) extraite(s)")
                .build();
        when(orchestrator.orchestrer(eq(ELEVE_ID), any(BulletinUploadRequest.class)))
                .thenReturn(attendu);

        MultipartFile file = new MockMultipartFile(
                "file", "bulletin.pdf", "application/pdf", "x".getBytes());

        ResponseEntity<BulletinUploadResponse> response = controller.upload(
                ELEVE_ID, file, "2024-2025", Periode.MILIEU,
                TypePeriode.TRIMESTRE, 2);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("2024-2025", response.getBody().getAnneeScolaire());
        assertEquals(Periode.MILIEU, response.getBody().getPeriode());
        assertEquals("Trimestre 2", response.getBody().getSemestreOuTrimestre());
    }

    @Test
    @DisplayName("Chantier C / POST /bulletins/batch : 3 fichiers → 200 + 3 réponses")
    void uploadBatch3Fichiers() {
        BulletinUploadResponse r = BulletinUploadResponse.builder()
                .anneeScolaire("X").build();
        when(orchestrator.orchestrerBatch(eq(ELEVE_ID), any())).thenReturn(List.of(r, r, r));

        MultipartFile[] files = new MultipartFile[]{
                new MockMultipartFile("files", "b1.pdf", "application/pdf", "1".getBytes()),
                new MockMultipartFile("files", "b2.pdf", "application/pdf", "2".getBytes()),
                new MockMultipartFile("files", "b3.pdf", "application/pdf", "3".getBytes())
        };
        String[] annees = {"2023-2024", "2024-2025", "2025-2026"};
        Periode[] periodes = {Periode.FIN, Periode.MILIEU, Periode.DEBUT};
        TypePeriode[] types = {TypePeriode.TRIMESTRE, TypePeriode.TRIMESTRE, TypePeriode.TRIMESTRE};
        Integer[] numeros = {3, 2, 1};

        ResponseEntity<List<BulletinUploadResponse>> response = controller.uploadBatch(
                ELEVE_ID, files, annees, periodes, types, numeros);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        verify(orchestrator, times(1)).orchestrerBatch(eq(ELEVE_ID), any());
    }

    @Test
    @DisplayName("Chantier C / POST /bulletins/batch : 4 fichiers → 400 (trop)")
    void uploadBatch4FichiersTrop() {
        MultipartFile[] files = new MultipartFile[4];
        for (int i = 0; i < 4; i++) {
            files[i] = new MockMultipartFile("files", "b" + i + ".pdf",
                    "application/pdf", "x".getBytes());
        }
        String[] annees = new String[4];
        Periode[] periodes = new Periode[4];
        TypePeriode[] types = new TypePeriode[4];
        Integer[] numeros = new Integer[4];
        for (int i = 0; i < 4; i++) {
            annees[i] = "2020-202" + i;
            periodes[i] = Periode.FIN;
            types[i] = TypePeriode.TRIMESTRE;
            numeros[i] = 3;
        }

        assertThrows(IllegalArgumentException.class,
                () -> controller.uploadBatch(ELEVE_ID, files, annees, periodes, types, numeros));
    }

    @Test
    @DisplayName("Chantier C / POST /bulletins/batch : tableaux de tailles différentes → 400")
    void uploadBatchTaillesDifferentes() {
        MultipartFile[] files = new MultipartFile[]{
                new MockMultipartFile("files", "b1.pdf", "application/pdf", "x".getBytes()),
                new MockMultipartFile("files", "b2.pdf", "application/pdf", "x".getBytes())
        };
        // anneeScolaire de taille 1 alors qu'on a 2 fichiers
        String[] annees = {"2024-2025"};
        Periode[] periodes = {Periode.FIN, Periode.MILIEU};
        TypePeriode[] types = {TypePeriode.TRIMESTRE, TypePeriode.TRIMESTRE};
        Integer[] numeros = {3, 2};

        assertThrows(IllegalArgumentException.class,
                () -> controller.uploadBatch(ELEVE_ID, files, annees, periodes, types, numeros));
    }
}
