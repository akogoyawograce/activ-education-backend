package tg.edtch.activEducation.profil.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.profil.application.dto.request.ParentRequest;
import tg.edtch.activEducation.profil.application.dto.response.ParentResponse;
import tg.edtch.activEducation.profil.application.mapper.ParentMapper;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.entite.Parent;
import tg.edtch.activEducation.profil.domain.entite.Role;
import tg.edtch.activEducation.profil.domain.enums.RoleNom;
import tg.edtch.activEducation.profil.domain.service.ParentService;
import tg.edtch.activEducation.profil.repository.EleveRepository;
import tg.edtch.activEducation.profil.repository.ParentRepository;
import tg.edtch.activEducation.profil.repository.RoleRepository;
import tg.edtch.activEducation.profil.repository.UtilisateurRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service Parent.
 * La relation parent-enfant est gérée exclusivement via les trackingId UUID.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ParentServiceImpl implements ParentService {

    private final ParentRepository parentRepository;
    private final EleveRepository eleveRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ParentMapper parentMapper;

    @Override
    public ParentResponse creerParent(ParentRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Un compte avec l'email '" + request.getEmail() + "' existe déjà.");
        }

        Parent parent = parentMapper.toEntity(request);
        parent.setMotDePasseHash(passwordEncoder.encode(request.getMotDePasse()));

        // Rôle ROLE_PARENT
        Role roleParent = roleRepository.findByNom(RoleNom.ROLE_PARENT)
                .orElseThrow(() -> new NoSuchElementException(
                        "Rôle ROLE_PARENT introuvable. Vérifiez l'initialisation des données (DataLoader)."));
        parent.getRoles().add(roleParent);

        // Résolution des enfants via leurs trackingId (si fournis)
        if (request.getEnfantsTrackingIds() != null && !request.getEnfantsTrackingIds().isEmpty()) {
            List<Eleve> enfants = request.getEnfantsTrackingIds().stream()
                    .map(tid -> eleveRepository.findByTrackingId(tid)
                            .orElseThrow(() -> new NoSuchElementException(
                                    "Élève introuvable pour le trackingId : " + tid)))
                    .collect(Collectors.toList());
            parent.getEnfants().addAll(enfants);
        }

        Parent saved = parentRepository.save(parent);
        log.info("Nouveau parent créé : email={} trackingId={}", saved.getEmail(), saved.getTrackingId());
        return parentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ParentResponse getParent(UUID trackingId) {
        return parentMapper.toResponse(findOrThrow(trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ParentResponse> listerTous(Pageable pageable) {
        return parentRepository.findAllByEstActifTrue(pageable)
                .map(parentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentResponse> getParentsParEleve(UUID eleveTrackingId) {
        return parentRepository.findParentsByEleveTrackingId(eleveTrackingId)
                .stream()
                .map(parentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ParentResponse modifierParent(UUID trackingId, ParentRequest request) {
        Parent parent = findOrThrow(trackingId);
        parentMapper.updateFromRequest(request, parent);

        if (request.getMotDePasse() != null && !request.getMotDePasse().isBlank()) {
            parent.setMotDePasseHash(passwordEncoder.encode(request.getMotDePasse()));
        }

        Parent saved = parentRepository.save(parent);
        log.info("Parent modifié : trackingId={}", trackingId);
        return parentMapper.toResponse(saved);
    }

    @Override
    public ParentResponse ajouterEnfant(UUID parentTrackingId, UUID eleveTrackingId) {
        Parent parent = findOrThrow(parentTrackingId);
        Eleve eleve = eleveRepository.findByTrackingId(eleveTrackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Élève introuvable pour le trackingId : " + eleveTrackingId));

        boolean dejaRattache = parent.getEnfants().stream()
                .anyMatch(e -> e.getTrackingId().equals(eleveTrackingId));
        if (dejaRattache) {
            throw new IllegalStateException("Cet élève est déjà rattaché à ce parent.");
        }

        parent.getEnfants().add(eleve);
        Parent saved = parentRepository.save(parent);
        log.info("Enfant ajouté : parentTrackingId={} eleveTrackingId={}", parentTrackingId, eleveTrackingId);
        return parentMapper.toResponse(saved);
    }

    @Override
    public ParentResponse retirerEnfant(UUID parentTrackingId, UUID eleveTrackingId) {
        Parent parent = findOrThrow(parentTrackingId);

        boolean removed = parent.getEnfants()
                .removeIf(e -> e.getTrackingId().equals(eleveTrackingId));
        if (!removed) {
            throw new NoSuchElementException(
                    "Aucun lien trouvé entre ce parent et l'élève trackingId : " + eleveTrackingId);
        }

        Parent saved = parentRepository.save(parent);
        log.info("Enfant retiré : parentTrackingId={} eleveTrackingId={}", parentTrackingId, eleveTrackingId);
        return parentMapper.toResponse(saved);
    }

    @Override
    public void desactiverParent(UUID trackingId) {
        Parent parent = findOrThrow(trackingId);
        parent.setEstActif(false);
        parentRepository.save(parent);
        log.info("Parent désactivé (soft-delete) : trackingId={}", trackingId);
    }

    // ─── Helper privé ─────────────────────────────────────────────────────────
    private Parent findOrThrow(UUID trackingId) {
        return parentRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Parent introuvable pour le trackingId : " + trackingId));
    }
}
