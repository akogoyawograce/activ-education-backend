package tg.edtch.activEducation.emploi.domain.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.emploi.domain.entite.OffreEmploi;
import tg.edtch.activEducation.emploi.domain.entite.Candidature;
import tg.edtch.activEducation.emploi.repository.OffreEmploiRepository;
import tg.edtch.activEducation.emploi.repository.CandidatureRepository;
import java.time.LocalDate;
import java.util.*;
@Service @Transactional
public class EmploiService {
    private final OffreEmploiRepository offreRepo;
    private final CandidatureRepository candidatureRepo;
    public EmploiService(OffreEmploiRepository offreRepo, CandidatureRepository candidatureRepo) { this.offreRepo = offreRepo; this.candidatureRepo = candidatureRepo; }
    public List<OffreEmploi> getOffres() { return offreRepo.findByEstPublieTrueAndEstActifTrueOrderByCreatedAtDesc(); }
    public OffreEmploi creerOffre(OffreEmploi req) { return offreRepo.save(req); }
    public Candidature postuler(String offreId, String eleveId, String message) {
        var candidature = Candidature.builder().offreTrackingId(offreId).eleveTrackingId(eleveId).message(message).build();
        return candidatureRepo.save(candidature);
    }
    public List<Candidature> getCandidatures(String eleveId) { return candidatureRepo.findByEleveTrackingId(eleveId); }
}
