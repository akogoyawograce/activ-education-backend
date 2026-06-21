package tg.edtch.activEducation.profil.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.profil.application.dto.request.NoteSaisiManuelRequest;
import tg.edtch.activEducation.profil.application.dto.response.NoteSaisiManuelResponse;

import java.util.List;
import java.util.UUID;

/**
 * Contrat de service pour la gestion des notes saisies manuellement.
 * Toutes les opérations utilisent des {@code UUID trackingId} — jamais des Long
 * id.
 */
public interface NoteSaisiManuelService {

    /**
     * Ajoute une note pour un élève identifié par son trackingId.
     */
    NoteSaisiManuelResponse ajouterNote(UUID eleveTrackingId, NoteSaisiManuelRequest request);

    /**
     * Récupère une note par son propre trackingId public.
     */
    NoteSaisiManuelResponse getNote(UUID trackingId);

    /**
     * Retourne toutes les notes d'un élève triées par année scolaire décroissante.
     */
    List<NoteSaisiManuelResponse> getNotesByEleve(UUID eleveTrackingId);

    /**
     * Retourne une page paginée des notes d'un élève.
     */
    Page<NoteSaisiManuelResponse> getNotesByElevePagine(UUID eleveTrackingId, Pageable pageable);

    /**
     * Met à jour une note identifiée par son trackingId.
     * L'élève rattaché est non modifiable.
     */
    NoteSaisiManuelResponse modifierNote(UUID trackingId, NoteSaisiManuelRequest request);

    /**
     * Supprime définitivement une note (hard-delete — les notes ne sont pas
     * softdeleted).
     */
    void supprimerNote(UUID trackingId);
}
