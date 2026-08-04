package tg.edtch.activEducation.shared.ai.domain.entite;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

@Entity
@Table(name = "profils_orientation", uniqueConstraints = {
    @UniqueConstraint(columnNames = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProfilOrientation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true, length = 100)
    private String userId;

    @Column(name = "ambitions", columnDefinition = "TEXT")
    private String ambitions;

    @Column(name = "domaines_interet", columnDefinition = "TEXT")
    private String domainesInteret;

    @Column(name = "resume_parcours", columnDefinition = "TEXT")
    private String resumeParcours;

    @Column(name = "dernier_domaine", length = 200)
    private String dernierDomaine;

    @Column(name = "premiere_ambition", columnDefinition = "TEXT")
    private String premiereAmbition;
}
