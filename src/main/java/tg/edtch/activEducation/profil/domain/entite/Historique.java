package tg.edtch.activEducation.profil.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

/**
 * Entité représentant une entrée dans l'historique personnel d'un utilisateur
 * (ex: passage de test completé, document uploadé, connexion, etc.).
 */
@Entity
@Table(name = "historique_utilisateur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Historique extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /**
     * L'action effectuée par l'utilisateur.
     * Ex: "Connexion", "TestRiasec", "UploadDocument".
     */
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    /**
     * Détails supplémentaires ou métadonnées JSON si pertinent.
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;
}
