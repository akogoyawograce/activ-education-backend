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
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheSerie;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheFiliereService;
import tg.edtch.activEducation.bibliotheque.repository.FicheFiliereRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheSerieRepository;
import tg.edtch.activEducation.shared.minio.service.MinioService;
import tg.edtch.activEducation.shared.minio.enums.FileType;
import tg.edtch.activEducation.profil.domain.service.HistoriqueService;
import tg.edtch.activEducation.profil.application.dto.request.HistoriqueRequest;
import tg.edtch.activEducation.shared.minio.dto.FileUploadResponse;
import tg.edtch.activEducation.shared.ai.service.GeminiEmbeddingService;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

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
    private final FicheSerieRepository serieRepository;
    private final FicheFiliereMapper filiereMapper;
    private final MinioService minioService;
    private final GeminiEmbeddingService geminiEmbeddingService;
    private final HistoriqueService historiqueService;

    @Override
    public FicheFiliereResponse creerFiliere(FicheFiliereRequest request, List<MultipartFile> images,
            List<MultipartFile> videos, List<MultipartFile> documents) {
        Set<FicheSerie> series = resolveSeries(request.getSeriesTrackingIds());
        FicheFiliere filiere = filiereMapper.toEntity(request, series);
        handleUpload(filiere, images, videos, documents);
        // Génération de l'embedding sémantique pour la recherche globale
        try {
            String texte = (filiere.getTitre() != null ? filiere.getTitre() : "") + " "
                    + (filiere.getResume() != null ? filiere.getResume() : "") + " "
                    + (filiere.getContenu() != null ? filiere.getContenu() : "");
            filiere.setEmbedding(geminiEmbeddingService.generateEmbedding(texte.trim()));
        } catch (Exception e) {
            log.warn("Impossible de générer l'embedding pour FicheFiliere: {}", e.getMessage());
        }
        FicheFiliere saved = filiereRepository.save(filiere);
        log.info("Fiche filière créée : trackingId={}", saved.getTrackingId());
        return filiereMapper.toResponse(saved);
    }

    @Override
    public FicheFiliereResponse remplacerMedias(UUID trackingId, List<MultipartFile> images, List<MultipartFile> videos,
            List<MultipartFile> documents) {
        FicheFiliere filiere = findOrThrow(trackingId);
        deleteOldMedias(filiere);
        handleUpload(filiere, images, videos, documents);
        FicheFiliere saved = filiereRepository.save(filiere);
        log.info("Medias remplacés pour filière : trackingId={}", trackingId);
        return filiereMapper.toResponse(saved);
    }

    @Override
    public FicheFiliereResponse ajouterMedias(UUID trackingId, List<MultipartFile> images, List<MultipartFile> videos,
            List<MultipartFile> documents) {
        FicheFiliere filiere = findOrThrow(trackingId);
        handleUpload(filiere, images, videos, documents);
        FicheFiliere saved = filiereRepository.save(filiere);
        log.info("Medias ajoutés pour filière : trackingId={}", trackingId);
        return filiereMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public FicheFiliereResponse getFiliere(UUID trackingId, UUID utilisateurTrackingId) {
        FicheFiliere filiere = findOrThrow(trackingId);
        filiere.setNbConsultations(filiere.getNbConsultations() + 1);
        if (utilisateurTrackingId != null) {
            HistoriqueRequest req = new HistoriqueRequest();
            req.setAction("CONSULTATION_FICHE");
            req.setDetails(trackingId.toString());
            historiqueService.enregistrer(utilisateurTrackingId, req);
        }
        return filiereMapper.toResponse(filiere);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheFiliereResponse> listerPublies(Pageable pageable) {
        return filiereRepository.findAllByEstPublieTrue(pageable)
                .map(filiereMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheFiliereResponse> listerNonPublies(Pageable pageable) {
        return filiereRepository.findAllByEstPublieFalse(pageable)
                .map(filiereMapper::toResponse);
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
        Set<FicheSerie> series = resolveSeries(request.getSeriesTrackingIds());
        filiereMapper.updateFromRequest(request, filiere, series);
        FicheFiliere saved = filiereRepository.save(filiere);
        log.info("Fiche filière modifiée : trackingId={}", trackingId);
        return filiereMapper.toResponse(saved);
    }

    @Override
    public void supprimerFiliere(UUID trackingId) {
        FicheFiliere filiere = findOrThrow(trackingId);
        deleteOldMedias(filiere);
        filiereRepository.delete(filiere);
        log.info("Fiche filière supprimée : trackingId={}", trackingId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheFiliereResponse> rechercher(String motCle, Pageable pageable) {
        return filiereRepository.rechercherParTerme(motCle, pageable)
                .map(filiereMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheFiliereResponse> listerParDomaine(String domaine, Pageable pageable) {
        return filiereRepository.findByDomaineIgnoreCaseAndEstPublieTrue(domaine, pageable)
                .map(filiereMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> obtenirTousLesDomaines() {
        return filiereRepository.findAllDomaines();
    }

    private FicheFiliere findOrThrow(UUID trackingId) {
        return filiereRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Fiche filière introuvable pour le trackingId : " + trackingId));
    }

    private Set<FicheSerie> resolveSeries(Set<UUID> trackingIds) {
        if (trackingIds == null || trackingIds.isEmpty())
            return Set.of();
        return trackingIds.stream()
                .map(tid -> serieRepository.findByTrackingId(tid)
                        .orElseThrow(() -> new NoSuchElementException("Série introuvable : " + tid)))
                .collect(Collectors.toSet());
    }

    private void handleUpload(FicheFiliere filiere, List<MultipartFile> images, List<MultipartFile> videos,
            List<MultipartFile> documents) {
        if (images != null && !images.isEmpty()) {
            filiere.getImageUrls().addAll(minioService.uploadMultipleFiles(images, FileType.IMAGE).stream()
                    .map(FileUploadResponse::getFileUrl).collect(Collectors.toSet()));
        }
        if (videos != null && !videos.isEmpty()) {
            filiere.getVideoUrls().addAll(minioService.uploadMultipleFiles(videos, FileType.VIDEO).stream()
                    .map(FileUploadResponse::getFileUrl).collect(Collectors.toSet()));
        }
        if (documents != null && !documents.isEmpty()) {
            filiere.getDocumentUrls().addAll(minioService.uploadMultipleFiles(documents, FileType.DOCUMENT).stream()
                    .map(FileUploadResponse::getFileUrl).collect(Collectors.toSet()));
        }
    }

    private void deleteOldMedias(FicheFiliere filiere) {
        if (filiere.getImageUrls() != null) {
            filiere.getImageUrls()
                    .forEach(url -> minioService.deleteFile(minioService.extractFileNameFromUrl(url), FileType.IMAGE));
            filiere.getImageUrls().clear();
        }
        if (filiere.getVideoUrls() != null) {
            filiere.getVideoUrls()
                    .forEach(url -> minioService.deleteFile(minioService.extractFileNameFromUrl(url), FileType.VIDEO));
            filiere.getVideoUrls().clear();
        }
        if (filiere.getDocumentUrls() != null) {
            filiere.getDocumentUrls().forEach(
                    url -> minioService.deleteFile(minioService.extractFileNameFromUrl(url), FileType.DOCUMENT));
            filiere.getDocumentUrls().clear();
        }
    }
}
