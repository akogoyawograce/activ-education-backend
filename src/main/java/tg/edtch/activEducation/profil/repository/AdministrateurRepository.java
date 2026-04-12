package tg.edtch.activEducation.profil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.profil.domain.entite.Administrateur;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdministrateurRepository extends JpaRepository<Administrateur, Long> {

    Optional<Administrateur> findByEmail(String email);

    /** trackingId reste UUID (identifiant public, pas la PK). */
    Optional<Administrateur> findByTrackingId(UUID trackingId);
}
