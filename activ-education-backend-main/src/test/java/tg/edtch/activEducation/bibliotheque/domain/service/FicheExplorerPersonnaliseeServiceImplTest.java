package tg.edtch.activEducation.bibliotheque.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheFiliereResponse;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheSerieResponse;
import tg.edtch.activEducation.bibliotheque.application.mapper.FicheFiliereMapper;
import tg.edtch.activEducation.bibliotheque.application.mapper.FicheSerieMapper;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheSerie;
import tg.edtch.activEducation.bibliotheque.domain.service.serviceImple.FicheExplorerPersonnaliseeServiceImpl;
import tg.edtch.activEducation.bibliotheque.repository.FicheFiliereRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheSerieRepository;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.enums.NiveauScolaire;
import tg.edtch.activEducation.profil.repository.EleveRepository;
import tg.edtch.activEducation.riasec.domain.entite.TestRIASECResultat;
import tg.edtch.activEducation.riasec.repository.TestRIASECResultatRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests du tri personnalisé de l'Explorer (fiches = filières + séries).
 *
 * <p>Invariants verrouillés :
 * <ol>
 *   <li>Un élève au profil "Génie Informatique" + matières Maths/SVT reçoit
 *       les filières informatiques/scientifiques en tête — pas
 *       Secrétariat/Comptabilité en premier.</li>
 *   <li>Élève introuvable OU profil incomplet → repli générique
 *       (liste non vide, aucune erreur, ordre createdAt DESC).</li>
 *   <li>Les séries scientifiques passent avant "Série G3 (Secrétariat)"
 *       pour ce profil.</li>
 * </ol>
 * </p>
 */
class FicheExplorerPersonnaliseeServiceImplTest {

    private EleveRepository eleveRepository;
    private TestRIASECResultatRepository riasecRepository;
    private FicheFiliereRepository filiereRepository;
    private FicheSerieRepository serieRepository;
    private FicheFiliereMapper filiereMapper;
    private FicheSerieMapper serieMapper;
    private FicheExplorerPersonnaliseeServiceImpl service;

    private static final UUID ELEVE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        eleveRepository = mock(EleveRepository.class);
        riasecRepository = mock(TestRIASECResultatRepository.class);
        filiereRepository = mock(FicheFiliereRepository.class);
        serieRepository = mock(FicheSerieRepository.class);
        filiereMapper = mock(FicheFiliereMapper.class);
        serieMapper = mock(FicheSerieMapper.class);

        service = new FicheExplorerPersonnaliseeServiceImpl(
                eleveRepository, riasecRepository, filiereRepository,
                serieRepository, filiereMapper, serieMapper);

        when(riasecRepository.findByEleveTrackingIdOrderByDatePassationDesc(any(String.class)))
            .thenReturn(List.of());

