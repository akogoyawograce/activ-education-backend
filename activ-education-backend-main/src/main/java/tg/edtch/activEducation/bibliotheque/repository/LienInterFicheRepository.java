package tg.edtch.activEducation.bibliotheque.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.bibliotheque.domain.entite.LienInterFiche;

import java.util.List;

@Repository
public interface LienInterFicheRepository extends JpaRepository<LienInterFiche, Long> {
    List<LienInterFiche> findBySourceTypeAndSourceTrackingId(String sourceType, String sourceTrackingId);
    List<LienInterFiche> findByTargetTypeAndTargetTrackingId(String targetType, String targetTrackingId);
    void deleteBySourceTrackingIdOrTargetTrackingId(String sourceId, String targetId);
}
