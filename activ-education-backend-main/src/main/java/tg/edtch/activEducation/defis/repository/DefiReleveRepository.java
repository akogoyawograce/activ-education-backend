package tg.edtch.activEducation.defis.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.defis.domain.entite.DefiReleve;
import java.util.Optional; import java.util.UUID;
public interface DefiReleveRepository extends JpaRepository<DefiReleve, Long> {
    Optional<DefiReleve> findByTrackingId(UUID trackingId);
    java.util.List<DefiReleve> findByEleveTrackingId(String eleveTrackingId);
}
