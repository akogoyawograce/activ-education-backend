package tg.edtch.activEducation.prediction.domain.service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Calcul de la trajectoire académique à partir de l'historique des
 * moyennes annuelles (3 ans glissants).
 *
 * <p>Régression linéaire simple : on ajuste une droite
 * {@code note = a + b·annee} sur les points disponibles. La pente {@code b}
 * est la "tendance" : positive si l'élève progresse, négative s'il régresse.</p>
 *
 * <p>Voir {@code RESULTATS_PROTOTYPE.md} § 3 et {@code CHANGELOG_SCHEMA.md} § 3.</p>
 */
public interface NoteTrajectoireService {

    /**
     * Calcule la trajectoire à partir d'une liste de notes moyennes annuelles.
     * La liste doit être triée par année scolaire croissante (n-2, n-1, n).
     *
     * @param notesTriéesCroissant liste de notes (échelle 0..20)
     * @return résultat avec note extrapolée, pente, et confiance
     */
    Trajectoire calculer(List<BigDecimal> notesTriéesCroissant);

    /**
     * Résultat de la trajectoire.
     *
     * @param noteActuelle       dernière note connue (la plus récente)
     * @param noteExtrapolée     projection pour l'année suivante (régression linéaire)
     * @param pente              différence de moyenne par an (négatif = régression)
     * @param nbPointsUtilisés   nombre de notes effectivement utilisées (1..3)
     * @param confiance          niveau de confiance de la projection (0..1)
     */
    record Trajectoire(
            BigDecimal noteActuelle,
            BigDecimal noteExtrapolée,
            BigDecimal pente,
            int nbPointsUtilisés,
            double confiance
    ) { }
}
