package tg.edtch.activEducation.attestations.domain.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.attestations.domain.dto.AttestationRequest;
import tg.edtch.activEducation.attestations.domain.entite.Attestation;
import tg.edtch.activEducation.attestations.repository.AttestationRepository;
import java.util.List; import java.util.UUID;
@Service @Transactional
public class AttestationService {
    private final AttestationRepository repo;
    public AttestationService(AttestationRepository repo) { this.repo = repo; }
    public Attestation creer(AttestationRequest req) {
        var code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return repo.save(Attestation.builder().eleveTrackingId(req.eleveTrackingId()).titre(req.titre()).typeAttestation(req.typeAttestation()).contenuJson(req.contenuJson()).codeVerification(code).build());
    }
    public Attestation getByCode(String code) {
        return repo.findAll().stream().filter(a -> code.equals(a.getCodeVerification())).findFirst().orElseThrow();
    }
    public List<Attestation> getByEleve(String eleveId) { return repo.findAll().stream().filter(a -> eleveId.equals(a.getEleveTrackingId())).toList(); }
}
