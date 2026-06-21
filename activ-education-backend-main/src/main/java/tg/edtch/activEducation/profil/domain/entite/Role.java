package tg.edtch.activEducation.profil.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.profil.domain.enums.RoleNom;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant un rôle utilisateur dans la plateforme.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nom", nullable = false, unique = true, length = 50)
    private RoleNom nom;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Utilisateur> utilisateurs = new HashSet<>();
}
