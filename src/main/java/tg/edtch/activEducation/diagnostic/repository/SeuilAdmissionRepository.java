package tg.edtch.activEducation.diagnostic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.diagnostic.domain.entite.SeuilAdmission;

import java.util.List;

@Repository
public interface SeuilAdmissionRepository extends JpaRepository<SeuilAdmission, Long> {

    List<SeuilAdmission> findByFiliereId(Long filiereId);
}
