package tg.edtch.activEducation.profil.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.UUID;

/**
 * Entité représentant une entrée dans l'historique d'activité d'un utilisateur
 * (ex: connexion, test complété, document uploadé...).
 * L'historique est en lecture seule — une entrée n'est jamais modifiée.
 */
@Entity
@Table(name = "historique_utilisateur", indexes = {
        @Index(name = "idx_historique_tracking_id", columnList = "tracking_id", unique = true),
        @Index(name = "idx_historique_utilisateur_id", columnList = "utilisateur_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Historique extends BaseEntity {

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

    /**
     * Type d'action effectuée.
     * Ex: "CONNEXION", "TEST_RIASEC", "UPLOAD_DOCUMENT", "SAISIE_NOTE".
     */
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    /**
     * Détails supplémentaires ou métadonnées JSON si pertinent.
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null) {
            this.trackingId = UUID.randomUUID();
        }
    }
}
