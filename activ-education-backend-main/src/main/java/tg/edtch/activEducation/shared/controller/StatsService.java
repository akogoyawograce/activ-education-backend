package tg.edtch.activEducation.shared.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.bibliotheque.domain.entite.Fiche;
import tg.edtch.activEducation.bibliotheque.domain.entite.FicheEtablissement;
import tg.edtch.activEducation.bibliotheque.repository.FicheEtablissementRepository;
import tg.edtch.activEducation.bibliotheque.repository.FicheRepository;
import tg.edtch.activEducation.profil.repository.EleveRepository;
import tg.edtch.activEducation.profil.repository.ConseillerRepository;
import tg.edtch.activEducation.diagnostic.repository.QuizRepository;
import tg.edtch.activEducation.diagnostic.repository.ResultatDiagnosticRepository;
import tg.edtch.activEducation.accompagnement.repository.RendezVousRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final EleveRepository eleveRepository;
    private final ConseillerRepository conseillerRepository;
    private final QuizRepository quizRepository;
    private final ResultatDiagnosticRepository resultatDiagnosticRepository;
    private final RendezVousRepository rendezVousRepository;
    private final FicheEtablissementRepository etablissementRepository;
    private final FicheRepository ficheRepository;

    @SuppressWarnings("unused")
    public Map<String, Long> getKPIs() {
        return Map.of(
                "totalEleves", eleveRepository.count(),
                "totalConseillers", conseillerRepository.count(),
                "totalQuiz", quizRepository.count(),
                "totalResultats", resultatDiagnosticRepository.count(),
                "totalEtablissements", etablissementRepository.count(),
                "totalFiches", ficheRepository.count());
    }

    public List<Map<String, Object>> getInscriptionsParJour(int jours) {
        List<Object[]> raw = eleveRepository.compterInscriptionsParJour(
                LocalDateTime.now().minusDays(jours));
        return formatDateCount(raw);
    }

    public List<Map<String, Object>> getQuizCompletesParJour(int jours) {
        List<Object[]> raw = resultatDiagnosticRepository.compterResultatsParJour(
                LocalDateTime.now().minusDays(jours));
        return formatDateCount(raw);
    }

    public List<Map<String, Object>> getRDVParsMois(int mois) {
        List<Object[]> raw = rendezVousRepository.compterRDVParsMois(
                LocalDate.now().minusMonths(mois));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : raw) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("mois", row[0] + "-" + row[1]);
            entry.put("count", row[2]);
            result.add(entry);
        }
        return result;
    }

    public Map<String, Long> getTypeApprenantDistribution() {
        List<Object[]> raw = eleveRepository.countByTypeApprenant();
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : raw) {
            result.put((String) row[0], (Long) row[1]);
        }
        return result;
    }

    public List<Map<String, Object>> getFichesModifieesRecentes(int limite) {
        List<Fiche> fiches = ficheRepository.findTopByOrderByUpdatedAtDesc(
                org.springframework.data.domain.PageRequest.of(0, limite));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Fiche f : fiches) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("titre", f.getTitre());
            entry.put("type", f.getClass().getSimpleName().replace("Fiche", ""));
            entry.put("trackingId", f.getTrackingId().toString());
            entry.put("modifieeLe", f.getUpdatedAt() != null ? f.getUpdatedAt().toString() : "");
            entry.put("estPublie", f.getEstPublie());
            result.add(entry);
        }
        return result;
    }

    public Map<String, Long> getQuizParDomaine() {
        List<Object[]> raw = quizRepository.compterParDomaineRaw();
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : raw) {
            result.put((String) row[0], (Long) row[1]);
        }
        return result;
    }

    private List<Map<String, Object>> formatDateCount(List<Object[]> raw) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : raw) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", row[0].toString());
            entry.put("count", row[1]);
            result.add(entry);
        }
        return result;
    }
}
