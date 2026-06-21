package tg.edtch.activEducation.bibliotheque.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.profil.domain.entite.Utilisateur;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.UUID;

/**
 * Représente une fiche ajoutée aux favoris par un utilisateur.
 */
@Entity
@Table(name = "favoris", indexes = {
        @Index(name = "idx_favori_tracking_id", columnList = "tracking_id", unique = true),
        @Index(name = "idx_favori_utilisateur_id", columnList = "utilisateur_id"),
        @Index(name = "idx_favori_fiche_id", columnList = "fiche_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Favori extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fiche_id", nullable = false)
    private Fiche fiche;

    @Column(name = "note_personnelle")
    private String notePersonnelle;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null) {
            this.trackingId = UUID.randomUUID();
        }
    }
}
