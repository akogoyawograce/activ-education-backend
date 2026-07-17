package tg.edtch.activEducation.sallevirtuelle.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.sallevirtuelle.domain.entite.VisiteVirtuelle;
import java.util.Optional; import java.util.UUID;
public interface VisiteVirtuelleRepository extends JpaRepository<VisiteVirtuelle, Long> {
    Optional<VisiteVirtuelle> findByTrackingId(UUID trackingId);
    java.util.List<VisiteVirtuelle> findByEstPublieTrue();
}
