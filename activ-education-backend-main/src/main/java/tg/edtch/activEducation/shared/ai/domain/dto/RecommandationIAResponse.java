package tg.edtch.activEducation.shared.ai.domain.dto;

/**
 * Réponse typée du v1 {@code GET /api/v1/eleves/{trackingId}/recommandation-ia}.
 *
 * <p>Avant le 6 août 2026, l'endpoint ne renvoyait qu'un {@code Map<String,String>}
 * avec la clé {@code recommandation}, ce qui forçait le frontend à
 * interpréter le texte lui-même pour distinguer "vraie recommandation" de
 * "message d'erreur technique" — ambiguïté qui masquait notamment le
 * cas "profil incomplet" vs "LLM en panne" (même chaîne
 * "Désolé, je n'ai pas pu générer…").</p>
 *
 * <p>Avec ce DTO typé, le frontend peut afficher un bandeau dédié
 * selon le {@link Statut} :
 * <ul>
 *   <li>{@code OK} : texte = vraie recommandation générée par l'IA</li>
 *   <li>{@code PROFIL_INCOMPLET} : texte = message invitant à compléter
 *       le profil (déterministe, pas d'appel LLM)</li>
 *   <li>{@code ERREUR_LLM} : texte = message d'erreur technique
 *       (LLM down, quota épuisé, …)</li>
 * </ul>
 */
public record RecommandationIAResponse(
    String recommandation,
    Statut type
) {
    public enum Statut {
        OK,
        PROFIL_INCOMPLET,
        ERREUR_LLM
    }

    /** Constructeur "succès" — l'IA a généré une recommandation. */
    public static RecommandationIAResponse ok(String recommandation) {
        return new RecommandationIAResponse(recommandation, Statut.OK);
    }

    /** Constructeur "profil incomplet" — pas d'appel LLM, message déterministe. */
    public static RecommandationIAResponse profilIncomplet(String message) {
        return new RecommandationIAResponse(message, Statut.PROFIL_INCOMPLET);
    }

    /** Constructeur "erreur technique" — LLM down/quota/timeout. */
    public static RecommandationIAResponse erreurLlm(String message) {
        return new RecommandationIAResponse(message, Statut.ERREUR_LLM);
    }
}
