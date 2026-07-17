package tg.edtch.activEducation.reseau.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.reseau.domain.entite.CommentaireReseau;

import java.util.Optional;
import java.util.UUID;

public interface CommentaireReseauRepository extends JpaRepository<CommentaireReseau, Long> {
    Optional<CommentaireReseau> findByTrackingId(UUID trackingId);
    Page<CommentaireReseau> findByPublicationTrackingIdOrderByCreatedAtDesc(String publicationTrackingId, Pageable pageable);
    int countByPublicationTrackingId(String publicationTrackingId);
    void deleteByTrackingId(UUID trackingId);
}
