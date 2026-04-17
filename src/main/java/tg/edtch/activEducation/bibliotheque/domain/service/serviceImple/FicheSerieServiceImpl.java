package tg.edtch.activEducation.bibliotheque.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheSerieRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheSerieResponse;
import tg.edtch.activEducation.bibliotheque.application.mapper.FicheSerieMapper;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheSerie;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheSerieService;
import tg.edtch.activEducation.bibliotheque.repository.FicheFiliereRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheSerieRepository;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FicheSerieServiceImpl implements FicheSerieService {

    private final FicheSerieRepository serieRepository;
    private final FicheFiliereRepository filiereRepository;
    private final FicheSerieMapper serieMapper;

    @Override
    public FicheSerieResponse creerSerie(FicheSerieRequest request) {
        Set<FicheFiliere> filieres = resolveFilieres(request.getFilieresTrackingIds());
        FicheSerie serie = serieMapper.toEntity(request, filieres);
        FicheSerie saved = serieRepository.save(serie);
        log.info("Fiche série créée : trackingId={}", saved.getTrackingId());
        return serieMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FicheSerieResponse getSerie(UUID trackingId) {
        FicheSerie serie = findOrThrow(trackingId);
        serie.setNbConsultations(serie.getNbConsultations() + 1);
        return serieMapper.toResponse(serie);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheSerieResponse> listerToutes(Pageable pageable) {
        return serieRepository.findAll(pageable)
                .map(serieMapper::toResponse);
    }

    @Override
    public FicheSerieResponse modifierSerie(UUID trackingId, FicheSerieRequest request) {
        FicheSerie serie = findOrThrow(trackingId);
        Set<FicheFiliere> filieres = resolveFilieres(request.getFilieresTrackingIds());
        serieMapper.updateFromRequest(request, serie, filieres);
        FicheSerie saved = serieRepository.save(serie);
        log.info("Fiche série modifiée : trackingId={}", trackingId);
        return serieMapper.toResponse(saved);
    }

    @Override
    public void supprimerSerie(UUID trackingId) {
        FicheSerie serie = findOrThrow(trackingId);
        serieRepository.delete(serie);
        log.info("Fiche série supprimée : trackingId={}", trackingId);
    }

    private FicheSerie findOrThrow(UUID trackingId) {
        return serieRepository.findByTrackingId(trackingId)
                .orElseThrow(
                        () -> new NoSuchElementException("Fiche série introuvable pour le trackingId : " + trackingId));
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
