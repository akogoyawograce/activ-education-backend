package tg.edtch.activEducation.profil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.profil.domain.entite.ConsentementParental;

import java.util.Optional;

public interface ConsentementParentalRepository extends JpaRepository<ConsentementParental, Long> {
    Optional<ConsentementParental> findByEleveId(Long eleveId);
    Optional<ConsentementParental> findByTokenValidation(String tokenValidation);
    boolean existsByEleveIdAndConsentiTrue(Long eleveId);
    void deleteByEleveId(Long eleveId);
}
