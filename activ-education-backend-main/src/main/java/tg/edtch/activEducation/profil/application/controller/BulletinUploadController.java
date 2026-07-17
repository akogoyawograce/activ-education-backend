package tg.edtch.activEducation.profil.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tg.edtch.activEducation.profil.application.dto.request.BulletinUploadRequest;
import tg.edtch.activEducation.profil.application.dto.response.BulletinUploadResponse;
import tg.edtch.activEducation.profil.domain.enums.Periode;
import tg.edtch.activEducation.profil.domain.enums.TypePeriode;
import tg.edtch.activEducation.profil.domain.service.BulletinUploadOrchestrator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints d'upload de bulletins scolaires (Chantier C).
 *
 * <p>Le front appelle {@code POST /bulletins} pour 1 fichier, ou
 * {@code POST /bulletins/batch} pour 1 à 3 fichiers. Dans les 2 cas,
 * le backend orchestre : OCR → sauvegarde fichier → création des
 * {@code NoteSaisiManuel} → déclenchement du moteur 3 signaux.</p>
 *
 * <p>La sécurité reprend le pattern des autres endpoints élèves :
 * {@code @security.isOwner(#eleveTrackingId) or hasRole('ADMIN')}.
 * Les parents ne sont PAS inclus ici car l'upload est personnel —
 * un parent qui regarderait l'écran de son enfant ne doit pas pouvoir
 * uploader à sa place.</p>
 */
@RestController
@RequestMapping("/api/v1/eleves/{eleveTrackingId}/bulletins")
@RequiredArgsConstructor
@Tag(name = "Bulletins", description = "Upload PDF/image de bulletins → OCR → notes → recommandation 3 signaux")
public class BulletinUploadController {

    private final BulletinUploadOrchestrator orchestrator;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/eleves/{eleveTrackingId}/bulletins
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("@security.isOwner(#eleveTrackingId) or hasRole('ADMIN')")
    @Operation(summary = "Uploader 1 bulletin (PDF ou image) et déclencher la recommandation 3 signaux",
               description = "Chaîne : OCR → sauvegarde fichier → création des notes → moteur 3 signaux. "
                       + "Le retour consolidé contient les notes extraites, les notes persistées, et le top N des filières recommandées.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bulletin analysé et recommandation calculée"),
            @ApiResponse(responseCode = "400", description = "Données invalides (année mal formée, etc.)"),
            @ApiResponse(responseCode = "404", description = "Élève introuvable")
    })
    public ResponseEntity<BulletinUploadResponse> upload(
            @Parameter(description = "UUID public de l'élève", required = true)
            @PathVariable UUID eleveTrackingId,
            @Parameter(description = "Fichier PDF ou image (JPEG, PNG)", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Année scolaire (ex. 2024-2025)", required = true,
                       example = "2024-2025")
            @RequestParam("anneeScolaire") String anneeScolaire,
            @Parameter(description = "Période dans l'année (DEBUT/MILIEU/FIN)", required = true)
            @RequestParam("periode") Periode periode,
            @Parameter(description = "Type de découpage (TRIMESTRE/SEMESTRE)", required = true)
            @RequestParam("typePeriode") TypePeriode typePeriode,
            @Parameter(description = "Numéro de la période (1, 2 ou 3)", required = true)
            @RequestParam("numeroPeriode") Integer numeroPeriode) {

        BulletinUploadRequest request = BulletinUploadRequest.builder()
                .file(file)
                .anneeScolaire(anneeScolaire)
                .periode(periode)
                .typePeriode(typePeriode)
                .numeroPeriode(numeroPeriode)
                .build();

        return ResponseEntity.ok(orchestrator.orchestrer(eleveTrackingId, request));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/eleves/{eleveTrackingId}/bulletins/batch
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/batch")
    @PreAuthorize("@security.isOwner(#eleveTrackingId) or hasRole('ADMIN')")
    @Operation(summary = "Uploader 1 à 3 bulletins en lot (chaque année/période distincte)",
               description = "Les fichiers et les métadonnées sont passés en parallèle "
                       + "(files[i], anneeScolaire[i], periodes[i], typePeriodes[i], numerosPeriode[i]). "
                       + "Le moteur 3 signaux est déclenché après chaque bulletin.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bulletins analysés et recommandations calculées"),
            @ApiResponse(responseCode = "400", description = "Données invalides : nombre de fichiers != nombre de métadonnées, ou hors borne [1, 3]"),
            @ApiResponse(responseCode = "404", description = "Élève introuvable")
    })
    public ResponseEntity<List<BulletinUploadResponse>> uploadBatch(
            @Parameter(description = "UUID public de l'élève", required = true)
            @PathVariable UUID eleveTrackingId,
            @Parameter(description = "1 à 3 fichiers PDF/image", required = true)
            @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "Années scolaires (même taille que files)", required = true,
                       example = "[\"2023-2024\", \"2024-2025\", \"2025-2026\"]")
            @RequestParam("anneeScolaire") String[] anneeScolaire,
            @Parameter(description = "Périodes (DEBUT/MILIEU/FIN)", required = true,
                       example = "[\"FIN\", \"MILIEU\", \"DEBUT\"]")
            @RequestParam("periodes") Periode[] periodes,
            @Parameter(description = "Types de période (TRIMESTRE/SEMESTRE)", required = true,
                       example = "[\"TRIMESTRE\", \"TRIMESTRE\", \"TRIMESTRE\"]")
            @RequestParam("typePeriodes") TypePeriode[] typePeriodes,
            @Parameter(description = "Numéros de période (1..3)", required = true,
                       example = "[3, 2, 1]")
            @RequestParam("numerosPeriode") Integer[] numerosPeriode) {

        // Validation : tous les tableaux doivent avoir la même taille.
        int n = files.length;
        if (anneeScolaire.length != n || periodes.length != n
                || typePeriodes.length != n || numerosPeriode.length != n) {
            throw new IllegalArgumentException(
                    "Tous les tableaux (files, anneeScolaire, periodes, typePeriodes, "
                            + "numerosPeriode) doivent avoir la même taille. Reçu : files="
                            + n + ", annees=" + anneeScolaire.length
                            + ", periodes=" + periodes.length
                            + ", typePeriodes=" + typePeriodes.length
                            + ", numerosPeriode=" + numerosPeriode.length);
        }
        if (n == 0 || n > 3) {
            throw new IllegalArgumentException(
                    "Le batch doit contenir entre 1 et 3 bulletins (reçu : " + n + ").");
        }

        // Reconstituer la liste de BulletinUploadRequest à partir des
        // tableaux parallèles (le front envoie un fichier par année).
        List<BulletinUploadRequest> requests = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            requests.add(BulletinUploadRequest.builder()
                    .file(files[i])
                    .anneeScolaire(anneeScolaire[i])
                    .periode(periodes[i])
                    .typePeriode(typePeriodes[i])
                    .numeroPeriode(numerosPeriode[i])
                    .build());
        }

        return ResponseEntity.ok(orchestrator.orchestrerBatch(eleveTrackingId, requests));
    }
}
