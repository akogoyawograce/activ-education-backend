package tg.edtch.activEducation.bibliotheque.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheEtablissementRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheEtablissementResponse;

import java.util.UUID;

public interface FicheEtablissementService {
    FicheEtablissementResponse creerEtablissement(FicheEtablissementRequest request);

    FicheEtablissementResponse getEtablissement(UUID trackingId);

    Page<FicheEtablissementResponse> listerTous(Pageable pageable);

    FicheEtablissementResponse modifierEtablissement(UUID trackingId, FicheEtablissementRequest request);

    void supprimerEtablissement(UUID trackingId);
}
