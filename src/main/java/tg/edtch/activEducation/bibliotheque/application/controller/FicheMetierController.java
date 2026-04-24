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
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheMetierRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheMetierResponse;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheMetierService;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bibliotheque/metiers")
@RequiredArgsConstructor
@Tag(name = "Bibliothèque : Métiers", description = "API de gestion des fiches de métiers")
public class FicheMetierController {

    private final FicheMetierService metierService;

    /** Création sans fichiers (JSON simple) */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Créer une nouvelle fiche métier (sans médias)")
    public ResponseEntity<FicheMetierResponse> creerJson(
            @Valid @RequestBody FicheMetierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(metierService.creerMetier(request, null, null, null));
    }

    /** Création avec fichiers (multipart) */
    @PostMapping(value = "/avec-medias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Créer une nouvelle fiche métier (avec médias)")
    public ResponseEntity<FicheMetierResponse> creerAvecMedias(
            @Parameter(description = "Données JSON de la fiche métier", required = true, schema = @Schema(implementation = FicheMetierRequest.class)) @RequestPart("request") String requestJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) throws Exception {

        System.out.println("====== PAYLOAD JSON RECU (Metier) ======\n" + requestJson + "\n==========================");
        FicheMetierRequest request = new ObjectMapper().readValue(requestJson, FicheMetierRequest.class);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(metierService.creerMetier(request, images, videos, documents));
    }

    @PutMapping(value = "/{trackingId}/medias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Remplacer les médias d'une fiche métier")
    public ResponseEntity<FicheMetierResponse> remplacerMedias(
            @PathVariable UUID trackingId,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {
        return ResponseEntity.ok(metierService.remplacerMedias(trackingId, images, videos, documents));
    }

    @PostMapping(value = "/{trackingId}/medias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Ajouter des médias à une fiche métier")
    public ResponseEntity<FicheMetierResponse> ajouterMedias(
            @PathVariable UUID trackingId,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {
        return ResponseEntity.ok(metierService.ajouterMedias(trackingId, images, videos, documents));
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Récupérer une fiche métier par son trackingId")
    public ResponseEntity<FicheMetierResponse> get(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(metierService.getMetier(trackingId));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les fiches métiers (paginé)")
    public ResponseEntity<Page<FicheMetierResponse>> lister(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity
                .ok(metierService.listerTous(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier une fiche métier existante")
    public ResponseEntity<FicheMetierResponse> modifier(
            @PathVariable UUID trackingId,
            @Valid @RequestBody FicheMetierRequest request) {
        return ResponseEntity.ok(metierService.modifierMetier(trackingId, request));
    }

    @DeleteMapping("/{trackingId}")
    @Operation(summary = "Supprimer une fiche métier")
    public ResponseEntity<Void> supprimer(@PathVariable UUID trackingId) {
        metierService.supprimerMetier(trackingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recherche")
    @Operation(summary = "Rechercher des métiers par mot-clé")
    public ResponseEntity<Page<FicheMetierResponse>> rechercher(
            @RequestParam String motCle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(metierService.rechercher(motCle,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/secteur/{secteur}")
    @Operation(summary = "Lister les métiers par secteur")
    public ResponseEntity<Page<FicheMetierResponse>> listerParSecteur(
            @PathVariable String secteur,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(metierService.listerParSecteur(secteur,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/secteurs")
    @Operation(summary = "Lister tous les secteurs contenant des métiers")
    public ResponseEntity<List<String>> getSecteurs() {
        return ResponseEntity.ok(metierService.obtenirTousLesSecteurs());
    }
}
