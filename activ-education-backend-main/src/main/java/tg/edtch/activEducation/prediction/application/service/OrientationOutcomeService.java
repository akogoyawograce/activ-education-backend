package tg.edtch.activEducation.prediction.application.service;

import tg.edtch.activEducation.prediction.application.dto.OrientationOutcomeRequest;
import tg.edtch.activEducation.prediction.application.dto.OrientationOutcomeResponse;

import java.util.List;
import java.util.UUID;

/**
 * Gestion des choix d'orientation des élèves (table
 * {@code orientation_outcome}) — source de vérité pour l'entraînement
 * supervisé (Phase 5).
 */
public interface OrientationOutcomeService {

    /**
     * Crée un nouveau suivi d'orientation pour un élève. Si un suivi existe
     * déjà pour la même filière, on ne duplique pas : on met à jour.
     */
    OrientationOutcomeResponse creerOuMettreAJour(UUID eleveTrackingId,
                                                  OrientationOutcomeRequest request);

    /** Liste des choix d'orientation (passés + en cours) d'un élève. */
    List<OrientationOutcomeResponse> listerParEleve(UUID eleveTrackingId);

    /**
     * Met à jour le statut d'un outcome (ADMIS / RECALE / ABANDON / REORIENTE)
     * et éventuellement la satisfaction. Endpoint admin/conseiller.
     */
    OrientationOutcomeResponse mettreAJourStatut(UUID outcomeTrackingId,
                                                 String statut,
                                                 Integer satisfaction,
                                                 String commentaire);
}
