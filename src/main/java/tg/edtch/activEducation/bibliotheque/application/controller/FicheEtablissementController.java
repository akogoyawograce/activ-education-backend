package tg.edtch.activEducation.bibliotheque.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheEtablissementRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheEtablissementResponse;
import tg.edtch.activEducation.bibliotheque.domain.service.FicheEtablissementService;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bibliotheque/etablissements")
@RequiredArgsConstructor
@Tag(name = "Bibliothèque : Établissements", description = "API de gestion des fiches d'établissements")
public class FicheEtablissementController {

    private final FicheEtablissementService etablissementService;

    /** Création sans fichiers (JSON simple) */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Créer une nouvelle fiche établissement (sans médias)")
    public ResponseEntity<FicheEtablissementResponse> creerJson(
            @Valid @RequestBody FicheEtablissementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(etablissementService.creerEtablissement(request, null, null, null));
    }

    /** Création avec fichiers (multipart) */
    @PostMapping(value = "/avec-medias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Créer une nouvelle fiche établissement (avec médias)")
    public ResponseEntity<FicheEtablissementResponse> creerAvecMedias(
            @Parameter(description = "Données JSON de l'établissement", required = true, schema = @Schema(implementation = FicheEtablissementRequest.class)) @RequestPart("request") String requestJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) throws Exception {

        System.out.println(
                "====== PAYLOAD JSON RECU (Etablissement) ======\n" + requestJson + "\n==========================");
        FicheEtablissementRequest request = new ObjectMapper().readValue(requestJson, FicheEtablissementRequest.class);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(etablissementService.creerEtablissement(request, images, videos, documents));
    }

    @PutMapping(value = "/{trackingId}/medias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Remplacer les médias d'une fiche établissement")
    public ResponseEntity<FicheEtablissementResponse> remplacerMedias(
            @PathVariable UUID trackingId,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {
        return ResponseEntity.ok(etablissementService.remplacerMedias(trackingId, images, videos, documents));
    }

    @PostMapping(value = "/{trackingId}/medias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Ajouter des médias à une fiche établissement")
    public ResponseEntity<FicheEtablissementResponse> ajouterMedias(
            @PathVariable UUID trackingId,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {
        return ResponseEntity.ok(etablissementService.ajouterMedias(trackingId, images, videos, documents));
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Récupérer une fiche établissement par son trackingId")
    public ResponseEntity<FicheEtablissementResponse> get(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(etablissementService.getEtablissement(trackingId));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les fiches établissements (paginé)")
    public ResponseEntity<Page<FicheEtablissementResponse>> lister(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                etablissementService.listerTous(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/public")
    @Operation(summary = "Lister les fiches établissements publiques (paginé)")
    public ResponseEntity<Page<FicheEtablissementResponse>> listerPublies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(etablissementService.listerPublies(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/non-public")
    @Operation(summary = "Lister les fiches établissements non publiques (paginé)")
    public ResponseEntity<Page<FicheEtablissementResponse>> listerNonPublies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(etablissementService.listerNonPublies(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier une fiche établissement existante")
    public ResponseEntity<FicheEtablissementResponse> modifier(
            @PathVariable UUID trackingId,
            @Valid @RequestBody FicheEtablissementRequest request) {
        return ResponseEntity.ok(etablissementService.modifierEtablissement(trackingId, request));
    }

    @DeleteMapping("/{trackingId}")
    @Operation(summary = "Supprimer une fiche établissement")
    public ResponseEntity<Void> supprimer(@PathVariable UUID trackingId) {
        etablissementService.supprimerEtablissement(trackingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recherche")
    @Operation(summary = "Rechercher des établissements par mot-clé")
    public ResponseEntity<Page<FicheEtablissementResponse>> rechercher(
            @RequestParam String motCle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(etablissementService.rechercher(motCle,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/ville/{ville}")
    @Operation(summary = "Lister les établissements par ville")
    public ResponseEntity<Page<FicheEtablissementResponse>> listerParVille(
            @PathVariable String ville,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(etablissementService.listerParVille(ville,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Lister les établissements par type")
    public ResponseEntity<Page<FicheEtablissementResponse>> listerParType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(etablissementService.listerParType(type,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/villes")
    @Operation(summary = "Lister toutes les villes contenant des établissements")
    public ResponseEntity<List<String>> getVilles() {
        return ResponseEntity.ok(etablissementService.obtenirToutesLesVilles());
    }
}
