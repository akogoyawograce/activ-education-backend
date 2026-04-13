package tg.edtch.activEducation.profil.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.profil.domain.entite.Historique;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HistoriqueRepository extends JpaRepository<Historique, Long> {

    /** Recherche par identifiant public de l'entrée d'historique. */
    Optional<Historique> findByTrackingId(UUID trackingId);

    /**
     * Historique d'un utilisateur via son trackingId public, trié par date
     * décroissante.
     */
    List<Historique> findByUtilisateurTrackingIdOrderByCreatedAtDesc(UUID utilisateurTrackingId);

    /** Historique paginé d'un utilisateur via son trackingId public. */
    Page<Historique> findByUtilisateurTrackingIdOrderByCreatedAtDesc(UUID utilisateurTrackingId, Pageable pageable);

    /** Historique filtré par action pour un utilisateur donné (via trackingId). */
    List<Historique> findByUtilisateurTrackingIdAndActionOrderByCreatedAtDesc(UUID utilisateurTrackingId,
            String action);

    /** Méthode interne conservée pour compatibilité ascendante. */
    List<Historique> findByUtilisateurIdOrderByCreatedAtDesc(Long utilisateurId);
}
