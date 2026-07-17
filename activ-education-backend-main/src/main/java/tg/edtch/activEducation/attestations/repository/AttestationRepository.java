package tg.edtch.activEducation.attestations.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.attestations.domain.entite.Attestation;
import java.util.Optional; import java.util.UUID;
public interface AttestationRepository extends JpaRepository<Attestation, Long> {
    Optional<Attestation> findByTrackingId(UUID trackingId);

}
