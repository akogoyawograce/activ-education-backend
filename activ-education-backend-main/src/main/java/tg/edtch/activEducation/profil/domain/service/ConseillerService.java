package tg.edtch.activEducation.profil.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.profil.application.dto.request.ConseillerRequest;
import tg.edtch.activEducation.profil.application.dto.response.ConseillerResponse;

import java.util.List;
import java.util.UUID;

/**
 * Contrat de service pour la gestion des Conseillers en orientation.
 * Tous les identifiants exposés sont des {@code UUID trackingId} — jamais des
 * Long id.
 */
public interface ConseillerService {

    /**
     * Crée un nouveau compte conseiller avec le rôle ROLE_CONSEILLER.
     * Lève {@link IllegalArgumentException} si l'email est déjà pris.
     */
    ConseillerResponse creerConseiller(ConseillerRequest request);

    /**
     * Récupère un conseiller par son identifiant public UUID.
     * Lève {@link java.util.NoSuchElementException} si introuvable.
     */
    ConseillerResponse getConseiller(UUID trackingId);

    /**
     * Retourne une page paginée de conseillers actifs.
     *
     * @param pageable paramètre de pagination et tri (Spring Data)
     */
    Page<ConseillerResponse> listerTous(Pageable pageable);

    /**
     * Retourne la liste des conseillers dont la charge de travail
     * est inférieure au seuil spécifié.
     *
     * @param seuil nombre maximum de dossiers actifs
     */
    List<ConseillerResponse> listerConseillersDispo(int seuil);

    /**
     * Met à jour les informations modifiables d'un conseiller.
     * L'email est non modifiable (contrainte de sécurité).
     */
    ConseillerResponse modifierConseiller(UUID trackingId, ConseillerRequest request);

    /**
     * Désactive logiquement le compte conseiller (soft-delete : estActif = false).
     */
    void desactiverConseiller(UUID trackingId);
}
