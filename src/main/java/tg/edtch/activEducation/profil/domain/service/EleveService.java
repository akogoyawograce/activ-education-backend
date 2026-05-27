package tg.edtch.activEducation.profil.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.profil.application.dto.request.EleveRequest;
import tg.edtch.activEducation.profil.application.dto.response.EleveResponse;

import java.util.UUID;

/**
 * Contrat de service pour la gestion des Élèves.
 * Tous les identifiants exposés sont des {@code UUID trackingId} — jamais des
 * Long id.
 */
public interface EleveService {

    /**
     * Inscrit un nouvel élève. Crée le compte avec le rôle ROLE_ELEVE.
     * Lève {@link IllegalArgumentException} si l'email est déjà pris.
     */
    EleveResponse inscrireEleve(EleveRequest request);

    /**
     * Récupère un élève par son identifiant public UUID.
     * Lève {@link java.util.NoSuchElementException} si introuvable.
     */
    EleveResponse getEleve(UUID trackingId);

    /**
     * Récupère un élève par son email.
     * Lève {@link java.util.NoSuchElementException} si introuvable.
     */
    EleveResponse getEleveByEmail(String email);

    /**
     * Retourne une page d'élèves actifs.
     *
     * @param pageable paramètre de pagination et tri (Spring Data)
     */
    Page<EleveResponse> listerTous(Pageable pageable);

    /**
     * Met à jour les informations modifiables d'un élève.
     * L'email est non modifiable (contrainte de sécurité).
     */
    EleveResponse modifierEleve(UUID trackingId, EleveRequest request);

    /**
     * Désactive logiquement le compte d'un élève (soft-delete : estActif = false).
     */
    void desactiverEleve(UUID trackingId);
}
