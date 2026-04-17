package tg.edtch.activEducation.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.profil.domain.entite.Role;
import tg.edtch.activEducation.profil.domain.enums.RoleNom;
import tg.edtch.activEducation.profil.repository.RoleRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initialiserRole(RoleNom.ROLE_ELEVE);
        initialiserRole(RoleNom.ROLE_CONSEILLER);
        initialiserRole(RoleNom.ROLE_PARENT);
        initialiserRole(RoleNom.ROLE_ADMIN);
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
}
