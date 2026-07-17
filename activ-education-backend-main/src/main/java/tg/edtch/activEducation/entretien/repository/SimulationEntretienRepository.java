package tg.edtch.activEducation.entretien.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.entretien.domain.entite.SimulationEntretien;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SimulationEntretienRepository extends JpaRepository<SimulationEntretien, Long> {
    Optional<SimulationEntretien> findByTrackingId(UUID trackingId);
    List<SimulationEntretien> findByEleveTrackingIdOrderByCreatedAtDesc(String eleveTrackingId);
}
