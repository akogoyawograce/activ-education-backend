package tg.edtch.activEducation.defis.domain.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.defis.domain.entite.DefiOrientation;
import tg.edtch.activEducation.defis.domain.entite.DefiReleve;
import tg.edtch.activEducation.defis.repository.DefiOrientationRepository;
import tg.edtch.activEducation.defis.repository.DefiReleveRepository;
import java.util.*;
@Service @Transactional
public class DefisService {
    private final DefiOrientationRepository defiRepo;
    private final DefiReleveRepository releveRepo;
    public DefisService(DefiOrientationRepository defiRepo, DefiReleveRepository releveRepo) { this.defiRepo = defiRepo; this.releveRepo = releveRepo; }
    public List<DefiOrientation> getDefis() { return defiRepo.findAll(); }
    public DefiReleve relever(String eleveId, String defiCode) {
        return releveRepo.save(DefiReleve.builder().eleveTrackingId(eleveId).defiCode(defiCode).build());
    }
    public List<DefiReleve> getProgression(String eleveId) { return releveRepo.findByEleveTrackingId(eleveId); }
}
