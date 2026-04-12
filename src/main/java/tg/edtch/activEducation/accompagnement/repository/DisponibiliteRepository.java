package tg.edtch.activEducation.accompagnement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.accompagnement.domain.entite.Disponibilite;

import java.util.List;

@Repository
public interface DisponibiliteRepository extends JpaRepository<Disponibilite, Long> {

    List<Disponibilite> findByConseillerId(Long conseillerId);

    List<Disponibilite> findByConseillerIdAndJourSemaineOrderByHeureDebutAsc(Long conseillerId, Integer jourSemaine);
}
