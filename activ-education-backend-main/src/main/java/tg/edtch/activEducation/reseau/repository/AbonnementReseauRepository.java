package tg.edtch.activEducation.reseau.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.reseau.domain.entite.AbonnementReseau;

import java.util.List;
import java.util.Optional;

public interface AbonnementReseauRepository extends JpaRepository<AbonnementReseau, Long> {
    List<AbonnementReseau> findByAbonneTrackingId(String abonneTrackingId);
    List<AbonnementReseau> findByAbonnementTrackingId(String abonnementTrackingId);
    Optional<AbonnementReseau> findByAbonneTrackingIdAndAbonnementTrackingId(String abonne, String abonnement);
    boolean existsByAbonneTrackingIdAndAbonnementTrackingId(String abonne, String abonnement);
    int countByAbonnementTrackingId(String abonnementTrackingId);
}
