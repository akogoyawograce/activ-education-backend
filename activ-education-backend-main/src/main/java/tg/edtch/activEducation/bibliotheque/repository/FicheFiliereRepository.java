package tg.edtch.activEducation.bibliotheque.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

        @EntityGraph(attributePaths = { "metiersPrepares", "imageUrls", "videoUrls", "documentUrls" })
        Page<FicheFiliere> findAllByEstPublieTrue(Pageable pageable);

        @EntityGraph(attributePaths = { "metiersPrepares", "imageUrls", "videoUrls", "documentUrls" })
        Page<FicheFiliere> findByDomaineIgnoreCaseAndEstPublieTrue(String domaine, Pageable pageable);

        @EntityGraph(attributePaths = { "metiersPrepares", "imageUrls", "videoUrls", "documentUrls" })
        @Query("SELECT f FROM FicheFiliere f WHERE f.estPublie = true AND " +
                        "(LOWER(f.titre) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
                        " LOWER(f.resume) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
                        " LOWER(f.contenu) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
                        " LOWER(f.domaine) LIKE LOWER(CONCAT('%', :terme, '%')))")
        Page<FicheFiliere> rechercherParTerme(@org.springframework.data.repository.query.Param("terme") String terme,
                        Pageable pageable);

        List<FicheFiliere> findByNiveauRequisContainingIgnoreCaseAndEstPublieTrue(String niveauRequis);

        @Query("SELECT DISTINCT f.domaine FROM FicheFiliere f WHERE f.domaine IS NOT NULL AND f.estPublie = true ORDER BY f.domaine")
        List<String> findAllDomaines();

        @EntityGraph(attributePaths = { "metiersPrepares", "imageUrls", "videoUrls", "documentUrls" })
        Page<FicheFiliere> findAllByEstPublieFalse(Pageable pageable);

        @EntityGraph(attributePaths = { "metiersPrepares", "imageUrls", "videoUrls", "documentUrls" })
        Page<FicheFiliere> findAll(Pageable pageable);
}
