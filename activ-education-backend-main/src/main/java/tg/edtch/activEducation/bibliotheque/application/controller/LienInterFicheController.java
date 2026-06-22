package tg.edtch.activEducation.bibliotheque.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.bibliotheque.domain.entite.LienInterFiche;
import tg.edtch.activEducation.bibliotheque.repository.LienInterFicheRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bibliotheque/liens")
@RequiredArgsConstructor
@Tag(name = "Bibliothèque : Liens inter-fiches", description = "Graphe de connaissances entre fiches")
public class LienInterFicheController {

    private final LienInterFicheRepository lienRepository;

    @GetMapping("/{type}/{trackingId}")
    @Operation(summary = "Liens sortants et entrants d'une fiche")
    public ResponseEntity<Map<String, Object>> getLiens(
            @PathVariable String type,
            @PathVariable String trackingId) {
        List<LienInterFiche> sortants = lienRepository
                .findBySourceTypeAndSourceTrackingId(type.toUpperCase(), trackingId);
        List<LienInterFiche> entrants = lienRepository
                .findByTargetTypeAndTargetTrackingId(type.toUpperCase(), trackingId);
        return ResponseEntity.ok(Map.of(
                "sortants", sortants,
                "entrants", entrants));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un lien entre deux fiches")
    public ResponseEntity<LienInterFiche> creerLien(@RequestBody LienInterFiche lien) {
        return ResponseEntity.ok(lienRepository.save(lien));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un lien")
    public ResponseEntity<Void> supprimerLien(@PathVariable Long id) {
        lienRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
