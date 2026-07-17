package tg.edtch.activEducation.prediction.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tg.edtch.activEducation.profil.domain.enums.NiveauScolaire;

/**
 * Représentation externe d'un niveau scolaire.
 * Utilisé par {@code GET /api/v1/niveaux} pour peuvoir l'écran
 * "sélection du niveau actuel" côté mobile (Phase 4).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NiveauResponse {

    /** Code canonique (ex. "LYCEE_TLE"). */
    private String code;

    /** Libellé humain (ex. "Lycée - Terminale"). */
    private String label;

    /** Catégorie large ("secondaire" / "superieur"). */
    private String categorie;

    /** Code court utilisable comme icône ou tri (ex. "TLE" pour Terminale). */
    private String codeCourt;

    public static NiveauResponse from(NiveauScolaire n) {
        return NiveauResponse.builder()
                .code(n.name())
                .label(n.getLabel())
                .categorie(n.estSuperieur() ? "superieur" : (n.estSecondaire() ? "secondaire" : "college"))
                .codeCourt(n.getCodeCourt())
                .build();
    }
}
