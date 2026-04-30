package tg.edtch.activEducation.bibliotheque.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.bibliotheque.application.dto.request.EntreeFAQRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.EntreeFAQResponse;

import java.util.List;
import java.util.UUID;

public interface EntreeFAQService {
    EntreeFAQResponse creerEntree(EntreeFAQRequest request);

    EntreeFAQResponse getEntree(UUID trackingId);

    Page<EntreeFAQResponse> listerToutes(Pageable pageable);

    List<EntreeFAQResponse> listerParCategorie(String categorie);

    EntreeFAQResponse modifierEntree(UUID trackingId, EntreeFAQRequest request);

    void supprimerEntree(UUID trackingId);

    List<String> listerCategories();

    tg.edtch.activEducation.bibliotheque.application.dto.response.RechercheIAResponse rechercherParIA(
            String questionUser, int limite);
}
