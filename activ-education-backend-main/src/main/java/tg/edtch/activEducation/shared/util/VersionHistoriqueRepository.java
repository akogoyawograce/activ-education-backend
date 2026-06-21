package tg.edtch.activEducation.shared.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VersionHistoriqueRepository extends JpaRepository<VersionHistorique, Long> {
    List<VersionHistorique> findByItemTypeAndItemTrackingIdOrderByCreatedAtDesc(String itemType, String itemTrackingId);
    Page<VersionHistorique> findByItemTypeOrderByCreatedAtDesc(String itemType, Pageable pageable);
    void deleteByItemTrackingId(String itemTrackingId);
}
