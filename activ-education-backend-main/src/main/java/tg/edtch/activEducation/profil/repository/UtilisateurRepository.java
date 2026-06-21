package tg.edtch.activEducation.profil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.profil.domain.entite.Utilisateur;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);

    /** trackingId reste UUID (identifiant public, pas la PK). */
    Optional<Utilisateur> findByTrackingId(UUID trackingId);

    @Query("SELECT u FROM Utilisateur u WHERE u.email = :email AND u.estActif = true")
    Optional<Utilisateur> findActiveUserByEmail(@Param("email") String email);
}
