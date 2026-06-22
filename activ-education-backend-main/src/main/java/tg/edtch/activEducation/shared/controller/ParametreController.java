package tg.edtch.activEducation.shared.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.shared.util.ParametreApplication;
import tg.edtch.activEducation.shared.util.ParametreApplicationRepository;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/admin/parametres")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin : Paramètres", description = "Configuration de l'application")
public class ParametreController {

    private final ParametreApplicationRepository parametreRepository;

    @GetMapping
    @Operation(summary = "Lister tous les paramètres")
    public ResponseEntity<List<ParametreApplication>> lister() {
        return ResponseEntity.ok(parametreRepository.findAll());
    }

    @GetMapping("/{cle}")
    @Operation(summary = "Lire un paramètre par sa clé")
    public ResponseEntity<ParametreApplication> get(@PathVariable String cle) {
        return ResponseEntity.ok(parametreRepository.findByCle(cle)
                .orElseThrow(() -> new NoSuchElementException("Paramètre introuvable : " + cle)));
    }

    @PutMapping("/{cle}")
    @Operation(summary = "Mettre à jour la valeur d'un paramètre")
    public ResponseEntity<ParametreApplication> update(@PathVariable String cle, @RequestBody Map<String, String> body) {
        ParametreApplication param = parametreRepository.findByCle(cle)
                .orElseThrow(() -> new NoSuchElementException("Paramètre introuvable : " + cle));
        param.setValeur(body.get("valeur"));
        if (body.containsKey("description")) param.setDescription(body.get("description"));
        return ResponseEntity.ok(parametreRepository.save(param));
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau paramètre")
    public ResponseEntity<ParametreApplication> create(@RequestBody ParametreApplication param) {
        return ResponseEntity.ok(parametreRepository.save(param));
    }
}
