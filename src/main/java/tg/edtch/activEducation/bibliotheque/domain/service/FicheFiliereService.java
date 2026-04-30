package tg.edtch.activEducation.bibliotheque.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheFiliereRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheFiliereResponse;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

public interface FicheFiliereService {
        FicheFiliereResponse creerFiliere(FicheFiliereRequest request, List<MultipartFile> images,
                        List<MultipartFile> videos, List<MultipartFile> documents);

        FicheFiliereResponse remplacerMedias(UUID trackingId, List<MultipartFile> images, List<MultipartFile> videos,
                        List<MultipartFile> documents);

        FicheFiliereResponse ajouterMedias(UUID trackingId, List<MultipartFile> images, List<MultipartFile> videos,
                        List<MultipartFile> documents);

        FicheFiliereResponse getFiliere(UUID trackingId);

        Page<FicheFiliereResponse> listerToutes(Pageable pageable);

        Page<FicheFiliereResponse> listerPublies(Pageable pageable);

        Page<FicheFiliereResponse> listerNonPublies(Pageable pageable);

        FicheFiliereResponse modifierFiliere(UUID trackingId, FicheFiliereRequest request);

        void supprimerFiliere(UUID trackingId);

        Page<FicheFiliereResponse> rechercher(String motCle, Pageable pageable);

        Page<FicheFiliereResponse> listerParDomaine(String domaine, Pageable pageable);

        List<String> obtenirTousLesDomaines();
}
