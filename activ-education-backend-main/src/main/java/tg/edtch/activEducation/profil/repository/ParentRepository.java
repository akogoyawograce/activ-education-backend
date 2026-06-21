package tg.edtch.activEducation.profil.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /** Recherche par identifiant public — jamais la PK interne. */
    Optional<Parent> findByTrackingId(UUID trackingId);

    /** Liste paginée des parents actifs. */
    Page<Parent> findAllByEstActifTrue(Pageable pageable);

    /**
     * Retrouve les parents d'un élève via son id interne (usage interne
     * uniquement).
     */
    @Query("SELECT p FROM Parent p JOIN p.enfants e WHERE e.id = :eleveId")
    List<Parent> findParentsByEleveId(@Param("eleveId") Long eleveId);

    /** Retrouve les parents d'un élève via le trackingId public de l'élève. */
    @Query("SELECT p FROM Parent p JOIN p.enfants e WHERE e.trackingId = :eleveTrackingId")
    List<Parent> findParentsByEleveTrackingId(@Param("eleveTrackingId") UUID eleveTrackingId);
}
