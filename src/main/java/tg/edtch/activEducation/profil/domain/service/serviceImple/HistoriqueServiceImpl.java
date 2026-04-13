package tg.edtch.activEducation.profil.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.profil.application.dto.request.HistoriqueRequest;
import tg.edtch.activEducation.profil.application.dto.response.HistoriqueResponse;
import tg.edtch.activEducation.profil.application.mapper.HistoriqueMapper;
import tg.edtch.activEducation.profil.domain.entite.Historique;
import tg.edtch.activEducation.profil.domain.entite.Utilisateur;
import tg.edtch.activEducation.profil.domain.service.HistoriqueService;
import tg.edtch.activEducation.profil.repository.HistoriqueRepository;
import tg.edtch.activEducation.profil.repository.UtilisateurRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service Historique.
 * L'historique est append-only — aucune modification des entrées existantes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HistoriqueServiceImpl implements HistoriqueService {

    private final HistoriqueRepository historiqueRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final HistoriqueMapper historiqueMapper;

    @Override
    public HistoriqueResponse enregistrer(UUID utilisateurTrackingId, HistoriqueRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByTrackingId(utilisateurTrackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Utilisateur introuvable pour le trackingId : " + utilisateurTrackingId));

        Historique historique = historiqueMapper.toEntity(request, utilisateur);
        Historique saved = historiqueRepository.save(historique);
        log.info("Historique enregistré : utilisateur={} action={} trackingId={}",
                utilisateurTrackingId, saved.getAction(), saved.getTrackingId());
        return historiqueMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public HistoriqueResponse getEntree(UUID trackingId) {
        return historiqueMapper.toResponse(
                historiqueRepository.findByTrackingId(trackingId)
                        .orElseThrow(() -> new NoSuchElementException(
                                "Entrée d'historique introuvable pour le trackingId : " + trackingId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriqueResponse> getHistoriqueUtilisateur(UUID utilisateurTrackingId) {
        return historiqueRepository
                .findByUtilisateurTrackingIdOrderByCreatedAtDesc(utilisateurTrackingId)
                .stream()
                .map(historiqueMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistoriqueResponse> getHistoriqueUtilisateurPagine(UUID utilisateurTrackingId, Pageable pageable) {
        return historiqueRepository
                .findByUtilisateurTrackingIdOrderByCreatedAtDesc(utilisateurTrackingId, pageable)
                .map(historiqueMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriqueResponse> getHistoriqueParAction(UUID utilisateurTrackingId, String action) {
        return historiqueRepository
                .findByUtilisateurTrackingIdAndActionOrderByCreatedAtDesc(utilisateurTrackingId, action)
                .stream()
                .map(historiqueMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void effacerHistoriqueUtilisateur(UUID utilisateurTrackingId) {
        List<Historique> entrees = historiqueRepository
                .findByUtilisateurTrackingIdOrderByCreatedAtDesc(utilisateurTrackingId);
        historiqueRepository.deleteAll(entrees);
        log.warn("Historique effacé (admin) pour l'utilisateur trackingId={} — {} entrées supprimées",
                utilisateurTrackingId, entrees.size());
    }
}
