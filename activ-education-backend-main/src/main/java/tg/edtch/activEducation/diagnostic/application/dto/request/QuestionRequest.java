package tg.edtch.activEducation.diagnostic.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de requête pour la création ou modification d'une Question.
 * Le quiz est identifié via son trackingId dans l'URL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {

    @NotBlank(message = "Le texte de la question est obligatoire")
    private String texteQuestion;

    /** Ordre d'affichage dans le quiz (optionnel, trié côté service si absent). */
    @Min(value = 1, message = "L'ordre doit être ≥ 1")
    private Integer ordre;

    /** Niveau scolaire cible pour filtrage. */
    private String niveauCible;

    /** Domaine thématique (ex: "Sciences", "Lettres", "Technique"). */
    private String domaine;

    /** Difficulté (1-5). */
    private Integer difficulte;

    /** Mots-clés CSV. */
    private String tags;

    /** Type de question : RIASEC, CONNAISSANCE, INTERET, PERSONNALITE. */
    private String typeQuestion;
}
