package tg.edtch.activEducation.cartemetiers.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.cartemetiers.domain.entite.MetierRegionData;
import java.util.Optional; import java.util.UUID;
public interface MetierRegionDataRepository extends JpaRepository<MetierRegionData, Long> {
    Optional<MetierRegionData> findByTrackingId(UUID trackingId);
    java.util.List<MetierRegionData> findByRegion(String region);
}
