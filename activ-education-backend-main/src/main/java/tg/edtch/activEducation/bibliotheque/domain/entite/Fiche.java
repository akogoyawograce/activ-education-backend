package tg.edtch.activEducation.bibliotheque.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Classe abstraite mère de toutes les fiches de la bibliothèque d'exploration.
 * Stratégie d'héritage JOINED: chaque sous-fiche a sa propre table liée
 * à la table fiches via la clé primaire.
 */

@Entity
@Table(name = "fiches", indexes = {
        @Index(name = "idx_fiche_tracking_id", columnList = "tracking_id", unique = true),
        @Index(name = "idx_fiche_est_publie", columnList = "est_publie")
})
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Fiche extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /**
     * Identifiant public pour les URLs et partages.
     */
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "titre", nullable = false, length = 255)
    private String titre;

    /**
     * Court résumé affiché dans les listes et résultats de recherche.
     */
    @Column(name = "resume", length = 500)
    private String resume;

    /**
     * Contenu principal de la fiche (Markdown ou HTML).
     */
    @Column(name = "contenu", columnDefinition = "TEXT")
    private String contenu;

    /**
     * URLs vers des vidéos explicatives associées (YouTube, Vimeo ou upload MinIO).
     */
    @ElementCollection
    @CollectionTable(name = "fiche_videos", joinColumns = @JoinColumn(name = "fiche_id"))
    @Column(name = "video_url", length = 500)
    @Builder.Default
    private Set<String> videoUrls = new HashSet<>();

    /**
     * URLs vers les images miniatures / bannières de la fiche.
     */
    @ElementCollection
    @CollectionTable(name = "fiche_images", joinColumns = @JoinColumn(name = "fiche_id"))
    @Column(name = "image_url", length = 500)
    @Builder.Default
    private Set<String> imageUrls = new HashSet<>();

    /**
     * URLs vers des documents joints (PDF, DOCX, etc.).
     */
    @ElementCollection
    @CollectionTable(name = "fiche_documents", joinColumns = @JoinColumn(name = "fiche_id"))
    @Column(name = "document_url", length = 500)
    @Builder.Default
    private Set<String> documentUrls = new HashSet<>();

    /**
     * Indique si la fiche est visible par le public.
     */
    @Column(name = "est_publie", nullable = false)
    @Builder.Default
    private Boolean estPublie = false;

    /**
     * Nombre de fois que cette fiche a été consultée.
     */
    @Column(name = "nb_consultations")
    @Builder.Default
    private Long nbConsultations = 0L;

    /**
     * Vecteur d'embedding (768 dimensions) généré par OpenAI pour la recherche
     * sémantique.
     * Permet une recherche par similarité cosinus via pgvector.
     */
    @Column(name = "embedding")
    private float[] embedding;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null) {
            this.trackingId = UUID.randomUUID();
        }
    }

    @Transient
    public abstract String getTypeResultat();
}
