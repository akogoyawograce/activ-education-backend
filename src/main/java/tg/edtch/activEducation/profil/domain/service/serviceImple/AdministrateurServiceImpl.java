package tg.edtch.activEducation.profil.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.profil.application.dto.request.AdministrateurRequest;
import tg.edtch.activEducation.profil.application.dto.response.AdministrateurResponse;
import tg.edtch.activEducation.profil.application.mapper.AdministrateurMapper;
import tg.edtch.activEducation.profil.domain.entite.Administrateur;
import tg.edtch.activEducation.profil.domain.entite.Role;
import tg.edtch.activEducation.profil.domain.enums.RoleNom;
import tg.edtch.activEducation.profil.domain.service.AdministrateurService;
import tg.edtch.activEducation.profil.repository.AdministrateurRepository;
import tg.edtch.activEducation.profil.repository.RoleRepository;
import tg.edtch.activEducation.profil.repository.UtilisateurRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Implémentation du service Administrateur.
 * Toutes les opérations utilisent le {@code trackingId} (UUID) — jamais le Long
 * id.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdministrateurServiceImpl implements AdministrateurService {

    private final AdministrateurRepository adminRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final AdministrateurMapper adminMapper;

    @Override
    public AdministrateurResponse creerAdministrateur(AdministrateurRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Un compte avec l'email '" + request.getEmail() + "' existe déjà.");
        }

        Administrateur admin = adminMapper.toEntity(request);
        // TODO: activer le hachage (PasswordEncoder) avant la mise en production
        admin.setMotDePasseHash(request.getMotDePasse());

        // Association du rôle ROLE_ADMIN
        Role roleAdmin = roleRepository.findByNom(RoleNom.ROLE_ADMIN)
                .orElseThrow(() -> new NoSuchElementException(
                        "Rôle ROLE_ADMIN introuvable. Vérifiez l'initialisation des données (DataLoader)."));
        admin.getRoles().add(roleAdmin);

        Administrateur saved = adminRepository.save(admin);
        log.info("Nouvel administrateur créé : email={} niveauAcces={} trackingId={}",
                saved.getEmail(), saved.getNiveauAcces(), saved.getTrackingId());
        return adminMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AdministrateurResponse getAdministrateur(UUID trackingId) {
        return adminMapper.toResponse(findOrThrow(trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdministrateurResponse> listerTous(Pageable pageable) {
        return adminRepository.findAllByEstActifTrue(pageable)
                .map(adminMapper::toResponse);
    }

    @Override
    public AdministrateurResponse modifierAdministrateur(UUID trackingId, AdministrateurRequest request) {
        Administrateur admin = findOrThrow(trackingId);
        adminMapper.updateFromRequest(request, admin);

        if (request.getMotDePasse() != null && !request.getMotDePasse().isBlank()) {
            // TODO: activer le hachage (PasswordEncoder) avant la mise en production
            admin.setMotDePasseHash(request.getMotDePasse());
        }

        Administrateur saved = adminRepository.save(admin);
        log.info("Administrateur modifié : trackingId={}", trackingId);
        return adminMapper.toResponse(saved);
    }

    @Override
    public void desactiverAdministrateur(UUID trackingId) {
        Administrateur admin = findOrThrow(trackingId);
        admin.setEstActif(false);
        adminRepository.save(admin);
        log.info("Administrateur désactivé (soft-delete) : trackingId={}", trackingId);
    }

    // ─── Helper privé ─────────────────────────────────────────────────────────
    private Administrateur findOrThrow(UUID trackingId) {
        return adminRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Administrateur introuvable pour le trackingId : " + trackingId));
    }
}
