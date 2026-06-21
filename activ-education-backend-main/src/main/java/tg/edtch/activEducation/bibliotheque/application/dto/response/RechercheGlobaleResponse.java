package tg.edtch.activEducation.bibliotheque.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de réponse unifié pour la recherche sémantique globale sur toutes les
 * fiches.
 * Chaque résultat identifie son type et expose ses informations essentielles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechercheGlobaleResponse {

    /** Identifiant public de la fiche. */
    private UUID trackingId;

    /** Type de fiche : "METIER", "FILIERE", "ETABLISSEMENT" ou "SERIE". */
    private String typeResultat;

    /** Titre de la fiche. */
    private String titre;

    /** Résumé court affiché dans les listes de résultats. */
    private String resume;

    /** URL de la première image de la fiche (miniature/bannière), si disponible. */
    private String imageCouverture;
}
