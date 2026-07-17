package tg.edtch.activEducation.badge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.badge.domain.entite.Badge;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    Optional<Badge> findByTrackingId(UUID trackingId);
    Optional<Badge> findByCode(String code);
    List<Badge> findAllByOrderByOrdreAffichageAsc();
}
