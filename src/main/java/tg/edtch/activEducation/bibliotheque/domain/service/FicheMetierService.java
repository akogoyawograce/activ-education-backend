package tg.edtch.activEducation.bibliotheque.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheMetierRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheMetierResponse;

import java.util.UUID;

public interface FicheMetierService {
    FicheMetierResponse creerMetier(FicheMetierRequest request);

    FicheMetierResponse getMetier(UUID trackingId);

    Page<FicheMetierResponse> listerTous(Pageable pageable);

    FicheMetierResponse modifierMetier(UUID trackingId, FicheMetierRequest request);

    void supprimerMetier(UUID trackingId);
}
