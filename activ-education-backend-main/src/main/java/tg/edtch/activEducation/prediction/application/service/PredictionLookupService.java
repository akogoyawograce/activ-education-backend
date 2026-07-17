package tg.edtch.activEducation.prediction.application.service;

import tg.edtch.activEducation.prediction.application.dto.FilierePourNiveauResponse;
import tg.edtch.activEducation.prediction.application.dto.NiveauResponse;

import java.util.List;

/**
 * Services de lecture seule (lookup) du module Prédiction — Phase 2.
 *
 * <p>Volontairement séparé du {@code PredictionService} historique (qui
 * gère la table {@code prediction_reussite}) : ce nouveau service ne
 * mute rien, il sert juste à alimenter l'écran mobile "sélection du
 * niveau / de la filière" et l'export dataset.</p>
 */
public interface PredictionLookupService {

    /** Liste des niveaux disponibles (7 valeurs canoniques). */
    List<NiveauResponse> listerNiveaux();

    /**
     * Filières éligibles pour un niveau donné (lookup via {@code niveaux_filieres}).
     * Renvoie une liste vide si le niveau est inconnu ou sans filière éligible.
     */
    List<FilierePourNiveauResponse> filieresPourNiveau(String niveau);
}
