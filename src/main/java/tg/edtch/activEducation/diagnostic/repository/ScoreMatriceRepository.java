package tg.edtch.activEducation.diagnostic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.diagnostic.domain.entite.ScoreMatrice;

@Repository
public interface ScoreMatriceRepository extends JpaRepository<ScoreMatrice, Long> {

}
