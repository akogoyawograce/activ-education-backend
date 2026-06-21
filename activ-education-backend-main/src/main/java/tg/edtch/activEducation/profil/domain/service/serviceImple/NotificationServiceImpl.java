package tg.edtch.activEducation.profil.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.profil.application.dto.request.NotificationRequest;
import tg.edtch.activEducation.profil.application.dto.response.NotificationResponse;
import tg.edtch.activEducation.profil.application.mapper.NotificationMapper;
import tg.edtch.activEducation.profil.domain.entite.Notification;
import tg.edtch.activEducation.profil.domain.entite.Utilisateur;
import tg.edtch.activEducation.profil.domain.service.NotificationService;
import tg.edtch.activEducation.profil.repository.NotificationRepository;
import tg.edtch.activEducation.profil.repository.UtilisateurRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service Notification.
 * Toutes les opérations utilisent des trackingId UUID publics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public NotificationResponse envoyer(UUID utilisateurTrackingId, NotificationRequest request) {
        Utilisateur destinataire = utilisateurRepository.findByTrackingId(utilisateurTrackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Utilisateur introuvable pour le trackingId : " + utilisateurTrackingId));

        Notification notification = notificationMapper.toEntity(request, destinataire);
        Notification saved = notificationRepository.save(notification);
        log.info("Notification envoyée : destinataire={} titre='{}' trackingId={}",
                utilisateurTrackingId, saved.getTitre(), saved.getTrackingId());
        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotification(UUID trackingId) {
        return notificationMapper.toResponse(findOrThrow(trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsUtilisateur(UUID utilisateurTrackingId) {
        return notificationRepository
                .findByUtilisateurTrackingIdOrderByCreatedAtDesc(utilisateurTrackingId)
                .stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsPagine(UUID utilisateurTrackingId, Pageable pageable) {
        return notificationRepository
                .findByUtilisateurTrackingIdOrderByCreatedAtDesc(utilisateurTrackingId, pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNonLues(UUID utilisateurTrackingId) {
        return notificationRepository
                .findByUtilisateurTrackingIdAndLueFalseOrderByCreatedAtDesc(utilisateurTrackingId)
                .stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long compterNonLues(UUID utilisateurTrackingId) {
        return notificationRepository.countByUtilisateurTrackingIdAndLueFalse(utilisateurTrackingId);
    }

    @Override
    public NotificationResponse marquerCommeLue(UUID trackingId) {
        Notification notification = findOrThrow(trackingId);
        notification.setLue(true);
        Notification saved = notificationRepository.save(notification);
        log.debug("Notification marquée comme lue : trackingId={}", trackingId);
        return notificationMapper.toResponse(saved);
    }

    @Override
    public void marquerToutesCommeLues(UUID utilisateurTrackingId) {
        notificationRepository.markAllAsReadForUtilisateur(utilisateurTrackingId);
        log.info("Toutes les notifications marquées comme lues pour l'utilisateur : trackingId={}",
                utilisateurTrackingId);
    }

    @Override
    public void supprimerNotification(UUID trackingId) {
        Notification notification = findOrThrow(trackingId);
        notificationRepository.delete(notification);
        log.info("Notification supprimée : trackingId={}", trackingId);
    }

    // ─── Helper privé ─────────────────────────────────────────────────────────
    private Notification findOrThrow(UUID trackingId) {
        return notificationRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Notification introuvable pour le trackingId : " + trackingId));
    }
}
