package tg.edtch.activEducation.profil.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import tg.edtch.activEducation.profil.application.dto.response.DocumentResponse;

import java.util.UUID;

public interface DocumentService {

    DocumentResponse uploadDocument(UUID eleveTrackingId, MultipartFile file, String typeDocument, String description, String dateDocument);

    Page<DocumentResponse> getDocuments(UUID eleveTrackingId, Pageable pageable);

    Page<DocumentResponse> getBulletins(UUID eleveTrackingId, Pageable pageable);

    DocumentResponse getDocument(Long documentId);

    void deleteDocument(UUID eleveTrackingId, Long documentId);

    long countDocuments(UUID eleveTrackingId);
}
