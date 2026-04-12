package tg.edtch.activEducation.profil.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.profil.domain.entite.NoteSaisiManuel;

import java.util.List;

@Repository
public interface NoteSaisiManuelRepository extends JpaRepository<NoteSaisiManuel, Long> {

    List<NoteSaisiManuel> findByEleveIdOrderByAnneeScolaireDesc(Long eleveId);

    Page<NoteSaisiManuel> findByEleveId(Long eleveId, Pageable pageable);
}
