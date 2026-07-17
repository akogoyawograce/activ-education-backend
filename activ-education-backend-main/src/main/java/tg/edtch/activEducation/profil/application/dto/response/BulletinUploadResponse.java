package tg.edtch.activEducation.profil.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tg.edtch.activEducation.prediction.application.dto.Recommandation3SignauxResponse;
import tg.edtch.activEducation.profil.application.dto.response.NoteSaisiManuelResponse;
import tg.edtch.activEducation.profil.domain.enums.Periode;
import tg.edtch.activEducation.profil.domain.service.OcrService;

import java.util.List;
import java.util.UUID;

/**
 * Réponse consolidée d'un upload de bulletin (Chantier C).
 *
 * <p>Retourne en un seul objet tout ce que le front doit afficher :
 * <ol>
 *   <li>Le document uploadé (id + URL MinIO) pour l'historique.</li>
 *   <li>Les notes extraites par l'OCR (matière + note + coefficient).</li>
 *   <li>Les notes effectivement sauvegardées (avec leur trackingId).</li>
 *   <li>La recommandation 3 signaux : top N filières classées par score
 *       final (aspiration + réalité + engagement pondérés).</li>
 * </ol>
 *
 * <p>Permet au front d'afficher un écran "Voici ce qu'on a compris de
 * ton bulletin + voici les filières qu'on te recommande" sans appels
 * REST supplémentaires.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulletinUploadResponse {

    /** Identifiant du document uploadé (UUID public). */
    private UUID trackingId;

    /** Notes extraites par l'OCR (toutes matières détectées). */
    private List<OcrService.NoteExtraite> notesExtraites;

    /** Notes effectivement persistées (1 entrée par matière). */
    private List<NoteSaisiManuelResponse> notesCrees;

    /** Recommandation 3 signaux (peut être null si le moteur a 0 candidats). */
    private Recommandation3SignauxResponse recommandation;

    /** Echo : période indiquée par l'utilisateur. */
    private Periode periode;

    /** Echo : année scolaire. */
    private String anneeScolaire;

    /** Echo : "Trimestre 2" / "Semestre 1" — pour traçabilité. */
    private String semestreOuTrimestre;

    /** Résumé en langage naturel pour l'UI. */
    private String message;
}
