package tg.edtch.activEducation.shared.util;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "versions_historique")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VersionHistorique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_type", nullable = false, length = 100)
    private String itemType;

    @Column(name = "item_tracking_id", nullable = false, length = 36)
    private String itemTrackingId;

    @Column(name = "event", nullable = false, length = 50)
    private String event;

    @Column(name = "whodunnit", length = 100)
    private String whodunnit;

    @Column(name = "object_data", columnDefinition = "TEXT")
    private String objectData;

    @Column(name = "object_changes", columnDefinition = "TEXT")
    private String objectChanges;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
