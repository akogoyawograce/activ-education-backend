package tg.edtch.activEducation.accompagnement.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.accompagnement.domain.entite.Message;
import tg.edtch.activEducation.accompagnement.domain.entite.Ticket;
import tg.edtch.activEducation.accompagnement.domain.service.TicketService;
import tg.edtch.activEducation.shared.security.userdetails.CustomUserDetails;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Système de tickets pour la messagerie conseiller")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Créer un nouveau ticket")
    public ResponseEntity<Ticket> creerTicket(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody CreerTicketRequest request) {
        Ticket ticket = ticketService.creerTicket(user.getId(), request.sujet(), request.categorie());
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/{trackingId}/assigner/{conseillerTrackingId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assigner un ticket à un conseiller")
    public ResponseEntity<Ticket> assignerTicket(
            @PathVariable UUID trackingId,
            @PathVariable UUID conseillerTrackingId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Ticket ticket = ticketService.assignerTicket(trackingId, user.getId());
        return ResponseEntity.ok(ticket);
    }

    @PatchMapping("/{trackingId}/statut")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Changer le statut d'un ticket")
    public ResponseEntity<Ticket> changerStatut(
            @PathVariable UUID trackingId,
            @Valid @RequestBody ChangerStatutRequest request) {
        Ticket ticket = ticketService.changerStatut(trackingId, request.statut());
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/{trackingId}/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Ajouter un message à un ticket")
    public ResponseEntity<Map<String, Object>> ajouterMessage(
            @PathVariable UUID trackingId,
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody AjouterMessageRequest request) {
        Message message = ticketService.ajouterMessage(trackingId, user.getId(), request.contenu());
        return ResponseEntity.ok(Map.of(
                "trackingId", message.getTrackingId(),
                "contenu", message.getContenu(),
                "dateEnvoi", message.getDateEnvoi()));
    }

    @GetMapping("/mes-tickets")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mes tickets en tant qu'utilisateur")
    public ResponseEntity<Page<Ticket>> mesTickets(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ticketService.mesTickets(user.getId(),
                PageRequest.of(page, size, Sort.by("dateDerniereActivite").descending())));
    }

    @GetMapping("/assignes")
    @PreAuthorize("hasAnyRole('CONSEILLER', 'ADMIN')")
    @Operation(summary = "Tickets assignés au conseiller connecté")
    public ResponseEntity<Page<Ticket>> ticketsAssignes(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ticketService.ticketsAssignes(user.getId(),
                PageRequest.of(page, size, Sort.by("dateDerniereActivite").descending())));
    }

    @GetMapping("/statistiques")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Statistiques des tickets")
    public ResponseEntity<Map<String, Long>> statistiques() {
        return ResponseEntity.ok(Map.of(
                "ouverts", ticketService.countByStatut("OUVERT"),
                "assignes", ticketService.countByStatut("ASSIGNE"),
                "enCours", ticketService.countByStatut("EN_COURS"),
                "resolus", ticketService.countByStatut("RESOLU"),
                "fermes", ticketService.countByStatut("FERME")));
    }

    public record CreerTicketRequest(@NotBlank String sujet, String categorie) {}
    public record ChangerStatutRequest(@NotBlank String statut) {}
    public record AjouterMessageRequest(@NotBlank String contenu) {}
}
