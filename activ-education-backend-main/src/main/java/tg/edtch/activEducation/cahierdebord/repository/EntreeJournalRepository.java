package tg.edtch.activEducation.cahierdebord.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.cahierdebord.domain.entite.EntreeJournal;
import java.util.Optional;
import java.util.UUID;

public interface EntreeJournalRepository extends JpaRepository<EntreeJournal, Long> {
    Optional<EntreeJournal> findByTrackingId(UUID trackingId);
    Page<EntreeJournal> findByEleveTrackingIdOrderByDateEntreeDesc(String eleveTrackingId, Pageable pageable);
    Page<EntreeJournal> findByEleveTrackingIdAndTypeEntree(String eleveTrackingId, String typeEntree, Pageable pageable);
    void deleteByTrackingId(UUID trackingId);
}
