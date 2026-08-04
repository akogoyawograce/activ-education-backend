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
import tg.edtch.activEducation.bibliotheque.domain.entite.NiveauFiliere;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheFiliereService;
import tg.edtch.activEducation.bibliotheque.repository.FicheFiliereRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheSerieRepository;
import tg.edtch.activEducation.bibliotheque.domain.repository.NiveauFiliereRepository;
import tg.edtch.activEducation.profil.domain.enums.NiveauScolaire;
import tg.edtch.activEducation.shared.minio.service.MinioService;
import tg.edtch.activEducation.shared.minio.enums.FileType;
import tg.edtch.activEducation.profil.domain.service.HistoriqueService;
import tg.edtch.activEducation.bibliotheque.domain.service.RechercheOrphelineService;
import tg.edtch.activEducation.profil.application.dto.request.HistoriqueRequest;
import tg.edtch.activEducation.shared.minio.dto.FileUploadResponse;
import tg.edtch.activEducation.shared.ai.service.AIEmbeddingService;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

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
    private final NiveauFiliereRepository niveauFiliereRepository;
    private final FicheFiliereMapper filiereMapper;
    private final MinioService minioService;
    private final AIEmbeddingService aiEmbeddingService;
    private final HistoriqueService historiqueService;
    private final RechercheOrphelineService orphelineService;

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
            filiere.setEmbedding(aiEmbeddingService.generateEmbedding(texte.trim()));
        } catch (Exception e) {
            log.warn("Impossible de générer l'embedding pour FicheFiliere: {}", e.getMessage());
        }
        FicheFiliere saved = filiereRepository.save(filiere);
        sauvegarderNiveaux(saved, request.getNiveaux());
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
        if (request.getNiveaux() != null) {
            niveauFiliereRepository.findByFicheFiliereId(saved.getId())
                    .forEach(nf -> niveauFiliereRepository.delete(nf));
            sauvegarderNiveaux(saved, request.getNiveaux());
        }
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
        Page<FicheFiliereResponse> resultat = filiereRepository.rechercherParTerme(motCle, pageable)
                .map(filiereMapper::toResponse);
        if (resultat.isEmpty() && motCle != null && !motCle.trim().isEmpty()) {
            orphelineService.signaler(motCle, "FILIERE");
        }
        return resultat;
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

    private void sauvegarderNiveaux(FicheFiliere filiere, List<String> codesNiveaux) {
        if (codesNiveaux == null || codesNiveaux.isEmpty()) {
            return;
        }
        List<NiveauFiliere> entites = codesNiveaux.stream()
                .map(code -> {
                    NiveauScolaire ns = NiveauScolaire.parse(code);
                    if (ns == null) {
                        log.warn("Niveau non reconnu '{}' ignoré pour la filière {}", code, filiere.getTrackingId());
                        return null;
                    }
                    return NiveauFiliere.builder()
                            .ficheFiliere(filiere)
                            .niveau(ns)
                            .estPrincipal(ns == NiveauScolaire.BAC_1)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!entites.isEmpty()) {
            niveauFiliereRepository.saveAll(entites);
            log.info("{} niveaux_filieres créés pour {}", entites.size(), filiere.getTrackingId());
        }
    }

    private FicheFiliere findOrThrow(UUID trackingId) {
        return filiereRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Fiche filière introuvable pour le trackingId : " + trackingId));
    }

    private Set<FicheSerie> resolveSeries(Set<UUID> trackingIds) {
        if (trackingIds == null || trackingIds.isEmpty())
            return new HashSet<>();
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
