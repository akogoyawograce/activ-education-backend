package tg.edtch.activEducation.diagnostic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.diagnostic.domain.entite.ResultatDiagnostic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResultatDiagnosticRepository extends JpaRepository<ResultatDiagnostic, Long> {

    /** Recherche par identifiant public. */
    Optional<ResultatDiagnostic> findByTrackingId(UUID trackingId);

    /** Résultats paginés d'un élève, triés par date décroissante. */
    Page<ResultatDiagnostic> findByEleveTrackingIdOrderByDatePassageDesc(UUID eleveTrackingId, Pageable pageable);

    /** Dernier résultat d'un élève pour un quiz spécifique. */
    Optional<ResultatDiagnostic> findFirstByEleveTrackingIdAndQuizTrackingIdOrderByDatePassageDesc(
            UUID eleveTrackingId, UUID quizTrackingId);

    /** Dernier résultat d'un élève tous quizzes confondus. */
    Optional<ResultatDiagnostic> findFirstByEleveTrackingIdOrderByDatePassageDesc(UUID eleveTrackingId);

    @Query("SELECT CAST(r.datePassage AS date), COUNT(r) FROM ResultatDiagnostic r WHERE r.datePassage >= :depuis GROUP BY CAST(r.datePassage AS date) ORDER BY CAST(r.datePassage AS date)")
    List<Object[]> compterResultatsParJour(@Param("depuis") LocalDateTime depuis);
}
