package tg.edtch.activEducation.accompagnement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.accompagnement.domain.entite.Disponibilite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DisponibiliteRepository extends JpaRepository<Disponibilite, Long> {

    /** Recherche par identifiant public de la disponibilité. */
    Optional<Disponibilite> findByTrackingId(UUID trackingId);

    /** Toutes les disponibilités d'un conseiller via son trackingId public. */
    List<Disponibilite> findByConseillerTrackingIdOrderByJourSemaineAscHeureDebutAsc(UUID conseillerTrackingId);

    /** Disponibilités d'un conseiller pour un jour donné, triées par heure. */
    List<Disponibilite> findByConseillerTrackingIdAndJourSemaineOrderByHeureDebutAsc(UUID conseillerTrackingId,
            Integer jourSemaine);

    /** Disponibilités paginées d'un conseiller. */
    Page<Disponibilite> findByConseillerTrackingId(UUID conseillerTrackingId, Pageable pageable);
}
