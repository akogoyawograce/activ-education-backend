package tg.edtch.activEducation.bibliotheque.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheEtablissementRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheEtablissementResponse;
import tg.edtch.activEducation.bibliotheque.application.mapper.FicheEtablissementMapper;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheEtablissement;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheEtablissementService;
import tg.edtch.activEducation.bibliotheque.repository.FicheEtablissementRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheFiliereRepository;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FicheEtablissementServiceImpl implements FicheEtablissementService {

    private final FicheEtablissementRepository etablissementRepository;
    private final FicheFiliereRepository filiereRepository;
    private final FicheEtablissementMapper etablissementMapper;

    @Override
    public FicheEtablissementResponse creerEtablissement(FicheEtablissementRequest request) {
        Set<FicheFiliere> filieres = resolveFilieres(request.getFilieresTrackingIds());
        FicheEtablissement etablissement = etablissementMapper.toEntity(request, filieres);
        FicheEtablissement saved = etablissementRepository.save(etablissement);
        log.info("Fiche établissement créée : trackingId={}", saved.getTrackingId());
        return etablissementMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FicheEtablissementResponse getEtablissement(UUID trackingId) {
        FicheEtablissement etablissement = findOrThrow(trackingId);
        etablissement.setNbConsultations(etablissement.getNbConsultations() + 1);
        return etablissementMapper.toResponse(etablissement);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheEtablissementResponse> listerTous(Pageable pageable) {
        return etablissementRepository.findAll(pageable)
                .map(etablissementMapper::toResponse);
    }

    @Override
    public FicheEtablissementResponse modifierEtablissement(UUID trackingId, FicheEtablissementRequest request) {
        FicheEtablissement etablissement = findOrThrow(trackingId);
        Set<FicheFiliere> filieres = resolveFilieres(request.getFilieresTrackingIds());
        etablissementMapper.updateFromRequest(request, etablissement, filieres);
        FicheEtablissement saved = etablissementRepository.save(etablissement);
        log.info("Fiche établissement modifiée : trackingId={}", trackingId);
        return etablissementMapper.toResponse(saved);
    }

    @Override
    public void supprimerEtablissement(UUID trackingId) {
        FicheEtablissement etablissement = findOrThrow(trackingId);
        etablissementRepository.delete(etablissement);
        log.info("Fiche établissement supprimée : trackingId={}", trackingId);
    }

    private FicheEtablissement findOrThrow(UUID trackingId) {
        return etablissementRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Fiche établissement introuvable pour le trackingId : " + trackingId));
    }

    private Set<FicheFiliere> resolveFilieres(Set<UUID> trackingIds) {
        if (trackingIds == null || trackingIds.isEmpty())
            return Set.of();
        return trackingIds.stream()
                .map(tid -> filiereRepository.findByTrackingId(tid)
                        .orElseThrow(() -> new NoSuchElementException("Filière introuvable : " + tid)))
                .collect(Collectors.toSet());
    }
}
