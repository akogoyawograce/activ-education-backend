package tg.edtch.activEducation.accompagnement.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.accompagnement.application.dto.request.MessageRequest;
import tg.edtch.activEducation.accompagnement.application.dto.response.MessageResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Contrat de service pour la messagerie de la plateforme.
 * Tous les identifiants exposés sont des {@code UUID trackingId}.
 */
public interface MessageService {

    /**
     * Envoie un message depuis un expéditeur (identifié par son trackingId dans
     * l'URL)
     * vers un destinataire (identifié dans le body par son trackingId).
     */
    MessageResponse envoyerMessage(UUID expediteurTrackingId, MessageRequest request);

    /**
     * Récupère un message par son trackingId public.
     */
    MessageResponse getMessage(UUID trackingId);

    /**
     * Retourne la conversation complète entre deux utilisateurs, triée
     * chronologiquement.
     */
    List<MessageResponse> getConversation(UUID user1TrackingId, UUID user2TrackingId);

    /**
     * Retourne les messages reçus par un utilisateur, paginés.
     */
    Page<MessageResponse> getMessagesRecus(UUID destinataireTrackingId, Pageable pageable);

    /**
     * Retourne les messages envoyés par un utilisateur, paginés.
     */
    Page<MessageResponse> getMessagesEnvoyes(UUID expediteurTrackingId, Pageable pageable);

    /**
     * Retourne le nombre de messages non lus pour un utilisateur.
     */
    long compterNonLus(UUID destinataireTrackingId);

    /**
     * Marque comme lus tous les messages envoyés par {@code expediteurTrackingId}
     * à {@code destinataireTrackingId} dans le cadre d'une conversation.
     */
    void marquerConversationCommeLue(UUID expediteurTrackingId, UUID destinataireTrackingId);

    /**
     * Supprime définitivement un message (l'expéditeur ne peut supprimer que ses
     * propres messages).
     */
    void supprimerMessage(UUID trackingId);
}
