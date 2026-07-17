package tg.edtch.activEducation.horsligne.domain.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.horsligne.domain.entite.SyncLog;
import tg.edtch.activEducation.horsligne.repository.SyncLogRepository;
import java.util.List;
@Service @Transactional
public class HorsLigneService {
    private final SyncLogRepository repo;
    public HorsLigneService(SyncLogRepository repo) { this.repo = repo; }
    public SyncLog enregistrerSync(String eleveId, String typeDonnees, Integer tailleKb) {
        return repo.save(SyncLog.builder().eleveTrackingId(eleveId).typeDonnees(typeDonnees).tailleKb(tailleKb).build());
    }
    public List<SyncLog> getSyncLogs(String eleveId) { return repo.findByEleveTrackingId(eleveId); }
}
