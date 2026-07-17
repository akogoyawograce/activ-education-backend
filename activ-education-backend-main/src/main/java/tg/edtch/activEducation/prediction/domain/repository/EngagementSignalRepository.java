package tg.edtch.activEducation.prediction.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.prediction.domain.entite.EngagementSignal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EngagementSignalRepository extends JpaRepository<EngagementSignal, Long> {

    Optional<EngagementSignal> findByTrackingId(UUID trackingId);

    List<EngagementSignal> findByEleveId(Long eleveId);

    List<EngagementSignal> findByEleveIdAndFicheType(Long eleveId,
            EngagementSignal.TypeFiche ficheType);

    Optional<EngagementSignal> findByEleveIdAndFicheIdAndFicheType(Long eleveId,
            Long ficheId, EngagementSignal.TypeFiche ficheType);
}
