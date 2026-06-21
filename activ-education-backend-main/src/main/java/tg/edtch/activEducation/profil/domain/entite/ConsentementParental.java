package tg.edtch.activEducation.profil.domain.entite;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "consentements_parentaux", uniqueConstraints = {
        @UniqueConstraint(columnNames = "eleve_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentementParental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "eleve_id", nullable = false)
    private Long eleveId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "email_parent", length = 150)
    private String emailParent;

    @Column(name = "consenti", nullable = false)
    private boolean consenti;

    @Column(name = "token_validation", length = 100)
    private String tokenValidation;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "ip_validation", length = 45)
    private String ipValidation;

    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande;

    @PrePersist
    protected void onCreate() {
        if (dateDemande == null) dateDemande = LocalDateTime.now();
    }
}
