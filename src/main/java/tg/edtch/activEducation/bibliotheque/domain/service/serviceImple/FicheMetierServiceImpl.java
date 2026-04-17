package tg.edtch.activEducation.bibliotheque.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheMetierRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheMetierResponse;
import tg.edtch.activEducation.bibliotheque.application.mapper.FicheMetierMapper;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheMetier;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheMetierService;
import tg.edtch.activEducation.bibliotheque.repository.FicheMetierRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FicheMetierServiceImpl implements FicheMetierService {

    private final FicheMetierRepository metierRepository;
    private final FicheMetierMapper metierMapper;

    @Override
    public FicheMetierResponse creerMetier(FicheMetierRequest request) {
        FicheMetier metier = metierMapper.toEntity(request);
        FicheMetier saved = metierRepository.save(metier);
        log.info("Fiche métier créée : trackingId={}", saved.getTrackingId());
        return metierMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FicheMetierResponse getMetier(UUID trackingId) {
        FicheMetier metier = findOrThrow(trackingId);
        metier.setNbConsultations(metier.getNbConsultations() + 1);
        return metierMapper.toResponse(metier);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheMetierResponse> listerTous(Pageable pageable) {
        return metierRepository.findAll(pageable)
                .map(metierMapper::toResponse);
    }

    @Override
    public FicheMetierResponse modifierMetier(UUID trackingId, FicheMetierRequest request) {
        FicheMetier metier = findOrThrow(trackingId);
        metierMapper.updateFromRequest(request, metier);
        FicheMetier saved = metierRepository.save(metier);
        log.info("Fiche métier modifiée : trackingId={}", trackingId);
        return metierMapper.toResponse(saved);
    }

    @Override
    public void supprimerMetier(UUID trackingId) {
        FicheMetier metier = findOrThrow(trackingId);
        metierRepository.delete(metier);
        log.info("Fiche métier supprimée : trackingId={}", trackingId);
    }

    private FicheMetier findOrThrow(UUID trackingId) {
        return metierRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Fiche métier introuvable pour le trackingId : " + trackingId));
    }
}
