package tg.edtch.activEducation.bibliotheque.domain.service;

import tg.edtch.activEducation.bibliotheque.application.dto.request.EtablissementDetailsXlsxRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.EtablissementDetailsXlsxResponse;

import java.util.UUID;

public interface EtablissementDetailsXlsxService {

    /**
     * Récupère les détails étendus (xlsx) d'une fiche, ou
     * lance NoSuchElementException si la fiche n'existe pas.
     * Si aucun détail n'est attaché, renvoie une réponse avec
     * {@code donneesEtenduesJson=null}.
     */
    EtablissementDetailsXlsxResponse getDetails(UUID trackingId);

    /**
     * Crée ou remplace (idempotent) les détails étendus d'une fiche.
     */
    EtablissementDetailsXlsxResponse upsertDetails(UUID trackingId,
                    EtablissementDetailsXlsxRequest request);

    /**
     * Supprime les détails étendus (rollback éventuel).
     */
    void deleteDetails(UUID trackingId);
}
