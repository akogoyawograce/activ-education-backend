package tg.edtch.activEducation.diagnostic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.diagnostic.domain.entite.ResultatDiagnostic;

import java.util.Optional;

@Repository
public interface ResultatDiagnosticRepository extends JpaRepository<ResultatDiagnostic, Long> {

    Page<ResultatDiagnostic> findByEleveIdOrderByDatePassageDesc(Long eleveId, Pageable pageable);

    Optional<ResultatDiagnostic> findFirstByEleveIdAndQuizIdOrderByDatePassageDesc(Long eleveId, Long quizId);
}
