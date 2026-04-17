package tg.edtch.activEducation.diagnostic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.diagnostic.domain.entite.ScoreMatrice;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScoreMatriceRepository extends JpaRepository<ScoreMatrice, Long> {

    /** Recherche par identifiant public. */
    Optional<ScoreMatrice> findByTrackingId(UUID trackingId);

    /** Liste paginée de toutes les matrices. */
    Page<ScoreMatrice> findAllBy(Pageable pageable);
}
