package tg.edtch.activEducation.parrainage.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.parrainage.domain.entite.Parrainage;
import java.util.Optional; import java.util.UUID;
public interface ParrainageRepository extends JpaRepository<Parrainage, Long> {
    Optional<Parrainage> findByTrackingId(UUID trackingId);
    java.util.List<Parrainage> findByParrainTrackingId(String parrainTrackingId);
}
