package tg.edtch.activEducation.badge.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "badges_devernes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"eleve_tracking_id", "badge_tracking_id"})
}, indexes = {
    @Index(name = "idx_badge_eleve", columnList = "eleve_tracking_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BadgeDecerne extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "eleve_tracking_id", nullable = false, length = 36)
    private String eleveTrackingId;

    @Column(name = "badge_tracking_id", nullable = false, length = 36)
    private String badgeTrackingId;

    @Column(name = "date_obtention", nullable = false)
    @Builder.Default
    private LocalDateTime dateObtention = LocalDateTime.now();

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null) this.trackingId = UUID.randomUUID();
    }
}
