package tg.edtch.activEducation.accompagnement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.accompagnement.domain.entite.RendezVous;

import java.util.List;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    List<RendezVous> findByEleveIdOrderByDateHeurePrevueDesc(Long eleveId);

    List<RendezVous> findByConseillerIdOrderByDateHeurePrevueDesc(Long conseillerId);

    Page<RendezVous> findByEleveId(Long eleveId, Pageable pageable);

    Page<RendezVous> findByConseillerId(Long conseillerId, Pageable pageable);
}
