package tg.edtch.activEducation.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.profil.domain.entite.Administrateur;
import tg.edtch.activEducation.profil.domain.entite.Role;
import tg.edtch.activEducation.profil.domain.enums.RoleNom;
import tg.edtch.activEducation.profil.repository.AdministrateurRepository;
import tg.edtch.activEducation.profil.repository.RoleRepository;
import tg.edtch.activEducation.shared.util.ParametreApplication;
import tg.edtch.activEducation.shared.util.ParametreApplicationRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AdministrateurRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final ParametreApplicationRepository parametreRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initialiserRole(RoleNom.ROLE_ELEVE);
        initialiserRole(RoleNom.ROLE_CONSEILLER);
        initialiserRole(RoleNom.ROLE_PARENT);
        initialiserRole(RoleNom.ROLE_ADMIN);

        creerAdminParDefaut();
        initialiserParametres();
    }

    private void initialiserParametres() {
        creerParametreSiAbsent("quiz.poids_academique", "60",
                "Poids des notes académiques dans le calcul (0-100)", "RECOMMENDATION");
        creerParametreSiAbsent("quiz.poids_quiz", "40",
                "Poids du quiz d'orientation dans le calcul (0-100)", "RECOMMENDATION");
        creerParametreSiAbsent("quiz.seuil_recommandation", "0.6",
                "Seuil minimum de similarité pour recommander une filière", "RECOMMENDATION");
        creerParametreSiAbsent("quiz.nombre_recommandations", "5",
                "Nombre maximum de recommandations à afficher", "RECOMMENDATION");
        creerParametreSiAbsent("maintenance.enabled", "false",
                "État du mode maintenance (true/false)", "MAINTENANCE");
        creerParametreSiAbsent("maintenance.message", "Plateforme en maintenance. Revenez dans quelques instants.",
                "Message affiché en mode maintenance", "MAINTENANCE");
    }

    private void creerParametreSiAbsent(String cle, String valeur, String description, String categorie) {
        if (parametreRepository.findByCle(cle).isEmpty()) {
            parametreRepository.save(ParametreApplication.builder()
                    .cle(cle)
                    .valeur(valeur)
                    .description(description)
                    .categorie(categorie)
                    .build());
            log.info("Paramètre initialisé : {} = {}", cle, valeur);
        }
    }

    private void initialiserRole(RoleNom nomRole) {
        if (roleRepository.findByNom(nomRole).isEmpty()) {
            Role role = Role.builder()
                    .nom(nomRole)
                    .createdBy("system")
                    .build();
            roleRepository.save(role);
            log.info("Rôle initialisé : {}", nomRole);
        }
    }

    private void creerAdminParDefaut() {
        String adminEmail = "admin@activeducation.tg";
        String defaultPassword = "admin123!";
        
        adminRepository.findByEmail(adminEmail).ifPresentOrElse(
            admin -> {
                admin.setMotDePasseHash(passwordEncoder.encode(defaultPassword));
                adminRepository.save(admin);
                log.info("Compte Administrateur existant mis à jour avec le mot de passe par défaut.");
            },
            () -> {
                Role roleAdmin = roleRepository.findByNom(RoleNom.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Rôle ADMIN non initialisé"));

                Administrateur admin = Administrateur.builder()
                        .email(adminEmail)
                        .motDePasseHash(passwordEncoder.encode(defaultPassword))
                        .nom("ADMIN")
                        .prenom("Platform")
                        .niveauAcces("SUPER_ADMIN")
                        .roles(Set.of(roleAdmin))
                        .estActif(true)
                        .dateInscription(LocalDateTime.now())
                        .createdBy("system")
                        .build();

                adminRepository.save(admin);
                log.info("Compte Administrateur par défaut créé : {}", adminEmail);
            }
        );
    }
}
