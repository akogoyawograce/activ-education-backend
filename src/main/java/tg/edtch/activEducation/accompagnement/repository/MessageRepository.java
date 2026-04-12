package tg.edtch.activEducation.accompagnement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.accompagnement.domain.entite.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE (m.expediteur.id = :user1_id AND m.destinataire.id = :user2_id) " +
            "OR (m.expediteur.id = :user2_id AND m.destinataire.id = :user1_id) " +
            "ORDER BY m.dateEnvoi ASC")
    List<Message> findConversation(@Param("user1_id") Long user1Id, @Param("user2_id") Long user2Id);

    Page<Message> findByDestinataireIdOrderByDateEnvoiDesc(Long destinataireId, Pageable pageable);

    long countByDestinataireIdAndLuFalse(Long destinataireId);
}
