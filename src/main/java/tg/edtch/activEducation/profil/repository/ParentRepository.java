package tg.edtch.activEducation.profil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.profil.domain.entite.Parent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {

    Optional<Parent> findByEmail(String email);

    /** trackingId reste UUID (identifiant public, pas la PK). */
    Optional<Parent> findByTrackingId(UUID trackingId);

    @Query("SELECT p FROM Parent p JOIN p.enfants e WHERE e.id = :eleveId")
    List<Parent> findParentsByEleveId(@Param("eleveId") Long eleveId);
}
