package tg.edtch.activEducation.bibliotheque.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheSerie;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FicheSerieRepository extends JpaRepository<FicheSerie, Long> {

    Optional<FicheSerie> findByTrackingId(UUID trackingId);

    Page<FicheSerie> findAllByEstPublieTrue(Pageable pageable);

    List<FicheSerie> findByNiveauIgnoreCaseAndEstPublieTrue(String niveau);

    @Query("SELECT f FROM FicheSerie f WHERE f.estPublie = true AND LOWER(f.titre) LIKE LOWER(CONCAT('%', :motCle, '%'))")
    Page<FicheSerie> rechercherParMotCle(@org.springframework.data.repository.query.Param("motCle") String motCle,
            Pageable pageable);
}
