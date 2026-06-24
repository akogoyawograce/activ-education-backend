package tg.edtch.activEducation.shared.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.profil.application.dto.request.AdministrateurRequest;
import tg.edtch.activEducation.profil.application.dto.request.ConseillerRequest;
import tg.edtch.activEducation.profil.application.dto.request.EleveRequest;
import tg.edtch.activEducation.profil.application.dto.request.ParentRequest;
import tg.edtch.activEducation.profil.application.dto.response.AdministrateurResponse;
import tg.edtch.activEducation.profil.application.dto.response.ConseillerResponse;
import tg.edtch.activEducation.profil.application.dto.response.EleveResponse;
import tg.edtch.activEducation.profil.application.dto.response.ParentResponse;
import tg.edtch.activEducation.profil.domain.enums.TypeApprenant;
import tg.edtch.activEducation.profil.domain.service.AdministrateurService;
import tg.edtch.activEducation.profil.domain.service.ConseillerService;
import tg.edtch.activEducation.profil.domain.service.EleveService;
import tg.edtch.activEducation.profil.domain.service.ParentService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Test", description = "Endpoints de test pour le développement")
public class TestController {

    private final EleveService eleveService;
    private final ParentService parentService;
    private final ConseillerService conseillerService;
    private final AdministrateurService administrateurService;

    @PostMapping("/create-user")
    @Operation(summary = "Créer un utilisateur de test", description = "Crée un utilisateur du type spécifié. Réservé ADMIN.")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateTestUserRequest request) {
        String type = request.getTypeUtilisateur().toUpperCase();

        return switch (type) {
            case "ELEVE" -> {
                EleveRequest req = EleveRequest.builder()
                        .nom(request.getNom())
                        .prenom(request.getPrenom())
                        .email(request.getEmail())
                        .motDePasse(request.getMotDePasse())
                        .typeApprenant(TypeApprenant.LYCEEN)
                        .build();
                EleveResponse res = eleveService.inscrireEleve(req);
                yield ResponseEntity.status(HttpStatus.CREATED).body(res);
            }
            case "PARENT" -> {
                ParentRequest req = ParentRequest.builder()
                        .nom(request.getNom())
                        .prenom(request.getPrenom())
                        .email(request.getEmail())
                        .motDePasse(request.getMotDePasse())
                        .build();
                ParentResponse res = parentService.creerParent(req);
                yield ResponseEntity.status(HttpStatus.CREATED).body(res);
            }
            case "CONSEILLER" -> {
                ConseillerRequest req = ConseillerRequest.builder()
                        .nom(request.getNom())
                        .prenom(request.getPrenom())
                        .email(request.getEmail())
                        .motDePasse(request.getMotDePasse())
                        .build();
                ConseillerResponse res = conseillerService.creerConseiller(req);
                yield ResponseEntity.status(HttpStatus.CREATED).body(res);
            }
            case "ADMIN" -> {
                AdministrateurRequest req = AdministrateurRequest.builder()
                        .nom(request.getNom())
                        .prenom(request.getPrenom())
                        .email(request.getEmail())
                        .motDePasse(request.getMotDePasse())
                        .niveauAcces("MODERATEUR")
                        .build();
                AdministrateurResponse res = administrateurService.creerAdministrateur(req);
                yield ResponseEntity.status(HttpStatus.CREATED).body(res);
            }
            default -> ResponseEntity.badRequest()
                    .body(Map.of("error", "Type invalide. Utilisez ELEVE, PARENT, CONSEILLER ou ADMIN."));
        };
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateTestUserRequest {
        @NotBlank(message = "Le type d'utilisateur est obligatoire")
        @Pattern(regexp = "ELEVE|PARENT|CONSEILLER|ADMIN", flags = Pattern.Flag.CASE_INSENSITIVE)
        private String typeUtilisateur;

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        private String email;

        @NotBlank(message = "Le mot de passe est obligatoire")
        private String motDePasse;

        @NotBlank(message = "Le nom est obligatoire")
        private String nom;

        @NotBlank(message = "Le prénom est obligatoire")
        private String prenom;
    }
}
