package tg.edtch.activEducation.sallevirtuelle.domain.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.sallevirtuelle.domain.entite.VisiteVirtuelle;
import tg.edtch.activEducation.sallevirtuelle.repository.VisiteVirtuelleRepository;
import java.util.*;
@Service @Transactional
public class VisiteVirtuelleService {
    private final VisiteVirtuelleRepository repo;
    public VisiteVirtuelleService(VisiteVirtuelleRepository repo) { this.repo = repo; }
    public List<VisiteVirtuelle> getAll() { return repo.findByEstPublieTrue(); }
    public VisiteVirtuelle creer(VisiteVirtuelle v) { return repo.save(v); }
}
