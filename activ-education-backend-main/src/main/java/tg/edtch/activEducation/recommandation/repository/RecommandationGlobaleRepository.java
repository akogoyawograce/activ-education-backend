package tg.edtch.activEducation.recommandation.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.recommandation.domain.entite.RecommandationGlobale;
import java.util.Optional; import java.util.UUID;
public interface RecommandationGlobaleRepository extends JpaRepository<RecommandationGlobale, Long> {
    Optional<RecommandationGlobale> findByTrackingId(UUID trackingId);
    java.util.List<RecommandationGlobale> findByEleveTrackingIdOrderByScoreGlobalDesc(String eleveTrackingId);
}
