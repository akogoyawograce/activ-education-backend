package tg.edtch.activEducation.diagnostic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.diagnostic.domain.entite.SeuilAdmission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeuilAdmissionRepository extends JpaRepository<SeuilAdmission, Long> {

    /** Recherche par identifiant public du seuil. */
    Optional<SeuilAdmission> findByTrackingId(UUID trackingId);

    /**
     * Tous les seuils d'admission d'une filière (via son trackingId public hérité
     * de Fiche).
     */
    List<SeuilAdmission> findByFiliereTrackingId(UUID filiereTrackingId);
}
