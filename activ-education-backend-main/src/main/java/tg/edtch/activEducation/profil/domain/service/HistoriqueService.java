package tg.edtch.activEducation.profil.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.profil.application.dto.request.HistoriqueRequest;
import tg.edtch.activEducation.profil.application.dto.response.HistoriqueResponse;

import java.util.List;
import java.util.UUID;

/**
 * Contrat de service pour la gestion de l'historique d'activité.
 * L'historique est append-only — pas de modification, soft-delete impossible.
 * Tous les identifiants exposés sont des {@code UUID trackingId}.
 */
public interface HistoriqueService {

    /**
     * Enregistre une nouvelle entrée dans l'historique d'un utilisateur.
     *
     * @param utilisateurTrackingId trackingId public de l'utilisateur
     */
    HistoriqueResponse enregistrer(UUID utilisateurTrackingId, HistoriqueRequest request);

    /**
     * Récupère une entrée d'historique par son propre trackingId.
     */
    HistoriqueResponse getEntree(UUID trackingId);

    /**
     * Retourne tout l'historique d'un utilisateur, trié par date décroissante.
     */
    List<HistoriqueResponse> getHistoriqueUtilisateur(UUID utilisateurTrackingId);

    /**
     * Retourne l'historique paginé d'un utilisateur.
     */
    Page<HistoriqueResponse> getHistoriqueUtilisateurPagine(UUID utilisateurTrackingId, Pageable pageable);

    /**
     * Filtre l'historique d'un utilisateur par type d'action.
     *
     * @param action ex. "CONNEXION", "TEST_RIASEC"
     */
    List<HistoriqueResponse> getHistoriqueParAction(UUID utilisateurTrackingId, String action);

    /**
     * Supprime définitivement toutes les entrées d'historique d'un utilisateur.
     * Opération irréversible réservée aux administrateurs.
     */
    void effacerHistoriqueUtilisateur(UUID utilisateurTrackingId);
}
