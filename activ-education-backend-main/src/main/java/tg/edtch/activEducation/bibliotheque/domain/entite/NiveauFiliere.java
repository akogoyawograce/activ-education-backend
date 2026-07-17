package tg.edtch.activEducation.bibliotheque.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.profil.domain.converter.NiveauScolaireConverter;
import tg.edtch.activEducation.profil.domain.enums.NiveauScolaire;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.UUID;

/**
 * Mapping entre une {@link FicheFiliere} et un {@link NiveauScolaire} éligible.
 *
 * <p>Une filière peut être ouverte à plusieurs niveaux (ex. "Gestion_Commerce"
 * accepte BAC_1, BAC_2 et BAC_3). Plutôt qu'un champ {@code niveau_requis}
 * String sur {@link FicheFiliere} — déjà existant et conservé pour rétrocompat
 * du filtrage par libellé — cette table de mapping est la source de vérité
 * pour le filtrage algorithmique (Phase 2 du module Prédiction).</p>
 *
 * <p>Voir {@code CHANGELOG_SCHEMA.md} § 2.</p>
 */
@Entity
@Table(name = "niveaux_filieres",
       uniqueConstraints = @UniqueConstraint(name = "uk_filiere_niveau",
                                             columnNames = {"fiche_filiere_id", "niveau"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NiveauFiliere extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    /**
     * Filière concernée. Référence la clé primaire interne (JOINED sur fiches).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fiche_filiere_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_niveaux_filieres_fiche"))
    private FicheFiliere ficheFiliere;

    /**
     * Niveau éligible pour cette filière.
     */
    @Convert(converter = NiveauScolaireConverter.class)
    @Column(name = "niveau", nullable = false, length = 20)
    private NiveauScolaire niveau;

    /**
     * Indique si ce niveau est le niveau "principal" d'entrée dans la filière
     * (ex. BAC_1 pour une Licence 3 ans). Utilisé par le moteur de la
     * Phase 3 pour prioriser les recommandations quand plusieurs niveaux
     * sont éligibles.
     */
    @Column(name = "est_principal", nullable = false)
    @Builder.Default
    private Boolean estPrincipal = false;

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null) {
            this.trackingId = UUID.randomUUID();
        }
    }
}