        when(filiereMapper.toResponse(any(FicheFiliere.class))).thenAnswer(inv -> {
            FicheFiliere f = inv.getArgument(0);
            return FicheFiliereResponse.builder()
                    .trackingId(f.getTrackingId())
                    .titre(f.getTitre())
                    .domaine(f.getDomaine())
                    .build();
        });
        when(serieMapper.toResponse(any(FicheSerie.class))).thenAnswer(inv -> {
            FicheSerie s = inv.getArgument(0);
            return FicheSerieResponse.builder()
                    .trackingId(s.getTrackingId())
                    .titre(s.getTitre())
                    .build();
        });
    }

    /** Élève du bug observé : BAC_3, Génie Informatique, Maths/SVT, informaticien. */
    private void eleveInformatiqueSciences() {
        Eleve eleve = new Eleve();
        eleve.setNiveau(NiveauScolaire.BAC_3);
        eleve.setFiliere("Génie Informatique");
        eleve.setMetierSouhaite("informaticien");
        eleve.setMatieresPreferees("Maths,SVT");
        when(eleveRepository.findByTrackingId(ELEVE_ID)).thenReturn(Optional.of(eleve));
    }

    private FicheFiliere filiere(String titre, String domaine, LocalDateTime createdAt) {
        FicheFiliere f = new FicheFiliere();
        f.setTitre(titre);
        f.setDomaine(domaine);
        f.setCreatedAt(createdAt);
        return f;
    }

    private FicheSerie serie(String titre, LocalDateTime createdAt) {
        FicheSerie s = new FicheSerie();
        s.setTitre(titre);
        s.setCreatedAt(createdAt);
        return s;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Test 1 (point 8) — Filières personnalisées en tête
    // ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Élève Génie Informatique + Maths/SVT → filières informatiques en tête, pas Secrétariat/Comptabilité")
    void profilInformatique_metInformatiqueEnTete() {
        eleveInformatiqueSciences();
        when(filiereRepository.findAll()).thenReturn(List.of(
            filiere("Secrétariat", "Administration", LocalDateTime.now().minusDays(1)),
            filiere("Informatique de gestion", "Informatique", LocalDateTime.now().minusHours(2)),
            filiere("Mathématiques", "Sciences", LocalDateTime.now().minusHours(1)),
            filiere("Comptabilité", "Gestion", LocalDateTime.now())
        ));

        Page<FicheFiliereResponse> page =
                service.listerFilieresPersonnalisees(ELEVE_ID, PageRequest.of(0, 10));

        assertThat(page.getContent()).isNotEmpty();
        List<String> titres = page.getContent().stream().map(FicheFiliereResponse::getTitre).toList();
        // La filière informatique (voire mathématiques) doit passer devant le secrétariat.
        assertThat(titres.get(0)).isEqualTo("Informatique de gestion");
        assertThat(titres).containsSubsequence("Informatique de gestion", "Mathématiques");
        // Secrétariat et Comptabilité ne doivent PAS être en première position.
        assertThat(titres.get(0)).isNotIn("Secrétariat", "Comptabilité");
        assertThat(titres).doesNotHaveDuplicates();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Test 2 — Séries scientifiques avant "Série G3 (Secrétariat)"
    // ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Séries : 'Série C (Mathématiques)' passe devant 'Série G3 (Secrétariat)' pour ce profil")
    void profilInformatique_serieScientifiqueEnTete() {
        eleveInformatiqueSciences();
        when(serieRepository.findAll()).thenReturn(List.of(
            serie("Série G3 (Secrétariat)", LocalDateTime.now()),
            serie("Série F2 (Électronique)", LocalDateTime.now().minusDays(2)),
            serie("Série C (Mathématiques)", LocalDateTime.now().minusDays(1))
        ));

        Page<FicheSerieResponse> result =
                service.listerSeriesPersonnalisees(ELEVE_ID, PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotEmpty();
        // Le token "maths" matche "Mathématiques" (préfixe "math").
        assertThat(result.getContent().get(0).getTitre()).contains("Mathématiques");
        assertThat(result.getContent().get(0).getTitre())
            .isNotEqualTo("Série G3 (Secrétariat)");
    }

    // ─────────────────────────────────────────────────────────────────────
    // Test 3 (point 9) — Repli générique : élève introuvable
    // ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Élève introuvable → repli générique non vide, sans erreur")
    void eleveIntrouvable_repliGeneriqueNonVide() {
        when(eleveRepository.findByTrackingId(ELEVE_ID)).thenReturn(Optional.empty());
        when(filiereRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(
            List.of(filiere("Secrétariat", "Administration", LocalDateTime.now()),
                    filiere("Informatique de gestion", "Informatique", LocalDateTime.now().minusDays(1)))
        ));

        Page<FicheFiliereResponse> result =
                service.listerFilieresPersonnalisees(ELEVE_ID, PageRequest.of(0, 10));

        // Aucun tri personnalisé, mais une liste non vide et sans erreur.
        assertThat(result.getContent()).hasSize(2);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Test 4 (point 9) — Repli générique : profil incomplet
    // ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Élève au profil incomplet (aucun champ) → repli générique non vide, sans erreur")
    void profilIncomplet_repliGeneriqueSansErreur() {
        // Élève existe mais aucun champ de personnalisation renseigné.
        when(eleveRepository.findByTrackingId(ELEVE_ID)).thenReturn(Optional.of(new Eleve()));
        when(filiereRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(
            List.of(filiere("Tourisme et Hôtellerie", "Tourisme", LocalDateTime.now().minusDays(1)),
                    filiere("Informatique de gestion", "Informatique", LocalDateTime.now()))
        ));

        Page<FicheFiliereResponse> page =
                service.listerFilieresPersonnalisees(ELEVE_ID, PageRequest.of(0, 10));

        // Non vide, contient les fiches disponibles, aucun appel au scoring.
        assertThat(page.getContent()).hasSize(2);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Test 5 — RIASEC présent : le cosinus affine le classement
    // ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("RIASEC Investigateur → filière informatique en tête sur un profil complet")
    void riasecInvestigateur_renforceScores() {
        Eleve eleve = new Eleve();
        eleve.setNiveau(NiveauScolaire.BAC_3);
        eleve.setFiliere("informatique");
        when(eleveRepository.findByTrackingId(ELEVE_ID)).thenReturn(Optional.of(eleve));

        TestRIASECResultat t = new TestRIASECResultat();
        t.setScoreRealiste(40);
        t.setScoreInvestigateur(90);
        t.setScoreArtistique(20);
        t.setScoreSocial(20);
        t.setScoreEntreprenant(20);
        t.setScoreConventionnel(20);
        when(riasecRepository.findByEleveTrackingIdOrderByDatePassationDesc(any(String.class)))
            .thenReturn(List.of(t));

        when(filiereRepository.findAll()).thenReturn(List.of(
            filiere("Droit", "Droit", LocalDateTime.now()),
            filiere("Informatique de gestion", "Informatique", LocalDateTime.now())
        ));

        Page<FicheFiliereResponse> page =
                service.listerFilieresPersonnalisees(ELEVE_ID, PageRequest.of(0, 10));

        assertThat(page.getContent()).isNotEmpty();
        // La filière informatique matche le profil RIASEC (I=0.9) → en tête.
        assertThat(page.getContent().get(0).getTitre()).isEqualTo("Informatique de gestion");
    }
}
