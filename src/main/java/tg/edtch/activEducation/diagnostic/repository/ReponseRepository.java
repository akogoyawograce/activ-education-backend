package tg.edtch.activEducation.diagnostic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.diagnostic.domain.entite.Reponse;

import java.util.List;

@Repository
public interface ReponseRepository extends JpaRepository<Reponse, Long> {

    List<Reponse> findByQuestionId(Long questionId);
}
