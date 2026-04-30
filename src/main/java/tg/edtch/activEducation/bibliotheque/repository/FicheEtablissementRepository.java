package tg.edtch.activEducation.bibliotheque.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

        @EntityGraph(attributePaths = {"filieresProposees", "imageUrls", "videoUrls", "documentUrls"})
        Page<FicheEtablissement> findAllByEstPublieTrue(Pageable pageable);

        @EntityGraph(attributePaths = {"filieresProposees", "imageUrls", "videoUrls", "documentUrls"})
        Page<FicheEtablissement> findByVilleIgnoreCaseAndEstPublieTrue(String ville, Pageable pageable);

        @EntityGraph(attributePaths = {"filieresProposees", "imageUrls", "videoUrls", "documentUrls"})
        Page<FicheEtablissement> findByTypeEtablissementAndEstPublieTrue(
                        FicheEtablissement.TypeEtablissement typeEtablissement, Pageable pageable);

        @Query("SELECT DISTINCT f.ville FROM FicheEtablissement f WHERE f.ville IS NOT NULL AND f.estPublie = true ORDER BY f.ville")
        List<String> findAllVilles();

        @EntityGraph(attributePaths = {"filieresProposees", "imageUrls", "videoUrls", "documentUrls"})
        @Query("SELECT f FROM FicheEtablissement f WHERE f.estPublie = true AND " +
                        "(LOWER(f.titre) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
                        " LOWER(f.ville) LIKE LOWER(CONCAT('%', :terme, '%')))")
        Page<FicheEtablissement> rechercherParTerme(@Param("terme") String terme, Pageable pageable);

        @EntityGraph(attributePaths = {"filieresProposees", "imageUrls", "videoUrls", "documentUrls"})
        Page<FicheEtablissement> findAll(Pageable pageable);

        @EntityGraph(attributePaths = {"filieresProposees", "imageUrls", "videoUrls", "documentUrls"})
        Page<FicheEtablissement> findAllByEstPublieFalse(Pageable pageable);
}
