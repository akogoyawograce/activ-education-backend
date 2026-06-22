package tg.edtch.activEducation.diagnostic.domain.service;

import tg.edtch.activEducation.diagnostic.application.dto.request.SeuilAdmissionRequest;
import tg.edtch.activEducation.diagnostic.application.dto.response.SeuilAdmissionResponse;

import tg.edtch.activEducation.profil.domain.service.OcrService;

import java.util.List;
import java.util.UUID;

/**
 * Contrat de service pour la gestion des seuils d'admission par filière.
 */
public interface SeuilAdmissionService {

    SeuilAdmissionResponse creerSeuil(SeuilAdmissionRequest request);

    SeuilAdmissionResponse getSeuil(UUID trackingId);

    /**
     * Retourne tous les seuils d'une filière identifiée par son trackingId public.
     */
    List<SeuilAdmissionResponse> getSeuilsParFiliere(UUID filiereTrackingId);

    List<SeuilAdmissionResponse> listerSeuils();

    SeuilAdmissionResponse modifierSeuil(UUID trackingId, SeuilAdmissionRequest request);

    void supprimerSeuil(UUID trackingId);

    List<SeuilAdmissionResponse> getMatchingFilieres(List<OcrService.NoteExtraite> notes);
}
