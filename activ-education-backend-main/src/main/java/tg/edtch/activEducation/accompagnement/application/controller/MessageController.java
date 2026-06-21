package tg.edtch.activEducation.accompagnement.application.controller;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.accompagnement.application.dto.request.MessageRequest;
import tg.edtch.activEducation.accompagnement.application.dto.response.MessageResponse;
import tg.edtch.activEducation.accompagnement.domain.service.MessageService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller REST pour la messagerie de la plateforme.
 * Toutes les opérations utilisent les {@code trackingId} (UUID) — jamais les
 * clés primaires internes.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "API de messagerie entre utilisateurs. Conversations identifiées via UUID publics.")
public class MessageController {

    private final MessageService messageService;

    // POST /api/v1/utilisateurs/{expediteurTrackingId}/messages
    @PostMapping("/utilisateurs/{expediteurTrackingId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@security.isOwner(#expediteurTrackingId) or hasRole('ADMIN')")
    @Operation(summary = "Envoyer un message", description = "Envoie un message depuis l'expéditeur (UUID dans l'URL) vers le destinataire (UUID dans le body).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Message envoyé", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides ou auto-envoi interdit", content = @Content),
            @ApiResponse(responseCode = "404", description = "Expéditeur ou destinataire introuvable", content = @Content)
    })
    public ResponseEntity<MessageResponse> envoyerMessage(
            @Parameter(description = "UUID public de l'expéditeur", required = true) @PathVariable UUID expediteurTrackingId,
            @Valid @RequestBody MessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.envoyerMessage(expediteurTrackingId, request));
    }

    // GET /api/v1/messages/{trackingId}
    @GetMapping("/messages/{trackingId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer un message par son UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Message trouvé", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Message introuvable", content = @Content)
    })
    public ResponseEntity<MessageResponse> getMessage(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(messageService.getMessage(trackingId));
    }

    // GET /api/v1/messages/conversation?user1=...&user2=...
    @GetMapping("/messages/conversation")
    @PreAuthorize("@security.isOwner(#user1) or @security.isOwner(#user2) or hasRole('ADMIN')")
    @Operation(summary = "Obtenir la conversation entre deux utilisateurs", description = "Retourne tous les messages échangés entre user1 et user2, triés chronologiquement.")
    @ApiResponse(responseCode = "200", description = "Conversation retournée")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @Parameter(description = "UUID public de l'utilisateur 1", required = true) @RequestParam UUID user1,
            @Parameter(description = "UUID public de l'utilisateur 2", required = true) @RequestParam UUID user2) {
        return ResponseEntity.ok(messageService.getConversation(user1, user2));
    }

    // GET /api/v1/utilisateurs/{destinataireTrackingId}/messages/recus
    @GetMapping("/utilisateurs/{destinataireTrackingId}/messages/recus")
    @PreAuthorize("@security.isOwner(#destinataireTrackingId) or hasRole('ADMIN')")
    @Operation(summary = "Messages reçus d'un utilisateur (paginés)")
    @ApiResponse(responseCode = "200", description = "Page de messages reçus")
    public ResponseEntity<Page<MessageResponse>> getMessagesRecus(
            @PathVariable UUID destinataireTrackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                messageService.getMessagesRecus(
                        destinataireTrackingId,
                        PageRequest.of(page, size, Sort.by("dateEnvoi").descending())));
    }

    // GET /api/v1/utilisateurs/{expediteurTrackingId}/messages/envoyes
    @GetMapping("/utilisateurs/{expediteurTrackingId}/messages/envoyes")
    @PreAuthorize("@security.isOwner(#expediteurTrackingId) or hasRole('ADMIN')")
    @Operation(summary = "Messages envoyés par un utilisateur (paginés)")
    @ApiResponse(responseCode = "200", description = "Page de messages envoyés")
    public ResponseEntity<Page<MessageResponse>> getMessagesEnvoyes(
            @PathVariable UUID expediteurTrackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                messageService.getMessagesEnvoyes(
                        expediteurTrackingId,
                        PageRequest.of(page, size, Sort.by("dateEnvoi").descending())));
    }

    // GET /api/v1/utilisateurs/{destinataireTrackingId}/messages/non-lus/compteur
    @GetMapping("/utilisateurs/{destinataireTrackingId}/messages/non-lus/compteur")
    @PreAuthorize("@security.isOwner(#destinataireTrackingId) or hasRole('ADMIN')")
    @Operation(summary = "Compter les messages non lus", description = "Retourne : {\"nonLus\": 3}")
    @ApiResponse(responseCode = "200", description = "Compteur retourné")
    public ResponseEntity<Map<String, Long>> compterNonLus(@PathVariable UUID destinataireTrackingId) {
        return ResponseEntity.ok(Map.of("nonLus", messageService.compterNonLus(destinataireTrackingId)));
    }

    // PATCH /api/v1/messages/conversation/lire?expediteur=...&destinataire=...
    @PatchMapping("/messages/conversation/lire")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@security.isOwner(#destinataire) or hasRole('ADMIN')")
    @Operation(summary = "Marquer une conversation comme lue", description = "Marque tous les messages envoyés par 'expediteur' à 'destinataire' comme lus.")
    @ApiResponse(responseCode = "204", description = "Conversation marquée comme lue")
    public ResponseEntity<Void> marquerConversationCommeLue(
            @RequestParam UUID expediteur,
            @RequestParam UUID destinataire) {
        messageService.marquerConversationCommeLue(expediteur, destinataire);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/v1/messages/{trackingId}
    @DeleteMapping("/messages/{trackingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un message", description = "Suppression définitive (hard-delete).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Message supprimé"),
            @ApiResponse(responseCode = "404", description = "Message introuvable", content = @Content)
    })
    public ResponseEntity<Void> supprimerMessage(@PathVariable UUID trackingId) {
        messageService.supprimerMessage(trackingId);
        return ResponseEntity.noContent().build();
    }
}
