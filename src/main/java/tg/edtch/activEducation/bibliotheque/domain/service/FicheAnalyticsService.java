package tg.edtch.activEducation.bibliotheque.domain.service;

import tg.edtch.activEducation.bibliotheque.application.dto.response.RechercheGlobaleResponse;
import java.util.List;
import java.util.UUID;

public interface FicheAnalyticsService {
    List<RechercheGlobaleResponse> getTendances(int limite);

    List<RechercheGlobaleResponse> getConsultationsRecentes(UUID utilisateurTrackingId, int limite);
}
