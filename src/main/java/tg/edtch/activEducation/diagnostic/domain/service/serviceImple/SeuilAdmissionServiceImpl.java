package tg.edtch.activEducation.diagnostic.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.bibliotheque.repository.FicheFiliereRepository;
import tg.edtch.activEducation.diagnostic.application.dto.request.SeuilAdmissionRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.SeuilAdmissionResponse;
import tg.edtch.activEducation.diagnostic.application.mapper.SeuilAdmissionMapper;
import tg.edtch.activEducation.diagnostic.domain.entite.SeuilAdmission;
import tg.edtch.activEducation.diagnostic.domain.service.SeuilAdmissionService;
import tg.edtch.activEducation.diagnostic.repository.SeuilAdmissionRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SeuilAdmissionServiceImpl implements SeuilAdmissionService {

    private final SeuilAdmissionRepository seuilAdmissionRepository;
    private final FicheFiliereRepository ficheFiliereRepository;
    private final SeuilAdmissionMapper seuilAdmissionMapper;

    @Override
    public SeuilAdmissionResponse creerSeuil(SeuilAdmissionRequest request) {
        FicheFiliere filiere = resolveFiliere(request.getFiliereTrackingId());
        SeuilAdmission seuil = seuilAdmissionMapper.toEntity(request, filiere);
        SeuilAdmission saved = seuilAdmissionRepository.save(seuil);
        log.info("SeuilAdmission créé : matiere='{}' noteMin={} filiere={} trackingId={}",
                saved.getMatiereRequise(), saved.getNoteMinimum(),
                request.getFiliereTrackingId(), saved.getTrackingId());
        return seuilAdmissionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SeuilAdmissionResponse getSeuil(UUID trackingId) {
        return seuilAdmissionMapper.toResponse(findOrThrow(trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeuilAdmissionResponse> listerSeuils() {
        return seuilAdmissionRepository.findAll()
                .stream()
                .map(seuilAdmissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeuilAdmissionResponse> getSeuilsParFiliere(UUID filiereTrackingId) {
        return seuilAdmissionRepository.findByFiliereTrackingId(filiereTrackingId)
                .stream()
                .map(seuilAdmissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SeuilAdmissionResponse modifierSeuil(UUID trackingId, SeuilAdmissionRequest request) {
        SeuilAdmission seuil = findOrThrow(trackingId);
        FicheFiliere filiere = resolveFiliere(request.getFiliereTrackingId());
        seuilAdmissionMapper.updateFromRequest(request, seuil, filiere);
        SeuilAdmission saved = seuilAdmissionRepository.save(seuil);
        log.info("SeuilAdmission modifié : trackingId={}", trackingId);
        return seuilAdmissionMapper.toResponse(saved);
    }

    @Override
    public void supprimerSeuil(UUID trackingId) {
        SeuilAdmission seuil = findOrThrow(trackingId);
        seuilAdmissionRepository.delete(seuil);
        log.info("SeuilAdmission supprimé : trackingId={}", trackingId);
    }

    // ─── Helpers privés ───────────────────────────────────────────────────────
    private SeuilAdmission findOrThrow(UUID trackingId) {
        return seuilAdmissionRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "SeuilAdmission introuvable pour le trackingId : " + trackingId));
    }

    /**
     * Résout la filière depuis son trackingId public.
     * Retourne null si aucun trackingId fourni (filière optionnelle).
     */
    private FicheFiliere resolveFiliere(UUID filiereTrackingId) {
        if (filiereTrackingId == null)
            return null;
        return ficheFiliereRepository.findByTrackingId(filiereTrackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "FicheFiliere introuvable pour le trackingId : " + filiereTrackingId));
    }
}
