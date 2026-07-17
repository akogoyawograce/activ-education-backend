package tg.edtch.activEducation.profil.domain.service;

import tg.edtch.activEducation.profil.application.dto.request.NotesHistoriqueRequest;
import tg.edtch.activEducation.profil.application.dto.response.NotesHistoriqueResponse;

import java.util.List;
import java.util.UUID;

/**
 * Contrat pour la gestion de l'historique de notes (3 ans glissants).
 * Source de données pour la trajectoire académique du moteur de recommandation.
 */
public interface NotesHistoriqueService {

    NotesHistoriqueResponse ajouter(UUID eleveTrackingId, NotesHistoriqueRequest request);

    NotesHistoriqueResponse get(UUID trackingId);

    /** Toutes les lignes d'un élève, triées par année décroissante. */
    List<NotesHistoriqueResponse> listerParEleve(UUID eleveTrackingId);

    /** Uniquement les "moyenne générale", ordonnées DESC pour trajectoire. */
    List<NotesHistoriqueResponse> listerMoyennesGenerales(UUID eleveTrackingId);

    void supprimer(UUID trackingId);
}
