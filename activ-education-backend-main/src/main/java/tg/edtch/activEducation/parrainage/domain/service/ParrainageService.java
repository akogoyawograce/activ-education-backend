package tg.edtch.activEducation.parrainage.domain.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.parrainage.domain.dto.ParrainageRequest;
import tg.edtch.activEducation.parrainage.domain.entite.Parrainage;
import tg.edtch.activEducation.parrainage.repository.ParrainageRepository;
import java.util.List; import java.util.UUID;
@Service @Transactional
public class ParrainageService {
    private final ParrainageRepository repo;
    public ParrainageService(ParrainageRepository repo) { this.repo = repo; }
    public Parrainage creer(ParrainageRequest req) {
        return repo.save(Parrainage.builder().parrainTrackingId(req.parrainTrackingId()).filleulTrackingId(req.filleulTrackingId()).build());
    }
    public List<Parrainage> getParrainages(String parrainId) { return repo.findByParrainTrackingId(parrainId); }
    public Parrainage mettreAJour(UUID trackingId, String statut) {
        var p = repo.findByTrackingId(trackingId).orElseThrow();
        p.setStatut(statut);
        return repo.save(p);
    }
}
