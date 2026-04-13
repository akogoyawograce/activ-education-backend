package tg.edtch.activEducation.diagnostic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.diagnostic.domain.entite.ResultatDiagnostic;

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
}
