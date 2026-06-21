package tg.edtch.activEducation.accompagnement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import tg.edtch.activEducation.accompagnement.domain.entite.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTrackingId(UUID trackingId);

    Page<Ticket> findByExpediteurIdOrderByDateDerniereActiviteDesc(Long expediteurId, Pageable pageable);

    Page<Ticket> findByAssigneeAIdOrderByDateDerniereActiviteDesc(Long assigneeAId, Pageable pageable);

    Page<Ticket> findByStatutOrderByDateDerniereActiviteDesc(String statut, Pageable pageable);

    List<Ticket> findByStatutIn(List<String> statuts);

    long countByStatut(String statut);

    long countByAssigneeAIdAndStatutIn(Long assigneeId, List<String> statuts);

    @Query("SELECT t.assigneeAId, COUNT(t) FROM Ticket t WHERE t.statut IN ('OUVERT', 'ASSIGNE', 'EN_COURS') GROUP BY t.assigneeAId ORDER BY COUNT(t) ASC")
    List<Object[]> findConseillerChargeTravail();

    @Modifying
    @Query("UPDATE Ticket t SET t.statut = :statut, t.dateDerniereActivite = CURRENT_TIMESTAMP WHERE t.id = :id")
    void updateStatut(Long id, String statut);
}
