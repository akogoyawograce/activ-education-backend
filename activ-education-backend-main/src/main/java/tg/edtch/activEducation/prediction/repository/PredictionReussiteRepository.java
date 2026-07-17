package tg.edtch.activEducation.prediction.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.prediction.domain.entite.PredictionReussite;
import java.util.Optional; import java.util.UUID;
public interface PredictionReussiteRepository extends JpaRepository<PredictionReussite, Long> {
    Optional<PredictionReussite> findByTrackingId(UUID trackingId);
    java.util.List<PredictionReussite> findByEleveTrackingIdOrderByDatePredictionDesc(String eleveTrackingId);
}
