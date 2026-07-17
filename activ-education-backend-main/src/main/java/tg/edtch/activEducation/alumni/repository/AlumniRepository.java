package tg.edtch.activEducation.alumni.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.alumni.domain.entite.Alumni;
import java.util.Optional; import java.util.UUID;
public interface AlumniRepository extends JpaRepository<Alumni, Long> {
    Optional<Alumni> findByTrackingId(UUID trackingId);
    java.util.List<Alumni> findByEstMentorTrue();
}
