package tg.edtch.activEducation.reseau.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tg.edtch.activEducation.reseau.domain.entite.PublicationReseau;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PublicationReseauRepository extends JpaRepository<PublicationReseau, Long> {
    Optional<PublicationReseau> findByTrackingId(UUID trackingId);
    Page<PublicationReseau> findByAuteurTrackingIdOrderByCreatedAtDesc(String auteurTrackingId, Pageable pageable);
    Page<PublicationReseau> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM PublicationReseau p ORDER BY p.nombreReactions DESC")
    Page<PublicationReseau> findTendances(Pageable pageable);

    @Query("SELECT p FROM PublicationReseau p WHERE p.auteurTrackingId IN :abonnements ORDER BY p.createdAt DESC")
    Page<PublicationReseau> findFeedAbonnements(@Param("abonnements") List<String> abonnements, Pageable pageable);

    void deleteByTrackingId(UUID trackingId);
}
