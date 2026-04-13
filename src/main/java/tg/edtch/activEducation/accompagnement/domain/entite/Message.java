package tg.edtch.activEducation.accompagnement.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.profil.domain.entite.Utilisateur;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant un message dans la messagerie de la plateforme.
 */
@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_message_tracking_id", columnList = "tracking_id", unique = true),
        @Index(name = "idx_message_expediteur", columnList = "expediteur_id"),
        @Index(name = "idx_message_destinataire", columnList = "destinataire_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Message extends BaseEntity {

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

    @Column(name = "contenu", nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(name = "date_envoi", nullable = false)
    private LocalDateTime dateEnvoi;

    @Column(name = "lu", nullable = false)
    @Builder.Default
    private Boolean lu = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expediteur_id", nullable = false)
    private Utilisateur expediteur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Utilisateur destinataire;

    @PrePersist
    protected void onPrePersistMessage() {
        if (this.trackingId == null)
            this.trackingId = UUID.randomUUID();
        if (this.dateEnvoi == null)
            this.dateEnvoi = LocalDateTime.now();
        if (this.lu == null)
            this.lu = false;
    }
}
