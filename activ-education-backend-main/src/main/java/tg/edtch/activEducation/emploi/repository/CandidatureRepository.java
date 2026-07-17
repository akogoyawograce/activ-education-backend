package tg.edtch.activEducation.emploi.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.emploi.domain.entite.Candidature;
import java.util.Optional; import java.util.UUID;
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    Optional<Candidature> findByTrackingId(UUID trackingId);
    java.util.List<Candidature> findByEleveTrackingId(String eleveTrackingId);
}
