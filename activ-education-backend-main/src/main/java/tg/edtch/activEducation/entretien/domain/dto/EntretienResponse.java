package tg.edtch.activEducation.entretien.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

/**
 * Réponse API pour les endpoints d'entretien simulé
 * ({@code /start} et {@code /{sessionId}/repondre}).
 *
 * <p>
 * Le champ {@code erreur} permet au frontend de distinguer une vraie
 * question du recruteur (avec {@code question != null}) d'un échec
 * technique (avec {@code erreur=true} + {@code messageErreur}).
 * Avant cette séparation, le JSON d'erreur
 * {@code {"score": 10, "feedback": "Erreur d'évaluation, veuillez réessayer."}}
 * était injecté tel quel dans le champ {@code question} et affiché
 * comme une question du recruteur dans la bulle de conversation.
 *
 * <p>
 * Si {@code erreur=true}, {@code question} est {@code null}.
 * Le frontend doit afficher {@code messageErreur} avec un bouton "Réessayer".
 *
 * <p>
 * {@link JsonInclude.Include#NON_NULL} évite d'envoyer des champs null
 * qui pollueraient la réponse JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EntretienResponse(
    UUID sessionId,
    String metierTitre,
    String question,
    int questionNumero,
    int totalQuestions,
    String statut,
    Boolean erreur,
    String messageErreur
) {
    /** Constructeur "succès" — question valide du recruteur. */
    public EntretienResponse(UUID sessionId, String metierTitre, String question,
                             int questionNumero, int totalQuestions, String statut) {
        this(sessionId, metierTitre, question, questionNumero, totalQuestions, statut,
             null, null);
    }

    /** Constructeur "erreur technique" — pas de question, message d'erreur. */
    public static EntretienResponse erreur(UUID sessionId, String metierTitre,
                                           int questionNumero, int totalQuestions,
                                           String statut, String messageErreur) {
        return new EntretienResponse(sessionId, metierTitre, null,
                                     questionNumero, totalQuestions, statut,
                                     true, messageErreur);
    }
}
