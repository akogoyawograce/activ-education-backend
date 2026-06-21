package tg.edtch.activEducation.diagnostic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.diagnostic.domain.entite.Question;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /** Recherche par identifiant public. */
    Optional<Question> findByTrackingId(UUID trackingId);

    /**
     * Toutes les questions d'un quiz (via son trackingId public), triées par ordre.
     */
    List<Question> findByQuizTrackingIdOrderByOrdreAsc(UUID quizTrackingId);
}
