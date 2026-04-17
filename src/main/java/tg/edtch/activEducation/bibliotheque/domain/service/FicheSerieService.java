package tg.edtch.activEducation.bibliotheque.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheSerieRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheSerieResponse;

import java.util.UUID;

public interface FicheSerieService {
    FicheSerieResponse creerSerie(FicheSerieRequest request);

    FicheSerieResponse getSerie(UUID trackingId);

    Page<FicheSerieResponse> listerToutes(Pageable pageable);

    FicheSerieResponse modifierSerie(UUID trackingId, FicheSerieRequest request);

    void supprimerSerie(UUID trackingId);
}
