package tg.edtch.activEducation.bibliotheque.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import tg.edtch.activEducation.bibliotheque.application.dto.request.EtablissementDetailsXlsxRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.EtablissementDetailsXlsxResponse;
import tg.edtch.activEducation.bibliotheque.domain.service.EtablissementDetailsXlsxService;

import java.util.UUID;

/**
 * API minimale pour gérer les détails étendus d'une fiche établissement
 * (importés depuis Base_Etablissements_Superieurs_Togo.xlsx).
 *
 * <p>
 * MVP : on stocke tout en JSON dans {@code etablissement_details_xlsx}.
 * Cf. {@link tg.edtch.activEducation.bibliotheque.domain.entite.EtablissementDetailsXlsx}.
 *
 * <p>
 * Endpoints exposés sous
 * {@code /api/v1/bibliotheque/etablissements/{trackingId}/details-xlsx}.
 * Sécurité : GET public (lecture comme le reste du contrôleur bibliothèque),
 * POST/PUT/DELETE en ADMIN uniquement.
 *
 * @see <a href="https://github.com/missions-Tog/ActivEducation/issues">Phase 2
 *      du sprint xlsx — 6 août 2026</a>
 */
@RestController
@RequestMapping("/api/v1/bibliotheque/etablissements/{trackingId}/details-xlsx")
@RequiredArgsConstructor
@Tag(name = "Bibliothèque : Établissements (détails xlsx)", description = "Détails étendus des fiches établissements importés depuis le xlsx national")
public class EtablissementDetailsXlsxController {

    private final EtablissementDetailsXlsxService service;

    /**
     * Récupère les détails étendus d'un établissement.
     * Public (lecture comme tous les autres GET du namespace).
     */
    @GetMapping
    @Operation(summary = "Récupérer les détails étendus (xlsx) d'un établissement")
    public ResponseEntity<EtablissementDetailsXlsxResponse> get(
            @PathVariable UUID trackingId) {
        return ResponseEntity.ok(service.getDetails(trackingId));
    }

    /**
     * Crée ou remplace les détails étendus d'un établissement (idempotent).
     * Réservé ADMIN/SUPER_ADMIN — utilisé par le script d'import.
     */
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer ou remplacer les détails étendus d'un établissement (utilisé par l'import xlsx)")
    public ResponseEntity<EtablissementDetailsXlsxResponse> upsert(
            @PathVariable UUID trackingId,
            @Valid @RequestBody EtablissementDetailsXlsxRequest request) {
        return ResponseEntity.ok(service.upsertDetails(trackingId, request));
    }

    /**
     * Supprime les détails étendus (rollback éventuel).
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer les détails étendus d'un établissement")
    public ResponseEntity<Void> delete(@PathVariable UUID trackingId) {
        service.deleteDetails(trackingId);
        return ResponseEntity.noContent().build();
    }
}
