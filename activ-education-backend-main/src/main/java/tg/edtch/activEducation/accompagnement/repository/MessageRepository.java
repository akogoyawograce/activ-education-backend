package tg.edtch.activEducation.accompagnement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.accompagnement.domain.entite.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /** Recherche par identifiant public du message. */
    Optional<Message> findByTrackingId(UUID trackingId);

    /**
     * Récupère la conversation entre deux utilisateurs (via leurs trackingId
     * publics),
     * triée chronologiquement.
     */
    @Query("SELECT m FROM Message m WHERE " +
            "(m.expediteur.trackingId = :user1 AND m.destinataire.trackingId = :user2) OR " +
            "(m.expediteur.trackingId = :user2 AND m.destinataire.trackingId = :user1) " +
            "ORDER BY m.dateEnvoi ASC")
    List<Message> findConversation(@Param("user1") UUID user1TrackingId,
            @Param("user2") UUID user2TrackingId);

    /** Messages reçus par un utilisateur, paginés. */
    Page<Message> findByDestinataireTrackingIdOrderByDateEnvoiDesc(UUID destinataireTrackingId, Pageable pageable);

    /** Messages envoyés par un utilisateur, paginés. */
    Page<Message> findByExpediteurTrackingIdOrderByDateEnvoiDesc(UUID expediteurTrackingId, Pageable pageable);

    /** Nombre de messages non lus pour un destinataire. */
    long countByDestinataireTrackingIdAndLuFalse(UUID destinataireTrackingId);

    /**
     * Marque comme lus tous les messages d'une conversation envoyés par user1 à
     * user2.
     */
    @Modifying
    @Query("UPDATE Message m SET m.lu = true WHERE " +
            "m.expediteur.trackingId = :expediteurTrackingId AND " +
            "m.destinataire.trackingId = :destinataireTrackingId AND m.lu = false")
    void markConversationAsRead(@Param("expediteurTrackingId") UUID expediteurTrackingId,
            @Param("destinataireTrackingId") UUID destinataireTrackingId);
}
