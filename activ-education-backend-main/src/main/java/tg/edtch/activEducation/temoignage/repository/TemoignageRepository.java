package tg.edtch.activEducation.temoignage.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.temoignage.domain.entite.Temoignage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemoignageRepository extends JpaRepository<Temoignage, Long> {
    Optional<Temoignage> findByTrackingId(UUID trackingId);
    Page<Temoignage> findByEstPublieTrueOrderByEstEnVedetteDescCreatedAtDesc(Pageable pageable);
    Page<Temoignage> findByEstPublieTrueAndMetierTrackingIdOrderByCreatedAtDesc(String metierTrackingId, Pageable pageable);
    Page<Temoignage> findByEstPublieTrueAndFiliereTrackingIdOrderByCreatedAtDesc(String filiereTrackingId, Pageable pageable);
    List<Temoignage> findTop3ByEstPublieTrueAndEstEnVedetteTrueOrderByCreatedAtDesc();
    Page<Temoignage> findAllByOrderByCreatedAtDesc(Pageable pageable);
    void deleteByTrackingId(UUID trackingId);
    long countByEstPublieTrue();
}
