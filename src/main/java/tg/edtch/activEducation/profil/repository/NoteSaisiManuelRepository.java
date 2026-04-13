package tg.edtch.activEducation.profil.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.profil.domain.entite.NoteSaisiManuel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteSaisiManuelRepository extends JpaRepository<NoteSaisiManuel, Long> {

    /** Recherche par identifiant public — jamais la PK interne. */
    Optional<NoteSaisiManuel> findByTrackingId(UUID trackingId);

    /**
     * Notes d'un élève via son trackingId public, triées par année scolaire
     * décroissante.
     */
    List<NoteSaisiManuel> findByEleveTrackingIdOrderByAnneeScolaireDesc(UUID eleveTrackingId);

    /** Notes d'un élève paginées via son trackingId public. */
    Page<NoteSaisiManuel> findByEleveTrackingId(UUID eleveTrackingId, Pageable pageable);

    /**
     * Méthode interne (usage administratif uniquement) — préférer
     * findByEleveTrackingId.
     */
    List<NoteSaisiManuel> findByEleveIdOrderByAnneeScolaireDesc(Long eleveId);
}
