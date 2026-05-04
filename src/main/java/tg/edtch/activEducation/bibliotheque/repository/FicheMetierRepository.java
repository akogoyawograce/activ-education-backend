package tg.edtch.activEducation.bibliotheque.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheMetier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FicheMetierRepository extends JpaRepository<FicheMetier, Long> {

    Optional<FicheMetier> findByTrackingId(UUID trackingId);

    @EntityGraph(attributePaths = { "filieresPreparantes", "imageUrls", "videoUrls", "documentUrls" })
    Page<FicheMetier> findAllByEstPublieTrue(Pageable pageable);

    @EntityGraph(attributePaths = { "filieresPreparantes", "imageUrls", "videoUrls", "documentUrls" })
    Page<FicheMetier> findBySecteurIgnoreCaseAndEstPublieTrue(String secteur, Pageable pageable);

    @EntityGraph(attributePaths = { "filieresPreparantes", "imageUrls", "videoUrls", "documentUrls" })
    @Query("SELECT f FROM FicheMetier f WHERE f.estPublie = true AND " +
            "(LOWER(f.titre) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
            " LOWER(f.resume) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
            " LOWER(f.contenu) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
            " LOWER(f.secteur) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
            " LOWER(f.missions) LIKE LOWER(CONCAT('%', :terme, '%')))")
    Page<FicheMetier> rechercherParTerme(@Param("terme") String terme, Pageable pageable);

    @Query("SELECT DISTINCT f.secteur FROM FicheMetier f WHERE f.secteur IS NOT NULL AND f.estPublie = true ORDER BY f.secteur")
    List<String> findAllSecteurs();

    @EntityGraph(attributePaths = { "filieresPreparantes", "imageUrls", "videoUrls", "documentUrls" })
    Page<FicheMetier> findAllByEstPublieFalse(Pageable pageable);

    @EntityGraph(attributePaths = { "filieresPreparantes", "imageUrls", "videoUrls", "documentUrls" })
    Page<FicheMetier> findAll(Pageable pageable);
}
