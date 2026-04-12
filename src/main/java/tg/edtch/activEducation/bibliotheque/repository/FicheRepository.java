package tg.edtch.activEducation.bibliotheque.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.bibliotheque.domain.entite.Fiche;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FicheRepository extends JpaRepository<Fiche, Long> {

    /** trackingId reste UUID (identifiant public, pas la PK). */
    Optional<Fiche> findByTrackingId(UUID trackingId);

    Page<Fiche> findAllByEstPublieTrue(Pageable pageable);

    @Query("SELECT f FROM Fiche f WHERE LOWER(f.titre) LIKE LOWER(CONCAT('%', :terme, '%')) AND f.estPublie = true")
    Page<Fiche> rechercherParTitre(@Param("terme") String terme, Pageable pageable);

    @Modifying
    @Query("UPDATE Fiche f SET f.nbConsultations = f.nbConsultations + 1 WHERE f.id = :id")
    void incrementerConsultations(@Param("id") Long id);
}
