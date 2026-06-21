package tg.edtch.activEducation.accompagnement.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.accompagnement.domain.entite.Message;
import tg.edtch.activEducation.accompagnement.domain.entite.Ticket;
import tg.edtch.activEducation.accompagnement.repository.MessageRepository;
import tg.edtch.activEducation.accompagnement.repository.TicketRepository;
import tg.edtch.activEducation.profil.domain.entite.Utilisateur;
import tg.edtch.activEducation.profil.repository.UtilisateurRepository;
import tg.edtch.activEducation.shared.security.exception.InvalidTokenException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final MessageRepository messageRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional
    public Ticket creerTicket(Long expediteurId, String sujet, String categorie) {
        Ticket ticket = Ticket.builder()
                .sujet(sujet)
                .categorie(categorie)
                .expediteurId(expediteurId)
                .statut("OUVERT")
                .priorite("NORMALE")
                .dateOuverture(LocalDateTime.now())
                .dateDerniereActivite(LocalDateTime.now())
                .build();
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket assignerTicket(UUID trackingId, Long conseillerId) {
        Ticket ticket = ticketRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new InvalidTokenException("Ticket introuvable"));
        ticket.setAssigneeAId(conseillerId);
        ticket.setStatut("ASSIGNE");
        ticket.setDateDerniereActivite(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket changerStatut(UUID trackingId, String nouveauStatut) {
        Ticket ticket = ticketRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new InvalidTokenException("Ticket introuvable"));
        ticket.setStatut(nouveauStatut);
        ticket.setDateDerniereActivite(LocalDateTime.now());
        if ("FERME".equals(nouveauStatut) || "RESOLU".equals(nouveauStatut)) {
            ticket.setDateFermeture(LocalDateTime.now());
        }
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Message ajouterMessage(UUID ticketTrackingId, Long expediteurId, String contenu) {
        Ticket ticket = ticketRepository.findByTrackingId(ticketTrackingId)
                .orElseThrow(() -> new InvalidTokenException("Ticket introuvable"));

        Utilisateur expediteur = utilisateurRepository.findById(expediteurId)
                .orElseThrow(() -> new InvalidTokenException("Expéditeur introuvable"));
        Long destinataireId = ticket.getAssigneeAId() != null ? ticket.getAssigneeAId()
                : (ticket.getExpediteurId().equals(expediteurId) ? 0L : ticket.getExpediteurId());
        Utilisateur destinataire = utilisateurRepository.findById(destinataireId)
                .orElseThrow(() -> new InvalidTokenException("Destinataire introuvable"));

        Message message = Message.builder()
                .expediteur(expediteur)
                .destinataire(destinataire)
                .contenu(contenu)
                .build();
        message = messageRepository.save(message);

        ticket.setDateDerniereActivite(LocalDateTime.now());
        if ("OUVERT".equals(ticket.getStatut()) || "ASSIGNE".equals(ticket.getStatut())) {
            ticket.setStatut("EN_COURS");
        }
        ticketRepository.save(ticket);

        return message;
    }

    public Page<Ticket> mesTickets(Long utilisateurId, Pageable pageable) {
        return ticketRepository.findByExpediteurIdOrderByDateDerniereActiviteDesc(utilisateurId, pageable);
    }

    public Page<Ticket> ticketsAssignes(Long conseillerId, Pageable pageable) {
        return ticketRepository.findByAssigneeAIdOrderByDateDerniereActiviteDesc(conseillerId, pageable);
    }

    public Page<Ticket> ticketsParStatut(String statut, Pageable pageable) {
        return ticketRepository.findByStatutOrderByDateDerniereActiviteDesc(statut, pageable);
    }

    public long countByStatut(String statut) {
        return ticketRepository.countByStatut(statut);
    }

    public Long trouverConseillerMoinsCharge() {
        List<Object[]> charge = ticketRepository.findConseillerChargeTravail();
        if (charge.isEmpty()) return null;
        return (Long) charge.get(0)[0];
    }
}
