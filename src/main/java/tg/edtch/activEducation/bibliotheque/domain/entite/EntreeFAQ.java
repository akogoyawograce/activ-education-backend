package tg.edtch.activEducation.bibliotheque.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.UUID;

/**
 * Entité représentant une entrée de la FAQ (Foire Aux Questions).
 */
@Entity
@Table(name = "entrees_faq", indexes = {
        @Index(name = "idx_faq_tracking_id", columnList = "tracking_id", unique = true),
        @Index(name = "idx_faq_categorie", columnList = "categorie"),
        @Index(name = "idx_faq_est_publie", columnList = "est_publie")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EntreeFAQ extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @Column(name = "reponse", nullable = false, columnDefinition = "TEXT")
    private String reponse;

    @Column(name = "categorie", length = 100)
    private String categorie;

    @Column(name = "est_publie", nullable = false)
    @Builder.Default
    private Boolean estPublie = false;

    @Column(name = "nb_vues")
    @Builder.Default
    private Long nbVues = 0L;

    @Column(name = "embedding", columnDefinition = "vector(768)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 768)
    private float[] embedding;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null) {
            this.trackingId = UUID.randomUUID();
        }
    }
}
