package tg.edtch.activEducation.calendrier.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.calendrier.domain.entite.EvenementOrientation;
import java.util.Optional; import java.util.UUID;
public interface EvenementOrientationRepository extends JpaRepository<EvenementOrientation, Long> {
    Optional<EvenementOrientation> findByTrackingId(UUID trackingId);
    java.util.List<EvenementOrientation> findByDateDebutAfterOrderByDateDebutAsc(java.time.LocalDateTime date);
}
