package tg.edtch.activEducation.bibliotheque.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.bibliotheque.domain.entite.NiveauFiliere;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import tg.edtch.activEducation.profil.domain.enums.NiveauScolaire;

/**
 * Accès aux mappings {@link NiveauFiliere} (filière ↔ niveau éligible).
 */
@Repository
public interface NiveauFiliereRepository extends JpaRepository<NiveauFiliere, Long> {

    Optional<NiveauFiliere> findByTrackingId(UUID trackingId);

    List<NiveauFiliere> findByFicheFiliereId(Long ficheFiliereId);

    /** Filtre direct par niveau : utilisé par {@code GET /api/v1/filieres?niveau=...}. */
    List<NiveauFiliere> findByNiveau(NiveauScolaire niveau);

    boolean existsByFicheFiliereIdAndNiveau(Long ficheFiliereId,
            tg.edtch.activEducation.profil.domain.enums.NiveauScolaire niveau);
}
