package tg.edtch.activEducation.accompagnement.domain.service.serviceImple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.accompagnement.application.dto.request.MessageRequest;
import tg.edtch.activEducation.accompagnement.application.dto.response.MessageResponse;
import tg.edtch.activEducation.accompagnement.application.mapper.MessageMapper;
import tg.edtch.activEducation.accompagnement.domain.entite.Message;
import tg.edtch.activEducation.accompagnement.domain.service.MessageService;
import tg.edtch.activEducation.accompagnement.repository.MessageRepository;
import tg.edtch.activEducation.profil.domain.entite.Utilisateur;
import tg.edtch.activEducation.profil.repository.UtilisateurRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service de messagerie.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final MessageMapper messageMapper;

    @Override
    public MessageResponse envoyerMessage(UUID expediteurTrackingId, MessageRequest request) {
        if (expediteurTrackingId.equals(request.getDestinataireTrackingId())) {
            throw new IllegalArgumentException("Un utilisateur ne peut pas s'envoyer un message à lui-même.");
        }

        Utilisateur expediteur = findUtilisateurOrThrow(expediteurTrackingId, "Expéditeur");
        Utilisateur destinataire = findUtilisateurOrThrow(request.getDestinataireTrackingId(), "Destinataire");

        Message message = messageMapper.toEntity(request.getContenu(), expediteur, destinataire);
        Message saved = messageRepository.save(message);
        log.info("Message envoyé : expediteur={} destinataire={} trackingId={}",
                expediteurTrackingId, request.getDestinataireTrackingId(), saved.getTrackingId());
        return messageMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponse getMessage(UUID trackingId) {
        return messageMapper.toResponse(findMessageOrThrow(trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversation(UUID user1TrackingId, UUID user2TrackingId) {
        return messageRepository.findConversation(user1TrackingId, user2TrackingId)
                .stream()
                .map(messageMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessagesRecus(UUID destinataireTrackingId, Pageable pageable) {
        return messageRepository
                .findByDestinataireTrackingIdOrderByDateEnvoiDesc(destinataireTrackingId, pageable)
                .map(messageMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessagesEnvoyes(UUID expediteurTrackingId, Pageable pageable) {
        return messageRepository
                .findByExpediteurTrackingIdOrderByDateEnvoiDesc(expediteurTrackingId, pageable)
                .map(messageMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long compterNonLus(UUID destinataireTrackingId) {
        return messageRepository.countByDestinataireTrackingIdAndLuFalse(destinataireTrackingId);
    }

    @Override
    public void marquerConversationCommeLue(UUID expediteurTrackingId, UUID destinataireTrackingId) {
        messageRepository.markConversationAsRead(expediteurTrackingId, destinataireTrackingId);
        log.info("Conversation marquée comme lue : expediteur={} → destinataire={}",
                expediteurTrackingId, destinataireTrackingId);
    }

    @Override
    public void supprimerMessage(UUID trackingId) {
        Message message = findMessageOrThrow(trackingId);
        messageRepository.delete(message);
        log.info("Message supprimé (hard-delete) : trackingId={}", trackingId);
    }

    // ─── Helpers privés ───────────────────────────────────────────────────────
    private Utilisateur findUtilisateurOrThrow(UUID trackingId, String role) {
        return utilisateurRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        role + " introuvable pour le trackingId : " + trackingId));
    }

    private Message findMessageOrThrow(UUID trackingId) {
        return messageRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Message introuvable pour le trackingId : " + trackingId));
    }
}
