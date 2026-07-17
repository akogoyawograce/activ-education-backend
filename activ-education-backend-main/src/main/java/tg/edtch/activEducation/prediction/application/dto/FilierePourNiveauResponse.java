package tg.edtch.activEducation.prediction.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Représentation minimale d'une filière pour l'écran "liste filtrée par
 * niveau" côté mobile.
 *
 * <p>Volontairement plus léger que {@link tg.edtch.activEducation.bibliotheque.application.dto.response.FicheFiliereResponse}
 * (pas de description longue, pas de blocs SEO) — on évite de payer le
 * coût d'un full-dump RAG pour l'écran de filtrage.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilierePourNiveauResponse {

    private UUID trackingId;

    /** Titre de la fiche (cf. {@code Fiche.titre}). */
    private String titre;

    private String domaine;

    private String duree;

    /** Niveaux éligibles (codes canoniques, ex. ["BAC_1", "BAC_2"]). */
    private List<String> niveauxEligibles;

    /** True si l'un des niveaux éligibles est marqué "principal". */
    private Boolean aNiveauPrincipal;
}
