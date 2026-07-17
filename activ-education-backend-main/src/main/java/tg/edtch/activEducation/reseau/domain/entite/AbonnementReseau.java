package tg.edtch.activEducation.reseau.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "abonnements_reseau", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"abonne_tracking_id", "abonnement_tracking_id"})
}, indexes = {
    @Index(name = "idx_abonne", columnList = "abonne_tracking_id"),
    @Index(name = "idx_abonnement", columnList = "abonnement_tracking_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AbonnementReseau extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "abonne_tracking_id", nullable = false, length = 36)
    private String abonneTrackingId;

    @Column(name = "abonnement_tracking_id", nullable = false, length = 36)
    private String abonnementTrackingId;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null) this.trackingId = UUID.randomUUID();
    }
}
