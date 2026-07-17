package tg.edtch.activEducation.reseau.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "publications_reseau", indexes = {
    @Index(name = "idx_pub_auteur", columnList = "auteur_tracking_id"),
    @Index(name = "idx_pub_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PublicationReseau extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "auteur_tracking_id", nullable = false, length = 36)
    private String auteurTrackingId;

    @Column(name = "auteur_nom", length = 100)
    private String auteurNom;

    @Column(name = "auteur_role", length = 20)
    private String auteurRole;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(name = "type_publication", length = 30)
    @Builder.Default
    private String typePublication = "PUBLICATION";

    @Column(length = 200)
    private String tags;

    @Builder.Default
    private int nombreReactions = 0;

    @Builder.Default
    private int nombreCommentaires = 0;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null) this.trackingId = UUID.randomUUID();
    }
}
