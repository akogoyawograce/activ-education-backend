package tg.edtch.activEducation.profil.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.profil.domain.entite.Conseiller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConseillerRepository extends JpaRepository<Conseiller, Long> {

    Optional<Conseiller> findByEmail(String email);

    /** trackingId reste UUID (identifiant public, pas la PK). */
    Optional<Conseiller> findByTrackingId(UUID trackingId);

    Page<Conseiller> findAllByEstActifTrue(Pageable pageable);

    @Query("SELECT c FROM Conseiller c WHERE c.chargeTravail < :seuil AND c.estActif = true")
    List<Conseiller> findConseillersDisponibles(@Param("seuil") int seuil);

    List<Conseiller> findBySpecialitesContainingIgnoreCase(String specialite);

    Optional<Conseiller> findFirstByEstActifTrueOrderByChargeTravailAsc();
}
