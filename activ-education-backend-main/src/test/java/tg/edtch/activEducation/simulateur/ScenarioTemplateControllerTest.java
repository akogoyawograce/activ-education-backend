package tg.edtch.activEducation.simulateur;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tg.edtch.activEducation.simulateur.application.controller.ScenarioTemplateController;
import tg.edtch.activEducation.simulateur.application.dto.ScenarioTemplate;
import tg.edtch.activEducation.simulateur.application.dto.ScenarioTemplate.CategorieTemplate;
import tg.edtch.activEducation.simulateur.application.service.ScenarioTemplateRegistry;
import tg.edtch.activEducation.simulateur.domain.dto.ScenarioRequest;
import tg.edtch.activEducation.simulateur.domain.dto.ScenarioResult;
import tg.edtch.activEducation.simulateur.domain.service.SimulateurParcoursService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du {@link ScenarioTemplateController} — Chantier B.
 *
 * <p>On vérifie 3 choses :</p>
 * <ol>
 *   <li>{@code GET /scenarios-types} retourne 6 templates (un par catégorie).</li>
 *   <li>{@code GET /{id}} retourne 200 si l'id est connu, 404 sinon.</li>
 *   <li>{@code POST /{id}/executer} appelle bien le service de simulation
 *       et retourne son résultat (200), ou 404 si l'id est mauvais.</li>
 * </ol>
 *
 * <p>Le controller étant fin (juste de l'orchestration), on peut le
 * tester en mockant les 2 dépendances : le registre et le service.</p>
 */
@ExtendWith(MockitoExtension.class)
class ScenarioTemplateControllerTest {

    @Mock private SimulateurParcoursService simulateurService;

    private ScenarioTemplateRegistry registry; // bean réel (pas besoin de mocker)
    private ScenarioTemplateController controller;

    private static final UUID ID_PROGRESSION = UUID.fromString("11111111-1111-1111-1111-000000000001");
    private static final UUID ID_INEXISTANT = UUID.fromString("99999999-9999-9999-9999-000000000000");

    @BeforeEach
    void setUp() {
        registry = new ScenarioTemplateRegistry(); // bean réel avec les 6 hardcodés
        controller = new ScenarioTemplateController(registry, simulateurService);
    }

    @Test
    @DisplayName("Chantier B / GET /scenarios-types : 6 templates triés par catégorie")
    void lister6Templates() {
        ResponseEntity<List<ScenarioTemplate>> response = controller.lister();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(6, response.getBody().size(),
                "le registre doit contenir 6 templates hardcodés");

        // Vérification de l'ordering : par catégorie (ordinal) puis par titre.
        // Le 1er de l'enum CategorieTemplate est PROGRESSION_NOTES.
        assertEquals(CategorieTemplate.PROGRESSION_NOTES, response.getBody().get(0).categorie());
    }

    @Test
    @DisplayName("Chantier B / GET /{id} : id connu → 200, id inconnu → 404")
    void getParId() {
        // 1) id connu
        ResponseEntity<ScenarioTemplate> ok = controller.get(ID_PROGRESSION);
        assertEquals(HttpStatus.OK, ok.getStatusCode());
        assertNotNull(ok.getBody());
        assertEquals(ID_PROGRESSION, ok.getBody().trackingId());

        // 2) id inconnu → 404
        ResponseEntity<ScenarioTemplate> notFound = controller.get(ID_INEXISTANT);
        assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());
    }

    @Test
    @DisplayName("Chantier B / POST /{id}/executer : id connu → 200 + ScenarioResult")
    void executer() {
        // Mock du service : on renvoie un ScenarioResult "vide mais valide"
        ScenarioResult attendu = new ScenarioResult();
        attendu.setTitre("Maths +2 points");
        when(simulateurService.explorer(any(ScenarioRequest.class))).thenReturn(attendu);

        ResponseEntity<ScenarioResult> response = controller.executer(ID_PROGRESSION, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Maths +2 points", response.getBody().getTitre());
    }

    @Test
    @DisplayName("Chantier B / POST /{id}/executer : id inconnu → 404 (early return)")
    void executerIdInconnu() {
        ResponseEntity<ScenarioResult> response = controller.executer(ID_INEXISTANT, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Chantier B / Supplier : 2 appels donnent 2 ScenarioRequest distincts (no shared state)")
    void supplierRetourneUneInstanceFraiche() {
        // Sanity check : le Supplier du template doit recréer l'objet
        // à chaque appel pour éviter les mutations croisées.
        ScenarioTemplate template = registry.get(ID_PROGRESSION);
        Supplier<ScenarioRequest> supplier = template.requestSupplier();

        ScenarioRequest r1 = supplier.get();
        ScenarioRequest r2 = supplier.get();
        assertNotSame(r1, r2, "chaque appel doit créer un nouveau ScenarioRequest");
    }
}
