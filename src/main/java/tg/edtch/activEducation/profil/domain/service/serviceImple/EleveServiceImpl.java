package tg.edtch.activEducation.profil.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.profil.domain.service.EleveService;
import tg.edtch.activEducation.profil.application.dto.request.EleveRequest;
import tg.edtch.activEducation.profil.application.dto.response.EleveResponse;
import tg.edtch.activEducation.profil.application.mapper.EleveMapper;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.domain.entite.Role;
import tg.edtch.activEducation.profil.domain.enums.RoleNom;
import tg.edtch.activEducation.profil.repository.EleveRepository;
import tg.edtch.activEducation.profil.repository.RoleRepository;
import tg.edtch.activEducation.profil.repository.UtilisateurRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Implémentation du service Eleve.
 * Toutes les opérations utilisent le {@code trackingId} (UUID) pour identifier
 * les élèves.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EleveServiceImpl implements EleveService {

    private final EleveRepository eleveRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final EleveMapper eleveMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public EleveResponse inscrireEleve(EleveRequest request) {
        // Vérification de l'unicité de l'email au niveau global (table utilisateurs)
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Un compte avec l'adresse email '" + request.getEmail() + "' existe déjà.");
        }

        if (request.getMotDePasse() == null || request.getMotDePasse().isBlank()) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire.");
        }
        if (request.getMotDePasse().length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères.");
        }

        // Construction de l'entité via le Mapper (génère le trackingId automatiquement)
        Eleve eleve = eleveMapper.toEntity(request);

        // Hachage du mot de passe
        eleve.setMotDePasseHash(passwordEncoder.encode(request.getMotDePasse()));

        // Association du rôle ROLE_ELEVE
        Role roleEleve = roleRepository.findByNom(RoleNom.ROLE_ELEVE)
                .orElseThrow(() -> new NoSuchElementException(
                        "Rôle ROLE_ELEVE introuvable. Vérifiez l'initialisation des données (DataLoader)."));
        eleve.getRoles().add(roleEleve);

        Eleve saved = eleveRepository.save(eleve);
        log.info("Nouvel élève inscrit : email={} trackingId={}", saved.getEmail(), saved.getTrackingId());
        return eleveMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EleveResponse getEleve(UUID trackingId) {
        return eleveMapper.toResponse(findOrThrow(trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public EleveResponse getEleveByEmail(String email) {
        return eleveMapper.toResponse(
                eleveRepository.findByEmail(email)
                        .orElseThrow(() -> new NoSuchElementException(
                                "Aucun élève trouvé avec l'email : " + email)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EleveResponse> listerTous(Pageable pageable) {
        return eleveRepository.findAllByEstActifTrue(pageable)
                .map(eleveMapper::toResponse);
    }

    @Override
    public EleveResponse modifierEleve(UUID trackingId, EleveRequest request) {
        Eleve eleve = findOrThrow(trackingId);

        // Mise à jour partielle des champs modifiables via le Mapper
        eleveMapper.updateFromRequest(request, eleve);

        // Mise à jour du mot de passe uniquement s'il est fourni et non vide
        if (request.getMotDePasse() != null && !request.getMotDePasse().isBlank()) {
            eleve.setMotDePasseHash(passwordEncoder.encode(request.getMotDePasse()));
        }

        Eleve saved = eleveRepository.save(eleve);
        log.info("Élève modifié : trackingId={}", trackingId);
        return eleveMapper.toResponse(saved);
    }

    @Override
    public void desactiverEleve(UUID trackingId) {
        Eleve eleve = findOrThrow(trackingId);
        eleve.setEstActif(false);
        eleveRepository.save(eleve);
        log.info("Élève désactivé (soft-delete) : trackingId={}", trackingId);
    }

    // ─── Helper privé ─────────────────────────────────────────────────────────
    private Eleve findOrThrow(UUID trackingId) {
        return eleveRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Élève introuvable pour le trackingId : " + trackingId));
    }
}
