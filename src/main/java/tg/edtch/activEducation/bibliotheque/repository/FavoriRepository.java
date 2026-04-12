package tg.edtch.activEducation.bibliotheque.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.bibliotheque.domain.entite.Favori;

import java.util.Optional;

@Repository
public interface FavoriRepository extends JpaRepository<Favori, Long> {

    Page<Favori> findByUtilisateurId(Long utilisateurId, Pageable pageable);

    Optional<Favori> findByUtilisateurIdAndFicheId(Long utilisateurId, Long ficheId);

    boolean existsByUtilisateurIdAndFicheId(Long utilisateurId, Long ficheId);

    void deleteByUtilisateurIdAndFicheId(Long utilisateurId, Long ficheId);

    @Query("SELECT COUNT(f) FROM Favori f WHERE f.fiche.id = :ficheId")
    long countByFicheId(@Param("ficheId") Long ficheId);
}
