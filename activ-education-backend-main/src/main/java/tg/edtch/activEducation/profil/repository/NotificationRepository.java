package tg.edtch.activEducation.profil.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.profil.domain.entite.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Recherche par identifiant public de la notification. */
    Optional<Notification> findByTrackingId(UUID trackingId);

    /**
     * Toutes les notifications d'un utilisateur via son trackingId public, triées
     * par date décroissante.
     */
    List<Notification> findByUtilisateurTrackingIdOrderByCreatedAtDesc(UUID utilisateurTrackingId);

    /** Notifications paginées d'un utilisateur via son trackingId public. */
    Page<Notification> findByUtilisateurTrackingIdOrderByCreatedAtDesc(UUID utilisateurTrackingId, Pageable pageable);

    /** Notifications non lues d'un utilisateur. */
    List<Notification> findByUtilisateurTrackingIdAndLueFalseOrderByCreatedAtDesc(UUID utilisateurTrackingId);

    /**
     * Compte des notifications non lues d'un utilisateur via son trackingId public.
     */
    long countByUtilisateurTrackingIdAndLueFalse(UUID utilisateurTrackingId);

    /**
     * Marque toutes les notifications non lues d'un utilisateur comme lues (via
     * trackingId public).
     */
    @Modifying
    @Query("UPDATE Notification n SET n.lue = true WHERE n.utilisateur.trackingId = :utilisateurTrackingId AND n.lue = false")
    void markAllAsReadForUtilisateur(@Param("utilisateurTrackingId") UUID utilisateurTrackingId);
}
