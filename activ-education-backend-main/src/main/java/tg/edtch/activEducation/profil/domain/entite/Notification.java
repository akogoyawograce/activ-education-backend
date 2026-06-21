package tg.edtch.activEducation.profil.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.UUID;

/**
 * Entité représentant une notification destinée à un utilisateur.
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_tracking_id", columnList = "tracking_id", unique = true),
        @Index(name = "idx_notification_utilisateur_id", columnList = "utilisateur_id"),
        @Index(name = "idx_notification_lue", columnList = "lue")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /**
     * Identifiant public — seul identifiant exposé à l'extérieur.
     */
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "titre", nullable = false, length = 200)
    private String titre;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /** Indique si la notification a été lue par l'utilisateur. */
    @Column(name = "lue", nullable = false)
    @Builder.Default
    private Boolean lue = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null)
            this.trackingId = UUID.randomUUID();
        if (this.lue == null)
            this.lue = false;
    }
}
