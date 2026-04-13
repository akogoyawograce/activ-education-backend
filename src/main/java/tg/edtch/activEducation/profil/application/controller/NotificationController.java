package tg.edtch.activEducation.profil.application.controller;

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
import tg.edtch.activEducation.profil.application.dto.request.NotificationRequest;
import tg.edtch.activEducation.profil.application.dto.response.NotificationResponse;
import tg.edtch.activEducation.profil.domain.service.NotificationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller REST pour la gestion des notifications utilisateur.
 * Toutes les opérations utilisent le {@code trackingId} (UUID) — jamais la clé
 * primaire interne.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "API de gestion des notifications. Supporte les états lue/non-lue et les opérations en masse.")
public class NotificationController {

    private final NotificationService notificationService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/utilisateurs/{utilisateurTrackingId}/notifications
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/utilisateurs/{utilisateurTrackingId}/notifications")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Envoyer une notification à un utilisateur", description = "Crée et envoie une notification au destinataire identifié par son UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notification envoyée", content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable", content = @Content)
    })
    public ResponseEntity<NotificationResponse> envoyer(
            @Parameter(description = "UUID public du destinataire", required = true) @PathVariable UUID utilisateurTrackingId,
            @Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.envoyer(utilisateurTrackingId, request));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/notifications/{trackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/notifications/{trackingId}")
    @Operation(summary = "Récupérer une notification par son UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification trouvée", content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Notification introuvable", content = @Content)
    })
    public ResponseEntity<NotificationResponse> getNotification(
            @Parameter(description = "UUID public de la notification", required = true) @PathVariable UUID trackingId) {
        return ResponseEntity.ok(notificationService.getNotification(trackingId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/utilisateurs/{utilisateurTrackingId}/notifications
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/utilisateurs/{utilisateurTrackingId}/notifications")
    @Operation(summary = "Lister toutes les notifications d'un utilisateur")
    @ApiResponse(responseCode = "200", description = "Liste retournée")
    public ResponseEntity<List<NotificationResponse>> getNotificationsUtilisateur(
            @PathVariable UUID utilisateurTrackingId) {
        return ResponseEntity.ok(notificationService.getNotificationsUtilisateur(utilisateurTrackingId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/utilisateurs/{utilisateurTrackingId}/notifications/pagine
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/utilisateurs/{utilisateurTrackingId}/notifications/pagine")
    @Operation(summary = "Notifications paginées d'un utilisateur")
    @ApiResponse(responseCode = "200", description = "Page retournée")
    public ResponseEntity<Page<NotificationResponse>> getNotificationsPagine(
            @PathVariable UUID utilisateurTrackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                notificationService.getNotificationsPagine(
                        utilisateurTrackingId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/utilisateurs/{utilisateurTrackingId}/notifications/non-lues
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/utilisateurs/{utilisateurTrackingId}/notifications/non-lues")
    @Operation(summary = "Récupérer les notifications non lues d'un utilisateur")
    @ApiResponse(responseCode = "200", description = "Liste des notifications non lues")
    public ResponseEntity<List<NotificationResponse>> getNonLues(
            @PathVariable UUID utilisateurTrackingId) {
        return ResponseEntity.ok(notificationService.getNonLues(utilisateurTrackingId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/utilisateurs/{utilisateurTrackingId}/notifications/compteur
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/utilisateurs/{utilisateurTrackingId}/notifications/compteur")
    @Operation(summary = "Compter les notifications non lues", description = "Retourne un objet JSON avec le nombre de notifications non lues : {\"nonLues\": 5}")
    @ApiResponse(responseCode = "200", description = "Compteur retourné")
    public ResponseEntity<Map<String, Long>> compterNonLues(
            @PathVariable UUID utilisateurTrackingId) {
        long count = notificationService.compterNonLues(utilisateurTrackingId);
        return ResponseEntity.ok(Map.of("nonLues", count));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/v1/notifications/{trackingId}/lire
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/notifications/{trackingId}/lire")
    @Operation(summary = "Marquer une notification comme lue")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification marquée comme lue", content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Notification introuvable", content = @Content)
    })
    public ResponseEntity<NotificationResponse> marquerCommeLue(
            @Parameter(description = "UUID public de la notification", required = true) @PathVariable UUID trackingId) {
        return ResponseEntity.ok(notificationService.marquerCommeLue(trackingId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/v1/utilisateurs/{utilisateurTrackingId}/notifications/tout-lire
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/utilisateurs/{utilisateurTrackingId}/notifications/tout-lire")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Marquer toutes les notifications comme lues", description = "Opération en masse : marque toutes les notifications non lues d'un utilisateur comme lues.")
    @ApiResponse(responseCode = "204", description = "Toutes les notifications marquées comme lues")
    public ResponseEntity<Void> marquerToutesCommeLues(
            @PathVariable UUID utilisateurTrackingId) {
        notificationService.marquerToutesCommeLues(utilisateurTrackingId);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/notifications/{trackingId}
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/notifications/{trackingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une notification", description = "Suppression définitive d'une notification.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notification supprimée"),
            @ApiResponse(responseCode = "404", description = "Notification introuvable", content = @Content)
    })
    public ResponseEntity<Void> supprimerNotification(
            @PathVariable UUID trackingId) {
        notificationService.supprimerNotification(trackingId);
        return ResponseEntity.noContent().build();
    }
}
