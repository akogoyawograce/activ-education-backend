package tg.edtch.activEducation.accompagnement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.accompagnement.domain.entite.RendezVous;
import tg.edtch.activEducation.accompagnement.domain.entite.RendezVous.StatutRendezVous;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    /** Recherche par identifiant public du rendez-vous. */
    Optional<RendezVous> findByTrackingId(UUID trackingId);

    /**
     * Tous les RDV d'un élève (via son trackingId), triés par date décroissante.
     */
    List<RendezVous> findByEleveTrackingIdOrderByDateHeurePrevueDesc(UUID eleveTrackingId);

    /**
     * Tous les RDV d'un conseiller (via son trackingId), triés par date
     * décroissante.
     */
    List<RendezVous> findByConseillerTrackingIdOrderByDateHeurePrevueDesc(UUID conseillerTrackingId);

    /** RDV d'un élève paginés. */
    Page<RendezVous> findByEleveTrackingId(UUID eleveTrackingId, Pageable pageable);

    /** RDV d'un conseiller paginés. */
    Page<RendezVous> findByConseillerTrackingId(UUID conseillerTrackingId, Pageable pageable);

    /** RDV d'un élève filtrés par statut. */
    List<RendezVous> findByEleveTrackingIdAndStatutOrderByDateHeurePrevueDesc(UUID eleveTrackingId,
            StatutRendezVous statut);

    /** RDV d'un conseiller filtrés par statut. */
    List<RendezVous> findByConseillerTrackingIdAndStatutOrderByDateHeurePrevueDesc(UUID conseillerTrackingId,
            StatutRendezVous statut);
}
