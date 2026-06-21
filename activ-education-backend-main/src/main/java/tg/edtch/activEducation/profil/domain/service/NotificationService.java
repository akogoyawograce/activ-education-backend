package tg.edtch.activEducation.profil.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.profil.application.dto.request.NotificationRequest;
import tg.edtch.activEducation.profil.application.dto.response.NotificationResponse;

import java.util.List;
import java.util.UUID;

/**
 * Contrat de service pour la gestion des notifications utilisateur.
 * Tous les identifiants exposés sont des {@code UUID trackingId}.
 */
public interface NotificationService {

    /**
     * Envoie une notification à un utilisateur identifié par son trackingId.
     */
    NotificationResponse envoyer(UUID utilisateurTrackingId, NotificationRequest request);

    /**
     * Récupère une notification par son trackingId public.
     */
    NotificationResponse getNotification(UUID trackingId);

    /**
     * Retourne toutes les notifications d'un utilisateur, triées par date
     * décroissante.
     */
    List<NotificationResponse> getNotificationsUtilisateur(UUID utilisateurTrackingId);

    /**
     * Retourne les notifications paginées d'un utilisateur.
     */
    Page<NotificationResponse> getNotificationsPagine(UUID utilisateurTrackingId, Pageable pageable);

    /**
     * Retourne uniquement les notifications non lues d'un utilisateur.
     */
    List<NotificationResponse> getNonLues(UUID utilisateurTrackingId);

    /**
     * Retourne le nombre de notifications non lues d'un utilisateur.
     */
    long compterNonLues(UUID utilisateurTrackingId);

    /**
     * Marque une notification spécifique comme lue.
     */
    NotificationResponse marquerCommeLue(UUID trackingId);

    /**
     * Marque toutes les notifications non lues d'un utilisateur comme lues.
     */
    void marquerToutesCommeLues(UUID utilisateurTrackingId);

    /**
     * Supprime définitivement une notification.
     */
    void supprimerNotification(UUID trackingId);
}
