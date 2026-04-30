package tg.edtch.activEducation.bibliotheque.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheMetierRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheMetierResponse;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

public interface FicheMetierService {
        FicheMetierResponse creerMetier(FicheMetierRequest request, List<MultipartFile> images,
                        List<MultipartFile> videos,
                        List<MultipartFile> documents);

        FicheMetierResponse remplacerMedias(UUID trackingId, List<MultipartFile> images, List<MultipartFile> videos,
                        List<MultipartFile> documents);

        FicheMetierResponse ajouterMedias(UUID trackingId, List<MultipartFile> images, List<MultipartFile> videos,
                        List<MultipartFile> documents);

        FicheMetierResponse getMetier(UUID trackingId);

        Page<FicheMetierResponse> listerTous(Pageable pageable);

        Page<FicheMetierResponse> listerPublies(Pageable pageable);

        Page<FicheMetierResponse> listerNonPublies(Pageable pageable);

        FicheMetierResponse modifierMetier(UUID trackingId, FicheMetierRequest request);

        void supprimerMetier(UUID trackingId);

        Page<FicheMetierResponse> rechercher(String motCle, Pageable pageable);

        Page<FicheMetierResponse> listerParSecteur(String secteur, Pageable pageable);

        List<String> obtenirTousLesSecteurs();
}
