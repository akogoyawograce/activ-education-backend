package tg.edtch.activEducation.profil.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.profil.domain.entite.Historique;

import java.util.List;

@Repository
public interface HistoriqueRepository extends JpaRepository<Historique, Long> {

    List<Historique> findByUtilisateurIdOrderByCreatedAtDesc(Long utilisateurId);

    Page<Historique> findByUtilisateurIdOrderByCreatedAtDesc(Long utilisateurId, Pageable pageable);
}
