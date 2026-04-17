package tg.edtch.activEducation.profil.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.profil.application.dto.request.ConseillerRequest;
import tg.edtch.activEducation.profil.application.dto.response.ConseillerResponse;
import tg.edtch.activEducation.profil.application.mapper.ConseillerMapper;
import tg.edtch.activEducation.profil.domain.entite.Conseiller;
import tg.edtch.activEducation.profil.domain.entite.Role;
import tg.edtch.activEducation.profil.domain.enums.RoleNom;
import tg.edtch.activEducation.profil.domain.service.ConseillerService;
import tg.edtch.activEducation.profil.repository.ConseillerRepository;
import tg.edtch.activEducation.profil.repository.RoleRepository;
import tg.edtch.activEducation.profil.repository.UtilisateurRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service Conseiller.
 * Toutes les opérations utilisent le {@code trackingId} (UUID) — jamais le Long
 * id.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConseillerServiceImpl implements ConseillerService {

    private final ConseillerRepository conseillerRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final ConseillerMapper conseillerMapper;

    @Override
    public ConseillerResponse creerConseiller(ConseillerRequest request) {
        // Unicité de l'email au niveau global (table utilisateurs)
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Un compte avec l'adresse email '" + request.getEmail() + "' existe déjà.");
        }

        // Construction de l'entité via le Mapper (trackingId généré automatiquement)
        Conseiller conseiller = conseillerMapper.toEntity(request);

        // TODO: activer le hachage (PasswordEncoder) avant la mise en production
        conseiller.setMotDePasseHash(request.getMotDePasse());

        // Association du rôle ROLE_CONSEILLER
        Role roleConseiller = roleRepository.findByNom(RoleNom.ROLE_CONSEILLER)
                .orElseThrow(() -> new NoSuchElementException(
                        "Rôle ROLE_CONSEILLER introuvable. Vérifiez l'initialisation des données (DataLoader)."));
        conseiller.getRoles().add(roleConseiller);

        Conseiller saved = conseillerRepository.save(conseiller);
        log.info("Nouveau conseiller créé : email={} trackingId={}", saved.getEmail(), saved.getTrackingId());
        return conseillerMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ConseillerResponse getConseiller(UUID trackingId) {
        return conseillerMapper.toResponse(findOrThrow(trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConseillerResponse> listerTous(Pageable pageable) {
        return conseillerRepository.findAllByEstActifTrue(pageable)
                .map(conseillerMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConseillerResponse> listerConseillersDispo(int seuil) {
        return conseillerRepository.findConseillersDisponibles(seuil)
                .stream()
                .map(conseillerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ConseillerResponse modifierConseiller(UUID trackingId, ConseillerRequest request) {
        Conseiller conseiller = findOrThrow(trackingId);

        // Mise à jour partielle via le Mapper
        conseillerMapper.updateFromRequest(request, conseiller);

        // Mise à jour du mot de passe uniquement si fourni
        if (request.getMotDePasse() != null && !request.getMotDePasse().isBlank()) {
            // TODO: activer le hachage (PasswordEncoder) avant la mise en production
            conseiller.setMotDePasseHash(request.getMotDePasse());
        }

        Conseiller saved = conseillerRepository.save(conseiller);
        log.info("Conseiller modifié : trackingId={}", trackingId);
        return conseillerMapper.toResponse(saved);
    }

    @Override
    public void desactiverConseiller(UUID trackingId) {
        Conseiller conseiller = findOrThrow(trackingId);
        conseiller.setEstActif(false);
        conseillerRepository.save(conseiller);
        log.info("Conseiller désactivé (soft-delete) : trackingId={}", trackingId);
    }

    // ─── Helper privé ─────────────────────────────────────────────────────────
    private Conseiller findOrThrow(UUID trackingId) {
        return conseillerRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Conseiller introuvable pour le trackingId : " + trackingId));
    }
}
