package tg.edtch.activEducation.profil.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tg.edtch.activEducation.profil.application.dto.response.DocumentResponse;
import tg.edtch.activEducation.profil.domain.service.DocumentService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/eleves/{trackingId}/documents")
@RequiredArgsConstructor
@Tag(name = "Documents Élève", description = "API de gestion des documents d'un élève (bulletins, attestations, etc.)")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@security.isOwner(#trackingId) or hasRole('ADMIN')")
    @Operation(summary = "Uploader un document", description = "Upload un fichier et l'associe comme document de l'élève.")
    @ApiResponse(responseCode = "201", description = "Document uploadé avec succès")
    @ApiResponse(responseCode = "400", description = "Fichier invalide ou type de document incorrect")
    @ApiResponse(responseCode = "404", description = "Élève introuvable")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @Parameter(description = "Identifiant public (UUID) de l'élève") @PathVariable UUID trackingId,
            @Parameter(description = "Fichier à uploader (PDF, DOC, XLS, etc.)") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Type de document (BULLETIN, ATTESTATION, RELEVE_NOTES, CERTIFICAT_SCOLARITE, AUTRE)") @RequestParam("typeDocument") String typeDocument,
            @Parameter(description = "Description optionnelle") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Date du document (format ISO : yyyy-MM-dd)") @RequestParam(value = "dateDocument", required = false) String dateDocument) {
        DocumentResponse response = documentService.uploadDocument(trackingId, file, typeDocument, description, dateDocument);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("@security.isOwner(#trackingId) or hasRole('ADMIN')")
    @Operation(summary = "Lister les documents d'un élève", description = "Retourne une page de documents paginée et triée par date de création décroissante.")
    @ApiResponse(responseCode = "200", description = "Liste des documents")
    public ResponseEntity<Page<DocumentResponse>> getDocuments(
            @Parameter(description = "Identifiant public (UUID) de l'élève") @PathVariable UUID trackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DocumentResponse> result = documentService.getDocuments(trackingId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/count")
    @PreAuthorize("@security.isOwner(#trackingId) or hasRole('ADMIN')")
    @Operation(summary = "Compter les documents d'un élève", description = "Retourne le nombre total de documents.")
    public ResponseEntity<Long> countDocuments(
            @Parameter(description = "Identifiant public (UUID) de l'élève") @PathVariable UUID trackingId) {
        return ResponseEntity.ok(documentService.countDocuments(trackingId));
    }

    @GetMapping("/{documentId}")
    @PreAuthorize("@security.isOwner(#trackingId) or hasRole('ADMIN')")
    @Operation(summary = "Récupérer un document par son ID", description = "Retourne les détails d'un document spécifique.")
    @ApiResponse(responseCode = "200", description = "Document trouvé")
    @ApiResponse(responseCode = "404", description = "Document introuvable")
    public ResponseEntity<DocumentResponse> getDocument(
            @Parameter(description = "Identifiant public (UUID) de l'élève") @PathVariable UUID trackingId,
            @Parameter(description = "ID du document") @PathVariable Long documentId) {
        return ResponseEntity.ok(documentService.getDocument(documentId));
    }

    @DeleteMapping("/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@security.isOwner(#trackingId) or hasRole('ADMIN')")
    @Operation(summary = "Supprimer un document", description = "Supprime le fichier du stockage objet et l'enregistrement en base.")
    @ApiResponse(responseCode = "204", description = "Document supprimé")
    @ApiResponse(responseCode = "404", description = "Document introuvable")
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "Identifiant public (UUID) de l'élève") @PathVariable UUID trackingId,
            @Parameter(description = "ID du document") @PathVariable Long documentId) {
        documentService.deleteDocument(trackingId, documentId);
        return ResponseEntity.noContent().build();
    }
}
