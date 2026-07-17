package tg.edtch.activEducation.prediction.application.service;

import tg.edtch.activEducation.prediction.application.dto.Recommandation3SignauxResponse;

import java.util.UUID;

/**
 * Moteur de recommandation combinant 3 signaux pondérés :
 * <ol>
 *   <li><b>Aspiration</b> : similarité RIASEC élève ↔ profil typique filière.</li>
 *   <li><b>Réalité</b> : notes + trajectoire 3 ans vs seuil d'admission.</li>
 *   <li><b>Engagement</b> : consultations + favoris + RAG cosinus, plafonné.</li>
 * </ol>
 *
 * <p>Le moteur garantit que {@code score_engagement} ne dépasse jamais
 * {@code poids_engagement_max} (cf. {@code PredictionProperties}).</p>
 */
public interface Recommandation3SignauxService {

    /**
     * Calcule le top N de recommandations pour un élève.
     *
     * @param eleveTrackingId identifiant public UUID de l'élève
     * @return top N filières classées par score_final DESC
     */
    Recommandation3SignauxResponse recommander(UUID eleveTrackingId);
}
