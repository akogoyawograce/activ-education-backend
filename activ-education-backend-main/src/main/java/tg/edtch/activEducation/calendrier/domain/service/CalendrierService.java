package tg.edtch.activEducation.calendrier.domain.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.calendrier.domain.entite.EvenementOrientation;
import tg.edtch.activEducation.calendrier.repository.EvenementOrientationRepository;
import java.time.LocalDateTime;
import java.util.*;
@Service @Transactional
public class CalendrierService {
    private final EvenementOrientationRepository repo;
    public CalendrierService(EvenementOrientationRepository repo) { this.repo = repo; }
    public List<EvenementOrientation> getAVenir() { return repo.findByDateDebutAfterOrderByDateDebutAsc(LocalDateTime.now()); }
    public EvenementOrientation creer(EvenementOrientation e) { return repo.save(e); }
}
