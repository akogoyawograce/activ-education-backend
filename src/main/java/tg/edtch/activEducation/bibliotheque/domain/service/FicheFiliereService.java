package tg.edtch.activEducation.bibliotheque.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheFiliereRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheFiliereResponse;

import java.util.UUID;

public interface FicheFiliereService {
    FicheFiliereResponse creerFiliere(FicheFiliereRequest request);

    FicheFiliereResponse getFiliere(UUID trackingId);

    Page<FicheFiliereResponse> listerToutes(Pageable pageable);

    FicheFiliereResponse modifierFiliere(UUID trackingId, FicheFiliereRequest request);

    void supprimerFiliere(UUID trackingId);
}
