package tg.edtch.activEducation.diagnostic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.diagnostic.domain.entite.Quiz;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    /** Recherche par identifiant public. */
    Optional<Quiz> findByTrackingId(UUID trackingId);

    /** Liste paginée des quiz actifs. */
    Page<Quiz> findAllByEstActifTrue(Pageable pageable);
}
