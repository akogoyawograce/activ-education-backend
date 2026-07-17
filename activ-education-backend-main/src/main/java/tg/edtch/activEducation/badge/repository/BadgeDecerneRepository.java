package tg.edtch.activEducation.badge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.badge.domain.entite.BadgeDecerne;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BadgeDecerneRepository extends JpaRepository<BadgeDecerne, Long> {
    List<BadgeDecerne> findByEleveTrackingIdOrderByDateObtentionDesc(String eleveTrackingId);
    Optional<BadgeDecerne> findByEleveTrackingIdAndBadgeTrackingId(String eleveTrackingId, String badgeTrackingId);
    boolean existsByEleveTrackingIdAndBadgeTrackingId(String eleveTrackingId, String badgeTrackingId);
    int countByEleveTrackingId(String eleveTrackingId);
}
