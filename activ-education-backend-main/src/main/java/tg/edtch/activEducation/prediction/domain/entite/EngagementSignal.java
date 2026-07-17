package tg.edtch.activEducation.prediction.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Agrégat par (élève, fiche) des signaux d'intérêt comportemental :
 * consultations, favoris, recherches RAG.
 *
 * <p>C'est le 3ᵉ signal du moteur de recommandation (Phase 3). Vu son rôle
 * auxiliaire (plafond 20-25 % recommandé en Phase 0), il n'est pas mis à
 * jour en temps réel — un batch quotidien consolide les consultations et
 * favoris depuis {@code historique_utilisateur} et {@code favoris}.</p>
 *
 * <p>Le champ {@code fiche_id} est volontairement polymorphe : on ne pointe
 * pas vers une table de fiches précise (4 sous-types héritant de
 * {@code Fiche} en JOINED). On précise le type via {@link #ficheType}.</p>
 *
 * <p>Voir {@code CHANGELOG_SCHEMA.md} § 5.</p>
 */
@Entity
@Table(name = "engagement_signal",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_engagement_eleve_fiche",
           columnNames = {"eleve_id", "fiche_id", "fiche_type"}),
       indexes = {
           @Index(name = "idx_engagement_eleve",     columnList = "eleve_id"),
           @Index(name = "idx_engagement_fiche",     columnList = "fiche_id,fiche_type"),
           @Index(name = "idx_engagement_derniere",  columnList = "derniere_consultation")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EngagementSignal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "eleve_id", nullable = false)
    private Long eleveId;

    @Column(name = "fiche_id", nullable = false)
    private Long ficheId;

    @Enumerated(EnumType.STRING)
    @Column(name = "fiche_type", nullable = false, length = 30)
    private TypeFiche ficheType;

    @Column(name = "nb_consultations", nullable = false)
    @Builder.Default
    private Integer nbConsultations = 0;

    @Column(name = "en_favori", nullable = false)
    @Builder.Default
    private Boolean enFavori = false;

    /** Temps de lecture moyen en secondes (agrégé sur les 30 derniers jours). */
    @Column(name = "temps_lecture_moyen_secondes")
    private Integer tempsLectureMoyenSecondes;

    @Column(name = "derniere_consultation")
    private LocalDateTime derniereConsultation;

    /**
     * Score de similarité cosinus entre la dernière recherche RAG de
     * l'élève et l'embedding de la fiche (0 à 1).
     */
    @Column(name = "score_similarite_recherche", precision = 5, scale = 3)
    private BigDecimal scoreSimilariteRecherche;

    @Column(name = "derniere_actualisation", nullable = false)
    @Builder.Default
    private LocalDateTime derniereActualisation = LocalDateTime.now();

    public enum TypeFiche { SERIE, FILIERE, METIER, ETABLISSEMENT }

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null) {
            this.trackingId = UUID.randomUUID();
        }
        if (this.derniereActualisation == null) {
            this.derniereActualisation = LocalDateTime.now();
        }
    }
}
