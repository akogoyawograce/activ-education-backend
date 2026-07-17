package tg.edtch.activEducation.defis.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.defis.domain.entite.DefiOrientation;
import java.util.Optional; import java.util.UUID;
public interface DefiOrientationRepository extends JpaRepository<DefiOrientation, Long> {
    Optional<DefiOrientation> findByTrackingId(UUID trackingId);
    java.util.Optional<DefiOrientation> findByCode(String code);
}
