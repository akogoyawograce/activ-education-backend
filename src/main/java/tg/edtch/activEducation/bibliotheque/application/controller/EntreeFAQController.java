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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.bibliotheque.application.dto.request.EntreeFAQRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.EntreeFAQResponse;
import tg.edtch.activEducation.bibliotheque.domain.service.EntreeFAQService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bibliotheque/faq")
@RequiredArgsConstructor
@Tag(name = "Bibliothèque : FAQ", description = "API de gestion de la Foire Aux Questions")
public class EntreeFAQController {

    private final EntreeFAQService faqService;

    @PostMapping
    @Operation(summary = "Créer une nouvelle entrée FAQ")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Entrée FAQ créée", content = @Content(schema = @Schema(implementation = EntreeFAQResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content)
    })
    public ResponseEntity<EntreeFAQResponse> creer(@Valid @RequestBody EntreeFAQRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(faqService.creerEntree(request));
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Récupérer une entrée FAQ par son trackingId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrée trouvée", content = @Content(schema = @Schema(implementation = EntreeFAQResponse.class))),
            @ApiResponse(responseCode = "404", description = "Entrée introuvable", content = @Content)
    })
    public ResponseEntity<EntreeFAQResponse> get(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(faqService.getEntree(trackingId));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les entrées FAQ publiées (paginé)")
    public ResponseEntity<Page<EntreeFAQResponse>> lister(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(faqService.listerToutes(PageRequest.of(page, size)));
    }

    @GetMapping("/categorie/{categorie}")
    @Operation(summary = "Lister les entrées FAQ par catégorie")
    public ResponseEntity<List<EntreeFAQResponse>> listerParCategorie(@PathVariable String categorie) {
        return ResponseEntity.ok(faqService.listerParCategorie(categorie));
    }

    @GetMapping("/categories")
    @Operation(summary = "Lister toutes les catégories uniques utilisées")
    public ResponseEntity<List<String>> listerCategories() {
        return ResponseEntity.ok(faqService.listerCategories());
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier une entrée FAQ existante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrée mise à jour", content = @Content(schema = @Schema(implementation = EntreeFAQResponse.class))),
            @ApiResponse(responseCode = "404", description = "Entrée introuvable", content = @Content)
    })
    public ResponseEntity<EntreeFAQResponse> modifier(
            @PathVariable UUID trackingId,
            @Valid @RequestBody EntreeFAQRequest request) {
        return ResponseEntity.ok(faqService.modifierEntree(trackingId, request));
    }

    @DeleteMapping("/{trackingId}")
    @Operation(summary = "Supprimer une entrée FAQ")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Entrée supprimée"),
            @ApiResponse(responseCode = "404", description = "Entrée introuvable", content = @Content)
    })
    public ResponseEntity<Void> supprimer(@PathVariable UUID trackingId) {
        faqService.supprimerEntree(trackingId);
        return ResponseEntity.noContent().build();
    }
}
