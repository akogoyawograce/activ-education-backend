package tg.edtch.activEducation.alumni.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.alumni.domain.entite.Mentorat;
import java.util.Optional; import java.util.UUID;
public interface MentoratRepository extends JpaRepository<Mentorat, Long> {
    Optional<Mentorat> findByTrackingId(UUID trackingId);
    java.util.List<Mentorat> findByMentorTrackingId(String mentorTrackingId);
    java.util.List<Mentorat> findByMentoreTrackingId(String mentoreTrackingId);
}
