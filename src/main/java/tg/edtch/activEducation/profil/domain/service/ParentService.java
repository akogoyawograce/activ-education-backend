package tg.edtch.activEducation.profil.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.profil.application.dto.request.ParentRequest;
import tg.edtch.activEducation.profil.application.dto.response.ParentResponse;

import java.util.List;
import java.util.UUID;

/**
 * Contrat de service pour la gestion des Parents.
 * Tous les identifiants exposés sont des {@code UUID trackingId} — jamais des
 * Long id.
 */
public interface ParentService {

    /**
     * Crée un compte parent avec le rôle ROLE_PARENT.
     * Les enfants fournis dans la requête sont résolus via leurs trackingId.
     */
    ParentResponse creerParent(ParentRequest request);

    /**
     * Récupère un parent par son identifiant public UUID.
     */
    ParentResponse getParent(UUID trackingId);

    /**
     * Retourne une page paginée de parents actifs.
     */
    Page<ParentResponse> listerTous(Pageable pageable);

    /**
     * Retourne tous les parents d'un élève identifié par son trackingId public.
     */
    List<ParentResponse> getParentsParEleve(UUID eleveTrackingId);

    /**
     * Met à jour les informations modifiables d'un parent.
     * L'email est non modifiable.
     */
    ParentResponse modifierParent(UUID trackingId, ParentRequest request);

    /**
     * Rattache un élève (via son trackingId) à un parent existant.
     */
    ParentResponse ajouterEnfant(UUID parentTrackingId, UUID eleveTrackingId);

    /**
     * Retire le lien entre un parent et un élève.
     */
    ParentResponse retirerEnfant(UUID parentTrackingId, UUID eleveTrackingId);

    /**
     * Désactive logiquement le compte parent (soft-delete).
     */
    void desactiverParent(UUID trackingId);
}
