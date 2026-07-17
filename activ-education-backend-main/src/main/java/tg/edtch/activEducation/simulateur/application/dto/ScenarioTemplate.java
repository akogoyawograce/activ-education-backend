package tg.edtch.activEducation.simulateur.application.dto;

import tg.edtch.activEducation.simulateur.domain.dto.ScenarioRequest;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Modèle d'un scénario préfabriqué, proposé aux élèves qui ne savent pas
 * par où commencer leur exploration What-If.
 *
 * <p>Un template contient un {@link ScenarioRequest} "modèle" qui sera
 * passé à {@code SimulateurParcoursService.explorer(...)} au moment de
 * l'exécution. On utilise un {@link Supplier} (et non un champ figé)
 * pour deux raisons :
 * <ol>
 *   <li>Les valeurs par défaut (ex. ville "Lomé", série "C") sont recréées
 *       à chaque appel — pas de risque de mutation croisée entre exécutions.</li>
 *   <li>Le front peut surcharger certaines valeurs (ex. l'élève choisit
 *       une autre série) en post-processant le {@code ScenarioRequest} avant
 *       l'appel au service.</li>
 * </ol>
 *
 * <p>Le {@code trackingId} est stable (UUID généré une fois pour tous)
 * pour que le front puisse faire un GET par id.</p>
 */
public record ScenarioTemplate(
        UUID trackingId,
        String titre,
        String description,
        CategorieTemplate categorie,
        Supplier<ScenarioRequest> requestSupplier
) {

    /**
     * Catégorie métier du template. Utilisée par le front pour grouper /
     * filtrer l'affichage. Ordre logique d'affichage (de l'aîné au cadet).
     */
    public enum CategorieTemplate {
        PROGRESSION_NOTES,    // "Et si je montais ma moyenne de X ?"
        MOBILITE_GEO,         // "Et si j'allais à Y ?"
        ALTERNATIVE_FILIERE,  // "Et si je choisissais une filière courte ?"
        TRANSITION_SERIE,     // "Et si je passais de série X à Y ?"
        OPTION_MATIERE,       // "Et si j'ajoutais l'anglais LV2 ?"
        ETABLISSEMENT         // "Et si je visais une école privée ?"
    }
}
