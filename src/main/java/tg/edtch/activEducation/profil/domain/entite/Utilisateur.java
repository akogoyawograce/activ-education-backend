package tg.edtch.activEducation.profil.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entité de base représentant tout utilisateur de la plateforme Activ
 * EDUCATION.
 * Utilise la stratégie d'héritage JOINED : chaque sous-classe a sa propre
 * table,
 * liée à la table utilisateurs par une clé étrangère.
 */
@Entity
@Table(name = "utilisateurs", indexes = {
        @Index(name = "idx_utilisateur_email", columnList = "email", unique = true),
        @Index(name = "idx_utilisateur_tracking_id", columnList = "tracking_id", unique = true)
})
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Utilisateur extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /**
     * Identifiant public non-séquentiel pour les références externes (URL, QR
     * Code...).
     */
    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "mot_de_passe_hash", nullable = false)
    private String motDePasseHash;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "date_inscription", updatable = false)
    private LocalDateTime dateInscription;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    @Column(name = "est_actif", nullable = false)
    @Builder.Default
    private Boolean estActif = true;

    /**
     * Relation ManyToMany avec Role via une table de jointure explicite.
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "utilisateur_roles", joinColumns = @JoinColumn(name = "utilisateur_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    protected void onPrePersist() {
        if (this.dateInscription == null) {
            this.dateInscription = LocalDateTime.now();
        }
        if (this.trackingId == null) {
            this.trackingId = UUID.randomUUID();
        }
    }
}
