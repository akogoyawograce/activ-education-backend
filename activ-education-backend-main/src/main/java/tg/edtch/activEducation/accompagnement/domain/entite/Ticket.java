package tg.edtch.activEducation.accompagnement.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sujet", nullable = false, length = 255)
    private String sujet;

    @Column(name = "statut", nullable = false, length = 30)
    @Builder.Default
    private String statut = "OUVERT";

    @Column(name = "priorite", length = 20)
    @Builder.Default
    private String priorite = "NORMALE";

    @Column(name = "expediteur_id", nullable = false)
    private Long expediteurId;

    @Column(name = "assignee_a_id")
    private Long assigneeAId;

    @Column(name = "date_ouverture", nullable = false)
    private LocalDateTime dateOuverture;

    @Column(name = "date_fermeture")
    private LocalDateTime dateFermeture;

    @Column(name = "date_derniere_activite")
    private LocalDateTime dateDerniereActivite;

    @Column(name = "categorie", length = 50)
    private String categorie;
}
