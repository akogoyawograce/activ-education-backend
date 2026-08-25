package tg.edtch.activEducation.bibliotheque.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Réponse exposant les détails étendus (xlsx) d'une fiche établissement.
 *
 * <p>
 * {@code donneesEtenduesJson} est renvoyé tel quel (string brute), le client le
 * décode en JSON. Pour la structure attendue, voir la javadoc de
 * {@code EtablissementDetailsXlsx.donneesEtenduesJson}.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EtablissementDetailsXlsxResponse {

    private UUID ficheTrackingId;
    private String idSourceXlsx;
    private String donneesEtenduesJson;
    private LocalDateTime dateExtraction;
}
