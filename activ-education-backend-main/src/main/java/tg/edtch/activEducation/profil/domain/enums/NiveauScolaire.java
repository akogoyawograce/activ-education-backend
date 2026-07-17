package tg.edtch.activEducation.profil.domain.enums;

import java.util.Arrays;

/**
 * Niveaux scolaires utilisés dans la plateforme Activ Education.
 *
 * <p>Le système éducatif togolais couvert va du collège au supérieur (Bac+3).
 * L'énumération est utilisée pour :</p>
 * <ul>
 *   <li>le champ {@code niveau} de l'entité {@code Eleve} (anciennement String) ;</li>
 *   <li>le filtrage des filières (cf. table {@code niveaux_filieres}) ;</li>
 *   <li>le ciblage des quiz d'orientation (cf. {@code Quiz.niveauCible}).</li>
 * </ul>
 *
 * <p><strong>Migration :</strong> la colonne {@code eleves.niveau} (VARCHAR 100)
 * est convertie en VARCHAR/ENUM. Voir {@code CHANGELOG_SCHEMA.md} (Phase 1 du
 * module Prédiction).</p>
 */
public enum NiveauScolaire {
    COLLEGE,
    LYCEE_2ND,
    LYCEE_1ERE,
    LYCEE_TLE,
    BAC_1,
    BAC_2,
    BAC_3;

    /**
     * Parse tolérant d'une chaîne vers l'enum.
     * Accepte le nom exact ({@code "LYCEE_TLE"}), une forme lisible
     * ({@code "Terminale"}, {@code "1ère"}, {@code "Licence 2"}), ou
     * les libellés historiquement stockés en base avant la migration
     * ({@code "Terminale C"}, {@code "Licence 2 Informatique"}).
     *
     * @return l'enum correspondant, ou {@code null} si la chaîne ne peut
     *         pas être interprétée. On ne lève pas d'exception : la migration
     *         peut avoir laissé des valeurs exotiques en base, et il vaut
     *         mieux les traiter comme "non renseigné" que de planter.
     */
    public static NiveauScolaire parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().toUpperCase().replace(' ', '_').replace('-', '_');

        // Tentative directe (nom exact de l'enum)
        for (NiveauScolaire n : values()) {
            if (n.name().equals(normalized)) {
                return n;
            }
        }

        // Synonymes usuels (libellés mobiles / anciens strings en base)
        if (normalized.startsWith("COLLEGE") || normalized.startsWith("COLLEG")) {
            return COLLEGE;
        }
        if (normalized.startsWith("SECONDE") || normalized.startsWith("2NDE")
                || normalized.startsWith("2EME") || normalized.contains("LYCEE_2")
                || normalized.contains("LYCEE_2NDE")) {
            return LYCEE_2ND;
        }
        if (normalized.startsWith("PREMIERE") || normalized.startsWith("1ERE")
                || normalized.startsWith("1ERE") || normalized.contains("1RE")
                || normalized.contains("LYCEE_1")) {
            return LYCEE_1ERE;
        }
        if (normalized.startsWith("TERMINALE") || normalized.startsWith("TLE")
                || normalized.contains("LYCEE_T")) {
            return LYCEE_TLE;
        }
        if (normalized.contains("LICENCE_1") || normalized.contains("L1")
                || normalized.contains("BAC_1") || normalized.contains("BAC+1")) {
            return BAC_1;
        }
        if (normalized.contains("LICENCE_2") || normalized.contains("L2")
                || normalized.contains("BAC_2") || normalized.contains("BAC+2")) {
            return BAC_2;
        }
        if (normalized.contains("LICENCE_3") || normalized.contains("L3")
                || normalized.contains("BAC_3") || normalized.contains("BAC+3")) {
            return BAC_3;
        }
        return null;
    }

    /**
     * Libellé humain pour affichage côté front (ex. "Terminale", "Licence 2").
     * Inverse approximatif de {@link #parse(String)} : on garde la fonction
     * idempotente pour les noms canoniques.
     */
    public String getLabel() {
        switch (this) {
            case COLLEGE:    return "Collège";
            case LYCEE_2ND:  return "Seconde";
            case LYCEE_1ERE: return "Première";
            case LYCEE_TLE:  return "Terminale";
            case BAC_1:      return "Licence 1";
            case BAC_2:      return "Licence 2";
            case BAC_3:      return "Licence 3";
            default:         return name();
        }
    }

    /**
     * Indique si ce niveau appartient au supérieur (≥ BAC).
     */
    public boolean estSuperieur() {
        return this == BAC_1 || this == BAC_2 || this == BAC_3;
    }

    /**
     * Indique si ce niveau appartient au secondaire (collège ou lycée).
     */
    public boolean estSecondaire() {
        return !estSuperieur();
    }

    /**
     * Code court à 3-4 caractères, utilisé pour les tris et l'affichage
     * condensé (ex. "TLE", "L2"). Distinct du libellé complet retourné
     * par {@link #getLabel()}.
     */
    public String getCodeCourt() {
        switch (this) {
            case COLLEGE:    return "CLG";
            case LYCEE_2ND:  return "2ND";
            case LYCEE_1ERE: return "1RE";
            case LYCEE_TLE:  return "TLE";
            case BAC_1:      return "L1";
            case BAC_2:      return "L2";
            case BAC_3:      return "L3";
            default:         return name();
        }
    }
}
