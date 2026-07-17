package tg.edtch.activEducation.reseau.application.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.reseau.domain.dto.*;
import tg.edtch.activEducation.reseau.domain.service.ReseauService;

import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/reseau")
@PreAuthorize("isAuthenticated()")
public class ReseauController {

    private final ReseauService service;

    public ReseauController(ReseauService service) {
        this.service = service;
    }

    @GetMapping("/feed/{utilisateurId}")
    public ResponseEntity<Page<PublicationResponse>> getFeed(
            @PathVariable String utilisateurId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getFeed(utilisateurId, page, size));
    }

    @GetMapping("/tendances")
    public ResponseEntity<Page<PublicationResponse>> getTendances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getTendances(page, size));
    }

    @GetMapping("/utilisateur/{auteurId}")
    public ResponseEntity<Page<PublicationResponse>> getPublicationsUtilisateur(
            @PathVariable String auteurId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String currentUserId) {
        return ResponseEntity.ok(service.getPublicationsUtilisateur(auteurId, page, size, currentUserId));
    }

    @PostMapping("/publications")
    public ResponseEntity<PublicationResponse> publier(
            @RequestParam String auteurId,
            @RequestParam String auteurNom,
            @RequestParam(defaultValue = "ELEVE") String auteurRole,
            @Valid @RequestBody PublicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.publier(auteurId, auteurNom, auteurRole, request));
    }

    @DeleteMapping("/publications/{trackingId}")
    public ResponseEntity<Void> supprimerPublication(
            @PathVariable UUID trackingId,
            @RequestParam String utilisateurId) {
        service.supprimerPublication(trackingId, utilisateurId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/publications/{trackingId}/reaction")
    public ResponseEntity<Void> reactionner(
            @PathVariable UUID trackingId,
            @RequestParam String utilisateurId) {
        service.reactionner(trackingId, utilisateurId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/publications/{trackingId}/commentaires")
    public ResponseEntity<Page<CommentaireResponse>> getCommentaires(
            @PathVariable UUID trackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getCommentaires(trackingId, page, size));
    }

    @PostMapping("/publications/{trackingId}/commentaires")
    public ResponseEntity<CommentaireResponse> commenter(
            @PathVariable UUID trackingId,
            @RequestParam String auteurId,
            @RequestParam String auteurNom,
            @Valid @RequestBody CommentaireRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.commenter(trackingId, auteurId, auteurNom, request));
    }

    @DeleteMapping("/commentaires/{trackingId}")
    public ResponseEntity<Void> supprimerCommentaire(
            @PathVariable UUID trackingId,
            @RequestParam String utilisateurId) {
        service.supprimerCommentaire(trackingId, utilisateurId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/abonnements")
    public ResponseEntity<Void> suivre(
            @RequestParam String abonneId,
            @RequestParam String abonnementId) {
        service.suivre(abonneId, abonnementId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/abonnements")
    public ResponseEntity<Void> nePlusSuivre(
            @RequestParam String abonneId,
            @RequestParam String abonnementId) {
        service.nePlusSuivre(abonneId, abonnementId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/abonnements/verifier")
    public ResponseEntity<Boolean> estAbonne(
            @RequestParam String abonneId,
            @RequestParam String abonnementId) {
        return ResponseEntity.ok(service.estAbonne(abonneId, abonnementId));
    }

    @GetMapping("/abonnements/nombre/{utilisateurId}")
    public ResponseEntity<Integer> nombreAbonnes(@PathVariable String utilisateurId) {
        return ResponseEntity.ok(service.nombreAbonnes(utilisateurId));
    }
}
