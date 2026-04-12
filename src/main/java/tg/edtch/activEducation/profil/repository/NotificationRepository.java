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

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUtilisateurIdOrderByCreatedAtDesc(Long utilisateurId);

    Page<Notification> findByUtilisateurIdOrderByCreatedAtDesc(Long utilisateurId, Pageable pageable);

    long countByUtilisateurIdAndLueFalse(Long utilisateurId);

    @Modifying
    @Query("UPDATE Notification n SET n.lue = true WHERE n.utilisateur.id = :utilisateurId AND n.lue = false")
    void markAllAsReadForUtilisateur(@Param("utilisateurId") Long utilisateurId);
}
