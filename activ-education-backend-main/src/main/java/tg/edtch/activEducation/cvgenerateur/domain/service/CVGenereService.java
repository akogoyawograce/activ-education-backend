package tg.edtch.activEducation.cvgenerateur.domain.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.cvgenerateur.domain.dto.CVGenereRequest;
import tg.edtch.activEducation.cvgenerateur.domain.entite.CVGenere;
import tg.edtch.activEducation.cvgenerateur.repository.CVGenereRepository;
import java.util.List; import java.util.UUID;
@Service @Transactional
public class CVGenereService {
    private final CVGenereRepository repo;
    public CVGenereService(CVGenereRepository repo) { this.repo = repo; }
    public List<CVGenere> getCVs(String eleveId) { return repo.findByEleveTrackingIdOrderByCreatedAtDesc(eleveId); }
    public CVGenere creer(String eleveId, CVGenereRequest req) {
        return repo.save(CVGenere.builder().eleveTrackingId(eleveId).titre(req.titre()).contenuJson(req.contenuJson()).template(req.template() != null ? req.template() : "classique").build());
    }
    public CVGenere modifier(UUID trackingId, CVGenereRequest req) {
        var c = repo.findByTrackingId(trackingId).orElseThrow();
        c.setTitre(req.titre()); if (req.contenuJson() != null) c.setContenuJson(req.contenuJson());
        if (req.template() != null) c.setTemplate(req.template());
        return repo.save(c);
    }
    public void supprimer(UUID trackingId) { var c = repo.findByTrackingId(trackingId).orElseThrow(); repo.delete(c); }
}
