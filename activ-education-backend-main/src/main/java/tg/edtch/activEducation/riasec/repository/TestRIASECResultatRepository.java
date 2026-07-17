package tg.edtch.activEducation.riasec.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.riasec.domain.entite.TestRIASECResultat;
import java.util.Optional; import java.util.UUID;
public interface TestRIASECResultatRepository extends JpaRepository<TestRIASECResultat, Long> {
    Optional<TestRIASECResultat> findByTrackingId(UUID trackingId);
    java.util.List<TestRIASECResultat> findByEleveTrackingIdOrderByDatePassationDesc(String eleveTrackingId);
}
