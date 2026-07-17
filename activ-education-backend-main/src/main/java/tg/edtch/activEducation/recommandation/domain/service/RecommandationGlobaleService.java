package tg.edtch.activEducation.recommandation.domain.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.recommandation.domain.entite.RecommandationGlobale;
import tg.edtch.activEducation.recommandation.repository.RecommandationGlobaleRepository;
import java.util.List;
@Service @Transactional
public class RecommandationGlobaleService {
    private final RecommandationGlobaleRepository repo;
    public RecommandationGlobaleService(RecommandationGlobaleRepository repo) { this.repo = repo; }
    public List<RecommandationGlobale> getRecommandations(String eleveId) { return repo.findByEleveTrackingIdOrderByScoreGlobalDesc(eleveId); }
    public RecommandationGlobale creer(RecommandationGlobale r) { return repo.save(r); }
}
