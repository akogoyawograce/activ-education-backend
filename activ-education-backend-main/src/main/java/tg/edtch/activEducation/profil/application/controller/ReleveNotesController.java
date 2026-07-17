package tg.edtch.activEducation.profil.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tg.edtch.activEducation.profil.application.dto.response.ReleveValidationResponse;
import tg.edtch.activEducation.profil.domain.service.ReleveNotesService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/eleves")
@RequiredArgsConstructor
@Tag(name = "Relevé de notes", description = "Validation de relevés BEPC/BAC pour mise à jour du niveau")
public class ReleveNotesController {

    private final ReleveNotesService releveNotesService;

    @PostMapping("/{trackingId}/releve-notes")
    @PreAuthorize("@security.isOwner(#trackingId) or hasRole('ADMIN')")
    @Operation(summary = "Valider un relevé de notes et mettre à jour le niveau",
            description = "Analyse un relevé de notes (BEPC ou BAC) via IA, valide l'authenticité, et met à jour le niveau et type d'apprenant si ADMIS.")
    @ApiResponse(responseCode = "200", description = "Analyse terminée (valide ou non)",
            content = @Content(schema = @Schema(implementation = ReleveValidationResponse.class)))
    @ApiResponse(responseCode = "404", description = "Élève introuvable")
    public ResponseEntity<ReleveValidationResponse> validerReleve(
            @Parameter(description = "Identifiant public (UUID) de l'élève", required = true)
            @PathVariable UUID trackingId,
            @Parameter(description = "Fichier PDF du relevé de notes", required = true)
            @RequestParam("file") MultipartFile file) {
        ReleveValidationResponse result = releveNotesService.validerEtMettreAJour(trackingId, file);
        return ResponseEntity.ok(result);
    }
}
