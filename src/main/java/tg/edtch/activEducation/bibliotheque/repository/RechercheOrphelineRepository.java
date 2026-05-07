package tg.edtch.activEducation.bibliotheque.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.bibliotheque.domain.entite.RechercheOrpheline;

import java.util.List;

@Repository
public interface RechercheOrphelineRepository extends JpaRepository<RechercheOrpheline, Long> {

    @Query("SELECT r.terme, COUNT(r) as frequence FROM RechercheOrpheline r GROUP BY r.terme ORDER BY frequence DESC")
    List<Object[]> trouverTermesLesPlusFrequents(Pageable pageable);
}
