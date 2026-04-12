package tg.edtch.activEducation.bibliotheque.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.profil.domain.entite.Utilisateur;
import tg.edtch.activEducation.shared.util.BaseEntity;

/**
 * Représente une fiche ajoutée aux favoris par un utilisateur.
 * Table de liaison enrichie (contient la date et des notes personnelles).
 */
@Entity
@Table(name = "favoris", uniqueConstraints = {
        @UniqueConstraint(name = "uk_favori_utilisateur_fiche", columnNames = { "utilisateur_id", "fiche_id" })
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fiche_id", nullable = false)
    private Fiche fiche;

    /**
     * Note personnelle optionnelle de l'utilisateur sur cette fiche.
     */
    @Column(name = "note_personnelle", length = 500)
    private String notePersonnelle;
}
