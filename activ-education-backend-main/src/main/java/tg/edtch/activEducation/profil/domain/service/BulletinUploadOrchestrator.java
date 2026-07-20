package tg.edtch.activEducation.profil.domain.service;

import tg.edtch.activEducation.profil.application.dto.request.BulletinUploadRequest;
import tg.edtch.activEducation.profil.application.dto.request.ValidationNoteRequest;
import tg.edtch.activEducation.profil.application.dto.response.BulletinUploadResponse;
import tg.edtch.activEducation.profil.application.dto.response.NoteSaisiManuelResponse;
import tg.edtch.activEducation.profil.application.dto.response.PreviewBulletinResponse;
import tg.edtch.activEducation.profil.domain.enums.Periode;

import java.util.List;
import java.util.UUID;

/**
 * Orchestrateur du flux d'upload d'un bulletin scolaire (Chantier C).
 *
 * <p>Chaîne d'appels :</p>
 * <ol>
 *   <li>OCR du fichier (PDFBox ou OpenAI vision) → notes par matière.</li>
 *   <li>Upload du fichier dans MinIO + persistance {@code Document}.</li>
 *   <li>Pour chaque note extraite : persistance {@code NoteSaisiManuel}
 *       avec {@code anneeScolaire} + {@code semestreOuTrimestre} dérivés.</li>
 *   <li>Déclenchement du moteur 3 signaux pour rafraîchir les
 *       recommandations de l'élève.</li>
 *   <li>Retour consolidé : document + notes + recommandation.</li>
 * </ol>
 */
public interface BulletinUploadOrchestrator {

    /**
     * Orchestre l'upload d'un bulletin unique.
     *
     * @param eleveTrackingId identifiant public UUID de l'élève
     * @param request         données d'upload (fichier + métadonnées)
     * @return résultat consolidé (document + notes + recommandation)
     * @throws java.util.NoSuchElementException si l'élève est introuvable
     */
    BulletinUploadResponse orchestrer(UUID eleveTrackingId, BulletinUploadRequest request);

    /**
     * Phase preview : OCR + upload doc uniquement, sans sauvegarder les notes
     * ni déclencher la recommandation. L'élève valide d'abord les notes.
     */
    PreviewBulletinResponse orchestrerPreview(UUID eleveTrackingId, BulletinUploadRequest request);

    /**
     * Phase confirm : sauvegarde les notes validées et déclenche la recommandation.
     */
    BulletinUploadResponse confirmerNotes(UUID eleveTrackingId, UUID documentTrackingId,
                                          String anneeScolaire, Periode periode,
                                          String semestreOuTrimestre,
                                          List<ValidationNoteRequest> notesValidees);

    /**
     * Orchestre l'upload de 1 à 3 bulletins en lot.
     *
     * <p>Les uploads sont traités séquentiellement (pas en parallèle :
     * la transaction sur la base ne supporterait pas un accès concurrent
     * pour un même élève).</p>
     *
     * @param eleveTrackingId identifiant public UUID de l'élève
     * @param requests        liste de bulletins (1 ≤ taille ≤ 3)
     * @return liste de résultats consolidés
     * @throws IllegalArgumentException si la liste est vide ou > 3
     */
    List<BulletinUploadResponse> orchestrerBatch(
            UUID eleveTrackingId, List<BulletinUploadRequest> requests);
}
