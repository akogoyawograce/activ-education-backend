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
import tg.edtch.activEducation.bibliotheque.application.dto.request.FavoriRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FavoriResponse;
import tg.edtch.activEducation.bibliotheque.domain.service.FavoriService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bibliotheque/favoris")
@RequiredArgsConstructor
@Tag(name = "Bibliothèque : Favoris", description = "API de gestion des favoris utilisateurs")
public class FavoriController {

    private final FavoriService favoriService;

    @PostMapping
    @Operation(summary = "Ajouter une fiche aux favoris")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Favori ajouté", content = @Content(schema = @Schema(implementation = FavoriResponse.class))),
            @ApiResponse(responseCode = "400", description = "Déjà présent ou données invalides", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utilisateur ou fiche introuvable", content = @Content)
    })
    public ResponseEntity<FavoriResponse> ajouter(@Valid @RequestBody FavoriRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(favoriService.ajouterFavori(request));
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Récupérer un favori par son trackingId")
    public ResponseEntity<FavoriResponse> get(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(favoriService.getFavori(trackingId));
    }

    @GetMapping("/utilisateur/{utilisateurTrackingId}")
    @Operation(summary = "Lister les favoris d'un utilisateur")
    public ResponseEntity<Page<FavoriResponse>> listerParUtilisateur(
            @PathVariable UUID utilisateurTrackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(favoriService.listerParUtilisateur(utilisateurTrackingId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @DeleteMapping("/{trackingId}")
    @Operation(summary = "Supprimer un favori")
    public ResponseEntity<Void> supprimer(@PathVariable UUID trackingId) {
        favoriService.supprimerFavori(trackingId);
        return ResponseEntity.noContent().build();
    }
}
