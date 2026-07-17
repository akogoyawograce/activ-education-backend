package tg.edtch.activEducation.vae.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.vae.domain.entite.DossierVAE;
import java.util.Optional; import java.util.UUID;
public interface DossierVAERepository extends JpaRepository<DossierVAE, Long> {
    Optional<DossierVAE> findByTrackingId(UUID trackingId);
    java.util.List<DossierVAE> findByEleveTrackingIdOrderByCreatedAtDesc(String eleveTrackingId);
}
