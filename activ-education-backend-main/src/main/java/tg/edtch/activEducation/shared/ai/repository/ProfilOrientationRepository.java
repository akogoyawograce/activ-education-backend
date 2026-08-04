package tg.edtch.activEducation.shared.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.shared.ai.domain.entite.ProfilOrientation;

import java.util.Optional;

public interface ProfilOrientationRepository extends JpaRepository<ProfilOrientation, Long> {
    Optional<ProfilOrientation> findByUserId(String userId);
}
