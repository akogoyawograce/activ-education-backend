package tg.edtch.activEducation.bibliotheque.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tg.edtch.activEducation.bibliotheque.application.dto.request.FicheEtablissementRequest;
import tg.edtch.activEducation.bibliotheque.application.dto.response.FicheEtablissementResponse;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

public interface FicheEtablissementService {
        FicheEtablissementResponse creerEtablissement(FicheEtablissementRequest request, List<MultipartFile> images,
                        List<MultipartFile> videos, List<MultipartFile> documents);

        FicheEtablissementResponse remplacerMedias(UUID trackingId, List<MultipartFile> images,
                        List<MultipartFile> videos,
                        List<MultipartFile> documents);

        FicheEtablissementResponse ajouterMedias(UUID trackingId, List<MultipartFile> images,
                        List<MultipartFile> videos,
                        List<MultipartFile> documents);

        FicheEtablissementResponse getEtablissement(UUID trackingId, UUID utilisateurTrackingId);

        Page<FicheEtablissementResponse> listerTous(Pageable pageable);

        Page<FicheEtablissementResponse> listerPublies(Pageable pageable);

        Page<FicheEtablissementResponse> listerNonPublies(Pageable pageable);

        FicheEtablissementResponse modifierEtablissement(UUID trackingId, FicheEtablissementRequest request);

        void supprimerEtablissement(UUID trackingId);

        Page<FicheEtablissementResponse> listerParVille(String ville, Pageable pageable);

        Page<FicheEtablissementResponse> listerParType(String type, Pageable pageable);

        Page<FicheEtablissementResponse> listerParNiveau(String niveau, Pageable pageable);

        List<String> obtenirToutesLesVilles();

        Page<FicheEtablissementResponse> rechercher(String motCle, Pageable pageable);

        Page<FicheEtablissementResponse> trouverProximite(double lat, double lng, double radiusKm, Pageable pageable);

        List<FicheEtablissementResponse> listerAvecCoordonnees();
}
