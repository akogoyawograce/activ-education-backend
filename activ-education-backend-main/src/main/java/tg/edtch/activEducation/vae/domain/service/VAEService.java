package tg.edtch.activEducation.vae.domain.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.vae.domain.dto.DossierVAERequest;
import tg.edtch.activEducation.vae.domain.entite.DossierVAE;
import tg.edtch.activEducation.vae.repository.DossierVAERepository;
import java.util.List; import java.util.UUID;
@Service @Transactional
public class VAEService {
    private final DossierVAERepository repo;
    public VAEService(DossierVAERepository repo) { this.repo = repo; }
    public List<DossierVAE> getDossiers(String eleveId) { return repo.findByEleveTrackingIdOrderByCreatedAtDesc(eleveId); }
    public DossierVAE creer(DossierVAERequest req) {
        return repo.save(DossierVAE.builder().eleveTrackingId(req.eleveTrackingId()).diplomeVise(req.diplomeVise()).niveauVise(req.niveauVise()).experiences(req.experiences()).build());
    }
    public DossierVAE mettreAJour(UUID trackingId, String statut, String conseillerId) {
        var d = repo.findByTrackingId(trackingId).orElseThrow();
        if (statut != null) d.setStatut(statut);
        if (conseillerId != null) d.setConseillerTrackingId(conseillerId);
        return repo.save(d);
    }
}
