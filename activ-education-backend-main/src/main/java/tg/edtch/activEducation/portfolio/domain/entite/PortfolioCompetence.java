package tg.edtch.activEducation.portfolio.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "portfolio_competences", indexes = {
    @Index(name = "idx_portfolio_eleve", columnList = "eleve_tracking_id"),
    @Index(name = "idx_portfolio_categorie", columnList = "categorie")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PortfolioCompetence extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "eleve_tracking_id", nullable = false, length = 36)
    private String eleveTrackingId;

    @Column(nullable = false, length = 100)
    private String titre;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 50)
    private String categorie;

    @Column(name = "niveau_estime", nullable = false)
    private Integer niveauEstime;

    @Column(length = 200)
    private String source;

    @Column(name = "est_visible", nullable = false)
    @Builder.Default
    private Boolean estVisible = true;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null) this.trackingId = UUID.randomUUID();
    }
}
