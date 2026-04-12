package tg.edtch.activEducation.bibliotheque.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheFiliere;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FicheFiliereRepository extends JpaRepository<FicheFiliere, Long> {

    Optional<FicheFiliere> findByTrackingId(UUID trackingId);

    Page<FicheFiliere> findAllByEstPublieTrue(Pageable pageable);

    List<FicheFiliere> findByDomaineIgnoreCaseAndEstPublieTrue(String domaine);

    List<FicheFiliere> findByNiveauRequisContainingIgnoreCaseAndEstPublieTrue(String niveauRequis);

    @Query("SELECT DISTINCT f.domaine FROM FicheFiliere f WHERE f.domaine IS NOT NULL AND f.estPublie = true ORDER BY f.domaine")
    List<String> findAllDomaines();
}
