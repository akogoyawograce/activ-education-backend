package tg.edtch.activEducation.profil.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.prediction.application.dto.Recommandation3SignauxResponse;
import tg.edtch.activEducation.prediction.application.service.Recommandation3SignauxService;
import tg.edtch.activEducation.profil.application.dto.request.BulletinUploadRequest;
import tg.edtch.activEducation.profil.application.dto.request.NoteSaisiManuelRequest;
import tg.edtch.activEducation.profil.application.dto.response.BulletinUploadResponse;
import tg.edtch.activEducation.profil.application.dto.response.DocumentResponse;
import tg.edtch.activEducation.profil.application.dto.response.NoteSaisiManuelResponse;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.enums.Periode;
import tg.edtch.activEducation.profil.domain.enums.TypePeriode;
import tg.edtch.activEducation.profil.domain.service.BulletinUploadOrchestrator;
import tg.edtch.activEducation.profil.domain.service.DocumentService;
import tg.edtch.activEducation.profil.domain.service.NoteSaisiManuelService;
import tg.edtch.activEducation.profil.domain.service.OcrService;
import tg.edtch.activEducation.profil.repository.EleveRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Implémentation de {@link BulletinUploadOrchestrator}.
 *
 * <p>Cette classe est volontairement fine : elle compose des services
 * existants ({@code OcrService}, {@code DocumentService},
 * {@code NoteSaisiManuelService}, {@code Recommandation3SignauxService})
 * sans logique métier propre. Toute la complexité est déjà dans ces
 * services.</p>
 *
 * <p>Limites connues (cf. plan — hors scope) :</p>
 * <ul>
 *   <li>Pas de validation manuelle post-OCR (on fait confiance à l'OCR).</li>
 *   <li>Pas d'OCR multi-page (les bulletins togolais font souvent 2 pages).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BulletinUploadOrchestratorImpl implements BulletinUploadOrchestrator {

    /** Taille max d'un batch (cf. plan — 1 à 3 bulletins). */
    private static final int BATCH_MAX = 3;

    private final EleveRepository eleveRepository;
    private final OcrService ocrService;
    private final DocumentService documentService;
    private final NoteSaisiManuelService noteSaisiManuelService;
    private final Recommandation3SignauxService recommandation3SignauxService;

    @Override
    @Transactional
    public BulletinUploadResponse orchestrer(UUID eleveTrackingId, BulletinUploadRequest request) {
        log.info("Orchestration bulletin : eleve={} annee={} periode={}/{}/T{}",
                eleveTrackingId, request.getAnneeScolaire(),
                request.getPeriode(), request.getTypePeriode(), request.getNumeroPeriode());

        // 1) Charger l'élève (404 si introuvable)
        Eleve eleve = eleveRepository.findByTrackingId(eleveTrackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Élève introuvable : " + eleveTrackingId));

        // 2) Dériver le label "Trimestre X" / "Semestre X"
        String semestreLabel = buildSemestreLabel(request);

        // 3) Sauvegarder le fichier dans MinIO + créer un Document
        //    Note : on stocke la description avec l'année + la période,
        //    ce qui permet à l'élève de retrouver le bulletin dans son
        //    historique sans avoir à rouvrir le PDF.
        String description = "Bulletin " + request.getAnneeScolaire()
                + " - " + semestreLabel
                + " (" + request.getPeriode().getLabel() + ")";
        DocumentResponse document = documentService.uploadDocument(
                eleveTrackingId,
                request.getFile(),
                "BULLETIN",
                description,
                null  // dateDocument : pas de date d'examen dans le bulletin
        );
        log.debug("Document créé : trackingId={}", document.getId());

        // 4) OCR → notes extraites
        List<OcrService.NoteExtraite> notesExtraites = ocrService.extraireNotes(request.getFile());
        log.info("OCR : {} notes extraites pour l'élève {}",
                notesExtraites.size(), eleveTrackingId);

        // 5) Pour chaque note extraite, créer une NoteSaisiManuel
        //    avec les bonnes métadonnées (année + période).
        List<NoteSaisiManuelResponse> notesCrees = new ArrayList<>(notesExtraites.size());
        for (OcrService.NoteExtraite n : notesExtraites) {
            NoteSaisiManuelRequest noteReq = new NoteSaisiManuelRequest();
            noteReq.setMatiere(n.matiere());
            noteReq.setNote(n.note());
            // L'OCR renvoie un coefficient en double (PDFBox peut sortir
            // 1.5, 2.0...). On stocke en Integer (cohérent avec
            // NoteSaisiManuel.coefficient) en arrondissant. Pour les
            // bulletins standards togolais, les coefficients sont des
            // entiers, donc l'arrondi est sans perte dans 99% des cas.
            noteReq.setCoefficient((int) Math.round(n.coefficient()));
            noteReq.setAnneeScolaire(request.getAnneeScolaire());
            noteReq.setSemestreOuTrimestre(semestreLabel);
            notesCrees.add(noteSaisiManuelService.ajouterNote(eleveTrackingId, noteReq));
        }

        // 6) Déclencher le moteur 3 signaux
        //    Note : le moteur est en @Transactional(readOnly=true), donc
        //    la transaction globale n'est pas polluée.
        Recommandation3SignauxResponse recommandation = recommandation3SignauxService
                .recommander(eleveTrackingId);
        int nbFilieres = recommandation.getTop() != null ? recommandation.getTop().size() : 0;
        log.info("Recommandation 3 signaux : {} filières pour l'élève {}",
                nbFilieres, eleveTrackingId);

        // 7) Consolider la réponse
        String message = String.format(
                "Bulletin analysé : %d note(s) extraite(s), %d filière(s) recommandée(s).",
                notesCrees.size(), nbFilieres);

        // L'attribut trackingId du BulletinUploadResponse est l'ID du
        // document uploadé (le front l'utilise pour afficher un lien
        // "voir le PDF"). On n'utilise pas l'ID interne (Long) pour
        // rester cohérent avec le reste de l'API qui expose des UUID.
        UUID documentTrackingId = UUID.nameUUIDFromBytes(
                ("bulletin-" + document.getId()).getBytes());

        return BulletinUploadResponse.builder()
                .trackingId(documentTrackingId)
                .notesExtraites(notesExtraites)
                .notesCrees(notesCrees)
                .recommandation(recommandation)
                .periode(request.getPeriode())
                .anneeScolaire(request.getAnneeScolaire())
                .semestreOuTrimestre(semestreLabel)
                .message(message)
                .build();
    }

    @Override
    public List<BulletinUploadResponse> orchestrerBatch(
            UUID eleveTrackingId, List<BulletinUploadRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Le batch ne peut pas être vide.");
        }
        if (requests.size() > BATCH_MAX) {
            throw new IllegalArgumentException(
                    "Le batch ne peut pas dépasser " + BATCH_MAX + " bulletins (reçu : "
                            + requests.size() + ").");
        }
        log.info("Batch upload : eleve={} nbBulletins={}", eleveTrackingId, requests.size());

        List<BulletinUploadResponse> resultats = new ArrayList<>(requests.size());
        for (int i = 0; i < requests.size(); i++) {
            BulletinUploadRequest req = requests.get(i);
            log.debug("Batch[{}/{}] : année={}", i + 1, requests.size(), req.getAnneeScolaire());
            resultats.add(orchestrer(eleveTrackingId, req));
        }
        return resultats;
    }

    /**
     * Dérive le label "Trimestre X" / "Semestre X" à partir de la
     * combinaison (TypePeriode, Periode, numeroPeriode).
     *
     * <p>Règles métier :</p>
     * <ul>
     *   <li>{@code TRIMESTRE} : "Trimestre {numeroPeriode}" (1, 2 ou 3).</li>
     *   <li>{@code SEMESTRE} : "Semestre {numeroPeriode}" (1 ou 2).</li>
     * </ul>
     *
     * <p>La {@link Periode} est indicative (utilisée dans la description
     * du document) mais ne change pas le label : c'est l'élève qui
     * indique explicitement le numéro via {@code numeroPeriode}.</p>
     *
     * <p>Note : pas de "fallback intelligent" (ex. forcer T1 si
     * DEBUT). On respecte ce que dit l'utilisateur — c'est lui qui
     * sait s'il a passé T1 ou T2.</p>
     */
    String buildSemestreLabel(BulletinUploadRequest req) {
        String typeLabel = req.getTypePeriode() == TypePeriode.TRIMESTRE
                ? "Trimestre" : "Semestre";
        return typeLabel + " " + req.getNumeroPeriode();
    }
}
