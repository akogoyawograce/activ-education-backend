package tg.edtch.activEducation.accompagnement.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.accompagnement.application.dto.request.DisponibiliteRequest;
import tg.edtch.activEducation.accompagnement.application.dto.response.DisponibiliteResponse;
import tg.edtch.activEducation.accompagnement.application.mapper.DisponibiliteMapper;
import tg.edtch.activEducation.accompagnement.domain.entite.Disponibilite;
import tg.edtch.activEducation.accompagnement.domain.service.DisponibiliteService;
import tg.edtch.activEducation.accompagnement.repository.DisponibiliteRepository;
import tg.edtch.activEducation.profil.domain.entite.Conseiller;
import tg.edtch.activEducation.profil.repository.ConseillerRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service Disponibilite.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DisponibiliteServiceImpl implements DisponibiliteService {

    private final DisponibiliteRepository disponibiliteRepository;
    private final ConseillerRepository conseillerRepository;
    private final DisponibiliteMapper disponibiliteMapper;

    @Override
    public DisponibiliteResponse ajouterDisponibilite(UUID conseillerTrackingId, DisponibiliteRequest request) {
        Conseiller conseiller = conseillerRepository.findByTrackingId(conseillerTrackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Conseiller introuvable pour le trackingId : " + conseillerTrackingId));

        // Validation métier : heure de fin doit être après heure de début
        if (request.getHeureFin().isBefore(request.getHeureDebut())
                || request.getHeureFin().equals(request.getHeureDebut())) {
            throw new IllegalArgumentException("L'heure de fin doit être postérieure à l'heure de début.");
        }

        Disponibilite dispo = disponibiliteMapper.toEntity(request, conseiller);
        Disponibilite saved = disponibiliteRepository.save(dispo);
        log.info("Disponibilité ajoutée : conseiller={} jour={} {}–{} trackingId={}",
                conseillerTrackingId, saved.getJourSemaine(), saved.getHeureDebut(), saved.getHeureFin(),
                saved.getTrackingId());
        return disponibiliteMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibiliteResponse getDisponibilite(UUID trackingId) {
        return disponibiliteMapper.toResponse(findOrThrow(trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisponibiliteResponse> getDisponibilitesConseiller(UUID conseillerTrackingId) {
        return disponibiliteRepository
                .findByConseillerTrackingIdOrderByJourSemaineAscHeureDebutAsc(conseillerTrackingId)
                .stream()
                .map(disponibiliteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DisponibiliteResponse> getDisponibilitesConseilleurPagine(UUID conseillerTrackingId,
            Pageable pageable) {
        return disponibiliteRepository
                .findByConseillerTrackingId(conseillerTrackingId, pageable)
                .map(disponibiliteMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisponibiliteResponse> getDisponibilitesParJour(UUID conseillerTrackingId, Integer jourSemaine) {
        return disponibiliteRepository
                .findByConseillerTrackingIdAndJourSemaineOrderByHeureDebutAsc(conseillerTrackingId, jourSemaine)
                .stream()
                .map(disponibiliteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DisponibiliteResponse modifierDisponibilite(UUID trackingId, DisponibiliteRequest request) {
        Disponibilite dispo = findOrThrow(trackingId);

        if (request.getHeureFin() != null && request.getHeureDebut() != null
                && !request.getHeureFin().isAfter(request.getHeureDebut())) {
            throw new IllegalArgumentException("L'heure de fin doit être postérieure à l'heure de début.");
        }

        disponibiliteMapper.updateFromRequest(request, dispo);
        Disponibilite saved = disponibiliteRepository.save(dispo);
        log.info("Disponibilité modifiée : trackingId={}", trackingId);
        return disponibiliteMapper.toResponse(saved);
    }

    @Override
    public void supprimerDisponibilite(UUID trackingId) {
        Disponibilite dispo = findOrThrow(trackingId);
        disponibiliteRepository.delete(dispo);
        log.info("Disponibilité supprimée (hard-delete) : trackingId={}", trackingId);
    }

    private Disponibilite findOrThrow(UUID trackingId) {
        return disponibiliteRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Disponibilité introuvable pour le trackingId : " + trackingId));
    }
}
