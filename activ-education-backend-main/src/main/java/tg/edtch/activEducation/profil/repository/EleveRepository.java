package tg.edtch.activEducation.profil.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.profil.domain.entite.Eleve;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EleveRepository extends JpaRepository<Eleve, Long> {

    Optional<Eleve> findByEmail(String email);

    /** trackingId reste UUID (identifiant public, pas la PK). */
    Optional<Eleve> findByTrackingId(UUID trackingId);

    Page<Eleve> findAllByEstActifTrue(Pageable pageable);

    List<Eleve> findByNiveau(String niveau);

    List<Eleve> findByEtablissementContainingIgnoreCase(String etablissement);

    @Query("SELECT e FROM Eleve e JOIN e.parents p WHERE p.id = :parentId")
    List<Eleve> findByParentId(@Param("parentId") Long parentId);

    @Query("SELECT CAST(e.dateInscription AS date), COUNT(e) FROM Eleve e WHERE e.dateInscription >= :depuis GROUP BY CAST(e.dateInscription AS date) ORDER BY CAST(e.dateInscription AS date)")
    List<Object[]> compterInscriptionsParJour(@Param("depuis") LocalDateTime depuis);
}
