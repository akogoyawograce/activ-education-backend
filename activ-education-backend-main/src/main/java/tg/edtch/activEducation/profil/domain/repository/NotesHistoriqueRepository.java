package tg.edtch.activEducation.profil.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.profil.domain.entite.NotesHistorique;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotesHistoriqueRepository extends JpaRepository<NotesHistorique, Long> {

    Optional<NotesHistorique> findByTrackingId(UUID trackingId);

    List<NotesHistorique> findByEleveIdOrderByAnneeScolaireDesc(Long eleveId);

    /**
     * Renvoie uniquement les lignes "moyenne générale" (les plus utiles pour
     * calculer la trajectoire). Tri par année scolaire DESC pour avoir
     * n, n-1, n-2 dans l'ordre.
     */
    List<NotesHistorique> findByEleveIdAndEstMoyenneGeneraleTrueOrderByAnneeScolaireDesc(
            Long eleveId);
}
