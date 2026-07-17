package tg.edtch.activEducation.mentorat.domain.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.alumni.domain.entite.Mentorat;
import tg.edtch.activEducation.alumni.repository.MentoratRepository;
import tg.edtch.activEducation.mentorat.domain.dto.MentoratRequest;
import java.util.List; import java.util.UUID;
@Service @Transactional
public class MentoratService {
    private final MentoratRepository repo;
    public MentoratService(MentoratRepository repo) { this.repo = repo; }
    public List<Mentorat> getMentorats(String personneId) {
        var asMentor = repo.findByMentorTrackingId(personneId);
        var asMentore = repo.findByMentoreTrackingId(personneId);
        asMentor.addAll(asMentore);
        return asMentor;
    }
    public Mentorat creer(MentoratRequest req) {
        return repo.save(Mentorat.builder().mentorTrackingId(req.mentorTrackingId()).mentoreTrackingId(req.mentoreTrackingId()).build());
    }
    public Mentorat mettreAJour(UUID trackingId, String statut, Integer nbSeances) {
        var m = repo.findByTrackingId(trackingId).orElseThrow();
        if (statut != null) m.setStatut(statut);
        if (nbSeances != null) m.setNbSeances(nbSeances);
        return repo.save(m);
    }
}
