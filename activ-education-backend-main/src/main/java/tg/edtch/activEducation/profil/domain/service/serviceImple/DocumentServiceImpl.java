package tg.edtch.activEducation.profil.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tg.edtch.activEducation.profil.application.dto.response.DocumentResponse;
import tg.edtch.activEducation.profil.domain.entite.Document;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.service.DocumentService;
import tg.edtch.activEducation.profil.repository.DocumentRepository;
import tg.edtch.activEducation.profil.repository.EleveRepository;

import java.util.Objects;
import tg.edtch.activEducation.shared.minio.dto.FileUploadResponse;
import tg.edtch.activEducation.shared.minio.enums.FileType;
import tg.edtch.activEducation.shared.minio.service.MinioService;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final EleveRepository eleveRepository;
    private final MinioService minioService;

    @Override
    public DocumentResponse uploadDocument(UUID eleveTrackingId, MultipartFile file, String typeDocument, String description, String dateDocument) {
        Eleve eleve = findEleveOrThrow(eleveTrackingId);

        FileUploadResponse upload = minioService.uploadFile(file, FileType.DOCUMENT);

        Document.TypeDocument type = Document.TypeDocument.AUTRE;
        if (typeDocument != null && !typeDocument.isBlank()) {
            try {
                type = Document.TypeDocument.valueOf(typeDocument.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Type de document invalide : {}, utilisation de AUTRE", typeDocument);
            }
        }

        LocalDate dateDoc = null;
        if (dateDocument != null && !dateDocument.isBlank()) {
            try {
                dateDoc = LocalDate.parse(dateDocument);
            } catch (Exception e) {
                log.warn("Date de document invalide : {}", dateDocument);
            }
        }

        Document document = Document.builder()
                .urlFichier(upload.getFileUrl())
                .nomFichier(file.getOriginalFilename())
                .typeDocument(type)
                .description(description)
                .dateDocument(dateDoc)
                .tailleFichier(file.getSize())
                .typeMime(file.getContentType())
                .eleve(eleve)
                .build();

        Document saved = documentRepository.save(document);
        log.info("Document uploadé : id={} nom={} eleve={}", saved.getId(), saved.getNomFichier(), eleveTrackingId);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> getDocuments(UUID eleveTrackingId, Pageable pageable) {
        Eleve eleve = findEleveOrThrow(eleveTrackingId);
        return documentRepository.findByEleveId(eleve.getId(), pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> getBulletins(UUID eleveTrackingId, Pageable pageable) {
        Objects.requireNonNull(eleveTrackingId, "eleveTrackingId must not be null");
        Eleve eleve = findEleveOrThrow(eleveTrackingId);
        return documentRepository.findByEleveIdAndTypeDocument(
                        eleve.getId(), Document.TypeDocument.BULLETIN, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocument(Long documentId) {
        return toResponse(documentRepository.findById(documentId)
                .orElseThrow(() -> new NoSuchElementException("Document introuvable pour l'id : " + documentId)));
    }

    @Override
    public void deleteDocument(UUID eleveTrackingId, Long documentId) {
        Eleve eleve = findEleveOrThrow(eleveTrackingId);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NoSuchElementException("Document introuvable pour l'id : " + documentId));

        if (!document.getEleve().getId().equals(eleve.getId())) {
            throw new SecurityException("Ce document ne vous appartient pas.");
        }

        String fileName = minioService.extractFileNameFromUrl(document.getUrlFichier());
        if (fileName != null && !fileName.isBlank()) {
            minioService.deleteFile(fileName, FileType.DOCUMENT);
        }

        documentRepository.delete(document);
        log.info("Document supprimé : id={} eleve={}", documentId, eleveTrackingId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countDocuments(UUID eleveTrackingId) {
        Eleve eleve = findEleveOrThrow(eleveTrackingId);
        return documentRepository.countByEleveId(eleve.getId());
    }

    private Eleve findEleveOrThrow(UUID trackingId) {
        return eleveRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException("Élève introuvable pour le trackingId : " + trackingId));
    }

    private DocumentResponse toResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .urlFichier(document.getUrlFichier())
                .nomFichier(document.getNomFichier())
                .typeDocument(document.getTypeDocument().name())
                .dateDocument(document.getDateDocument())
                .description(document.getDescription())
                .tailleFichier(document.getTailleFichier())
                .typeMime(document.getTypeMime())
                .createdAt(document.getCreatedAt())
                .build();
    }
}
