package tg.edtch.activEducation.diagnostic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tg.edtch.activEducation.diagnostic.domain.entite.Reponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReponseRepository extends JpaRepository<Reponse, Long> {

    /** Recherche par identifiant public. */
    Optional<Reponse> findByTrackingId(UUID trackingId);

    /** Toutes les options de réponse d'une question (via son trackingId public). */
    List<Reponse> findByQuestionTrackingId(UUID questionTrackingId);
}
