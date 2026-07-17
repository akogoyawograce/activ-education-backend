package tg.edtch.activEducation.cvgenerateur.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.cvgenerateur.domain.entite.CVGenere;
import java.util.Optional; import java.util.UUID;
public interface CVGenereRepository extends JpaRepository<CVGenere, Long> {
    Optional<CVGenere> findByTrackingId(UUID trackingId);
    java.util.List<CVGenere> findByEleveTrackingIdOrderByCreatedAtDesc(String eleveTrackingId);
}
