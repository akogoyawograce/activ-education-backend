package tg.edtch.activEducation.alumni.domain.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.alumni.domain.dto.AlumniRequest;
import tg.edtch.activEducation.alumni.domain.entite.Alumni;
import tg.edtch.activEducation.alumni.repository.AlumniRepository;
import java.util.List; import java.util.UUID;
@Service @Transactional
public class AlumniService {
    private final AlumniRepository repo;
    public AlumniService(AlumniRepository repo) { this.repo = repo; }
    public List<Alumni> getAll() { return repo.findAll(); }
    public List<Alumni> getMentors() { return repo.findByEstMentorTrue(); }
    public Alumni creer(AlumniRequest req) {
        return repo.save(Alumni.builder().ancienEleveTrackingId(req.ancienEleveTrackingId()).nom(req.nom()).email(req.email()).telephone(req.telephone()).promotion(req.promotion()).filiereSuivie(req.filiereSuivie()).metierActuel(req.metierActuel()).entreprise(req.entreprise()).secteur(req.secteur()).bio(req.bio()).estMentor(req.estMentor() != null ? req.estMentor() : false).build());
    }
    public Alumni modifier(UUID trackingId, AlumniRequest req) {
        var a = repo.findByTrackingId(trackingId).orElseThrow();
        a.setNom(req.nom()); a.setEmail(req.email()); a.setTelephone(req.telephone()); a.setPromotion(req.promotion());
        a.setFiliereSuivie(req.filiereSuivie()); a.setMetierActuel(req.metierActuel()); a.setEntreprise(req.entreprise());
        a.setSecteur(req.secteur()); a.setBio(req.bio());
        if (req.estMentor() != null) a.setEstMentor(req.estMentor());
        return repo.save(a);
    }
}
