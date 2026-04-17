package tg.edtch.activEducation.bibliotheque.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FavoriRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FavoriResponse;

import java.util.UUID;

public interface FavoriService {
    FavoriResponse ajouterFavori(FavoriRequest request);

    FavoriResponse getFavori(UUID trackingId);

    Page<FavoriResponse> listerParUtilisateur(UUID utilisateurTrackingId, Pageable pageable);

    void supprimerFavori(UUID trackingId);
}
