package tg.edtch.activEducation.bibliotheque.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheFiliereRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheFiliereResponse;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheFiliereService;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bibliotheque/filieres")
@RequiredArgsConstructor
@Tag(name = "Bibliothèque : Filières", description = "API de gestion des fiches de filières d'études")
public class FicheFiliereController {

    private final FicheFiliereService filiereService;

    /** Création sans fichiers (JSON simple) */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Créer une nouvelle fiche filière (sans médias)")
    public ResponseEntity<FicheFiliereResponse> creerJson(
            @Valid @RequestBody FicheFiliereRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(filiereService.creerFiliere(request, null, null, null));
    }

    /** Création avec fichiers (multipart) */
    @PostMapping(value = "/avec-medias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Créer une nouvelle fiche filière (avec médias)")
    public ResponseEntity<FicheFiliereResponse> creerAvecMedias(
            @Parameter(description = "Données JSON de la filière", required = true, schema = @Schema(implementation = FicheFiliereRequest.class)) @RequestPart("request") String requestJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) throws Exception {

        System.out
                .println("====== PAYLOAD JSON RECU (Filiere) ======\n" + requestJson + "\n==========================");
        FicheFiliereRequest request = new ObjectMapper().readValue(requestJson, FicheFiliereRequest.class);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(filiereService.creerFiliere(request, images, videos, documents));
    }

    @PutMapping(value = "/{trackingId}/medias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Remplacer les médias d'une fiche filière")
    public ResponseEntity<FicheFiliereResponse> remplacerMedias(
            @PathVariable UUID trackingId,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {
        return ResponseEntity.ok(filiereService.remplacerMedias(trackingId, images, videos, documents));
    }

    @PostMapping(value = "/{trackingId}/medias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Ajouter des médias à une fiche filière")
    public ResponseEntity<FicheFiliereResponse> ajouterMedias(
            @PathVariable UUID trackingId,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {
        return ResponseEntity.ok(filiereService.ajouterMedias(trackingId, images, videos, documents));
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Récupérer une fiche filière par son trackingId")
    public ResponseEntity<FicheFiliereResponse> get(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(filiereService.getFiliere(trackingId));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les fiches filières (paginé)")
    public ResponseEntity<Page<FicheFiliereResponse>> lister(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity
                .ok(filiereService.listerToutes(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier une fiche filière existante")
    public ResponseEntity<FicheFiliereResponse> modifier(
            @PathVariable UUID trackingId,
            @Valid @RequestBody FicheFiliereRequest request) {
        return ResponseEntity.ok(filiereService.modifierFiliere(trackingId, request));
    }

    @DeleteMapping("/{trackingId}")
    @Operation(summary = "Supprimer une fiche filière")
    public ResponseEntity<Void> supprimer(@PathVariable UUID trackingId) {
        filiereService.supprimerFiliere(trackingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recherche")
    @Operation(summary = "Rechercher des filières par mot-clé")
    public ResponseEntity<Page<FicheFiliereResponse>> rechercher(
            @RequestParam String motCle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(filiereService.rechercher(motCle,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/domaine/{domaine}")
    @Operation(summary = "Lister les filières par domaine")
    public ResponseEntity<Page<FicheFiliereResponse>> listerParDomaine(
            @PathVariable String domaine,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(filiereService.listerParDomaine(domaine,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/domaines")
    @Operation(summary = "Lister tous les domaines contenant des filières")
    public ResponseEntity<List<String>> getDomaines() {
        return ResponseEntity.ok(filiereService.obtenirTousLesDomaines());
    }
}
