package tg.edtch.activEducation.prediction.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.prediction.domain.entite.OrientationOutcome;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrientationOutcomeRepository extends JpaRepository<OrientationOutcome, Long> {

    Optional<OrientationOutcome> findByTrackingId(UUID trackingId);

    List<OrientationOutcome> findByEleveId(Long eleveId);

    List<OrientationOutcome> findByFiliereId(Long filiereId);

    List<OrientationOutcome> findByStatut(OrientationOutcome.StatutOrientation statut);

    /**
     * Source d'or pour l'entraînement supervisé (Phase 5) : uniquement les
     * issues closes avec un label exploitable (ADMIS / RECALE).
     */
    List<OrientationOutcome> findByStatutIn(
            List<OrientationOutcome.StatutOrientation> statuts);
}
