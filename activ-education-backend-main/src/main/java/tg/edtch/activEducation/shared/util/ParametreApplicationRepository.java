package tg.edtch.activEducation.shared.util;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParametreApplicationRepository extends JpaRepository<ParametreApplication, Long> {
    Optional<ParametreApplication> findByCle(String cle);
}
