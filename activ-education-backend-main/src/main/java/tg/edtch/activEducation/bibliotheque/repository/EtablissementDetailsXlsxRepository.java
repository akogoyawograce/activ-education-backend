package tg.edtch.activEducation.bibliotheque.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.bibliotheque.domain.entite.EtablissementDetailsXlsx;

import java.util.Optional;

@Repository
public interface EtablissementDetailsXlsxRepository extends JpaRepository<EtablissementDetailsXlsx, Long> {

    Optional<EtablissementDetailsXlsx> findByFicheId(Long ficheId);

    Optional<EtablissementDetailsXlsx> findByIdSourceXlsx(String idSourceXlsx);
}
