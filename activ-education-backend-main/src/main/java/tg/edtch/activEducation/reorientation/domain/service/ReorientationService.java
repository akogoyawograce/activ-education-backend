package tg.edtch.activEducation.reorientation.domain.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.reorientation.domain.dto.DemandeReorientationRequest;
import tg.edtch.activEducation.reorientation.domain.entite.DemandeReorientation;
import tg.edtch.activEducation.reorientation.repository.DemandeReorientationRepository;
import java.util.*;
@Service @Transactional
public class ReorientationService {
    private final DemandeReorientationRepository repo;
    public ReorientationService(DemandeReorientationRepository repo) { this.repo = repo; }
    public DemandeReorientation soumettre(String eleveId, DemandeReorientationRequest req) {
        return repo.save(DemandeReorientation.builder().eleveTrackingId(eleveId).filiereActuelle(req.filiereActuelle()).nouvelleFiliere(req.nouvelleFiliere()).metierVise(req.metierVise()).raison(req.raison()).build());
    }
    public DemandeReorientation traiter(UUID trackingId, String conseillerId, String statut, String commentaire) {
        var d = repo.findByTrackingId(trackingId).orElseThrow();
        d.setStatut(statut); d.setConseillerTrackingId(conseillerId); d.setConseillerCommentaire(commentaire);
        return repo.save(d);
    }
    public List<DemandeReorientation> getDemandes(String eleveId) { return repo.findByEleveTrackingIdOrderByCreatedAtDesc(eleveId); }
}
