package tg.edtch.activEducation.emploi.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.emploi.domain.entite.OffreEmploi;
import java.util.Optional; import java.util.UUID;
public interface OffreEmploiRepository extends JpaRepository<OffreEmploi, Long> {
    Optional<OffreEmploi> findByTrackingId(UUID trackingId);
    java.util.List<OffreEmploi> findByEstPublieTrueAndEstActifTrueOrderByCreatedAtDesc();
}
