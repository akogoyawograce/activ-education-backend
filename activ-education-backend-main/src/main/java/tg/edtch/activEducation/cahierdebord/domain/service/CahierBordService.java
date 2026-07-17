package tg.edtch.activEducation.cahierdebord.domain.service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.cahierdebord.domain.dto.EntreeJournalRequest;
import tg.edtch.activEducation.cahierdebord.domain.dto.EntreeJournalResponse;
import tg.edtch.activEducation.cahierdebord.domain.entite.EntreeJournal;
import tg.edtch.activEducation.cahierdebord.repository.EntreeJournalRepository;
import java.util.*;
@Service @Transactional
public class CahierBordService {
    private final EntreeJournalRepository repo;
    public CahierBordService(EntreeJournalRepository repo) { this.repo = repo; }
    public Page<EntreeJournalResponse> listerEntrees(String eleveId, int page, int size) {
        return repo.findByEleveTrackingIdOrderByDateEntreeDesc(eleveId, PageRequest.of(page, size))
            .map(this::toResponse);
    }
    public EntreeJournalResponse creerEntree(String eleveId, EntreeJournalRequest req) {
        var e = EntreeJournal.builder().eleveTrackingId(eleveId).titre(req.titre()).contenu(req.contenu())
            .humeur(req.humeur()).typeEntree(req.typeEntree()).tags(req.tags())
            .estPublic(req.estPublic() != null && req.estPublic()).build();
        return toResponse(repo.save(e));
    }
    public EntreeJournalResponse modifierEntree(UUID trackingId, EntreeJournalRequest req) {
        var e = repo.findByTrackingId(trackingId).orElseThrow();
        e.setTitre(req.titre()); e.setContenu(req.contenu()); e.setHumeur(req.humeur());
        e.setTypeEntree(req.typeEntree()); e.setTags(req.tags());
        if (req.estPublic() != null) e.setEstPublic(req.estPublic());
        return toResponse(repo.save(e));
    }
    public void supprimerEntree(UUID trackingId) { repo.deleteByTrackingId(trackingId); }
    public Map<String, Object> getStats(String eleveId) {
        var entrees = repo.findByEleveTrackingIdOrderByDateEntreeDesc(eleveId, PageRequest.of(0, 1000));
        var stats = new HashMap<String, Object>();
        var humeurs = new HashMap<String, Integer>();
        var types = new HashMap<String, Integer>();
        entrees.forEach(e -> {
            humeurs.merge(e.getHumeur(), 1, Integer::sum);
            types.merge(e.getTypeEntree(), 1, Integer::sum);
        });
        stats.put("total", entrees.getTotalElements());
        stats.put("humeurs", humeurs);
        stats.put("types", types);
        return stats;
    }
    private EntreeJournalResponse toResponse(EntreeJournal e) {
        return new EntreeJournalResponse(
            e.getTrackingId().toString(), e.getEleveTrackingId(), e.getTitre(), e.getContenu(),
            e.getHumeur(), e.getTypeEntree(), e.getTags(), e.getEstPublic(),
            e.getDateEntree() != null ? e.getDateEntree().toString() : null,
            e.getCreatedAt() != null ? e.getCreatedAt().toString() : null);
    }
}
