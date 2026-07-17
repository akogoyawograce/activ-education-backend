package tg.edtch.activEducation.shared.ai.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "oria_messages", indexes = {
    @Index(name = "idx_oria_session_id", columnList = "session_id"),
    @Index(name = "idx_oria_tracking_id", columnList = "tracking_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OriaMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "contenu", nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(name = "message_timestamp", nullable = false)
    private Instant messageTimestamp;

    @Column(name = "user_id", length = 100)
    private String userId;
}
