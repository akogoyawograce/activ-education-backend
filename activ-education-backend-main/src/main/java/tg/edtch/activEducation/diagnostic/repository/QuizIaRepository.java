package tg.edtch.activEducation.diagnostic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.diagnostic.domain.entite.QuizIA;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizIaRepository extends JpaRepository<QuizIA, Long> {

    Optional<QuizIA> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    Optional<QuizIA> findByQuizTrackingId(UUID quizTrackingId);
}
