package tg.edtch.activEducation.bibliotheque.domain.entite;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "liens_inter_fiches", indexes = {
        @Index(name = "idx_lien_source", columnList = "source_type, source_tracking_id"),
        @Index(name = "idx_lien_target", columnList = "target_type, target_tracking_id"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LienInterFiche {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private String trackingId = UUID.randomUUID().toString();

    @Column(name = "source_type", nullable = false, length = 50)
    private String sourceType;

    @Column(name = "source_tracking_id", nullable = false, length = 36)
    private String sourceTrackingId;

    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType;

    @Column(name = "target_tracking_id", nullable = false, length = 36)
    private String targetTrackingId;

    @Column(name = "type_lien", length = 50)
    private String typeLien;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
