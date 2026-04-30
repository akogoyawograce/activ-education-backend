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
import tg.edtch.activEducation.shared.minio.service.MinioService;
import tg.edtch.activEducation.shared.minio.enums.FileType;
import tg.edtch.activEducation.shared.minio.dto.FileUploadResponse;
import tg.edtch.activEducation.shared.ai.service.GeminiEmbeddingService;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FicheEtablissementServiceImpl implements FicheEtablissementService {

    private final FicheEtablissementRepository etablissementRepository;
    private final FicheFiliereRepository filiereRepository;
    private final FicheEtablissementMapper etablissementMapper;
    private final MinioService minioService;
    private final GeminiEmbeddingService geminiEmbeddingService;

    @Override
    public FicheEtablissementResponse creerEtablissement(FicheEtablissementRequest request, List<MultipartFile> images,
            List<MultipartFile> videos, List<MultipartFile> documents) {
        Set<FicheFiliere> filieres = resolveFilieres(request.getFilieresTrackingIds());
        FicheEtablissement etablissement = etablissementMapper.toEntity(request, filieres);
        handleUpload(etablissement, images, videos, documents);
        // Génération de l'embedding sémantique pour la recherche globale
        try {
            String texte = (etablissement.getTitre() != null ? etablissement.getTitre() : "") + " "
                    + (etablissement.getResume() != null ? etablissement.getResume() : "") + " "
                    + (etablissement.getContenu() != null ? etablissement.getContenu() : "");
            etablissement.setEmbedding(geminiEmbeddingService.generateEmbedding(texte.trim()));
        } catch (Exception e) {
            log.warn("Impossible de générer l'embedding pour FicheEtablissement: {}", e.getMessage());
        }
        FicheEtablissement saved = etablissementRepository.save(etablissement);
        log.info("Fiche établissement créée : trackingId={}", saved.getTrackingId());
        return etablissementMapper.toResponse(saved);
    }

    @Override
    public FicheEtablissementResponse remplacerMedias(UUID trackingId, List<MultipartFile> images,
            List<MultipartFile> videos, List<MultipartFile> documents) {
        FicheEtablissement etablissement = findOrThrow(trackingId);
        deleteOldMedias(etablissement);
        handleUpload(etablissement, images, videos, documents);
        FicheEtablissement saved = etablissementRepository.save(etablissement);
        log.info("Medias remplacés pour établissement : trackingId={}", trackingId);
        return etablissementMapper.toResponse(saved);
    }

    @Override
    public FicheEtablissementResponse ajouterMedias(UUID trackingId, List<MultipartFile> images,
            List<MultipartFile> videos, List<MultipartFile> documents) {
        FicheEtablissement etablissement = findOrThrow(trackingId);
        handleUpload(etablissement, images, videos, documents);
        FicheEtablissement saved = etablissementRepository.save(etablissement);
        log.info("Medias ajoutés pour établissement : trackingId={}", trackingId);
        return etablissementMapper.toResponse(saved);
    }

    @Override
    @Transactional
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
    @Transactional(readOnly = true)
    public Page<FicheEtablissementResponse> listerPublies(Pageable pageable) {
        return etablissementRepository.findAllByEstPublieTrue(pageable)
                .map(etablissementMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheEtablissementResponse> listerNonPublies(Pageable pageable) {
        return etablissementRepository.findAllByEstPublieFalse(pageable)
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
        deleteOldMedias(etablissement);
        etablissementRepository.delete(etablissement);
        log.info("Fiche établissement supprimée : trackingId={}", trackingId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheEtablissementResponse> listerParVille(String ville, Pageable pageable) {
        return etablissementRepository.findByVilleIgnoreCaseAndEstPublieTrue(ville, pageable)
                .map(etablissementMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheEtablissementResponse> listerParType(String type, Pageable pageable) {
        return etablissementRepository.findByTypeEtablissementAndEstPublieTrue(parseTypeEtablissement(type), pageable)
                .map(etablissementMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> obtenirToutesLesVilles() {
        return etablissementRepository.findAllVilles();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheEtablissementResponse> rechercher(String motCle, Pageable pageable) {
        return etablissementRepository.rechercherParTerme(motCle, pageable)
                .map(etablissementMapper::toResponse);
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

    private void handleUpload(FicheEtablissement etablissement, List<MultipartFile> images, List<MultipartFile> videos,
            List<MultipartFile> documents) {
        if (images != null && !images.isEmpty()) {
            etablissement.getImageUrls().addAll(minioService.uploadMultipleFiles(images, FileType.IMAGE).stream()
                    .map(FileUploadResponse::getFileUrl).collect(Collectors.toSet()));
        }
        if (videos != null && !videos.isEmpty()) {
            etablissement.getVideoUrls().addAll(minioService.uploadMultipleFiles(videos, FileType.VIDEO).stream()
                    .map(FileUploadResponse::getFileUrl).collect(Collectors.toSet()));
        }
        if (documents != null && !documents.isEmpty()) {
            etablissement.getDocumentUrls()
                    .addAll(minioService.uploadMultipleFiles(documents, FileType.DOCUMENT).stream()
                            .map(FileUploadResponse::getFileUrl).collect(Collectors.toSet()));
        }
    }

    private void deleteOldMedias(FicheEtablissement etablissement) {
        if (etablissement.getImageUrls() != null) {
            etablissement.getImageUrls()
                    .forEach(url -> minioService.deleteFile(minioService.extractFileNameFromUrl(url), FileType.IMAGE));
            etablissement.getImageUrls().clear();
        }
        if (etablissement.getVideoUrls() != null) {
            etablissement.getVideoUrls()
                    .forEach(url -> minioService.deleteFile(minioService.extractFileNameFromUrl(url), FileType.VIDEO));
            etablissement.getVideoUrls().clear();
        }
        if (etablissement.getDocumentUrls() != null) {
            etablissement.getDocumentUrls().forEach(
                    url -> minioService.deleteFile(minioService.extractFileNameFromUrl(url), FileType.DOCUMENT));
            etablissement.getDocumentUrls().clear();
        }
    }

    private FicheEtablissement.TypeEtablissement parseTypeEtablissement(String rawType) {
        String normalized = rawType == null ? ""
                : rawType.trim()
                        .replace("-", "_")
                        .replace(" ", "_")
                        .toUpperCase(Locale.ROOT);
        try {
            return FicheEtablissement.TypeEtablissement.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Type d'etablissement invalide: " + rawType + ". Valeurs possibles: "
                            + java.util.Arrays.toString(FicheEtablissement.TypeEtablissement.values()));
        }
    }
}
