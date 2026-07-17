package tg.edtch.activEducation.riasec.domain.service;
import tg.edtch.activEducation.riasec.domain.dto.RIASECResultatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tg.edtch.activEducation.riasec.domain.entite.TestRIASECResultat;
import tg.edtch.activEducation.riasec.repository.TestRIASECResultatRepository;
import java.util.*;
@Service @Transactional
public class RIASECService {
    private final TestRIASECResultatRepository repo;
    private final ObjectMapper mapper;
    public RIASECService(TestRIASECResultatRepository repo) { this.repo = repo; this.mapper = new ObjectMapper(); }
    private static final Map<String, List<String>> PROFIL_METIERS = Map.of(
        "R", List.of("Ingénieur", "Mécanicien", "Agriculteur", "Architecte", "Pilote"),
        "I", List.of("Médecin", "Chercheur", "Biologiste", "Pharmacien", "Data Scientist"),
        "A", List.of("Designer", "Musicien", "Écrivain", "Acteur", "Architecte d'intérieur"),
        "S", List.of("Enseignant", "Conseiller", "Infirmier", "Psychologue", "Travailleur social"),
        "E", List.of("Entrepreneur", "Manager", "Commercial", "Avocat", "Consultant"),
        "C", List.of("Comptable", "Banquier", "Administrateur", "Gestionnaire", "Analyste financier")
    );
    public RIASECResultatResponse passerTest(String eleveId, String reponsesJson) throws Exception {
        var reponses = mapper.readValue(reponsesJson, List.class);
        var scores = new HashMap<String, Integer>(Map.of("R",0,"I",0,"A",0,"S",0,"E",0,"C",0));
        String[] categories = {"R","I","A","S","E","C"};
        for (int i = 0; i < reponses.size() && i < 30; i++) {
            var val = ((Number) reponses.get(i)).intValue();
            scores.merge(categories[i / 5], val, Integer::sum);
        }
        var top3 = scores.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).limit(3).map(Map.Entry::getKey).toList();
        var codeProfil = String.join("", top3);
        var suggestions = PROFIL_METIERS.getOrDefault(String.valueOf(codeProfil.charAt(0)), List.of());
        var entity = TestRIASECResultat.builder().eleveTrackingId(eleveId).codeProfil(codeProfil)
            .titres(reponsesJson).scoreRealiste(scores.get("R")).scoreInvestigateur(scores.get("I"))
            .scoreArtistique(scores.get("A")).scoreSocial(scores.get("S")).scoreEntreprenant(scores.get("E"))
            .scoreConventionnel(scores.get("C")).suggestionsMetiers(String.join(", ", suggestions)).build();
        var saved = repo.save(entity);
        return toResponse(saved);
    }
    public List<RIASECResultatResponse> getResultats(String eleveId) {
        return repo.findByEleveTrackingIdOrderByDatePassationDesc(eleveId).stream().map(this::toResponse).toList();
    }
    private RIASECResultatResponse toResponse(TestRIASECResultat t) {
        return new RIASECResultatResponse(t.getTrackingId().toString(), t.getEleveTrackingId(),
            t.getCodeProfil(), t.getTitres(), t.getScoreRealiste(), t.getScoreInvestigateur(),
            t.getScoreArtistique(), t.getScoreSocial(), t.getScoreEntreprenant(), t.getScoreConventionnel(),
            t.getSuggestionsMetiers(), t.getDatePassation() != null ? t.getDatePassation().toString() : null);
    }
}
