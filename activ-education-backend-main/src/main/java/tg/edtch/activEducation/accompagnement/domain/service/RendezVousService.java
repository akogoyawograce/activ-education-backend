package tg.edtch.activEducation.accompagnement.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.accompagnement.application.dto.request.RendezVousRequest;
import tg.edtch.activEducation.accompagnement.application.dto.response.RendezVousResponse;

import java.util.List;
import java.util.UUID;

/**
 * Contrat de service pour la gestion des rendez-vous.
 * Tous les identifiants exposés sont des {@code UUID trackingId}.
 */
public interface RendezVousService {

    /**
     * Planifie un rendez-vous entre un élève et un conseiller.
     */
    RendezVousResponse planifier(RendezVousRequest request);

    /**
     * Récupère un rendez-vous par son trackingId public.
     */
    RendezVousResponse getRendezVous(UUID trackingId);

    /**
     * Retourne tous les RDV d'un élève, triés par date décroissante.
     */
    List<RendezVousResponse> getRendezVousEleve(UUID eleveTrackingId);

    /**
     * Retourne tous les RDV d'un conseiller, triés par date décroissante.
     */
    List<RendezVousResponse> getRendezVousConseiller(UUID conseillerTrackingId);

    /**
     * Retourne les RDV paginés d'un élève.
     */
    Page<RendezVousResponse> getRendezVousElevePagine(UUID eleveTrackingId, Pageable pageable);

    /**
     * Retourne les RDV paginés d'un conseiller.
     */
    Page<RendezVousResponse> getRendezVousConseillerPagine(UUID conseillerTrackingId, Pageable pageable);

    /**
     * Filtre les RDV d'un élève par statut (PLANIFIE, TERMINE, ANNULE).
     */
    List<RendezVousResponse> getRendezVousEleveParStatut(UUID eleveTrackingId, String statut);

    /**
     * Filtre les RDV d'un conseiller par statut.
     */
    List<RendezVousResponse> getRendezVousConseillerParStatut(UUID conseillerTrackingId, String statut);

    /**
     * Met à jour la date, le lien visio ou les notes d'un RDV PLANIFIÉ.
     */
    RendezVousResponse modifierRendezVous(UUID trackingId, RendezVousRequest request);

    /**
     * Marque un rendez-vous comme TERMINE.
     */
    RendezVousResponse terminer(UUID trackingId);

    /**
     * Annule un rendez-vous (statut → ANNULE).
     */
    RendezVousResponse annuler(UUID trackingId);

    /**
     * Génère et associe un lien de visioconférence à un RDV PLANIFIÉ.
     */
    RendezVousResponse genererLienVisio(UUID trackingId);
}
