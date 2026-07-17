package tg.edtch.activEducation.prediction.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Réponse du moteur 3 signaux pour un élève donné.
 *
 * <p>Renvoie :
 * <ul>
 *   <li>le top N de filières classées par score_final DESC,</li>
 *   <li>les poids effectivement utilisés (après plafonnement),</li>
 *   <li>des métadonnées de debug (profil élève agrégé résumé).</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recommandation3SignauxResponse {

    private UUID eleveTrackingId;

    /** Top N filières classées (peut être < N si pas assez de candidats). */
    private List<FiliereScoreeResponse> top;

    /** Pondération effective (après plafonnement de l'engagement). */
    private BigDecimal poidsAspiration;

    private BigDecimal poidsRealite;

    private BigDecimal poidsEngagement;

    /** Nombre de filières "découverte" ajoutées. */
    private Integer decouvertesAjoutees;

    /** Métadonnées sur l'élève ayant servi au calcul. */
    private ProfilEleve profil;
}
