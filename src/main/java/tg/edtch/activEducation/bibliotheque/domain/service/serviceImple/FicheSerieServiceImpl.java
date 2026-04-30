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
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheSerie;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheSerieService;
import tg.edtch.activEducation.bibliotheque.repository.FicheSerieRepository;
import tg.edtch.activEducation.shared.ai.service.GeminiEmbeddingService;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FicheSerieServiceImpl implements FicheSerieService {

    private final FicheSerieRepository serieRepository;
    private final FicheSerieMapper serieMapper;
    private final GeminiEmbeddingService geminiEmbeddingService;

    @Override
    public FicheSerieResponse creerSerie(FicheSerieRequest request) {
        FicheSerie serie = serieMapper.toEntity(request);
        // Génération de l'embedding sémantique pour la recherche globale
        try {
            String texte = (serie.getTitre() != null ? serie.getTitre() : "") + " "
                    + (serie.getResume() != null ? serie.getResume() : "") + " "
                    + (serie.getContenu() != null ? serie.getContenu() : "");
            serie.setEmbedding(geminiEmbeddingService.generateEmbedding(texte.trim()));
        } catch (Exception e) {
            log.warn("Impossible de générer l'embedding pour FicheSerie: {}", e.getMessage());
        }
        FicheSerie saved = serieRepository.save(serie);
        log.info("Fiche série créée : trackingId={}", saved.getTrackingId());
        return serieMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public FicheSerieResponse getSerie(UUID trackingId) {
        FicheSerie serie = findOrThrow(trackingId);
        serie.setNbConsultations(serie.getNbConsultations() + 1);
        return serieMapper.toResponse(serie);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheSerieResponse> listerPublies(Pageable pageable) {
        return serieRepository.findAllByEstPublieTrue(pageable)
                .map(serieMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheSerieResponse> listerNonPublies(Pageable pageable) {
        return serieRepository.findAllByEstPublieFalse(pageable)
                .map(serieMapper::toResponse);
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
        serieMapper.updateFromRequest(request, serie);
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

    @Override
    @Transactional(readOnly = true)
    public Page<FicheSerieResponse> rechercher(String motCle, Pageable pageable) {
        return serieRepository.rechercherParMotCle(motCle, pageable)
                .map(serieMapper::toResponse);
    }
}
