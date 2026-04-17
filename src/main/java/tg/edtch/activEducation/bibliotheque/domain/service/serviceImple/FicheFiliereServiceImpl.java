package tg.edtch.activEducation.bibliotheque.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheFiliereRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheFiliereResponse;
import tg.edtch.activEducation.bibliotheque.application.mapper.FicheFiliereMapper;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheMetier;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheFiliereService;
import tg.edtch.activEducation.bibliotheque.repository.FicheFiliereRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheMetierRepository;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FicheFiliereServiceImpl implements FicheFiliereService {

    private final FicheFiliereRepository filiereRepository;
    private final FicheMetierRepository metierRepository;
    private final FicheFiliereMapper filiereMapper;

    @Override
    public FicheFiliereResponse creerFiliere(FicheFiliereRequest request) {
        Set<FicheMetier> metiers = resolveMetiers(request.getMetiersTrackingIds());
        FicheFiliere filiere = filiereMapper.toEntity(request, metiers);
        FicheFiliere saved = filiereRepository.save(filiere);
        log.info("Fiche filière créée : trackingId={}", saved.getTrackingId());
        return filiereMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FicheFiliereResponse getFiliere(UUID trackingId) {
        FicheFiliere filiere = findOrThrow(trackingId);
        filiere.setNbConsultations(filiere.getNbConsultations() + 1);
        return filiereMapper.toResponse(filiere);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheFiliereResponse> listerToutes(Pageable pageable) {
        return filiereRepository.findAll(pageable)
                .map(filiereMapper::toResponse);
    }

    @Override
    public FicheFiliereResponse modifierFiliere(UUID trackingId, FicheFiliereRequest request) {
        FicheFiliere filiere = findOrThrow(trackingId);
        Set<FicheMetier> metiers = resolveMetiers(request.getMetiersTrackingIds());
        filiereMapper.updateFromRequest(request, filiere, metiers);
        FicheFiliere saved = filiereRepository.save(filiere);
        log.info("Fiche filière modifiée : trackingId={}", trackingId);
        return filiereMapper.toResponse(saved);
    }

    @Override
    public void supprimerFiliere(UUID trackingId) {
        FicheFiliere filiere = findOrThrow(trackingId);
        filiereRepository.delete(filiere);
        log.info("Fiche filière supprimée : trackingId={}", trackingId);
    }

    private FicheFiliere findOrThrow(UUID trackingId) {
        return filiereRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Fiche filière introuvable pour le trackingId : " + trackingId));
    }

    private Set<FicheMetier> resolveMetiers(Set<UUID> trackingIds) {
        if (trackingIds == null || trackingIds.isEmpty())
            return Set.of();
        return trackingIds.stream()
                .map(tid -> metierRepository.findByTrackingId(tid)
                        .orElseThrow(() -> new NoSuchElementException("Métier introuvable : " + tid)))
                .collect(Collectors.toSet());
    }
}
