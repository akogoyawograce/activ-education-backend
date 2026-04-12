package tg.edtch.activEducation.bibliotheque.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheEtablissement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FicheEtablissementRepository extends JpaRepository<FicheEtablissement, Long> {

        Optional<FicheEtablissement> findByTrackingId(UUID trackingId);

        Page<FicheEtablissement> findAllByEstPublieTrue(Pageable pageable);

        List<FicheEtablissement> findByVilleIgnoreCaseAndEstPublieTrue(String ville);

        List<FicheEtablissement> findByRegionIgnoreCaseAndEstPublieTrue(String region);

        List<FicheEtablissement> findByTypeEtablissementAndEstPublieTrue(
                        FicheEtablissement.TypeEtablissement typeEtablissement);

        @Query("SELECT DISTINCT f.ville FROM FicheEtablissement f WHERE f.ville IS NOT NULL AND f.estPublie = true ORDER BY f.ville")
        List<String> findAllVilles();

        @Query("SELECT f FROM FicheEtablissement f WHERE f.estPublie = true AND " +
                        "(LOWER(f.titre) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
                        " LOWER(f.ville) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
                        " LOWER(f.region) LIKE LOWER(CONCAT('%', :terme, '%')))")
        Page<FicheEtablissement> rechercherParTerme(@Param("terme") String terme, Pageable pageable);
}
