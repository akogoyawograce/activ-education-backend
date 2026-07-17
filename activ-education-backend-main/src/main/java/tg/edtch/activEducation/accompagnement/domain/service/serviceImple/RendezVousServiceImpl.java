package tg.edtch.activEducation.accompagnement.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.accompagnement.application.dto.request.RendezVousRequest;
import tg.edtch.activEducation.accompagnement.application.dto.response.RendezVousResponse;
import tg.edtch.activEducation.accompagnement.application.mapper.RendezVousMapper;
import tg.edtch.activEducation.accompagnement.domain.entite.RendezVous;
import tg.edtch.activEducation.accompagnement.domain.entite.RendezVous.StatutRendezVous;
import tg.edtch.activEducation.accompagnement.domain.service.RendezVousService;
import tg.edtch.activEducation.accompagnement.repository.RendezVousRepository;
import tg.edtch.activEducation.profil.domain.entite.Conseiller;
import tg.edtch.activEducation.profil.domain.entite.Eleve;
import tg.edtch.activEducation.profil.repository.ConseillerRepository;
import tg.edtch.activEducation.profil.repository.EleveRepository;
import tg.edtch.activEducation.shared.visio.VisioService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service RendezVous.
 * Gestion du cycle de vie : PLANIFIE → TERMINE | ANNULE.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RendezVousServiceImpl implements RendezVousService {

    private final RendezVousRepository rendezVousRepository;
    private final EleveRepository eleveRepository;
    private final ConseillerRepository conseillerRepository;
    private final RendezVousMapper rendezVousMapper;
    private final VisioService visioService;

    @Override
    public RendezVousResponse planifier(RendezVousRequest request) {
        Eleve eleve = eleveRepository.findByTrackingId(request.getEleveTrackingId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Élève introuvable pour le trackingId : " + request.getEleveTrackingId()));

        Conseiller conseiller = conseillerRepository.findByTrackingId(request.getConseillerTrackingId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Conseiller introuvable pour le trackingId : " + request.getConseillerTrackingId()));

        RendezVous rdv = rendezVousMapper.toEntity(request, eleve, conseiller);
        if (rdv.getLienVisio() == null || rdv.getLienVisio().isBlank()) {
            rdv.setLienVisio(visioService.genererLienVisio());
        }
        RendezVous saved = rendezVousRepository.save(rdv);
        log.info("Rendez-vous planifié : élève={} conseiller={} le={} trackingId={} lienVisio={}",
                request.getEleveTrackingId(), request.getConseillerTrackingId(),
                saved.getDateHeurePrevue(), saved.getTrackingId(), saved.getLienVisio());
        return rendezVousMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RendezVousResponse getRendezVous(UUID trackingId) {
        return rendezVousMapper.toResponse(findOrThrow(trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponse> getRendezVousEleve(UUID eleveTrackingId) {
        return rendezVousRepository.findByEleveTrackingIdOrderByDateHeurePrevueDesc(eleveTrackingId)
                .stream().map(rendezVousMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponse> getRendezVousConseiller(UUID conseillerTrackingId) {
        return rendezVousRepository.findByConseillerTrackingIdOrderByDateHeurePrevueDesc(conseillerTrackingId)
                .stream().map(rendezVousMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RendezVousResponse> getRendezVousElevePagine(UUID eleveTrackingId, Pageable pageable) {
        return rendezVousRepository.findByEleveTrackingId(eleveTrackingId, pageable)
                .map(rendezVousMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RendezVousResponse> getRendezVousConseillerPagine(UUID conseillerTrackingId, Pageable pageable) {
        return rendezVousRepository.findByConseillerTrackingId(conseillerTrackingId, pageable)
                .map(rendezVousMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponse> getRendezVousEleveParStatut(UUID eleveTrackingId, String statut) {
        StatutRendezVous statutEnum = parseStatut(statut);
        return rendezVousRepository
                .findByEleveTrackingIdAndStatutOrderByDateHeurePrevueDesc(eleveTrackingId, statutEnum)
                .stream().map(rendezVousMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponse> getRendezVousConseillerParStatut(UUID conseillerTrackingId, String statut) {
        StatutRendezVous statutEnum = parseStatut(statut);
        return rendezVousRepository
                .findByConseillerTrackingIdAndStatutOrderByDateHeurePrevueDesc(conseillerTrackingId, statutEnum)
                .stream().map(rendezVousMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public RendezVousResponse modifierRendezVous(UUID trackingId, RendezVousRequest request) {
        RendezVous rdv = findOrThrow(trackingId);
        if (rdv.getStatut() != StatutRendezVous.PLANIFIE) {
            throw new IllegalStateException(
                    "Seul un rendez-vous PLANIFIÉ peut être modifié. Statut actuel : " + rdv.getStatut());
        }
        rendezVousMapper.updateFromRequest(request, rdv);
        RendezVous saved = rendezVousRepository.save(rdv);
        log.info("Rendez-vous modifié : trackingId={}", trackingId);
        return rendezVousMapper.toResponse(saved);
    }

    @Override
    public RendezVousResponse terminer(UUID trackingId) {
        RendezVous rdv = findOrThrow(trackingId);
        if (rdv.getStatut() != StatutRendezVous.PLANIFIE) {
            throw new IllegalStateException(
                    "Seul un rendez-vous PLANIFIÉ peut être terminé. Statut actuel : " + rdv.getStatut());
        }
        rdv.setStatut(StatutRendezVous.TERMINE);
        RendezVous saved = rendezVousRepository.save(rdv);
        log.info("Rendez-vous terminé : trackingId={}", trackingId);
        return rendezVousMapper.toResponse(saved);
    }

    @Override
    public RendezVousResponse annuler(UUID trackingId) {
        RendezVous rdv = findOrThrow(trackingId);
        if (rdv.getStatut() == StatutRendezVous.TERMINE) {
            throw new IllegalStateException("Un rendez-vous déjà TERMINÉ ne peut pas être annulé.");
        }
        rdv.setStatut(StatutRendezVous.ANNULE);
        RendezVous saved = rendezVousRepository.save(rdv);
        log.info("Rendez-vous annulé : trackingId={}", trackingId);
        return rendezVousMapper.toResponse(saved);
    }

    @Override
    public RendezVousResponse genererLienVisio(UUID trackingId) {
        RendezVous rdv = findOrThrow(trackingId);
        if (rdv.getStatut() != StatutRendezVous.PLANIFIE) {
            throw new IllegalStateException(
                    "Un rendez-vous " + rdv.getStatut() + " ne peut pas recevoir de lien visio.");
        }
        rdv.setLienVisio(visioService.genererLienVisio());
        RendezVous saved = rendezVousRepository.save(rdv);
        log.info("Lien visio généré pour le rendez-vous : trackingId={} lien={}", trackingId, saved.getLienVisio());
        return rendezVousMapper.toResponse(saved);
    }

    // ─── Helpers privés ───────────────────────────────────────────────────────
    private RendezVous findOrThrow(UUID trackingId) {
        return rendezVousRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Rendez-vous introuvable pour le trackingId : " + trackingId));
    }

    private StatutRendezVous parseStatut(String statut) {
        try {
            return StatutRendezVous.valueOf(statut.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Statut invalide : '" + statut + "'. Valeurs acceptées : PLANIFIE, TERMINE, ANNULE");
        }
    }
}
