package tg.edtch.activEducation.horsligne.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.horsligne.domain.entite.SyncLog;
import java.util.Optional; import java.util.UUID;
public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {
    Optional<SyncLog> findByTrackingId(UUID trackingId);
    java.util.List<SyncLog> findByEleveTrackingId(String eleveTrackingId);
}
