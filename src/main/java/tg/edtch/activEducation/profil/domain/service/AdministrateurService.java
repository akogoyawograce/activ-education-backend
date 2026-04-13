package tg.edtch.activEducation.profil.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.profil.application.dto.request.AdministrateurRequest;
import tg.edtch.activEducation.profil.application.dto.response.AdministrateurResponse;

import java.util.UUID;

/**
 * Contrat de service pour la gestion des Administrateurs.
 * Tous les identifiants exposés sont des {@code UUID trackingId} — jamais des
 * Long id.
 */
public interface AdministrateurService {

    /**
     * Crée un compte administrateur avec le rôle ROLE_ADMIN.
     * Lève {@link IllegalArgumentException} si l'email est déjà pris.
     */
    AdministrateurResponse creerAdministrateur(AdministrateurRequest request);

    /**
     * Récupère un administrateur par son identifiant public UUID.
     * Lève {@link java.util.NoSuchElementException} si introuvable.
     */
    AdministrateurResponse getAdministrateur(UUID trackingId);

    /**
     * Retourne une page paginée d'administrateurs actifs.
     */
    Page<AdministrateurResponse> listerTous(Pageable pageable);

    /**
     * Met à jour les informations d'un administrateur.
     * L'email est non modifiable.
     */
    AdministrateurResponse modifierAdministrateur(UUID trackingId, AdministrateurRequest request);

    /**
     * Désactive logiquement le compte (soft-delete : estActif = false).
     */
    void desactiverAdministrateur(UUID trackingId);
}
