package tg.edtch.activEducation.reorientation.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.reorientation.domain.entite.DemandeReorientation;
import java.util.Optional; import java.util.UUID;
public interface DemandeReorientationRepository extends JpaRepository<DemandeReorientation, Long> {
    Optional<DemandeReorientation> findByTrackingId(UUID trackingId);
    java.util.List<DemandeReorientation> findByEleveTrackingIdOrderByCreatedAtDesc(String eleveTrackingId);
}
