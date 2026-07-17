package tg.edtch.activEducation.shared.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tg.edtch.activEducation.shared.ai.domain.entite.OriaMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OriaMessageRepository extends JpaRepository<OriaMessage, Long> {
    List<OriaMessage> findBySessionIdOrderByMessageTimestampAsc(String sessionId);
    Optional<OriaMessage> findByTrackingId(UUID trackingId);
    void deleteBySessionId(String sessionId);
}
