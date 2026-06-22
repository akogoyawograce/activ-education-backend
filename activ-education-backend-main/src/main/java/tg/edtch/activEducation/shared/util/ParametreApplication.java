package tg.edtch.activEducation.shared.util;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parametres_application")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametreApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cle", nullable = false, unique = true, length = 100)
    private String cle;

    @Column(name = "valeur", nullable = false, length = 500)
    private String valeur;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "categorie", length = 100)
    private String categorie;
}
