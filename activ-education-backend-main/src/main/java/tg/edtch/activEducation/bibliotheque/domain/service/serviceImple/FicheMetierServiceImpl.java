package tg.edtch.activEducation.bibliotheque.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheMetierRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheMetierResponse;
import tg.edtch.activEducation.bibliotheque.application.mapper.FicheMetierMapper;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheMetier;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheMetierService;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheMetierService;
import tg.edtch.activEducation.bibliotheque.repository.FicheMetierRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheFiliereRepository;
import tg.edtch.activEducation.shared.minio.service.MinioService;
import tg.edtch.activEducation.shared.minio.enums.FileType;
import tg.edtch.activEducation.profil.domain.service.HistoriqueService;
import tg.edtch.activEducation.bibliotheque.domain.service.RechercheOrphelineService;
import tg.edtch.activEducation.profil.application.dto.request.HistoriqueRequest;
import tg.edtch.activEducation.shared.minio.dto.FileUploadResponse;
import tg.edtch.activEducation.shared.ai.service.GeminiEmbeddingService;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FicheMetierServiceImpl implements FicheMetierService {

    private final FicheMetierRepository metierRepository;
    private final FicheFiliereRepository filiereRepository;
    private final FicheMetierMapper metierMapper;
    private final MinioService minioService;
    private final GeminiEmbeddingService geminiEmbeddingService;
    private final HistoriqueService historiqueService;
    private final RechercheOrphelineService orphelineService;

    @Override
    public FicheMetierResponse creerMetier(FicheMetierRequest request, List<MultipartFile> images,
            List<MultipartFile> videos, List<MultipartFile> documents) {
        Set<FicheFiliere> filieres = resolveFilieres(request.getFilieresTrackingIds());
        FicheMetier metier = metierMapper.toEntity(request, filieres);
        handleUpload(metier, images, videos, documents);
        // Génération de l'embedding sémantique pour la recherche globale
        try {
            String texte = (metier.getTitre() != null ? metier.getTitre() : "") + " "
                    + (metier.getResume() != null ? metier.getResume() : "") + " "
                    + (metier.getContenu() != null ? metier.getContenu() : "");
            metier.setEmbedding(geminiEmbeddingService.generateEmbedding(texte.trim()));
        } catch (Exception e) {
            log.warn("Impossible de générer l'embedding pour FicheMetier: {}", e.getMessage());
        }
        FicheMetier saved = metierRepository.save(metier);
        log.info("Fiche métier créée : trackingId={}", saved.getTrackingId());
        return metierMapper.toResponse(saved);
    }

    @Override
    public FicheMetierResponse remplacerMedias(UUID trackingId, List<MultipartFile> images, List<MultipartFile> videos,
            List<MultipartFile> documents) {
        FicheMetier metier = findOrThrow(trackingId);
        deleteOldMedias(metier);
        handleUpload(metier, images, videos, documents);
        FicheMetier saved = metierRepository.save(metier);
        log.info("Medias remplacés pour métier : trackingId={}", trackingId);
        return metierMapper.toResponse(saved);
    }

    @Override
    public FicheMetierResponse ajouterMedias(UUID trackingId, List<MultipartFile> images, List<MultipartFile> videos,
            List<MultipartFile> documents) {
        FicheMetier metier = findOrThrow(trackingId);
        handleUpload(metier, images, videos, documents);
        FicheMetier saved = metierRepository.save(metier);
        log.info("Medias ajoutés pour métier : trackingId={}", trackingId);
        return metierMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public FicheMetierResponse getMetier(UUID trackingId, UUID utilisateurTrackingId) {
        FicheMetier metier = findOrThrow(trackingId);
        metier.setNbConsultations(metier.getNbConsultations() + 1);
        if (utilisateurTrackingId != null) {
            HistoriqueRequest req = new HistoriqueRequest();
            req.setAction("CONSULTATION_FICHE");
            req.setDetails(trackingId.toString());
            historiqueService.enregistrer(utilisateurTrackingId, req);
        }
        return metierMapper.toResponse(metier);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheMetierResponse> listerTous(Pageable pageable) {
        return metierRepository.findAll(pageable)
                .map(metierMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheMetierResponse> listerPublies(Pageable pageable) {
        return metierRepository.findAllByEstPublieTrue(pageable)
                .map(metierMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheMetierResponse> listerNonPublies(Pageable pageable) {
        return metierRepository.findAllByEstPublieFalse(pageable)
                .map(metierMapper::toResponse);
    }

    @Override
    public FicheMetierResponse modifierMetier(UUID trackingId, FicheMetierRequest request) {
        FicheMetier metier = findOrThrow(trackingId);
        Set<FicheFiliere> filieres = resolveFilieres(request.getFilieresTrackingIds());
        metierMapper.updateFromRequest(request, metier, filieres);
        FicheMetier saved = metierRepository.save(metier);
        log.info("Fiche métier modifiée : trackingId={}", trackingId);
        return metierMapper.toResponse(saved);
    }

    @Override
    public void supprimerMetier(UUID trackingId) {
        FicheMetier metier = findOrThrow(trackingId);
        deleteOldMedias(metier);
        metierRepository.delete(metier);
        log.info("Fiche métier supprimée : trackingId={}", trackingId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheMetierResponse> rechercher(String motCle, Pageable pageable) {
        Page<FicheMetierResponse> resultat = metierRepository.rechercherParTerme(motCle, pageable)
                .map(metierMapper::toResponse);
        if (resultat.isEmpty() && motCle != null && !motCle.trim().isEmpty()) {
            orphelineService.signaler(motCle, "METIER");
        }
        return resultat;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheMetierResponse> listerParSecteur(String secteur, Pageable pageable) {
        return metierRepository.findBySecteurIgnoreCaseAndEstPublieTrue(secteur, pageable)
                .map(metierMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> obtenirTousLesSecteurs() {
        return metierRepository.findAllSecteurs();
    }

    private FicheMetier findOrThrow(UUID trackingId) {
        return metierRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Fiche métier introuvable pour le trackingId : " + trackingId));
    }

    private Set<FicheFiliere> resolveFilieres(Set<UUID> trackingIds) {
        if (trackingIds == null || trackingIds.isEmpty())
            return Set.of();
        return trackingIds.stream()
                .map(tid -> filiereRepository.findByTrackingId(tid)
                        .orElseThrow(() -> new NoSuchElementException("Filière introuvable : " + tid)))
                .collect(Collectors.toSet());
    }

    private void handleUpload(FicheMetier metier, List<MultipartFile> images, List<MultipartFile> videos,
            List<MultipartFile> documents) {
        if (images != null && !images.isEmpty()) {
            metier.getImageUrls().addAll(minioService.uploadMultipleFiles(images, FileType.IMAGE).stream()
                    .map(FileUploadResponse::getFileUrl).collect(Collectors.toSet()));
        }
        if (videos != null && !videos.isEmpty()) {
            metier.getVideoUrls().addAll(minioService.uploadMultipleFiles(videos, FileType.VIDEO).stream()
                    .map(FileUploadResponse::getFileUrl).collect(Collectors.toSet()));
        }
        if (documents != null && !documents.isEmpty()) {
            metier.getDocumentUrls().addAll(minioService.uploadMultipleFiles(documents, FileType.DOCUMENT).stream()
                    .map(FileUploadResponse::getFileUrl).collect(Collectors.toSet()));
        }
    }

    private void deleteOldMedias(FicheMetier metier) {
        if (metier.getImageUrls() != null) {
            metier.getImageUrls()
                    .forEach(url -> minioService.deleteFile(minioService.extractFileNameFromUrl(url), FileType.IMAGE));
            metier.getImageUrls().clear();
        }
        if (metier.getVideoUrls() != null) {
            metier.getVideoUrls()
                    .forEach(url -> minioService.deleteFile(minioService.extractFileNameFromUrl(url), FileType.VIDEO));
            metier.getVideoUrls().clear();
        }
        if (metier.getDocumentUrls() != null) {
            metier.getDocumentUrls().forEach(
                    url -> minioService.deleteFile(minioService.extractFileNameFromUrl(url), FileType.DOCUMENT));
            metier.getDocumentUrls().clear();
        }
    }
}
