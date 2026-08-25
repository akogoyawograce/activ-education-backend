package tg.edtch.activEducation.bibliotheque.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Requête pour créer/remplacer les détails étendus (xlsx) d'une fiche
 * établissement.
 *
 * <p>
 * Le champ {@code donneesEtendues} est un objet JSON libre (mappé par Jackson
 * via {@code Map<String,Object>} car la version Jackson 3 utilisée ici ne
 * sait pas désérialiser directement {@code JsonNode} en @RequestBody — d'où
 * l'erreur {@code HttpMessageConversionException} observée au 6 août 2026).
 * Cf. entité {@code EtablissementDetailsXlsx} pour la structure attendue.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtablissementDetailsXlsxRequest {

        /**
         * Identifiant source xlsx (ex : "TG-UL-001"). Optionnel.
         */
        @Size(max = 50)
        private String idSourceXlsx;

        /**
         * Données structurées du xlsx en JSON libre. Voir la javadoc de
         * {@code EtablissementDetailsXlsx.donneesEtenduesJson} pour la
         * structure attendue.
         */
        @NotNull(message = "Les données étendues (JSON) sont obligatoires")
        private Object donneesEtendues;

        /**
         * Date d'extraction depuis le xlsx (par défaut = maintenant).
         */
        private LocalDateTime dateExtraction;
}
