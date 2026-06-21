package tg.edtch.activEducation.profil.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.profil.domain.entite.Administrateur;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdministrateurRepository extends JpaRepository<Administrateur, Long> {

    Optional<Administrateur> findByEmail(String email);

    /** Recherche par identifiant public — jamais la PK interne. */
    Optional<Administrateur> findByTrackingId(UUID trackingId);

    /** Liste paginée des administrateurs actifs. */
    Page<Administrateur> findAllByEstActifTrue(Pageable pageable);
}
