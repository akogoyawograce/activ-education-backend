package tg.edtch.activEducation.reseau.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "commentaires_reseau", indexes = {
    @Index(name = "idx_comment_publication", columnList = "publication_tracking_id"),
    @Index(name = "idx_comment_auteur", columnList = "auteur_tracking_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CommentaireReseau extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "publication_tracking_id", nullable = false, length = 36)
    private String publicationTrackingId;

    @Column(name = "auteur_tracking_id", nullable = false, length = 36)
    private String auteurTrackingId;

    @Column(name = "auteur_nom", length = 100)
    private String auteurNom;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null) this.trackingId = UUID.randomUUID();
    }
}
