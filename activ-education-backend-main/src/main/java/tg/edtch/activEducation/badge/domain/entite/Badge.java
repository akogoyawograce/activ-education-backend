package tg.edtch.activEducation.badge.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "badges", indexes = {
    @Index(name = "idx_badge_code", columnList = "code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Badge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 300)
    private String description;

    @Column(length = 50)
    private String icone;

    @Column(name = "categorie", length = 50)
    private String categorie;

    @Column(name = "condition_explication", length = 200)
    private String conditionExplication;

    @Column(name = "ordre_affichage")
    @Builder.Default
    private Integer ordreAffichage = 0;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null) this.trackingId = UUID.randomUUID();
    }
}
