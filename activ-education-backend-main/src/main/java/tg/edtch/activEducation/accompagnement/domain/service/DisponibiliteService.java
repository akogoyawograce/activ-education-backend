package tg.edtch.activEducation.accompagnement.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.accompagnement.application.dto.request.DisponibiliteRequest;
import tg.edtch.activEducation.accompagnement.application.dto.response.DisponibiliteResponse;

import java.util.List;
import java.util.UUID;

/**
 * Contrat de service pour la gestion des disponibilités de conseiller.
 * Tous les identifiants exposés sont des {@code UUID trackingId}.
 */
public interface DisponibiliteService {

    /**
     * Ajoute un créneau de disponibilité pour un conseiller identifié par son
     * trackingId.
     */
    DisponibiliteResponse ajouterDisponibilite(UUID conseillerTrackingId, DisponibiliteRequest request);

    /**
     * Récupère une disponibilité par son trackingId public.
     */
    DisponibiliteResponse getDisponibilite(UUID trackingId);

    /**
     * Retourne tous les créneaux d'un conseiller, triés par jour puis heure.
     */
    List<DisponibiliteResponse> getDisponibilitesConseiller(UUID conseillerTrackingId);

    /**
     * Retourne les créneaux paginés d'un conseiller.
     */
    Page<DisponibiliteResponse> getDisponibilitesConseilleurPagine(UUID conseillerTrackingId, Pageable pageable);

    /**
     * Retourne les créneaux d'un conseiller pour un jour spécifique (1 = Lundi, 7 =
     * Dimanche).
     */
    List<DisponibiliteResponse> getDisponibilitesParJour(UUID conseillerTrackingId, Integer jourSemaine);

    /**
     * Met à jour un créneau de disponibilité.
     */
    DisponibiliteResponse modifierDisponibilite(UUID trackingId, DisponibiliteRequest request);

    /**
     * Supprime un créneau de disponibilité (hard-delete — les créneaux n'ont pas de
     * cycle de vie).
     */
    void supprimerDisponibilite(UUID trackingId);
}
