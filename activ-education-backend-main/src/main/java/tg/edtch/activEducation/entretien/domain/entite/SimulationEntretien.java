package tg.edtch.activEducation.entretien.domain.entite;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tg.edtch.activEducation.shared.util.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "simulations_entretien", indexes = {
    @Index(name = "idx_entretien_eleve", columnList = "eleve_tracking_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SimulationEntretien extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID trackingId = UUID.randomUUID();

    @Column(name = "eleve_tracking_id", nullable = false, length = 36)
    private String eleveTrackingId;

    @Column(name = "metier_titre", nullable = false, length = 200)
    private String metierTitre;

    @Column(name = "metier_tracking_id", length = 36)
    private String metierTrackingId;

    @Column(name = "questions_posees", columnDefinition = "TEXT")
    private String questionsPosees;

    @Column(name = "reponses_donnees", columnDefinition = "TEXT")
    private String reponsesDonnees;

    @Column(name = "evaluations", columnDefinition = "TEXT")
    private String evaluations;

    @Column(name = "score_final")
    private Double scoreFinal;

    @Column(name = "nb_questions")
    private Integer nbQuestions;

    @Column(name = "statut", nullable = false, length = 20)
    @Builder.Default
    private String statut = "EN_COURS";

    @PrePersist
    protected void onPrePersist() {
        if (this.trackingId == null) this.trackingId = UUID.randomUUID();
    }

    public void ajouterEchange(String question, String reponse, String evaluation) {
        var questions = questionsPosees != null ? questionsPosees + "|||" + question : question;
        var reponses = reponsesDonnees != null ? reponsesDonnees + "|||" + reponse : reponse;
        var evals = evaluations != null ? evaluations + "|||" + evaluation : evaluation;
        setQuestionsPosees(questions);
        setReponsesDonnees(reponses);
        setEvaluations(evals);
        setNbQuestions((nbQuestions != null ? nbQuestions : 0) + 1);
    }

    public String[] getQuestions() {
        return questionsPosees != null ? questionsPosees.split("\\|\\|\\|") : new String[0];
    }

    public String[] getReponses() {
        return reponsesDonnees != null ? reponsesDonnees.split("\\|\\|\\|") : new String[0];
    }

    public String[] getEvaluationsArray() {
        return evaluations != null ? evaluations.split("\\|\\|\\|") : new String[0];
    }
}
