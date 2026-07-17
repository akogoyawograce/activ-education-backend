package tg.edtch.activEducation.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.portfolio.domain.entite.PortfolioCompetence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioCompetenceRepository extends JpaRepository<PortfolioCompetence, Long> {
    Optional<PortfolioCompetence> findByTrackingId(UUID trackingId);
    List<PortfolioCompetence> findByEleveTrackingIdOrderByCategorieAscNiveauEstimeDesc(String eleveTrackingId);
    List<PortfolioCompetence> findByEleveTrackingIdAndCategorie(String eleveTrackingId, String categorie);
    void deleteByTrackingId(UUID trackingId);
}
